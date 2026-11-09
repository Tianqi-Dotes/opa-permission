package com.opa.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.mapper.TrinoConnectionMapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.model.TrinoConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

@Service
public class TrinoMetadataService {
    private static final Logger log = LoggerFactory.getLogger(TrinoMetadataService.class);
    private static final String RESOURCE_TYPE_TRINO_CONNECTION = "trino_connection";
    private static final String AUTH_TYPE_TOKEN = "TOKEN";
    private static final String ALLOWED_HOST = "localhost";
    private static final int ALLOWED_PORT = 8443;

    private final AbacResourceService abacResourceService;
    private final TrinoConnectionMapper trinoConnectionMapper;

    public TrinoMetadataService(AbacResourceService abacResourceService,
                               TrinoConnectionMapper trinoConnectionMapper) {
        this.abacResourceService = abacResourceService;
        this.trinoConnectionMapper = trinoConnectionMapper;
    }

    public List<?> listChildren(Long resourceId,
                                Long tenantId,
                                Jwt jwt,
                                String catalog,
                                String schema,
                                String table) {
        if (resourceId == null || resourceId <= 0) {
            throw new IllegalArgumentException("resourceId must be a positive integer");
        }
        validateLevel(catalog, schema, table);
        return withMetadata(resourceId, tenantId, jwt, metadata -> {
            if (table != null) {
                return readFields(metadata, catalog, schema, table);
            }
            if (schema != null) {
                return readTables(metadata, catalog, schema);
            }
            if (catalog != null) {
                return readSchemas(metadata, catalog);
            }
            return readCatalogs(metadata);
        });
    }

    public void validateNodeSelection(Long datasourceResourceId,
                                      Long tenantId,
                                      Jwt jwt,
                                      NodeSelection selection) {
        validateSelection(selection);
        validateNodeSelections(datasourceResourceId, tenantId, jwt, List.of(selection));
    }

    public void validateNodeSelections(Long datasourceResourceId,
                                       Long tenantId,
                                       Jwt jwt,
                                       List<NodeSelection> selections) {
        if (selections == null || selections.isEmpty()) {
            throw new IllegalArgumentException("At least one Trino resource selection is required");
        }
        selections.forEach(TrinoMetadataService::validateSelection);
        withMetadata(datasourceResourceId, tenantId, jwt, metadata -> {
            for (NodeSelection selection : selections) {
                validateNodeSelection(metadata, selection);
            }
            return null;
        });
    }

    private void validateNodeSelection(DatabaseMetaData metadata, NodeSelection selection) throws SQLException {
        if (!containsName(readCatalogs(metadata), selection.catalog(), CatalogNode::name)) {
            throw selectedNodeNotFound();
        }
        if ("catalog".equalsIgnoreCase(selection.nodeType())) {
            return;
        }
        if (!containsName(readSchemas(metadata, selection.catalog()), selection.schema(), SchemaNode::name)) {
            throw selectedNodeNotFound();
        }
        if ("schema".equalsIgnoreCase(selection.nodeType())) {
            return;
        }
        List<TableNode> tables = readTables(metadata, selection.catalog(), selection.schema());
        TableNode table = tables.stream()
                .filter(item -> item.name().equals(selection.table()))
                .findFirst()
                .orElseThrow(TrinoMetadataService::selectedNodeNotFound);
        if ("table".equalsIgnoreCase(selection.nodeType())) {
            return;
        }
        boolean fieldExists = readFields(
                metadata, selection.catalog(), selection.schema(), table.name()).stream()
                .anyMatch(item -> item.name().equals(selection.field()));
        if (!fieldExists) {
            throw selectedNodeNotFound();
        }
    }

    private TrinoConnection resolveConnection(Long resourceId, Long tenantId, Jwt jwt) {
        if (resourceId == null || resourceId <= 0) {
            throw new IllegalArgumentException("resourceId must be a positive integer");
        }
        AbacResource resource = abacResourceService.getById(resourceId);
        if (resource == null
                || !Long.toString(tenantId).equals(resource.getOwner())
                || !RESOURCE_TYPE_TRINO_CONNECTION.equalsIgnoreCase(resource.getResourceType())
                || !"active".equalsIgnoreCase(resource.getStatus())) {
            throw new TrinoMetadataNotFoundException("Trino datasource not found");
        }

        TrinoConnection connection = trinoConnectionMapper.selectOne(
                new QueryWrapper<TrinoConnection>()
                        .eq("resource_id", resourceId)
                        .eq("status", "active"));
        if (connection == null) {
            throw new TrinoMetadataNotFoundException("Trino datasource not found");
        }
        if (!AUTH_TYPE_TOKEN.equalsIgnoreCase(connection.getAuthType())) {
            throw new TrinoMetadataException("The Trino datasource does not use token authentication");
        }
        if (!isAllowedJdbcUrl(connection.getJdbcUrl())) {
            throw new TrinoMetadataException("The Trino datasource JDBC URL is not allowed");
        }
        if (jwt == null || !StringUtils.hasText(jwt.getTokenValue())) {
            throw new IllegalArgumentException("A valid bearer token is required");
        }
        return connection;
    }

    private <T> T withMetadata(Long resourceId,
                               Long tenantId,
                               Jwt jwt,
                               MetadataReader<T> reader) {
        TrinoConnection connection = resolveConnection(resourceId, tenantId, jwt);
        Properties properties = new Properties();
        properties.setProperty("accessToken", jwt.getTokenValue());
        properties.setProperty("extraCredentials", "jwt:" + jwt.getTokenValue());

        try (Connection jdbcConnection = DriverManager.getConnection(connection.getJdbcUrl(), properties)) {
            return reader.read(jdbcConnection.getMetaData());
        } catch (SQLException exception) {
            log.warn("Failed to read Trino metadata for resource {} (SQLState: {}, message: {})",
                    resourceId, exception.getSQLState(), exception.getMessage());
            throw new TrinoMetadataException("Failed to read metadata from Trino", exception);
        }
    }

    private static void validateLevel(String catalog, String schema, String table) {
        if ((catalog != null && !StringUtils.hasText(catalog))
                || (schema != null && !StringUtils.hasText(schema))
                || (table != null && !StringUtils.hasText(table))
                || (schema != null && catalog == null)
                || (table != null && schema == null)) {
            throw new IllegalArgumentException("catalog, schema, and table must be provided in order");
        }
    }

    private static void validateSelection(NodeSelection selection) {
        if (selection == null) {
            throw new IllegalArgumentException("A Trino resource selection is required");
        }

        String nodeType = selection.nodeType() == null
                ? ""
                : selection.nodeType().trim().toLowerCase(Locale.ROOT);
        boolean validPath = switch (nodeType) {
            case "catalog" -> StringUtils.hasText(selection.catalog())
                    && !StringUtils.hasText(selection.schema())
                    && !StringUtils.hasText(selection.table())
                    && !StringUtils.hasText(selection.field());
            case "schema" -> StringUtils.hasText(selection.catalog())
                    && StringUtils.hasText(selection.schema())
                    && !StringUtils.hasText(selection.table())
                    && !StringUtils.hasText(selection.field());
            case "table" -> StringUtils.hasText(selection.catalog())
                    && StringUtils.hasText(selection.schema())
                    && StringUtils.hasText(selection.table())
                    && !StringUtils.hasText(selection.field());
            case "field" -> StringUtils.hasText(selection.catalog())
                    && StringUtils.hasText(selection.schema())
                    && StringUtils.hasText(selection.table())
                    && StringUtils.hasText(selection.field());
            default -> false;
        };
        if (!validPath) {
            throw new IllegalArgumentException("The selected Trino resource path is invalid");
        }
        if (selection.catalog().length() > 256
                || lengthExceeds(selection.schema(), 256)
                || lengthExceeds(selection.table(), 256)
                || lengthExceeds(selection.field(), 256)) {
            throw new IllegalArgumentException("Trino resource names must be at most 256 characters");
        }
    }

    private static <T> boolean containsName(List<T> nodes, String name, Function<T, String> nameReader) {
        return nodes.stream().anyMatch(node -> nameReader.apply(node).equals(name));
    }

    private static boolean lengthExceeds(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    private static TrinoMetadataNotFoundException selectedNodeNotFound() {
        return new TrinoMetadataNotFoundException("The selected Trino resource no longer exists");
    }

    private List<CatalogNode> readCatalogs(DatabaseMetaData metadata) throws SQLException {
        List<CatalogNode> catalogs = new ArrayList<>();
        try (ResultSet catalogRows = metadata.getCatalogs()) {
            while (catalogRows.next()) {
                String catalogName = catalogRows.getString("TABLE_CAT");
                if (StringUtils.hasText(catalogName)) {
                    catalogs.add(new CatalogNode(catalogName));
                }
            }
        }
        catalogs.sort(Comparator.comparing(CatalogNode::name, String.CASE_INSENSITIVE_ORDER));
        return catalogs;
    }

    private List<SchemaNode> readSchemas(DatabaseMetaData metadata, String catalogName) throws SQLException {
        List<SchemaNode> schemas = new ArrayList<>();
        try (ResultSet schemaRows = metadata.getSchemas(catalogName, null)) {
            while (schemaRows.next()) {
                String schemaName = schemaRows.getString("TABLE_SCHEM");
                if (StringUtils.hasText(schemaName)) {
                    schemas.add(new SchemaNode(schemaName));
                }
            }
        }
        schemas.sort(Comparator.comparing(SchemaNode::name, String.CASE_INSENSITIVE_ORDER));
        return schemas;
    }

    private List<TableNode> readTables(DatabaseMetaData metadata, String catalogName, String schemaName)
            throws SQLException {
        List<TableNode> tables = new ArrayList<>();
        try (ResultSet tableRows = metadata.getTables(catalogName, schemaName, null, null)) {
            while (tableRows.next()) {
                String tableName = tableRows.getString("TABLE_NAME");
                if (StringUtils.hasText(tableName)) {
                    tables.add(new TableNode(tableName, tableRows.getString("TABLE_TYPE")));
                }
            }
        }
        tables.sort(Comparator.comparing(TableNode::name, String.CASE_INSENSITIVE_ORDER));
        return tables;
    }

    private List<FieldNode> readFields(DatabaseMetaData metadata,
                                       String catalogName,
                                       String schemaName,
                                       String tableName) throws SQLException {
        List<FieldNode> fields = new ArrayList<>();
        try (ResultSet columnRows = metadata.getColumns(catalogName, schemaName, tableName, null)) {
            while (columnRows.next()) {
                int nullable = columnRows.getInt("NULLABLE");
                Boolean isNullable = nullable == DatabaseMetaData.columnNullable
                        ? Boolean.TRUE
                        : nullable == DatabaseMetaData.columnNoNulls ? Boolean.FALSE : null;
                fields.add(new FieldNode(
                        columnRows.getString("COLUMN_NAME"),
                        columnRows.getString("TYPE_NAME"),
                        nullableInteger(columnRows, "COLUMN_SIZE"),
                        nullableInteger(columnRows, "DECIMAL_DIGITS"),
                        isNullable,
                        nullableInteger(columnRows, "ORDINAL_POSITION")));
            }
        }
        fields.sort(Comparator.comparing(
                field -> field.ordinalPosition() == null ? Integer.MAX_VALUE : field.ordinalPosition()));
        return fields;
    }

    private static Integer nullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private static boolean isAllowedJdbcUrl(String jdbcUrl) {
        if (!StringUtils.hasText(jdbcUrl) || !jdbcUrl.regionMatches(true, 0, "jdbc:", 0, 5)) {
            return false;
        }
        try {
            URI uri = URI.create(jdbcUrl.substring(5));
            if (!"trino".equalsIgnoreCase(uri.getScheme())
                    || !ALLOWED_HOST.equalsIgnoreCase(uri.getHost())
                    || uri.getPort() != ALLOWED_PORT
                    || uri.getRawUserInfo() != null
                    || uri.getFragment() != null) {
                return false;
            }

            boolean sslEnabled = false;
            String query = uri.getRawQuery();
            if (query != null) {
                for (String parameter : query.split("&")) {
                    String[] parts = parameter.split("=", 2);
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String value = parts.length == 2
                            ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                            : "";
                    if (isSecretParameter(key)) {
                        return false;
                    }
                    if ("ssl".equalsIgnoreCase(key)) {
                        if (!"true".equalsIgnoreCase(value)) {
                            return false;
                        }
                        sslEnabled = true;
                    }
                }
            }
            return sslEnabled;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean isSecretParameter(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("credential")
                || normalized.contains("accesskey")
                || normalized.contains("access_key");
    }

    @FunctionalInterface
    private interface MetadataReader<T> {
        T read(DatabaseMetaData metadata) throws SQLException;
    }

    public record NodeSelection(String nodeType,
                                String catalog,
                                String schema,
                                String table,
                                String field) {
    }

    public record CatalogNode(String name) {
    }

    public record SchemaNode(String name) {
    }

    public record TableNode(String name, String type) {
    }

    public record FieldNode(String name,
                            String type,
                            Integer size,
                            Integer decimalDigits,
                            Boolean nullable,
                            Integer ordinalPosition) {
    }
}
