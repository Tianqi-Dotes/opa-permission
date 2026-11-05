const pages = {
    "assign-roles": {
        section: "RBAC",
        title: "Assign Roles",
        description: "Manage role assignments for users in your workspace.",
        featureTitle: "Role assignment",
        featureDescription: "Select a workspace tool from the menu to get started. Feature forms will appear here.",
        symbol: "AR"
    },
    "test-permissions": {
        section: "RBAC",
        title: "Test Permissions",
        description: "Check whether a user is allowed to access a resource.",
        featureTitle: "Permission testing",
        featureDescription: "Test a user's access against the active role and permission rules.",
        symbol: "TP"
    },
    "assign-policy": {
        section: "RBAC",
        title: "Assign Policy",
        description: "Manage policies and associate them with the appropriate identities.",
        featureTitle: "Policy assignment",
        featureDescription: "Policy assignment tools will be available here.",
        symbol: "AP"
    },
    "attribute-dictionary": {
        section: "ABAC",
        title: "Attribute Dictionary",
        description: "Define the attributes available to ABAC rules.",
        featureTitle: "Attribute dictionary",
        featureDescription: "Manage attribute names, categories, and data types used by the policy editor.",
        symbol: "AD"
    },
    "attribute-templates": {
        section: "ABAC",
        title: "Attribute Template",
        description: "Manage platform defaults and tenant-specific attribute overrides.",
        featureTitle: "Attribute templates",
        featureDescription: "Configure default attributes for the platform and individual tenants.",
        symbol: "AT"
    },
    "subject-attributes": {
        section: "ABAC",
        title: "Subject Attributes",
        description: "Manage attributes associated with users and other subjects.",
        featureTitle: "Subject attributes",
        featureDescription: "View and manage the attributes that ABAC policies evaluate for a subject.",
        symbol: "SA"
    },
    "resource-attributes": {
        section: "ABAC",
        title: "Resource Attributes",
        description: "Manage protected resources and their ABAC attributes.",
        featureTitle: "Resource attributes",
        featureDescription: "Register resources and define the attributes used to evaluate access.",
        symbol: "RA"
    },
    "policy-config": {
        section: "ABAC",
        title: "Policy Config",
        description: "Define ABAC rules and generate the single Rego configuration.",
        featureTitle: "Policy configuration",
        featureDescription: "Build allow and deny rules from the attribute dictionary, then generate Rego.",
        symbol: "PC"
    },
    "playground": {
        section: "ABAC",
        title: "Playground",
        description: "Evaluate sample requests against the current ABAC policy.",
        featureTitle: "Policy playground",
        featureDescription: "Provide subject, resource, action, and environment input to inspect a policy decision.",
        symbol: "PG"
    },
    "publish-policy": {
        section: "ABAC",
        title: "Publish",
        description: "Review and publish the generated ABAC configuration.",
        featureTitle: "Publish policy",
        featureDescription: "Review the generated Rego and validation results before making the configuration available.",
        symbol: "PU"
    },
    "query-trino": {
        section: "Workspace",
        title: "Query Trino",
        description: "Run read-only queries against your connected Trino catalogs.",
        featureTitle: "Trino query",
        featureDescription: "The query workspace will be available here.",
        symbol: "QT"
    },
    "create-trino-table": {
        section: "Workspace",
        title: "Create Trino Table",
        description: "Create and manage tables in your Trino data catalogs.",
        featureTitle: "Table management",
        featureDescription: "Table management tools will be available here.",
        symbol: "CT"
    },
    "create-bucket": {
        section: "Workspace",
        title: "Create Bucket",
        description: "Register a bucket as an ABAC-protected resource.",
        featureTitle: "Create bucket",
        featureDescription: "Register a bucket resource for the current tenant.",
        symbol: "CB"
    }
};

const token = sessionStorage.getItem("accessToken");
if (!token) {
    window.location.replace("./index.html");
}

const navItems = document.querySelectorAll(".nav-item");
const sidebar = document.querySelector("#sidebar");
const mobileMenuButton = document.querySelector("#mobile-menu-button");
const logoutButton = document.querySelector("#logout-button");
const pageTitle = document.querySelector("#page-title");
const pageKicker = document.querySelector("#page-kicker");
const breadcrumbSection = document.querySelector("#breadcrumb-section");
const breadcrumbPage = document.querySelector("#breadcrumb-page");
const pageDescription = document.querySelector("#page-description");
const featureTitle = document.querySelector("#feature-title");
const featureDescription = document.querySelector("#feature-description");
const featureSymbol = document.querySelector("#feature-symbol");
const featureSection = document.querySelector("#feature-section");
const userName = document.querySelector("#user-name");
const userAvatar = document.querySelector("#user-avatar");
const featureCard = document.querySelector("#feature-card");
const infoRow = document.querySelector("#info-row");
const assignRolePanel = document.querySelector("#assign-role-panel");
const assignRoleForm = document.querySelector("#assign-role-form");
const assignRoleUser = document.querySelector("#assign-role-user");
const assignRoleRole = document.querySelector("#assign-role-role");
const assignRoleSubmit = document.querySelector("#assign-role-submit");
const assignRoleStatus = document.querySelector("#assign-role-status");
const refreshUsersButton = document.querySelector("#refresh-users");
const permissionTestPanel = document.querySelector("#permission-test-panel");
const permissionTestList = document.querySelector("#permission-test-list");
const permissionTestSummary = document.querySelector("#permission-test-summary");
const runAllPermissionTestsButton = document.querySelector("#run-all-permission-tests");
const attributeDictionaryPanel = document.querySelector("#attribute-dictionary-panel");
const attributeCategoryFilter = document.querySelector("#attribute-category-filter");
const refreshAttributesButton = document.querySelector("#refresh-attributes");
const newAttributeButton = document.querySelector("#new-attribute");
const attributeForm = document.querySelector("#attribute-form");
const attributeFormTitle = document.querySelector("#attribute-form-title");
const attributeName = document.querySelector("#attribute-name");
const attributeCategory = document.querySelector("#attribute-category");
const attributeDataType = document.querySelector("#attribute-data-type");
const attributeMultiValued = document.querySelector("#attribute-multi-valued");
const attributeDescription = document.querySelector("#attribute-description");
const saveAttributeButton = document.querySelector("#save-attribute");
const cancelAttributeButton = document.querySelector("#cancel-attribute");
const attributeRows = document.querySelector("#attribute-rows");
const attributeStatus = document.querySelector("#attribute-status");
const attributeTemplatePanel = document.querySelector("#attribute-template-panel");
const defaultTemplateTab = document.querySelector("#default-template-tab");
const tenantTemplateTab = document.querySelector("#tenant-template-tab");
const defaultTemplatePanel = document.querySelector("#default-template-panel");
const tenantTemplatePanel = document.querySelector("#tenant-template-panel");
const resourceAttributesPanel = document.querySelector("#resource-attributes-panel");
const internalResourceTab = document.querySelector("#internal-resource-tab");
const externalResourceTab = document.querySelector("#external-resource-tab");
const internalResourcePanel = document.querySelector("#internal-resource-panel");
const externalResourcePanel = document.querySelector("#external-resource-panel");
const internalResourceRows = document.querySelector("#internal-resource-rows");
const externalResourceRows = document.querySelector("#external-resource-rows");
const internalResourceListStatus = document.querySelector("#internal-resource-list-status");
const externalResourceListStatus = document.querySelector("#external-resource-list-status");
const refreshInternalResourcesButton = document.querySelector("#refresh-internal-resources");
const refreshExternalResourcesButton = document.querySelector("#refresh-external-resources");
const selectedResourceLabel = document.querySelector("#selected-resource-label");
const resourceAttributeFilterResourceId = document.querySelector("#resource-attribute-filter-resource-id");
const resourceAttributeFilterAttributeId = document.querySelector("#resource-attribute-filter-attribute-id");
const filterResourceAttributesButton = document.querySelector("#filter-resource-attributes");
const newResourceAttributeButton = document.querySelector("#new-resource-attribute");
const resourceAttributeForm = document.querySelector("#resource-attribute-form");
const resourceAttributeFormTitle = document.querySelector("#resource-attribute-form-title");
const resourceAttributeResourceId = document.querySelector("#resource-attribute-resource-id");
const resourceAttributeAttributeId = document.querySelector("#resource-attribute-attribute-id");
const resourceAttributeValueText = document.querySelector("#resource-attribute-value-text");
const saveResourceAttributeButton = document.querySelector("#save-resource-attribute");
const cancelResourceAttributeButton = document.querySelector("#cancel-resource-attribute");
const resourceAttributeRows = document.querySelector("#resource-attribute-rows");
const resourceAttributeStatus = document.querySelector("#resource-attribute-status");
const createBucketPanel = document.querySelector("#create-bucket-panel");
const createBucketForm = document.querySelector("#create-bucket-form");
const bucketNameInput = document.querySelector("#bucket-name");
const createBucketSubmit = document.querySelector("#create-bucket-submit");
const createBucketStatus = document.querySelector("#create-bucket-status");

const apiBaseUrl = (window.API_BASE_URL || "http://localhost:1111").replace(/\/+$/, "");
const trinoPermissionTests = [
    {name: "Query Trino", path: "/api/trino/query", body: {sql: ""}},
    {name: "Create Trino Catalog", path: "/api/trino/catalogs", body: {}},
    {name: "Create Trino Schema", path: "/api/trino/schemas", body: {}},
    {name: "Create Trino Table", path: "/api/trino/tables", body: {}}
];
const permissionTestRows = new Map();
let permissionTestRunInProgress = false;
let userListLoading = false;
let roleAssignmentInProgress = false;
let userListLoaded = false;
let attributeListRequestId = 0;
let attributeMutationInProgress = false;
let editingAttributeId = null;
let bucketCreationInProgress = false;
let resourceAttributeDefinitions = [];
let resourceAttributeListRequestId = 0;
let resourceAttributeMutationInProgress = false;
let editingResourceAttributeId = null;
let activeResourceScope = "internal";
let selectedResource = null;
const resourceListRequestIds = {internal: 0, external: 0};

function getTokenSubject(encodedToken) {
    try {
        const payload = encodedToken.split(".")[1];
        const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
        const binaryPayload = atob(base64);
        const bytes = Uint8Array.from(binaryPayload, (character) => character.charCodeAt(0));
        const claims = JSON.parse(new TextDecoder().decode(bytes));
        return typeof claims.sub === "string" && claims.sub ? claims.sub : "User";
    } catch {
        return "User";
    }
}

function activateAttributeTemplateTab(selectedTab, shouldFocus = false) {
    const isDefaultTemplate = selectedTab === defaultTemplateTab;
    defaultTemplateTab.classList.toggle("active", isDefaultTemplate);
    defaultTemplateTab.setAttribute("aria-selected", String(isDefaultTemplate));
    defaultTemplateTab.tabIndex = isDefaultTemplate ? 0 : -1;
    defaultTemplatePanel.hidden = !isDefaultTemplate;
    tenantTemplateTab.classList.toggle("active", !isDefaultTemplate);
    tenantTemplateTab.setAttribute("aria-selected", String(!isDefaultTemplate));
    tenantTemplateTab.tabIndex = isDefaultTemplate ? -1 : 0;
    tenantTemplatePanel.hidden = isDefaultTemplate;
    if (shouldFocus) {
        selectedTab.focus();
    }
}

function activateResourceScopeTab(selectedTab, shouldFocus = false) {
    const isInternal = selectedTab === internalResourceTab;
    internalResourceTab.classList.toggle("active", isInternal);
    internalResourceTab.setAttribute("aria-selected", String(isInternal));
    internalResourceTab.tabIndex = isInternal ? 0 : -1;
    internalResourcePanel.hidden = !isInternal;
    externalResourceTab.classList.toggle("active", !isInternal);
    externalResourceTab.setAttribute("aria-selected", String(!isInternal));
    externalResourceTab.tabIndex = isInternal ? -1 : 0;
    externalResourcePanel.hidden = isInternal;
    activeResourceScope = isInternal ? "internal" : "external";
    loadResourceList(activeResourceScope);
    if (shouldFocus) {
        selectedTab.focus();
    }
}

function showPage(pageKey) {
    const page = pages[pageKey];
    if (!page) {
        return;
    }

    pageTitle.textContent = page.title;
    pageKicker.textContent = page.section;
    breadcrumbSection.textContent = page.section;
    breadcrumbPage.textContent = page.title;
    pageDescription.textContent = page.description;
    featureTitle.textContent = page.featureTitle;
    featureDescription.textContent = page.featureDescription;
    featureSymbol.textContent = page.symbol;
    featureSection.textContent = page.section.toUpperCase();
    const isAssignRolesPage = pageKey === "assign-roles";
    const isPermissionTestPage = pageKey === "test-permissions";
    const isAttributeDictionaryPage = pageKey === "attribute-dictionary";
    const isAttributeTemplatesPage = pageKey === "attribute-templates";
    const isResourceAttributesPage = pageKey === "resource-attributes";
    const isCreateBucketPage = pageKey === "create-bucket";
    featureCard.hidden = isAssignRolesPage || isPermissionTestPage
        || isAttributeDictionaryPage || isAttributeTemplatesPage
        || isResourceAttributesPage || isCreateBucketPage;
    infoRow.hidden = isAssignRolesPage || isPermissionTestPage
        || isAttributeDictionaryPage || isAttributeTemplatesPage
        || isResourceAttributesPage || isCreateBucketPage;
    assignRolePanel.hidden = !isAssignRolesPage;
    permissionTestPanel.hidden = !isPermissionTestPage;
    attributeDictionaryPanel.hidden = !isAttributeDictionaryPage;
    attributeTemplatePanel.hidden = !isAttributeTemplatesPage;
    resourceAttributesPanel.hidden = !isResourceAttributesPage;
    createBucketPanel.hidden = !isCreateBucketPage;
    if (isAssignRolesPage) {
        loadUsers();
    }
    if (isAttributeDictionaryPage) {
        loadAttributeDefinitions();
    }
    if (isResourceAttributesPage) {
        loadResourceAttributes();
    }

    navItems.forEach((item) => {
        const isActive = item.dataset.page === pageKey;
        item.classList.toggle("active", isActive);
        if (isActive) {
            item.setAttribute("aria-current", "page");
        } else {
            item.removeAttribute("aria-current");
        }
    });

    sidebar.classList.remove("open");
    mobileMenuButton.setAttribute("aria-expanded", "false");
}

function updateAssignRoleSubmitState() {
    assignRoleSubmit.disabled = userListLoading || roleAssignmentInProgress
        || !assignRoleUser.value || !assignRoleRole.value;
}

async function getApiError(response) {
    if (response.status === 403) {
        return "Access denied: this account lacks an active permission for the endpoint. For the default setup, sign in as admin and rerun ddl.sql and seed_default_role_assignments.sql.";
    }
    try {
        const body = await response.json();
        return body.message || `Request failed with HTTP ${response.status}.`;
    } catch {
        return `Request failed with HTTP ${response.status}.`;
    }
}

async function createBucket(event) {
    event.preventDefault();
    if (bucketCreationInProgress || !createBucketForm.reportValidity()) {
        return;
    }

    const bucketName = bucketNameInput.value.trim();
    if (!bucketName) {
        createBucketStatus.textContent = "Bucket name is required.";
        bucketNameInput.focus();
        return;
    }

    bucketCreationInProgress = true;
    createBucketSubmit.disabled = true;
    createBucketStatus.textContent = "Creating bucket resource...";
    try {
        const response = await fetch(`${apiBaseUrl}/api/abac/resources/buckets`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({bucketName})
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const resource = await response.json();
        createBucketStatus.textContent =
            `Bucket "${bucketName}" was registered with resource ID ${resource.id} and code ${resource.resourceCode}.`;
        createBucketForm.reset();
    } catch (error) {
        createBucketStatus.textContent = error.message || "Unable to create the bucket resource.";
    } finally {
        bucketCreationInProgress = false;
        createBucketSubmit.disabled = false;
    }
}

function formatAttributeLabel(value) {
    return value ? value.charAt(0).toUpperCase() + value.slice(1) : "";
}

function renderAttributeDefinitions(definitions) {
    attributeRows.replaceChildren();
    if (definitions.length === 0) {
        const emptyRow = document.createElement("tr");
        const emptyCell = document.createElement("td");
        emptyCell.className = "attribute-empty-state";
        emptyCell.colSpan = 7;
        emptyCell.textContent = "No attribute definitions found.";
        emptyRow.append(emptyCell);
        attributeRows.append(emptyRow);
        return;
    }

    definitions.forEach((definition) => {
        const row = document.createElement("tr");
        const values = [
            definition.name,
            formatAttributeLabel(definition.category),
            formatAttributeLabel(definition.dataType),
            definition.multiValued ? "Yes" : "No",
            definition.description || "—",
            definition.isDefault ? "Platform default" : "Custom"
        ];
        values.forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value == null ? "" : String(value);
            row.append(cell);
        });

        const actionsCell = document.createElement("td");
        const actions = document.createElement("div");
        actions.className = "attribute-table-actions";
        const editButton = document.createElement("button");
        editButton.className = "secondary-button";
        editButton.type = "button";
        editButton.textContent = "Edit";
        editButton.setAttribute("aria-label", `Edit ${definition.name}`);
        const deleteButton = document.createElement("button");
        deleteButton.className = "secondary-button";
        deleteButton.type = "button";
        deleteButton.textContent = "Delete";
        deleteButton.setAttribute("aria-label", `Delete ${definition.name}`);
        if (definition.isDefault) {
            editButton.disabled = true;
            editButton.title = "Platform default attributes are read-only";
            deleteButton.disabled = true;
            deleteButton.title = "Platform default attributes cannot be deleted";
        } else {
            editButton.addEventListener("click", () => openAttributeForm(definition));
            deleteButton.addEventListener("click", () => deleteAttribute(definition, deleteButton));
        }
        actions.append(editButton, deleteButton);
        actionsCell.append(actions);
        row.append(actionsCell);
        attributeRows.append(row);
    });
}

async function loadAttributeDefinitions() {
    const requestId = ++attributeListRequestId;
    const category = attributeCategoryFilter.value;
    const query = category ? `?category=${encodeURIComponent(category)}` : "";
    refreshAttributesButton.disabled = true;
    attributeStatus.textContent = "Loading attribute definitions...";

    try {
        const response = await fetch(`${apiBaseUrl}/api/abac/attributes${query}`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const definitions = await response.json();
        if (!Array.isArray(definitions)) {
            throw new Error("The attribute definition response was not a list.");
        }
        if (requestId !== attributeListRequestId) {
            return;
        }

        renderAttributeDefinitions(definitions);
        attributeStatus.textContent = `Loaded ${definitions.length} attribute definition${definitions.length === 1 ? "" : "s"}.`;
    } catch (error) {
        if (requestId === attributeListRequestId) {
            attributeRows.replaceChildren();
            const errorRow = document.createElement("tr");
            const errorCell = document.createElement("td");
            errorCell.className = "attribute-empty-state";
            errorCell.colSpan = 6;
            errorCell.textContent = error.message || "Unable to load attribute definitions.";
            errorRow.append(errorCell);
            attributeRows.append(errorRow);
            attributeStatus.textContent = error.message || "Unable to load attribute definitions.";
        }
    } finally {
        if (requestId === attributeListRequestId) {
            refreshAttributesButton.disabled = false;
        }
    }
}

function resetAttributeForm() {
    editingAttributeId = null;
    attributeForm.reset();
    attributeForm.hidden = true;
    attributeFormTitle.textContent = "Add attribute";
    saveAttributeButton.textContent = "Save attribute";
}

function openAttributeForm(definition = null) {
    if (attributeMutationInProgress) {
        return;
    }
    editingAttributeId = definition ? definition.id : null;
    attributeFormTitle.textContent = definition ? "Edit attribute" : "Add attribute";
    saveAttributeButton.textContent = definition ? "Save changes" : "Save attribute";
    attributeName.value = definition ? definition.name : "";
    attributeCategory.value = definition ? definition.category : "subject";
    attributeDataType.value = definition ? definition.dataType : "string";
    attributeMultiValued.checked = definition ? Boolean(definition.multiValued) : false;
    attributeDescription.value = definition && definition.description ? definition.description : "";
    attributeForm.hidden = false;
    attributeName.focus();
}

async function saveAttribute(event) {
    event.preventDefault();
    if (attributeMutationInProgress || !attributeForm.reportValidity()) {
        return;
    }

    attributeMutationInProgress = true;
    saveAttributeButton.disabled = true;
    cancelAttributeButton.disabled = true;
    const isEditing = editingAttributeId !== null;
    const definition = {
        name: attributeName.value.trim(),
        category: attributeCategory.value,
        dataType: attributeDataType.value,
        multiValued: attributeMultiValued.checked,
        description: attributeDescription.value.trim()
    };

    try {
        const path = isEditing
            ? `/api/abac/attributes/${encodeURIComponent(String(editingAttributeId))}`
            : "/api/abac/attributes";
        const response = await fetch(`${apiBaseUrl}${path}`, {
            method: isEditing ? "PUT" : "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(definition)
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        attributeStatus.textContent = isEditing
            ? "Attribute definition updated."
            : "Attribute definition created.";
        resetAttributeForm();
        await loadAttributeDefinitions();
    } catch (error) {
        attributeStatus.textContent = error.message || "Unable to save the attribute definition.";
    } finally {
        attributeMutationInProgress = false;
        saveAttributeButton.disabled = false;
        cancelAttributeButton.disabled = false;
    }
}

async function deleteAttribute(definition, button) {
    if (attributeMutationInProgress
            || !window.confirm(`Delete the "${definition.name}" attribute definition?`)) {
        return;
    }

    attributeMutationInProgress = true;
    button.disabled = true;
    attributeStatus.textContent = `Deleting ${definition.name}...`;
    try {
        const response = await fetch(
            `${apiBaseUrl}/api/abac/attributes/${encodeURIComponent(String(definition.id))}`,
            {
                method: "DELETE",
                headers: {Authorization: `Bearer ${token}`}
            }
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        attributeStatus.textContent = `${definition.name} was deleted.`;
        await loadAttributeDefinitions();
    } catch (error) {
        attributeStatus.textContent = error.message || "Unable to delete the attribute definition.";
        button.disabled = false;
    } finally {
        attributeMutationInProgress = false;
    }
}

function populateResourceAttributeSelects() {
    const filterSelection = resourceAttributeFilterAttributeId.value;
    const formSelection = resourceAttributeAttributeId.value;

    resourceAttributeFilterAttributeId.replaceChildren(new Option("All resource attributes", ""));
    resourceAttributeAttributeId.replaceChildren(new Option("Select an attribute", ""));
    resourceAttributeDefinitions.forEach((definition) => {
        const label = `${definition.name} (${definition.dataType}${definition.multiValued ? ", multiple" : ""})`;
        resourceAttributeFilterAttributeId.append(new Option(label, String(definition.id)));
        resourceAttributeAttributeId.append(new Option(label, String(definition.id)));
    });

    if (resourceAttributeDefinitions.some((definition) => String(definition.id) === filterSelection)) {
        resourceAttributeFilterAttributeId.value = filterSelection;
    }
    if (resourceAttributeDefinitions.some((definition) => String(definition.id) === formSelection)) {
        resourceAttributeAttributeId.value = formSelection;
    }
    newResourceAttributeButton.disabled = resourceAttributeDefinitions.length === 0;
}

async function loadResourceAttributeDefinitions() {
    newResourceAttributeButton.disabled = true;
    const response = await fetch(`${apiBaseUrl}/api/abac/attributes?category=resource`, {
        headers: {Authorization: `Bearer ${token}`}
    });
    if (!response.ok) {
        throw new Error(await getApiError(response));
    }

    const definitions = await response.json();
    if (!Array.isArray(definitions)) {
        throw new Error("The resource attribute definition response was not a list.");
    }
    resourceAttributeDefinitions = definitions;
    populateResourceAttributeSelects();
}

function renderResourceList(scope, resources) {
    const rows = scope === "external" ? externalResourceRows : internalResourceRows;
    rows.replaceChildren();
    if (resources.length === 0) {
        const emptyRow = document.createElement("tr");
        const emptyCell = document.createElement("td");
        emptyCell.className = "attribute-empty-state";
        emptyCell.colSpan = 6;
        emptyCell.textContent = `No ${scope} resources found.`;
        emptyRow.append(emptyCell);
        rows.append(emptyRow);
        return;
    }

    resources.forEach((resource) => {
        const row = document.createElement("tr");
        [resource.id, resource.resourceName, resource.resourceType,
            resource.resourceCode, resource.status].forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value == null ? "" : String(value);
            row.append(cell);
        });

        const actionsCell = document.createElement("td");
        const selectButton = document.createElement("button");
        selectButton.className = "secondary-button";
        selectButton.type = "button";
        selectButton.textContent = "Select";
        selectButton.setAttribute("aria-label", `Select resource ${resource.resourceName || resource.id}`);
        selectButton.addEventListener("click", () => {
            selectedResource = resource;
            resourceAttributeFilterResourceId.value = String(resource.id);
            selectedResourceLabel.textContent =
                `Selected ${resource.resourceName || resource.resourceCode} (ID ${resource.id}).`;
            loadResourceAttributeValues();
        });
        actionsCell.append(selectButton);
        row.append(actionsCell);
        rows.append(row);
    });
}

async function loadResourceList(scope) {
    const isExternal = scope === "external";
    const rows = isExternal ? externalResourceRows : internalResourceRows;
    const status = isExternal ? externalResourceListStatus : internalResourceListStatus;
    const refreshButton = isExternal ? refreshExternalResourcesButton : refreshInternalResourcesButton;
    const requestId = ++resourceListRequestIds[scope];
    refreshButton.disabled = true;
    status.textContent = `Loading ${scope} resources...`;

    try {
        const query = new URLSearchParams({external: String(isExternal)});
        const response = await fetch(`${apiBaseUrl}/api/abac/resources?${query.toString()}`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const resources = await response.json();
        if (!Array.isArray(resources)) {
            throw new Error("The resource list response was not a list.");
        }
        if (requestId !== resourceListRequestIds[scope]) {
            return;
        }
        renderResourceList(scope, resources);
        status.textContent =
            `Loaded ${resources.length} ${scope} resource${resources.length === 1 ? "" : "s"}.`;
    } catch (error) {
        if (requestId === resourceListRequestIds[scope]) {
            rows.replaceChildren();
            const errorRow = document.createElement("tr");
            const errorCell = document.createElement("td");
            errorCell.className = "attribute-empty-state";
            errorCell.colSpan = 6;
            errorCell.textContent = error.message || `Unable to load ${scope} resources.`;
            errorRow.append(errorCell);
            rows.append(errorRow);
            status.textContent = error.message || `Unable to load ${scope} resources.`;
        }
    } finally {
        if (requestId === resourceListRequestIds[scope]) {
            refreshButton.disabled = false;
        }
    }
}

function renderResourceAttributeValues(values) {
    resourceAttributeRows.replaceChildren();
    if (values.length === 0) {
        const emptyRow = document.createElement("tr");
        const emptyCell = document.createElement("td");
        emptyCell.className = "attribute-empty-state";
        emptyCell.colSpan = 5;
        emptyCell.textContent = "No resource attribute values found.";
        emptyRow.append(emptyCell);
        resourceAttributeRows.append(emptyRow);
        return;
    }

    values.forEach((value) => {
        const definition = resourceAttributeDefinitions.find(
            (item) => String(item.id) === String(value.attributeId));
        const row = document.createElement("tr");
        [value.id, value.resourceId, definition ? definition.name : `Attribute #${value.attributeId}`,
            value.valueText].forEach((cellValue) => {
            const cell = document.createElement("td");
            cell.textContent = cellValue == null ? "" : String(cellValue);
            row.append(cell);
        });

        const actionsCell = document.createElement("td");
        const actions = document.createElement("div");
        actions.className = "attribute-table-actions";
        const editButton = document.createElement("button");
        editButton.className = "secondary-button";
        editButton.type = "button";
        editButton.textContent = "Edit";
        editButton.setAttribute("aria-label", `Edit attribute value ${value.id}`);
        editButton.addEventListener("click", () => openResourceAttributeForm(value));
        const deleteButton = document.createElement("button");
        deleteButton.className = "secondary-button";
        deleteButton.type = "button";
        deleteButton.textContent = "Delete";
        deleteButton.setAttribute("aria-label", `Delete attribute value ${value.id}`);
        deleteButton.addEventListener("click", () => deleteResourceAttribute(value, deleteButton));
        actions.append(editButton, deleteButton);
        actionsCell.append(actions);
        row.append(actionsCell);
        resourceAttributeRows.append(row);
    });
}

async function loadResourceAttributeValues() {
    const requestId = ++resourceAttributeListRequestId;
    const resourceId = resourceAttributeFilterResourceId.value.trim();
    if (resourceId && (!Number.isSafeInteger(Number(resourceId)) || Number(resourceId) <= 0)) {
        resourceAttributeStatus.textContent = "Resource ID must be a positive integer.";
        resourceAttributeFilterResourceId.focus();
        return;
    }
    if (resourceId) {
        if (!selectedResource || String(selectedResource.id) !== resourceId) {
            selectedResource = null;
            selectedResourceLabel.textContent = `Resource ID ${resourceId}`;
        }
    } else {
        selectedResource = null;
        selectedResourceLabel.textContent = "Showing values for all resources.";
    }

    const query = new URLSearchParams();
    if (resourceId) {
        query.set("resourceId", resourceId);
    }
    if (resourceAttributeFilterAttributeId.value) {
        query.set("attributeId", resourceAttributeFilterAttributeId.value);
    }
    const queryString = query.toString() ? `?${query.toString()}` : "";
    filterResourceAttributesButton.disabled = true;
    resourceAttributeStatus.textContent = "Loading resource attribute values...";

    try {
        const response = await fetch(`${apiBaseUrl}/api/abac/resource-attributes${queryString}`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const values = await response.json();
        if (!Array.isArray(values)) {
            throw new Error("The resource attribute value response was not a list.");
        }
        if (requestId !== resourceAttributeListRequestId) {
            return;
        }
        renderResourceAttributeValues(values);
        resourceAttributeStatus.textContent =
            `Loaded ${values.length} resource attribute value${values.length === 1 ? "" : "s"}.`;
    } catch (error) {
        if (requestId === resourceAttributeListRequestId) {
            resourceAttributeRows.replaceChildren();
            const errorRow = document.createElement("tr");
            const errorCell = document.createElement("td");
            errorCell.className = "attribute-empty-state";
            errorCell.colSpan = 5;
            errorCell.textContent = error.message || "Unable to load resource attribute values.";
            errorRow.append(errorCell);
            resourceAttributeRows.append(errorRow);
            resourceAttributeStatus.textContent =
                error.message || "Unable to load resource attribute values.";
        }
    } finally {
        if (requestId === resourceAttributeListRequestId) {
            filterResourceAttributesButton.disabled = false;
        }
    }
}

async function loadResourceAttributes() {
    resourceAttributeStatus.textContent = "Loading resource attributes...";
    try {
        await loadResourceAttributeDefinitions();
        await loadResourceAttributeValues();
    } catch (error) {
        resourceAttributeDefinitions = [];
        populateResourceAttributeSelects();
        resourceAttributeRows.replaceChildren();
        const errorRow = document.createElement("tr");
        const errorCell = document.createElement("td");
        errorCell.className = "attribute-empty-state";
        errorCell.colSpan = 5;
        errorCell.textContent = error.message || "Unable to load resource attributes.";
        errorRow.append(errorCell);
        resourceAttributeRows.append(errorRow);
        resourceAttributeStatus.textContent = error.message || "Unable to load resource attributes.";
    }
    await loadResourceList(activeResourceScope);
}

function openResourceAttributeForm(value = null) {
    if (resourceAttributeMutationInProgress || resourceAttributeDefinitions.length === 0) {
        return;
    }
    editingResourceAttributeId = value ? value.id : null;
    resourceAttributeFormTitle.textContent = value
        ? "Edit resource attribute value"
        : "Add resource attribute value";
    saveResourceAttributeButton.textContent = value ? "Save changes" : "Save value";
    resourceAttributeResourceId.value = value
        ? String(value.resourceId)
        : resourceAttributeFilterResourceId.value;
    resourceAttributeAttributeId.value = value
        ? String(value.attributeId)
        : resourceAttributeFilterAttributeId.value;
    resourceAttributeValueText.value = value && value.valueText != null ? value.valueText : "";
    resourceAttributeForm.hidden = false;
    resourceAttributeResourceId.focus();
}

function resetResourceAttributeForm() {
    editingResourceAttributeId = null;
    resourceAttributeForm.reset();
    resourceAttributeForm.hidden = true;
    resourceAttributeFormTitle.textContent = "Add resource attribute value";
    saveResourceAttributeButton.textContent = "Save value";
}

async function saveResourceAttribute(event) {
    event.preventDefault();
    if (resourceAttributeMutationInProgress || !resourceAttributeForm.reportValidity()) {
        return;
    }

    const resourceId = Number(resourceAttributeResourceId.value);
    const attributeId = Number(resourceAttributeAttributeId.value);
    if (!Number.isSafeInteger(resourceId) || resourceId <= 0
            || !Number.isSafeInteger(attributeId) || attributeId <= 0) {
        resourceAttributeStatus.textContent = "Select a valid resource and attribute.";
        return;
    }

    resourceAttributeMutationInProgress = true;
    saveResourceAttributeButton.disabled = true;
    cancelResourceAttributeButton.disabled = true;
    const isEditing = editingResourceAttributeId !== null;
    const value = {
        resourceId,
        attributeId,
        valueText: resourceAttributeValueText.value
    };
    try {
        const path = isEditing
            ? `/api/abac/resource-attributes/${encodeURIComponent(String(editingResourceAttributeId))}`
            : "/api/abac/resource-attributes";
        const response = await fetch(`${apiBaseUrl}${path}`, {
            method: isEditing ? "PUT" : "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(value)
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        resourceAttributeStatus.textContent = isEditing
            ? "Resource attribute value updated."
            : "Resource attribute value created.";
        resetResourceAttributeForm();
        await loadResourceAttributeValues();
    } catch (error) {
        resourceAttributeStatus.textContent =
            error.message || "Unable to save the resource attribute value.";
    } finally {
        resourceAttributeMutationInProgress = false;
        saveResourceAttributeButton.disabled = false;
        cancelResourceAttributeButton.disabled = false;
    }
}

async function deleteResourceAttribute(value, button) {
    if (resourceAttributeMutationInProgress
            || !window.confirm(`Delete resource attribute value ${value.id}?`)) {
        return;
    }

    resourceAttributeMutationInProgress = true;
    button.disabled = true;
    resourceAttributeStatus.textContent = `Deleting resource attribute value ${value.id}...`;
    try {
        const response = await fetch(
            `${apiBaseUrl}/api/abac/resource-attributes/${encodeURIComponent(String(value.id))}`,
            {
                method: "DELETE",
                headers: {Authorization: `Bearer ${token}`}
            }
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        resourceAttributeStatus.textContent = `Resource attribute value ${value.id} deleted.`;
        await loadResourceAttributeValues();
    } catch (error) {
        resourceAttributeStatus.textContent =
            error.message || "Unable to delete the resource attribute value.";
        button.disabled = false;
    } finally {
        resourceAttributeMutationInProgress = false;
    }
}

async function loadUsers() {
    if (userListLoading) {
        return;
    }

    userListLoading = true;
    refreshUsersButton.disabled = true;
    assignRoleUser.disabled = true;
    updateAssignRoleSubmitState();
    assignRoleStatus.textContent = "Loading users in your tenant...";

    try {
        const response = await fetch(`${apiBaseUrl}/api/permissions/users`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const users = await response.json();
        if (!Array.isArray(users)) {
            throw new Error("The user list response was not a list.");
        }

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = users.length ? "Select a user" : "No users found";
        assignRoleUser.replaceChildren(placeholder);
        users.forEach((user) => {
            const option = document.createElement("option");
            option.value = String(user.userId);
            option.textContent = `${user.username} (ID: ${user.userId})`;
            assignRoleUser.append(option);
        });
        userListLoaded = true;
        assignRoleUser.disabled = users.length === 0;
        assignRoleStatus.textContent = users.length
            ? `Loaded ${users.length} user${users.length === 1 ? "" : "s"}. Select a user and role.`
            : "No users were found in your tenant.";
    } catch (error) {
        userListLoaded = false;
        assignRoleUser.replaceChildren(new Option("Unable to load users", ""));
        assignRoleStatus.textContent = error.message || "Unable to load users.";
    } finally {
        userListLoading = false;
        refreshUsersButton.disabled = false;
        assignRoleUser.disabled = !userListLoaded || assignRoleUser.options.length <= 1;
        updateAssignRoleSubmitState();
    }
}

async function assignSelectedRole(event) {
    event.preventDefault();
    if (!userListLoaded || userListLoading || roleAssignmentInProgress
            || !assignRoleUser.value || !assignRoleRole.value) {
        return;
    }

    roleAssignmentInProgress = true;
    updateAssignRoleSubmitState();
    const userOption = assignRoleUser.selectedOptions[0];
    const roleOption = assignRoleRole.selectedOptions[0];
    assignRoleStatus.textContent = `Assigning ${roleOption.textContent} to ${userOption.textContent}...`;

    try {
        const response = await fetch(
            `${apiBaseUrl}/api/permissions/users/${encodeURIComponent(assignRoleUser.value)}/roles`,
            {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({roleId: Number(assignRoleRole.value)})
            }
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        assignRoleStatus.textContent =
            `${roleOption.textContent} was assigned to ${userOption.textContent}.`;
    } catch (error) {
        assignRoleStatus.textContent = error.message || "Role assignment failed.";
    } finally {
        roleAssignmentInProgress = false;
        updateAssignRoleSubmitState();
    }
}

function initializePermissionTests() {
    trinoPermissionTests.forEach((testCase) => {
        const row = document.createElement("div");
        row.className = "permission-test-row";

        const endpoint = document.createElement("div");
        endpoint.className = "permission-test-endpoint";
        const name = document.createElement("strong");
        name.textContent = testCase.name;
        const path = document.createElement("code");
        path.textContent = `POST ${testCase.path}`;
        endpoint.append(name, path);

        const result = document.createElement("span");
        result.className = "permission-test-result";
        result.textContent = "Not run";

        const button = document.createElement("button");
        button.className = "secondary-button";
        button.type = "button";
        button.textContent = "Test";
        button.addEventListener("click", () => runSinglePermissionTest(testCase));

        row.append(endpoint, result, button);
        permissionTestList.append(row);
        permissionTestRows.set(testCase.path, {result, button});
    });
}

function setPermissionTestsBusy(isBusy) {
    permissionTestRunInProgress = isBusy;
    runAllPermissionTestsButton.disabled = isBusy;
    permissionTestRows.forEach(({button}) => {
        button.disabled = isBusy;
    });
}

function displayPermissionTestResult(testCase, label, statusClass, detail) {
    const row = permissionTestRows.get(testCase.path);
    row.result.className = `permission-test-result ${statusClass}`;
    row.result.textContent = label;
    permissionTestSummary.textContent = detail;
}

async function executePermissionTest(testCase) {
    const row = permissionTestRows.get(testCase.path);
    row.result.className = "permission-test-result is-pending";
    row.result.textContent = "Checking...";

    try {
        const response = await fetch(`${apiBaseUrl}${testCase.path}`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(testCase.body)
        });

        if (response.status === 400) {
            displayPermissionTestResult(testCase, "Allowed", "is-allowed",
                `${testCase.name}: permission granted (request validation reached).`);
        } else if (response.status === 403) {
            displayPermissionTestResult(testCase, "Denied", "is-denied",
                `${testCase.name}: access denied by RBAC.`);
        } else if (response.status === 401) {
            displayPermissionTestResult(testCase, "Sign in again", "is-error",
                `${testCase.name}: the session token is missing or expired.`);
        } else {
            displayPermissionTestResult(testCase, `Unexpected (${response.status})`, "is-error",
                `${testCase.name}: unexpected HTTP ${response.status}.`);
        }
    } catch {
        displayPermissionTestResult(testCase, "Request failed", "is-error",
            `${testCase.name}: could not reach ${apiBaseUrl}.`);
    }
}

async function runSinglePermissionTest(testCase) {
    if (permissionTestRunInProgress) {
        return;
    }
    setPermissionTestsBusy(true);
    try {
        await executePermissionTest(testCase);
    } finally {
        setPermissionTestsBusy(false);
    }
}

async function runAllPermissionTests() {
    if (permissionTestRunInProgress) {
        return;
    }
    setPermissionTestsBusy(true);
    permissionTestSummary.textContent = "Running permission checks...";
    try {
        for (const testCase of trinoPermissionTests) {
            await executePermissionTest(testCase);
        }
        const allowed = trinoPermissionTests.filter(({path}) =>
            permissionTestRows.get(path).result.classList.contains("is-allowed")).length;
        const denied = trinoPermissionTests.filter(({path}) =>
            permissionTestRows.get(path).result.classList.contains("is-denied")).length;
        permissionTestSummary.textContent =
            `Checks complete: ${allowed} allowed, ${denied} denied. See each result for unexpected responses.`;
    } finally {
        setPermissionTestsBusy(false);
    }
}

const username = getTokenSubject(token);
userName.textContent = username;
userAvatar.textContent = username.charAt(0).toUpperCase();
initializePermissionTests();
runAllPermissionTestsButton.addEventListener("click", runAllPermissionTests);
refreshUsersButton.addEventListener("click", loadUsers);
assignRoleForm.addEventListener("submit", assignSelectedRole);
assignRoleUser.addEventListener("change", updateAssignRoleSubmitState);
assignRoleRole.addEventListener("change", updateAssignRoleSubmitState);
newAttributeButton.addEventListener("click", () => openAttributeForm());
refreshAttributesButton.addEventListener("click", loadAttributeDefinitions);
attributeCategoryFilter.addEventListener("change", loadAttributeDefinitions);
attributeForm.addEventListener("submit", saveAttribute);
cancelAttributeButton.addEventListener("click", resetAttributeForm);
defaultTemplateTab.addEventListener("click", () => activateAttributeTemplateTab(defaultTemplateTab));
tenantTemplateTab.addEventListener("click", () => activateAttributeTemplateTab(tenantTemplateTab));
[defaultTemplateTab, tenantTemplateTab].forEach((tab) => {
    tab.addEventListener("keydown", (event) => {
        if (event.key === "ArrowLeft" || event.key === "ArrowRight"
            || event.key === "Home" || event.key === "End") {
            event.preventDefault();
            const selectedTab = event.key === "Home" ? defaultTemplateTab
                : event.key === "End" ? tenantTemplateTab
                    : tab === defaultTemplateTab ? tenantTemplateTab : defaultTemplateTab;
            activateAttributeTemplateTab(selectedTab, true);
        }
    });
});
internalResourceTab.addEventListener("click", () => activateResourceScopeTab(internalResourceTab));
externalResourceTab.addEventListener("click", () => activateResourceScopeTab(externalResourceTab));
[internalResourceTab, externalResourceTab].forEach((tab) => {
    tab.addEventListener("keydown", (event) => {
        if (event.key === "ArrowLeft" || event.key === "ArrowRight"
            || event.key === "Home" || event.key === "End") {
            event.preventDefault();
            const selectedTab = event.key === "Home" ? internalResourceTab
                : event.key === "End" ? externalResourceTab
                    : tab === internalResourceTab ? externalResourceTab : internalResourceTab;
            activateResourceScopeTab(selectedTab, true);
        }
    });
});
createBucketForm.addEventListener("submit", createBucket);
filterResourceAttributesButton.addEventListener("click", loadResourceAttributeValues);
refreshInternalResourcesButton.addEventListener("click", () => loadResourceList("internal"));
refreshExternalResourcesButton.addEventListener("click", () => loadResourceList("external"));
newResourceAttributeButton.addEventListener("click", () => openResourceAttributeForm());
resourceAttributeForm.addEventListener("submit", saveResourceAttribute);
cancelResourceAttributeButton.addEventListener("click", resetResourceAttributeForm);

navItems.forEach((item) => {
    item.addEventListener("click", () => showPage(item.dataset.page));
});
showPage("assign-roles");

mobileMenuButton.addEventListener("click", () => {
    const isOpen = sidebar.classList.toggle("open");
    mobileMenuButton.setAttribute("aria-expanded", String(isOpen));
});

logoutButton.addEventListener("click", () => {
    sessionStorage.removeItem("accessToken");
    window.location.replace("./index.html");
});
