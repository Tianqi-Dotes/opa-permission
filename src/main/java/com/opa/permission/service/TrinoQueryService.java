package com.opa.permission.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Service
public class TrinoQueryService {
    private static final Logger log = LoggerFactory.getLogger(TrinoQueryService.class);
    private static final Pattern READ_ONLY_QUERY_START = Pattern.compile("(?is)^(SELECT|WITH)\\b");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_-]{0,127}");
    private static final Pattern PROPERTY_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_.-]{0,255}");
    private static final Pattern ENVIRONMENT_SECRET = Pattern.compile("\\$\\{ENV:[A-Za-z_][A-Za-z0-9_]*}");
    private static final Pattern SENSITIVE_PROPERTY =
            Pattern.compile("(?i).*(password|secret|token|credential|private.?key|access.?key).*");
    private static final Pattern INLINE_SECRET =
            Pattern.compile("(?i)(?:password|passwd|pwd|secret|token|credential|private.?key|access.?key)=([^&;]+)");
    private static final Pattern COLUMN_TYPE = Pattern.compile(
            "(?i)^(BOOLEAN|TINYINT|SMALLINT|INTEGER|BIGINT|REAL|DOUBLE|DOUBLE\\s+PRECISION|DATE|"
                    + "TIME(?:\\s*\\(\\s*\\d+\\s*\\))?(?:\\s+(?:WITH|WITHOUT) TIME ZONE)?|"
                    + "TIMESTAMP(?:\\s*\\(\\s*\\d+\\s*\\))?(?:\\s+(?:WITH|WITHOUT) TIME ZONE)?|"
                    + "VARCHAR(?:\\s*\\(\\s*\\d+\\s*\\))?|CHAR\\s*\\(\\s*\\d+\\s*\\)|VARBINARY|"
                    + "JSON|UUID|IPADDRESS|DECIMAL\\s*\\(\\s*\\d+\\s*(?:,\\s*\\d+\\s*)?\\))$");

    private final String jdbcUrl;
    private final int maxRows;
    private final int timeoutSeconds;

    public TrinoQueryService(
            @Value("${trino.jdbc-url}") String jdbcUrl,
            @Value("${trino.query.max-rows:1000}") int maxRows,
            @Value("${trino.query.timeout-seconds:60}") int timeoutSeconds) {
        if (maxRows < 1 || maxRows >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("trino.query.max-rows must be between 1 and 2147483646");
        }
        if (timeoutSeconds < 1) {
            throw new IllegalArgumentException("trino.query.timeout-seconds must be greater than zero");
        }
        this.jdbcUrl = jdbcUrl;
        this.maxRows = maxRows;
        this.timeoutSeconds = timeoutSeconds;
    }

    public QueryResult executeReadOnlyQuery(String sql, String accessToken) {
        String query = validateAndNormalize(sql);
        Properties properties = connectionProperties(accessToken);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, properties);
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(timeoutSeconds);
            statement.setMaxRows(maxRows + 1);
            if (!statement.execute(query)) {
                throw new TrinoQueryValidationException("Only queries that return rows are supported");
            }

            try (ResultSet resultSet = statement.getResultSet()) {
                return readResult(resultSet);
            }
        } catch (SQLException exception) {
            log.error("Trino query execution failed", exception);
            throw new TrinoQueryException("Trino query execution failed", exception);
        }
    }

    public void createCatalog(String catalogName, String connectorName,
                              Map<String, String> properties, String accessToken) {
        String catalog = quoteIdentifier(catalogName, "catalogName");
        String connector = quoteIdentifier(connectorName, "connectorName");
        String sql = "CREATE CATALOG " + catalog + " USING " + connector + renderProperties(properties);
        executeDdl(sql, accessToken);
    }

    public void createSchema(String catalogName, String schemaName, String location,
                             boolean ifNotExists, String accessToken) {
        String qualifiedName = quoteIdentifier(catalogName, "catalogName")
                + "." + quoteIdentifier(schemaName, "schemaName");
        String sql = "CREATE SCHEMA " + (ifNotExists ? "IF NOT EXISTS " : "")
                + qualifiedName;
        if (location != null && !location.isBlank()) {
            sql += " WITH (location = " + quoteLiteral(location) + ")";
        }
        executeDdl(sql, accessToken);
    }

    public void createTable(String catalogName, String schemaName, String tableName,
                            List<ColumnDefinition> columns, Map<String, String> properties,
                            boolean ifNotExists, String accessToken) {
        String qualifiedName = quoteIdentifier(catalogName, "catalogName")
                + "." + quoteIdentifier(schemaName, "schemaName")
                + "." + quoteIdentifier(tableName, "tableName");
        if (columns == null || columns.isEmpty() || columns.size() > 100) {
            throw new TrinoQueryValidationException("columns must contain between 1 and 100 entries");
        }

        List<String> columnDefinitions = new ArrayList<>(columns.size());
        java.util.Set<String> columnNames = new java.util.HashSet<>();
        for (ColumnDefinition column : columns) {
            if (column == null) {
                throw new TrinoQueryValidationException("columns must not contain null entries");
            }
            String name = quoteIdentifier(column.name(), "column name");
            String normalizedName = column.name().toLowerCase(Locale.ROOT);
            if (!columnNames.add(normalizedName)) {
                throw new TrinoQueryValidationException("column names must be unique");
            }
            String type = normalizeColumnType(column.type());
            columnDefinitions.add(name + " " + type);
        }

        String sql = "CREATE TABLE " + (ifNotExists ? "IF NOT EXISTS " : "")
                + qualifiedName + " (" + String.join(", ", columnDefinitions) + ")"
                + renderProperties(properties);
        executeDdl(sql, accessToken);
    }

    private void executeDdl(String sql, String accessToken) {
        Properties properties = connectionProperties(accessToken);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, properties);
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(timeoutSeconds);
            statement.execute(sql);
        } catch (SQLException exception) {
            log.error("Trino DDL execution failed (SQLState: {})", exception.getSQLState());
            throw new TrinoQueryException("Trino DDL execution failed", exception);
        }
    }

    private Properties connectionProperties(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new TrinoQueryValidationException("A valid bearer token is required");
        }
        Properties properties = new Properties();
        properties.setProperty("accessToken", accessToken);
        return properties;
    }

    private String renderProperties(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return "";
        }
        if (properties.size() > 100) {
            throw new TrinoQueryValidationException("properties must not contain more than 100 entries");
        }

        List<String> assignments = new ArrayList<>(properties.size());
        new TreeMap<>(properties).forEach((name, value) -> {
            if (name == null || !PROPERTY_NAME.matcher(name).matches()) {
                throw new TrinoQueryValidationException("property names must be valid identifiers");
            }
            if (value == null || value.length() > 4096) {
                throw new TrinoQueryValidationException("property values must be present and at most 4096 characters");
            }
            if (SENSITIVE_PROPERTY.matcher(name).matches()
                    && !ENVIRONMENT_SECRET.matcher(value).matches()) {
                throw new TrinoQueryValidationException(
                        "Sensitive catalog properties must use a Trino environment secret reference");
            }
            assignments.add(quoteIdentifier(name, "property name") + " = " + quoteLiteral(value));
        });
        return " WITH (" + String.join(", ", assignments) + ")";
    }

    private String quoteIdentifier(String value, String fieldName) {
        if (value == null || !IDENTIFIER.matcher(value).matches()) {
            throw new TrinoQueryValidationException(fieldName + " must be a valid Trino identifier");
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String quoteLiteral(String value) {
        var matcher = INLINE_SECRET.matcher(value);
        while (matcher.find()) {
            if (!ENVIRONMENT_SECRET.matcher(matcher.group(1)).matches()) {
                throw new TrinoQueryValidationException(
                        "Secrets in Trino DDL must use a Trino environment secret reference");
            }
        }
        return "'" + value.replace("'", "''") + "'";
    }

    private String normalizeColumnType(String type) {
        if (type == null) {
            throw new TrinoQueryValidationException("column type is required");
        }
        String normalized = type.trim().replaceAll("\\s+", " ");
        if (!COLUMN_TYPE.matcher(normalized).matches()) {
            throw new TrinoQueryValidationException("column type is not supported");
        }
        return normalized;
    }

    private String validateAndNormalize(String sql) {
        String query = sql.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1).trim();
        }
        if (query.isEmpty() || query.indexOf(';') >= 0 || !READ_ONLY_QUERY_START.matcher(query).find()) {
            throw new TrinoQueryValidationException("Only a single SELECT or WITH query is allowed");
        }
        return query;
    }

    private QueryResult readResult(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        List<String> columns = new ArrayList<>(columnCount);
        for (int index = 1; index <= columnCount; index++) {
            columns.add(metadata.getColumnLabel(index));
        }

        List<List<Object>> rows = new ArrayList<>(Math.min(maxRows, 256));
        boolean truncated = false;
        while (resultSet.next()) {
            if (rows.size() == maxRows) {
                truncated = true;
                break;
            }
            List<Object> row = new ArrayList<>(columnCount);
            for (int index = 1; index <= columnCount; index++) {
                row.add(resultSet.getObject(index));
            }
            rows.add(row);
        }
        return new QueryResult(columns, rows, truncated);
    }

    public record QueryResult(List<String> columns, List<List<Object>> rows, boolean truncated) {
    }

    public record ColumnDefinition(String name, String type) {
    }
}
