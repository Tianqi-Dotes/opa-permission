package com.opa.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.mapper.AbacResourceMapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.model.ResourceAttributeValue;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
public class TrinoExternalResourceAttributeService {
    private final AbacResourceService abacResourceService;
    private final AbacResourceMapper abacResourceMapper;
    private final AttributeDefinitionService attributeDefinitionService;
    private final ResourceAttributeValueService resourceAttributeValueService;
    private final TrinoMetadataService trinoMetadataService;

    public TrinoExternalResourceAttributeService(AbacResourceService abacResourceService,
                                                 AbacResourceMapper abacResourceMapper,
                                                 AttributeDefinitionService attributeDefinitionService,
                                                 ResourceAttributeValueService resourceAttributeValueService,
                                                 TrinoMetadataService trinoMetadataService) {
        this.abacResourceService = abacResourceService;
        this.abacResourceMapper = abacResourceMapper;
        this.attributeDefinitionService = attributeDefinitionService;
        this.resourceAttributeValueService = resourceAttributeValueService;
        this.trinoMetadataService = trinoMetadataService;
    }

    @Transactional
    public ReplaceResult replace(Long datasourceResourceId,
                                 Long tenantId,
                                 Jwt jwt,
                                 List<TrinoMetadataService.NodeSelection> selections,
                                 Long attributeId,
                                 String valueText) {
        AbacResource datasource = abacResourceService.getById(datasourceResourceId);
        if (datasource == null
                || !Long.toString(tenantId).equals(datasource.getOwner())
                || !Boolean.TRUE.equals(datasource.getExternal())
                || !"trino_connection".equalsIgnoreCase(datasource.getResourceType())
                || !"active".equalsIgnoreCase(datasource.getStatus())) {
            throw new TrinoMetadataNotFoundException("Trino datasource not found");
        }

        AttributeDefinition definition = attributeDefinitionService.getById(attributeId);
        if (definition == null) {
            throw new TrinoMetadataNotFoundException("Attribute definition not found");
        }
        if (!"resource".equalsIgnoreCase(definition.getCategory())) {
            throw new IllegalArgumentException("attributeId must reference a resource attribute");
        }
        if (valueText == null) {
            throw new IllegalArgumentException("valueText is required");
        }
        if (selections == null || selections.isEmpty()) {
            throw new IllegalArgumentException("At least one Trino resource selection is required");
        }

        List<TrinoMetadataService.NodeSelection> uniqueSelections =
                new ArrayList<>(new LinkedHashSet<>(selections));
        trinoMetadataService.validateNodeSelections(
                datasourceResourceId, tenantId, jwt, uniqueSelections);

        AbacResource lockedDatasource = abacResourceMapper.selectByIdForUpdate(datasourceResourceId);
        if (lockedDatasource == null
                || !Long.toString(tenantId).equals(lockedDatasource.getOwner())
                || !Boolean.TRUE.equals(lockedDatasource.getExternal())
                || !"trino_connection".equalsIgnoreCase(lockedDatasource.getResourceType())
                || !"active".equalsIgnoreCase(lockedDatasource.getStatus())) {
            throw new TrinoMetadataNotFoundException("Trino datasource not found");
        }

        List<Long> descendantIds = findDescendantIds(datasourceResourceId);
        if (!descendantIds.isEmpty()) {
            QueryWrapper<ResourceAttributeValue> valueQuery =
                    new QueryWrapper<ResourceAttributeValue>().in("resource_id", descendantIds);
            if (resourceAttributeValueService.count(valueQuery) > 0
                    && !resourceAttributeValueService.remove(
                            new QueryWrapper<ResourceAttributeValue>().in("resource_id", descendantIds))) {
                throw new IllegalStateException("Existing Trino resource attribute values were not removed");
            }
            if (!abacResourceService.removeByIds(descendantIds)) {
                throw new IllegalStateException("Existing Trino resources were not removed");
            }
        }

        long now = Instant.now().toEpochMilli();
        for (TrinoMetadataService.NodeSelection selection : uniqueSelections) {
            AbacResource selectedResource = createOrFindSelectedResource(
                    lockedDatasource, tenantId, selection);
            ResourceAttributeValue value = new ResourceAttributeValue();
            value.setResourceId(selectedResource.getId());
            value.setAttributeId(attributeId);
            value.setValueText(valueText);
            value.setCreateTime(now);
            value.setUpdateTime(now);
            if (!resourceAttributeValueService.save(value)) {
                throw new IllegalStateException("Trino resource attribute value was not created");
            }
        }
        return new ReplaceResult(uniqueSelections.size());
    }

    private List<Long> findDescendantIds(Long rootResourceId) {
        List<Long> descendants = new ArrayList<>();
        LinkedHashSet<Long> visited = new LinkedHashSet<>();
        visited.add(rootResourceId);
        List<Long> parents = List.of(rootResourceId);

        while (!parents.isEmpty()) {
            List<AbacResource> children = abacResourceService.list(
                    new QueryWrapper<AbacResource>().in("parent_resource_id", parents));
            List<Long> nextParents = new ArrayList<>();
            for (AbacResource child : children) {
                if (child.getId() != null && visited.add(child.getId())) {
                    descendants.add(child.getId());
                    nextParents.add(child.getId());
                }
            }
            parents = nextParents;
        }
        Collections.reverse(descendants);
        return descendants;
    }

    private AbacResource createOrFindSelectedResource(AbacResource datasource,
                                                       Long tenantId,
                                                       TrinoMetadataService.NodeSelection selection) {
        List<ResourcePathNode> path = pathFor(selection);
        AbacResource parent = datasource;
        long now = Instant.now().toEpochMilli();

        for (ResourcePathNode pathNode : path) {
            String resourceCode = TrinoResourceCode.generate(
                    tenantId, pathNode.type(), pathNode.name());
            AbacResource resource = abacResourceService.getOne(
                    new QueryWrapper<AbacResource>()
                            .eq("parent_resource_id", parent.getId())
                            .eq("resource_type", pathNode.type())
                            .eq("resource_code", resourceCode),
                    false);
            if (resource == null) {
                resource = new AbacResource();
                resource.setParentResourceId(parent.getId());
                resource.setExternal(true);
                resource.setResourceCode(resourceCode);
                resource.setResourceType(pathNode.type());
                resource.setResourceName(pathNode.name());
                resource.setOwner(Long.toString(tenantId));
                resource.setStatus("active");
                resource.setCreateTime(now);
                resource.setUpdateTime(now);
                if (!abacResourceService.save(resource)) {
                    throw new IllegalStateException("Trino resource node was not created");
                }
            } else if (!Objects.equals(resource.getParentResourceId(), parent.getId())
                    || !Long.toString(tenantId).equals(resource.getOwner())
                    || !Boolean.TRUE.equals(resource.getExternal())
                    || !pathNode.type().equalsIgnoreCase(resource.getResourceType())
                    || !pathNode.name().equals(resource.getResourceName())
                    || !"active".equalsIgnoreCase(resource.getStatus())) {
                throw new IllegalStateException("The existing Trino resource hierarchy is inconsistent");
            }
            parent = resource;
        }
        return parent;
    }

    private static List<ResourcePathNode> pathFor(TrinoMetadataService.NodeSelection selection) {
        List<ResourcePathNode> path = new ArrayList<>();
        path.add(new ResourcePathNode("trino_catalog", selection.catalog()));
        if (selection.schema() != null && !selection.schema().isBlank()) {
            path.add(new ResourcePathNode("trino_schema", selection.schema()));
        }
        if (selection.table() != null && !selection.table().isBlank()) {
            path.add(new ResourcePathNode("trino_table", selection.table()));
        }
        if (selection.field() != null && !selection.field().isBlank()) {
            path.add(new ResourcePathNode("trino_field", selection.field()));
        }
        return path;
    }

    private record ResourcePathNode(String type, String name) {
    }

    public record ReplaceResult(int resourceCount) {
    }
}
