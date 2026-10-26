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
import java.util.Properties;
import java.util.regex.Pattern;

@Service
public class TrinoQueryService {
    private static final Logger log = LoggerFactory.getLogger(TrinoQueryService.class);
    private static final Pattern READ_ONLY_QUERY_START = Pattern.compile("(?is)^(SELECT|WITH)\\b");

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
        Properties properties = new Properties();
        if (accessToken == null || accessToken.isBlank()) {
            throw new TrinoQueryValidationException("A valid bearer token is required");
        }
        properties.setProperty("accessToken", accessToken);

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
}
