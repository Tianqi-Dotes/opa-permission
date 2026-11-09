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
        description: "Manage subject attributes for users in your tenant.",
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
        description: "Select a resource, configure attribute matching, and compose access decisions.",
        featureTitle: "Policy configuration",
        featureDescription: "Build resource-scoped policies from dictionary attributes and preview generated artifacts.",
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
        description: "Execute SQL statements against your connected Trino catalogs.",
        featureTitle: "Trino query",
        featureDescription: "Run SQL statements through Trino with OPA authorization.",
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
    "create-datasource": {
        section: "Workspace",
        title: "Create Datasource",
        description: "Register a token-authenticated Trino JDBC datasource.",
        featureTitle: "Trino datasource",
        featureDescription: "Save a Trino JDBC connection and register its external ABAC resource.",
        symbol: "DS"
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
const trinoQueryPanel = document.querySelector("#trino-query-panel");
const trinoQueryForm = document.querySelector("#trino-query-form");
const trinoSqlInput = document.querySelector("#trino-sql");
const trinoQuerySubmit = document.querySelector("#trino-query-submit");
const trinoQueryStatus = document.querySelector("#trino-query-status");
const trinoQueryResults = document.querySelector("#trino-query-results");
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
const attributeTemplateViews = {
    default: {
        fromType: "project",
        rows: document.querySelector("#default-template-rows"),
        status: document.querySelector("#default-template-status"),
        refreshButton: document.querySelector("#refresh-default-templates"),
        detail: document.querySelector("#default-template-detail"),
        detailStatus: document.querySelector("#default-template-detail-status"),
        detailFields: document.querySelector("#default-template-detail-fields"),
        closeDetailButton: document.querySelector("#close-default-template-detail")
    },
    tenant: {
        fromType: "tenant",
        rows: document.querySelector("#tenant-template-rows"),
        status: document.querySelector("#tenant-template-status"),
        refreshButton: document.querySelector("#refresh-tenant-templates"),
        detail: document.querySelector("#tenant-template-detail"),
        detailStatus: document.querySelector("#tenant-template-detail-status"),
        detailFields: document.querySelector("#tenant-template-detail-fields"),
        closeDetailButton: document.querySelector("#close-tenant-template-detail")
    }
};
const subjectAttributesPanel = document.querySelector("#subject-attributes-panel");
const refreshSubjectsButton = document.querySelector("#refresh-subjects");
const subjectRows = document.querySelector("#subject-rows");
const subjectListStatus = document.querySelector("#subject-list-status");
const selectedSubjectLabel = document.querySelector("#selected-subject-label");
const subjectAttributeFilterAttributeId = document.querySelector("#subject-attribute-filter-attribute-id");
const filterSubjectAttributesButton = document.querySelector("#filter-subject-attributes");
const newSubjectAttributeButton = document.querySelector("#new-subject-attribute");
const subjectAttributeForm = document.querySelector("#subject-attribute-form");
const subjectAttributeFormTitle = document.querySelector("#subject-attribute-form-title");
const subjectAttributeSubjectId = document.querySelector("#subject-attribute-subject-id");
const subjectAttributeAttributeId = document.querySelector("#subject-attribute-attribute-id");
const subjectAttributeValueText = document.querySelector("#subject-attribute-value-text");
const saveSubjectAttributeButton = document.querySelector("#save-subject-attribute");
const cancelSubjectAttributeButton = document.querySelector("#cancel-subject-attribute");
const subjectAttributeRows = document.querySelector("#subject-attribute-rows");
const subjectAttributeStatus = document.querySelector("#subject-attribute-status");
const policyConfigPanel = document.querySelector("#policy-config-panel");
const policyConfigName = document.querySelector("#policy-config-name");
const policyConfigEntrypoint = document.querySelector("#policy-config-entrypoint");
const policyTotalRules = document.querySelector("#policy-total-rules");
const policyEnabledRules = document.querySelector("#policy-enabled-rules");
const policyAttributeCounts = document.querySelector("#policy-attribute-counts");
const policyDraftStatus = document.querySelector("#policy-draft-status");
const policyResourceSelect = document.querySelector("#policy-resource-select");
const refreshPolicyResourcesButton = document.querySelector("#refresh-policy-resources");
const policyResourceStatus = document.querySelector("#policy-resource-status");
const policyResourceSelection = document.querySelector("#policy-resource-selection");
const policyAttributeCatalogs = document.querySelector("#policy-attribute-catalogs");
const policyResourceAttributeList = document.querySelector("#policy-resource-attribute-list");
const policySubjectAttributeList = document.querySelector("#policy-subject-attribute-list");
const policyRuleList = document.querySelector("#policy-rule-list");
const policyRuleListStatus = document.querySelector("#policy-rule-list-status");
const addPolicyRuleButton = document.querySelector("#add-policy-rule");
const policyEmptyEditor = document.querySelector("#policy-empty-editor");
const policyRuleForm = document.querySelector("#policy-rule-form");
const policyRuleName = document.querySelector("#policy-rule-name");
const policyRuleEffect = document.querySelector("#policy-rule-effect");
const policyRuleEnabled = document.querySelector("#policy-rule-enabled");
const policyConditionList = document.querySelector("#policy-condition-list");
const addPolicyConditionButton = document.querySelector("#add-policy-condition");
const policyRowFilterSettings = document.querySelector("#policy-row-filter-settings");
const policyRowFilterAttribute = document.querySelector("#policy-row-filter-attribute");
const policyRowFilterOperator = document.querySelector("#policy-row-filter-operator");
const policyRowFilterValue = document.querySelector("#policy-row-filter-value");
const policyColumnMaskingSettings = document.querySelector("#policy-column-masking-settings");
const policyColumnMaskingList = document.querySelector("#policy-column-masking-list");
const policyEditorStatus = document.querySelector("#policy-editor-status");
const deletePolicyRuleButton = document.querySelector("#delete-policy-rule");
const policyStaticDataEnabled = document.querySelector("#policy-static-data-enabled");
const policyStaticDataEditor = document.querySelector("#policy-static-data-editor");
const policyStaticDataJson = document.querySelector("#policy-static-data-json");
const validatePolicyConfigButton = document.querySelector("#validate-policy-config");
const generatePolicyRegoButton = document.querySelector("#generate-policy-rego");
const policyValidationList = document.querySelector("#policy-validation-list");
const policyRegoEntrypoint = document.querySelector("#policy-rego-entrypoint");
const policyRegoPreview = document.querySelector("#policy-rego-preview");
const policyDataPreview = document.querySelector("#policy-data-preview");
const policyConfigStatus = document.querySelector("#policy-config-status");
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
const resourceAttributeResourceIdField = document.querySelector("#resource-attribute-resource-id-field");
const resourceAttributeResourceId = document.querySelector("#resource-attribute-resource-id");
const resourceAttributeAttributeId = document.querySelector("#resource-attribute-attribute-id");
const resourceAttributeValueText = document.querySelector("#resource-attribute-value-text");
const resourceAttributeTrinoPicker = document.querySelector("#resource-attribute-trino-picker");
const refreshResourceTrinoTreeButton = document.querySelector("#refresh-resource-trino-tree");
const resourceAttributeTrinoTree = document.querySelector("#resource-attribute-trino-tree");
const resourceAttributeTrinoTreeStatus = document.querySelector("#resource-attribute-trino-tree-status");
const saveResourceAttributeButton = document.querySelector("#save-resource-attribute");
const cancelResourceAttributeButton = document.querySelector("#cancel-resource-attribute");
const resourceAttributeRows = document.querySelector("#resource-attribute-rows");
const resourceAttributeStatus = document.querySelector("#resource-attribute-status");
const createBucketPanel = document.querySelector("#create-bucket-panel");
const createBucketForm = document.querySelector("#create-bucket-form");
const bucketNameInput = document.querySelector("#bucket-name");
const createBucketSubmit = document.querySelector("#create-bucket-submit");
const createBucketStatus = document.querySelector("#create-bucket-status");
const createDatasourcePanel = document.querySelector("#create-datasource-panel");
const createDatasourceForm = document.querySelector("#create-datasource-form");
const datasourceConnectionName = document.querySelector("#datasource-connection-name");
const datasourceJdbcUrl = document.querySelector("#datasource-jdbc-url");
const createDatasourceSubmit = document.querySelector("#create-datasource-submit");
const createDatasourceStatus = document.querySelector("#create-datasource-status");

const apiBaseUrl = (window.API_BASE_URL || "http://localhost:1111").replace(/\/+$/, "");
const trinoPermissionTests = [
    {name: "Query Trino", path: "/api/trino/query", body: {sql: ""}},
    {name: "Execute Trino SQL", path: "/api/trino/execute", body: {sql: ""}},
    {name: "Create Trino Catalog", path: "/api/trino/catalogs", body: {}},
    {name: "Create Trino Schema", path: "/api/trino/schemas", body: {}},
    {name: "Create Trino Table", path: "/api/trino/tables", body: {}},
    {name: "Create Trino Datasource", path: "/api/trino/connections", body: {}}
];
const permissionTestRows = new Map();
let permissionTestRunInProgress = false;
let trinoQueryInProgress = false;
let userListLoading = false;
let roleAssignmentInProgress = false;
let userListLoaded = false;
let attributeListRequestId = 0;
let attributeMutationInProgress = false;
let editingAttributeId = null;
let activeAttributeTemplateScope = "default";
const attributeTemplateListRequestIds = {default: 0, tenant: 0};
const attributeTemplateDetailRequestIds = {default: 0, tenant: 0};
let subjectAttributeDefinitions = [];
let subjectsInTenant = [];
let selectedSubject = null;
let subjectListRequestId = 0;
let subjectAttributeListRequestId = 0;
let subjectAttributeMutationInProgress = false;
let editingSubjectAttributeId = null;
let policyAttributeDefinitions = [];
let policyResources = [];
let policyRules = [];
let selectedPolicyRuleId = null;
let nextPolicyRuleId = 1;
let editingPolicyConditions = [];
let editingPolicyMasking = [];
let policyGenerated = false;
let policyAttributeRequestId = 0;
let policyResourceRequestId = 0;
let bucketCreationInProgress = false;
let datasourceCreationInProgress = false;
let resourceAttributeDefinitions = [];
let resourceAttributeListRequestId = 0;
let resourceAttributeMutationInProgress = false;
let editingResourceAttributeId = null;
let activeResourceScope = "internal";
let selectedResource = null;
let selectedExternalDatasource = null;
let selectedExternalTreeNodes = new Map();
let resourceAttributeUsesExternalTree = false;
let externalTreeRequestId = 0;
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
    activeAttributeTemplateScope = isDefaultTemplate ? "default" : "tenant";
    loadAttributeTemplateList(activeAttributeTemplateScope);
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
    const isSubjectAttributesPage = pageKey === "subject-attributes";
    const isResourceAttributesPage = pageKey === "resource-attributes";
    const isPolicyConfigPage = pageKey === "policy-config";
    const isCreateBucketPage = pageKey === "create-bucket";
    const isCreateDatasourcePage = pageKey === "create-datasource";
    const isTrinoQueryPage = pageKey === "query-trino";
    featureCard.hidden = isAssignRolesPage || isPermissionTestPage
        || isAttributeDictionaryPage || isAttributeTemplatesPage || isSubjectAttributesPage
        || isResourceAttributesPage || isPolicyConfigPage || isCreateBucketPage
        || isCreateDatasourcePage || isTrinoQueryPage;
    infoRow.hidden = isAssignRolesPage || isPermissionTestPage
        || isAttributeDictionaryPage || isAttributeTemplatesPage || isSubjectAttributesPage
        || isResourceAttributesPage || isPolicyConfigPage || isCreateBucketPage
        || isCreateDatasourcePage || isTrinoQueryPage;
    assignRolePanel.hidden = !isAssignRolesPage;
    permissionTestPanel.hidden = !isPermissionTestPage;
    trinoQueryPanel.hidden = !isTrinoQueryPage;
    attributeDictionaryPanel.hidden = !isAttributeDictionaryPage;
    attributeTemplatePanel.hidden = !isAttributeTemplatesPage;
    subjectAttributesPanel.hidden = !isSubjectAttributesPage;
    resourceAttributesPanel.hidden = !isResourceAttributesPage;
    policyConfigPanel.hidden = !isPolicyConfigPage;
    createBucketPanel.hidden = !isCreateBucketPage;
    createDatasourcePanel.hidden = !isCreateDatasourcePage;
    if (isAssignRolesPage) {
        loadUsers();
    }
    if (isAttributeDictionaryPage) {
        loadAttributeDefinitions();
    }
    if (isAttributeTemplatesPage) {
        loadAttributeTemplateList(activeAttributeTemplateScope);
    }
    if (isSubjectAttributesPage) {
        loadSubjectAttributes();
    }
    if (isResourceAttributesPage) {
        loadResourceAttributes();
    }
    if (isPolicyConfigPage) {
        loadPolicyResources();
        loadPolicyAttributeDefinitions();
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

async function createDatasource(event) {
    event.preventDefault();
    if (datasourceCreationInProgress || !createDatasourceForm.reportValidity()) {
        return;
    }

    const connectionName = datasourceConnectionName.value.trim();
    const jdbcUrl = datasourceJdbcUrl.value.trim();
    datasourceCreationInProgress = true;
    createDatasourceSubmit.disabled = true;
    createDatasourceStatus.textContent = "Saving Trino datasource...";
    try {
        const response = await fetch(`${apiBaseUrl}/api/trino/connections`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({connectionName, jdbcUrl})
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const resource = await response.json();
        createDatasourceStatus.textContent =
            `Datasource "${resource.resourceName}" was registered with resource ID ${resource.id} and code ${resource.resourceCode} using token authentication.`;
        createDatasourceForm.reset();
    } catch (error) {
        createDatasourceStatus.textContent = error.message || "Unable to save the Trino datasource.";
    } finally {
        datasourceCreationInProgress = false;
        createDatasourceSubmit.disabled = false;
    }
}

function formatTrinoCell(value) {
    if (value === null || value === undefined) {
        return "NULL";
    }
    if (typeof value === "object") {
        return JSON.stringify(value) ?? String(value);
    }
    return String(value);
}

function renderTrinoQueryResult(result) {
    trinoQueryResults.replaceChildren();
    if (!result.hasResultSet) {
        trinoQueryResults.hidden = true;
        return;
    }

    const columns = Array.isArray(result.columns) ? result.columns : [];
    const rows = Array.isArray(result.rows) ? result.rows : [];
    if (columns.length === 0) {
        trinoQueryResults.hidden = true;
        return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "attribute-table-wrap trino-query-table-wrap";
    const table = document.createElement("table");
    table.className = "attribute-table";
    const tableHead = document.createElement("thead");
    const headerRow = document.createElement("tr");
    columns.forEach((column) => {
        const header = document.createElement("th");
        header.scope = "col";
        header.textContent = column;
        headerRow.append(header);
    });
    tableHead.append(headerRow);
    table.append(tableHead);

    const tableBody = document.createElement("tbody");
    rows.forEach((row) => {
        const tableRow = document.createElement("tr");
        columns.forEach((_, index) => {
            const cell = document.createElement("td");
            cell.textContent = formatTrinoCell(row[index]);
            tableRow.append(cell);
        });
        tableBody.append(tableRow);
    });
    table.append(tableBody);
    wrapper.append(table);
    trinoQueryResults.append(wrapper);
    trinoQueryResults.hidden = false;
}

async function executeTrinoSql(event) {
    event.preventDefault();
    if (trinoQueryInProgress || !trinoQueryForm.reportValidity()) {
        return;
    }

    const sql = trinoSqlInput.value.trim();
    if (!sql) {
        trinoQueryStatus.textContent = "Enter a SQL statement.";
        trinoSqlInput.focus();
        return;
    }

    trinoQueryInProgress = true;
    trinoQuerySubmit.disabled = true;
    trinoQueryStatus.textContent = "Executing SQL...";
    trinoQueryResults.replaceChildren();
    trinoQueryResults.hidden = true;
    try {
        const response = await fetch(`${apiBaseUrl}/api/trino/execute`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({sql})
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const result = await response.json();
        renderTrinoQueryResult(result);
        if (result.hasResultSet) {
            const rowCount = Array.isArray(result.rows) ? result.rows.length : 0;
            trinoQueryStatus.textContent = result.truncated
                ? `Query succeeded. Showing ${rowCount} rows; additional rows were truncated.`
                : `Query succeeded. ${rowCount} row${rowCount === 1 ? "" : "s"} returned.`;
        } else if (result.updateCount !== null && result.updateCount !== undefined) {
            trinoQueryStatus.textContent = `SQL statement completed. ${result.updateCount} row(s) affected.`;
        } else {
            trinoQueryStatus.textContent = "SQL statement completed successfully.";
        }
    } catch (error) {
        trinoQueryStatus.textContent = error.message || "Unable to execute SQL on Trino.";
    } finally {
        trinoQueryInProgress = false;
        trinoQuerySubmit.disabled = false;
    }
}

function formatAttributeLabel(value) {
    return value ? value.charAt(0).toUpperCase() + value.slice(1) : "";
}

function formatAttributeTemplateValue(write) {
    if (write.defaultValueText != null) {
        return write.defaultValueText;
    }
    if (write.defaultValueJson != null) {
        try {
            return JSON.stringify(write.defaultValueJson);
        } catch {
            return String(write.defaultValueJson);
        }
    }
    return "—";
}

function formatPermissionPath(template) {
    const path = template.permissionPath || "Permission path unavailable";
    return template.httpMethod ? `${template.httpMethod} ${path}` : path;
}

function setAttributeTemplateRowsMessage(scope, message) {
    const view = attributeTemplateViews[scope];
    view.rows.replaceChildren();
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.className = "attribute-empty-state";
    cell.colSpan = 8;
    cell.textContent = message;
    row.append(cell);
    view.rows.append(row);
}

function renderAttributeTemplateList(scope, templates) {
    const view = attributeTemplateViews[scope];
    view.rows.replaceChildren();
    if (templates.length === 0) {
        setAttributeTemplateRowsMessage(scope, "No templates found.");
        return;
    }

    templates.forEach((template) => {
        const row = document.createElement("tr");
        [
            template.id,
            template.permissionName || "—",
            formatPermissionPath(template),
            formatAttributeLabel(template.targetType),
            template.attributeId,
            formatAttributeTemplateValue(template),
            template.shouldInject ? "Yes" : "No"
        ].forEach((value) => {
            const cell = document.createElement("td");
            cell.textContent = value == null ? "" : String(value);
            row.append(cell);
        });

        const actionsCell = document.createElement("td");
        const actions = document.createElement("div");
        actions.className = "attribute-table-actions";
        const detailButton = document.createElement("button");
        detailButton.className = "secondary-button";
        detailButton.type = "button";
        detailButton.textContent = "Details";
        detailButton.setAttribute("aria-label", `View template ${template.id}`);
        detailButton.addEventListener("click", () => loadAttributeTemplateDetail(scope, template.id));
        actions.append(detailButton);
        actionsCell.append(actions);
        row.append(actionsCell);
        view.rows.append(row);
    });
}

async function loadAttributeTemplateList(scope) {
    const view = attributeTemplateViews[scope];
    const requestId = ++attributeTemplateListRequestIds[scope];
    view.refreshButton.disabled = true;
    view.status.textContent = `Loading ${scope === "default" ? "default" : "tenant"} templates...`;

    try {
        const query = new URLSearchParams({fromType: view.fromType});
        const response = await fetch(
            `${apiBaseUrl}/api/abac/permission-default-attribute-writes?${query.toString()}`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const templates = await response.json();
        if (!Array.isArray(templates)) {
            throw new Error("The attribute template response was not a list.");
        }
        if (requestId !== attributeTemplateListRequestIds[scope]) {
            return;
        }

        renderAttributeTemplateList(scope, templates);
        view.status.textContent =
            `Loaded ${templates.length} ${scope === "default" ? "default" : "tenant"} template${templates.length === 1 ? "" : "s"}.`;
    } catch (error) {
        if (requestId === attributeTemplateListRequestIds[scope]) {
            setAttributeTemplateRowsMessage(scope, error.message || "Unable to load templates.");
            view.status.textContent = error.message || "Unable to load templates.";
        }
    } finally {
        if (requestId === attributeTemplateListRequestIds[scope]) {
            view.refreshButton.disabled = false;
        }
    }
}

function formatTemplateTimestamp(value) {
    if (value == null) {
        return "—";
    }
    const timestamp = Number(value);
    if (!Number.isFinite(timestamp)) {
        return String(value);
    }
    const date = new Date(timestamp);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
}

function appendAttributeTemplateDetailField(container, label, value, isMultiline = false) {
    const term = document.createElement("dt");
    term.textContent = label;
    const description = document.createElement("dd");
    description.textContent = value == null || value === "" ? "—" : String(value);
    if (isMultiline) {
        description.classList.add("is-multiline");
    }
    container.append(term, description);
}

function renderAttributeTemplateDetail(scope, template) {
    const fields = attributeTemplateViews[scope].detailFields;
    fields.replaceChildren();
    appendAttributeTemplateDetailField(fields, "ID", template.id);
    appendAttributeTemplateDetailField(fields, "Permission ID", template.permissionId);
    appendAttributeTemplateDetailField(fields, "Permission name", template.permissionName);
    appendAttributeTemplateDetailField(fields, "Permission path", template.permissionPath);
    appendAttributeTemplateDetailField(fields, "HTTP method", template.httpMethod);
    appendAttributeTemplateDetailField(fields, "Source", formatAttributeLabel(template.fromType));
    appendAttributeTemplateDetailField(fields, "Target", formatAttributeLabel(template.targetType));
    appendAttributeTemplateDetailField(fields, "Attribute ID", template.attributeId);
    appendAttributeTemplateDetailField(fields, "Default value text", template.defaultValueText, true);
    let jsonValue = "—";
    if (template.defaultValueJson != null) {
        try {
            jsonValue = JSON.stringify(template.defaultValueJson, null, 2);
        } catch {
            jsonValue = String(template.defaultValueJson);
        }
    }
    appendAttributeTemplateDetailField(fields, "Default value JSON", jsonValue, true);
    appendAttributeTemplateDetailField(fields, "Should inject", template.shouldInject ? "Yes" : "No");
    appendAttributeTemplateDetailField(fields, "Created", formatTemplateTimestamp(template.createTime));
    appendAttributeTemplateDetailField(fields, "Updated", formatTemplateTimestamp(template.updateTime));
}

async function loadAttributeTemplateDetail(scope, id) {
    const view = attributeTemplateViews[scope];
    const requestId = ++attributeTemplateDetailRequestIds[scope];
    view.detail.hidden = false;
    view.detailStatus.textContent = "Loading template details...";
    view.detailFields.replaceChildren();

    try {
        const response = await fetch(
            `${apiBaseUrl}/api/abac/permission-default-attribute-writes/${encodeURIComponent(String(id))}`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }

        const template = await response.json();
        if (requestId !== attributeTemplateDetailRequestIds[scope]) {
            return;
        }
        renderAttributeTemplateDetail(scope, template);
        view.detailStatus.textContent = `Details for template ${template.id} loaded.`;
    } catch (error) {
        if (requestId === attributeTemplateDetailRequestIds[scope]) {
            view.detailStatus.textContent = error.message || "Unable to load template details.";
        }
    }
}

function closeAttributeTemplateDetail(scope) {
    const view = attributeTemplateViews[scope];
    attributeTemplateDetailRequestIds[scope] += 1;
    view.detail.hidden = true;
    view.detailFields.replaceChildren();
    view.detailStatus.textContent = "";
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

const policyOperatorOptions = [
    ["eq", "Equals"],
    ["neq", "Not equal"],
    ["gt", "Greater than"],
    ["gte", "Greater than or equal"],
    ["lt", "Less than"],
    ["lte", "Less than or equal"],
    ["in", "Is in list"],
    ["exists", "Exists"]
];
const policyMaskStrategies = [
    ["redact", "Redact"],
    ["partial", "Partial mask"],
    ["hash", "Hash"]
];

function invalidatePolicyPreview() {
    policyGenerated = false;
    policyDraftStatus.textContent = "Not generated";
    policyRegoPreview.textContent = "Draft changed. Generate artifacts to refresh the previews.";
    policyDataPreview.textContent = policyStaticDataEnabled.checked
        ? "Generate artifacts to inspect data.json."
        : "No static data configured; data.json will not be generated.";
    policyConfigStatus.textContent = "Local draft has unsaved changes.";
}

function renderPolicySummary() {
    const enabledRules = policyRules.filter((rule) => rule.enabled);
    const subjectAttributeCount = policyAttributeDefinitions
        .filter((definition) => definition.category === "subject").length;
    const resourceAttributeCount = policyAttributeDefinitions
        .filter((definition) => definition.category === "resource").length;
    policyTotalRules.textContent = String(policyRules.length);
    policyEnabledRules.textContent = String(enabledRules.length);
    policyAttributeCounts.textContent = `${subjectAttributeCount} / ${resourceAttributeCount}`;
    policyDraftStatus.textContent = policyGenerated ? "Preview ready" : "Not generated";
    policyRegoEntrypoint.textContent = `Entrypoint: ${policyConfigEntrypoint.value.trim() || "—"}`;
}

function renderPolicyRuleList() {
    policyRuleList.replaceChildren();
    if (policyRules.length === 0) {
        policyRuleListStatus.textContent = "No rules added yet.";
        const empty = document.createElement("div");
        empty.className = "policy-rule-empty";
        empty.textContent = "Add a rule to begin.";
        policyRuleList.append(empty);
        renderPolicySummary();
        return;
    }

    policyRuleListStatus.textContent = `${policyRules.length} rule${policyRules.length === 1 ? "" : "s"} in this local draft.`;
    policyRules.forEach((rule) => {
        const button = document.createElement("button");
        button.className = "policy-rule-card";
        button.type = "button";
        button.dataset.policyRuleId = String(rule.id);
        button.setAttribute("aria-pressed", String(rule.id === selectedPolicyRuleId));
        const title = document.createElement("strong");
        title.textContent = rule.ruleName || "Untitled rule";
        const code = document.createElement("span");
        code.className = "policy-rule-code";
        code.textContent = rule.ruleCode;
        const meta = document.createElement("span");
        meta.className = "policy-rule-meta";
        const decisionLabel = rule.effect === "permit"
            ? "ALLOW"
            : rule.effect.replace(/_/g, " ").toUpperCase();
        meta.textContent = `${decisionLabel} · ${rule.enabled ? "Enabled" : "Disabled"}`;
        button.append(title, code, meta);
        policyRuleList.append(button);
    });
    renderPolicySummary();
}

function selectedPolicyRule() {
    return policyRules.find((rule) => rule.id === selectedPolicyRuleId) || null;
}

function renderPolicyConditionRows() {
    policyConditionList.replaceChildren();
    editingPolicyConditions.forEach((condition, index) => {
        const row = document.createElement("div");
        row.className = "policy-condition-row";

        const attributeLabel = document.createElement("label");
        attributeLabel.className = "form-field";
        const attributeCaption = document.createElement("span");
        attributeCaption.textContent = "Subject attribute";
        const attributeSelect = document.createElement("select");
        attributeSelect.required = true;
        attributeSelect.append(new Option("Select a subject attribute", ""));
        policyAttributeDefinitions
            .filter((definition) => definition.category === "subject")
            .forEach((definition) => {
                attributeSelect.append(new Option(
                    `${definition.name} (${definition.dataType})`,
                    `subject.${definition.name}`
                ));
            });
        attributeSelect.value = condition.attribute;
        attributeSelect.addEventListener("change", () => {
            condition.attribute = attributeSelect.value;
            invalidatePolicyPreview();
        });
        attributeLabel.append(attributeCaption, attributeSelect);

        const operatorLabel = document.createElement("label");
        operatorLabel.className = "form-field";
        const operatorCaption = document.createElement("span");
        operatorCaption.textContent = "Operator";
        const operatorSelect = document.createElement("select");
        policyOperatorOptions.forEach(([value, label]) => {
            operatorSelect.append(new Option(label, value));
        });
        operatorSelect.value = condition.operator;

        const valueTypeLabel = document.createElement("label");
        valueTypeLabel.className = "form-field";
        const valueTypeCaption = document.createElement("span");
        valueTypeCaption.textContent = "Right side";
        const valueTypeSelect = document.createElement("select");
        valueTypeSelect.append(
            new Option("Fixed value", "constant"),
            new Option("Resource attribute", "resource")
        );
        valueTypeSelect.value = condition.valueType;
        valueTypeLabel.append(valueTypeCaption, valueTypeSelect);

        const resourceAttributeLabel = document.createElement("label");
        resourceAttributeLabel.className = "form-field";
        const resourceAttributeCaption = document.createElement("span");
        resourceAttributeCaption.textContent = "Resource attribute";
        const resourceAttributeSelect = document.createElement("select");
        resourceAttributeSelect.append(new Option("Select a resource attribute", ""));
        policyAttributeDefinitions
            .filter((definition) => definition.category === "resource")
            .forEach((definition) => resourceAttributeSelect.append(new Option(
                `${definition.name} (${definition.dataType})`,
                `resource.${definition.name}`
            )));
        resourceAttributeSelect.value = condition.valueAttribute;
        resourceAttributeLabel.append(resourceAttributeCaption, resourceAttributeSelect);

        const valueLabel = document.createElement("label");
        valueLabel.className = "form-field policy-condition-value";
        const valueCaption = document.createElement("span");
        valueCaption.textContent = "Fixed value (JSON or text)";
        const valueInput = document.createElement("input");
        valueInput.type = "text";
        valueInput.placeholder = '"finance", 5, true, or ["read", "write"]';
        valueInput.value = condition.valueText;
        valueLabel.append(valueCaption, valueInput);

        const removeButton = document.createElement("button");
        removeButton.className = "secondary-button policy-remove-condition";
        removeButton.type = "button";
        removeButton.textContent = "Remove";
        removeButton.setAttribute("aria-label", `Remove condition ${index + 1}`);

        operatorSelect.addEventListener("change", () => {
            condition.operator = operatorSelect.value;
            updateValueVisibility();
            invalidatePolicyPreview();
        });
        valueTypeSelect.addEventListener("change", () => {
            condition.valueType = valueTypeSelect.value;
            renderPolicyConditionRows();
            invalidatePolicyPreview();
        });
        resourceAttributeSelect.addEventListener("change", () => {
            condition.valueAttribute = resourceAttributeSelect.value;
            invalidatePolicyPreview();
        });
        valueInput.addEventListener("input", () => {
            condition.valueText = valueInput.value;
            invalidatePolicyPreview();
        });

        function updateValueVisibility() {
            const operatorHasValue = condition.operator !== "exists";
            valueTypeLabel.hidden = !operatorHasValue;
            resourceAttributeLabel.hidden = !operatorHasValue || condition.valueType !== "resource";
            valueLabel.hidden = !operatorHasValue || condition.valueType !== "constant";
            resourceAttributeSelect.required =
                operatorHasValue && condition.valueType === "resource";
            valueInput.required = operatorHasValue && condition.valueType === "constant";
        }
        updateValueVisibility();
        removeButton.addEventListener("click", () => {
            editingPolicyConditions.splice(index, 1);
            renderPolicyConditionRows();
            invalidatePolicyPreview();
        });

        operatorLabel.append(operatorCaption, operatorSelect);
        row.append(attributeLabel, operatorLabel, valueTypeLabel, resourceAttributeLabel, valueLabel, removeButton);
        policyConditionList.append(row);
    });
}

function renderPolicyRuleEditor(rule) {
    if (!rule) {
        policyEmptyEditor.hidden = false;
        policyRuleForm.hidden = true;
        addPolicyConditionButton.disabled = true;
        deletePolicyRuleButton.disabled = true;
        return;
    }
    policyEmptyEditor.hidden = true;
    policyRuleForm.hidden = false;
    addPolicyConditionButton.disabled = false;
    deletePolicyRuleButton.disabled = false;
    policyRuleName.value = rule.ruleName;
    policyRuleEffect.value = rule.effect;
    policyRuleEnabled.checked = Boolean(rule.enabled);
    editingPolicyConditions = rule.conditions.map((condition) => ({...condition}));
    editingPolicyMasking = (rule.masking || []).map((item) => ({...item}));
    policyEditorStatus.textContent = "";
    populatePolicyOutcomeOptions(rule);
    updatePolicyOutcomeVisibility();
    renderPolicyConditionRows();
}

function selectPolicyRule(ruleId) {
    const rule = policyRules.find((candidate) => candidate.id === ruleId);
    if (!rule) {
        return;
    }
    if (selectedPolicyRuleId !== null && selectedPolicyRuleId !== ruleId
            && !applyPolicyRuleChanges()) {
        return;
    }
    selectedPolicyRuleId = ruleId;
    renderPolicyRuleEditor(rule);
    renderPolicyRuleList();
}

function addPolicyRule() {
    if (selectedPolicyRule() && !applyPolicyRuleChanges()) {
        return;
    }
    const id = nextPolicyRuleId++;
    const rule = {
        id,
        ruleCode: `rule_${id}`,
        ruleName: `New rule ${id}`,
        effect: "permit",
        enabled: true,
        conditions: [{
            attribute: "",
            operator: "eq",
            valueType: "constant",
            valueAttribute: "",
            valueText: ""
        }],
        rowFilter: {attribute: "", operator: "eq", valueText: ""},
        masking: []
    };
    policyRules.push(rule);
    selectedPolicyRuleId = id;
    renderPolicyRuleEditor(rule);
    renderPolicyRuleList();
    invalidatePolicyPreview();
    policyEditorStatus.textContent = "Complete the rule and apply your changes.";
}

function applyPolicyRuleChanges() {
    const rule = selectedPolicyRule();
    if (!rule) {
        return true;
    }
    if (!policyRuleForm.reportValidity()) {
        return false;
    }

    rule.ruleName = policyRuleName.value.trim();
    rule.effect = policyRuleEffect.value;
    rule.enabled = policyRuleEnabled.checked;
    rule.conditions = editingPolicyConditions.map((condition) => ({...condition}));
    rule.rowFilter = {
        attribute: policyRowFilterAttribute.value,
        operator: policyRowFilterOperator.value,
        valueText: policyRowFilterValue.value
    };
    rule.masking = editingPolicyMasking.map((item) => ({...item}));
    renderPolicyRuleList();
    policyEditorStatus.textContent = "Rule changes applied to the local draft.";
    invalidatePolicyPreview();
    return true;
}

function deleteSelectedPolicyRule() {
    const rule = selectedPolicyRule();
    if (!rule || !window.confirm(`Delete rule "${rule.ruleName}" from this local draft?`)) {
        return;
    }
    policyRules = policyRules.filter((candidate) => candidate.id !== rule.id);
    selectedPolicyRuleId = null;
    editingPolicyConditions = [];
    editingPolicyMasking = [];
    renderPolicyRuleEditor(null);
    renderPolicyRuleList();
    invalidatePolicyPreview();
}

function parsePolicyConditionValue(condition) {
    if (condition.operator === "exists") {
        return null;
    }
    const value = condition.valueText.trim();
    if (!value) {
        throw new Error("A value is required unless the operator is Exists.");
    }
    try {
        return JSON.parse(value);
    } catch {
        return value;
    }
}

function validatePolicyConfiguration() {
    const errors = [];
    if (!policyConfigName.value.trim()) {
        errors.push("Configuration name is required.");
    }
    if (!policyResourceSelect.value) {
        errors.push("Select a resource scope.");
    }
    const entrypoint = policyConfigEntrypoint.value.trim();
    if (!/^data\.[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)*\.decision$/.test(entrypoint)) {
        errors.push("Entrypoint must look like data.package.decision.");
    }
    if (policyRules.length === 0) {
        errors.push("Add at least one rule.");
    }
    if (!policyRules.some((rule) => rule.enabled)) {
        errors.push("Enable at least one rule.");
    }

    policyRules.forEach((rule) => {
        if (!rule.enabled) {
            return;
        }
        if (!rule.ruleName.trim()) {
            errors.push(`${rule.ruleCode}: rule name is required.`);
        }
        if (rule.conditions.length === 0) {
            errors.push(`${rule.ruleCode}: add at least one condition.`);
        }
        rule.conditions.forEach((condition, index) => {
            const subjectAttributeExists = policyAttributeDefinitions.some((definition) =>
                definition.category === "subject"
                && `subject.${definition.name}` === condition.attribute);
            if (!subjectAttributeExists) {
                errors.push(`${rule.ruleCode}, condition ${index + 1}: select a subject dictionary attribute.`);
            }
            if (condition.operator !== "exists") {
                if (condition.valueType === "resource") {
                    const resourceAttributeExists = policyAttributeDefinitions.some((definition) =>
                        definition.category === "resource"
                        && `resource.${definition.name}` === condition.valueAttribute);
                    if (!resourceAttributeExists) {
                        errors.push(`${rule.ruleCode}, condition ${index + 1}: select a resource dictionary attribute.`);
                    }
                } else if (condition.valueType === "constant") {
                    try {
                        const value = parsePolicyConditionValue(condition);
                        if (condition.operator === "in" && !Array.isArray(value)) {
                            errors.push(`${rule.ruleCode}, condition ${index + 1}: Is in list requires a JSON array.`);
                        }
                    } catch (error) {
                        errors.push(`${rule.ruleCode}, condition ${index + 1}: ${error.message}`);
                    }
                } else {
                    errors.push(`${rule.ruleCode}, condition ${index + 1}: choose a resource attribute or fixed value.`);
                }
            }
        });
        if (rule.effect === "row_filtering") {
            const hasResourceAttribute = policyAttributeDefinitions.some((definition) =>
                definition.category === "resource"
                && `resource.${definition.name}` === rule.rowFilter.attribute);
            if (!hasResourceAttribute) {
                errors.push(`${rule.ruleCode}: select a resource attribute for the row filter.`);
            }
            try {
                const value = parsePolicyConditionValue(rule.rowFilter);
                if (rule.rowFilter.operator === "in" && !Array.isArray(value)) {
                    errors.push(`${rule.ruleCode}: row filter "Is in list" requires a JSON array.`);
                }
            } catch (error) {
                errors.push(`${rule.ruleCode}: ${error.message}`);
            }
        }
        if (rule.effect === "column_masking" && rule.masking.length === 0) {
            errors.push(`${rule.ruleCode}: select at least one resource attribute to mask.`);
        }
    });
    if (policyStaticDataEnabled.checked) {
        try {
            const data = JSON.parse(policyStaticDataJson.value);
            if (!data || typeof data !== "object" || Array.isArray(data)) {
                errors.push("Static data must be a JSON object.");
            }
        } catch {
            errors.push("Static data must be valid JSON.");
        }
    }
    return errors;
}

function renderPolicyValidation(errors) {
    policyValidationList.replaceChildren();
    if (errors.length === 0) {
        const item = document.createElement("li");
        item.className = "is-success";
        item.textContent = "Configuration is valid and ready for artifact preview.";
        policyValidationList.append(item);
        return;
    }
    errors.forEach((message) => {
        const item = document.createElement("li");
        item.className = "is-error";
        item.textContent = message;
        policyValidationList.append(item);
    });
}

function regoReference(attribute) {
    const [category, ...path] = attribute.split(".");
    return `input.${category}${path.map((part) => `[${JSON.stringify(part)}]`).join("")}`;
}

function regoCondition(condition) {
    const reference = regoReference(condition.attribute);
    if (condition.operator === "exists") {
        return `${reference} != null`;
    }
    const value = condition.valueType === "resource"
        ? regoReference(condition.valueAttribute)
        : JSON.stringify(parsePolicyConditionValue(condition));
    const operators = {
        eq: "==",
        neq: "!=",
        gt: ">",
        gte: ">=",
        lt: "<",
        lte: "<="
    };
    if (condition.operator === "in") {
        return `${reference} in ${value}`;
    }
    return `${reference} ${operators[condition.operator]} ${value}`;
}

function ruleConditions(rule, index, resourceScopeCondition) {
    const name = `rule_match_${index + 1}`;
    const conditions = rule.conditions.map(regoCondition);
    const lines = [
        `${name} if {`,
        `    ${resourceScopeCondition}`,
        ...conditions.map((condition) => `    ${condition}`),
        "}"
    ];
    return {name, lines};
}

function generatePolicyRego() {
    const enabledRules = policyRules
        .filter((rule) => rule.enabled);
    const packageSegments = policyConfigEntrypoint.value.trim().split(".").slice(1, -1);
    const resource = policyResources.find(
        (candidate) => String(candidate.id) === policyResourceSelect.value);
    const resourceScopeCondition = `input.resource.id == ${JSON.stringify(Number(resource.id))}`;
    const lines = [
        `package ${packageSegments.join(".")}`,
        "",
        "import rego.v1",
        "",
        'default decision := {"allow": false, "type": "deny"}',
        ""
    ];
    const generatedRules = enabledRules.map((rule, index) => ({
        rule,
        ...ruleConditions(rule, index, resourceScopeCondition)
    }));
    generatedRules.forEach((generatedRule) => lines.push(...generatedRule.lines, ""));
    generatedRules.forEach((generatedRule, index) => {
        const earlierRules = generatedRules.slice(0, index);
        lines.push(
            `decision := ${JSON.stringify(policyDecisionPayload(generatedRule.rule))} if {`,
            `    ${generatedRule.name}`,
            ...earlierRules.map(({name}) => `    not ${name}`),
            "}",
            ""
        );
    });
    return `${lines.join("\n")}\n`;
}

function policyDecisionPayload(rule) {
    if (rule.effect === "deny") {
        return {allow: false, type: "deny"};
    }
    if (rule.effect === "row_filtering") {
        return {
            allow: true,
            type: "row_filtering",
            row_filter: {
                attribute: rule.rowFilter.attribute,
                operator: rule.rowFilter.operator,
                value: parsePolicyConditionValue(rule.rowFilter)
            }
        };
    }
    if (rule.effect === "column_masking") {
        return {
            allow: true,
            type: "column_masking",
            masks: Object.fromEntries(rule.masking.map((item) => [
                item.attribute.replace(/^resource\./, ""),
                item.strategy
            ]))
        };
    }
    return {allow: true, type: "allow"};
}

function validateCurrentPolicyDraft() {
    if (!applyPolicyRuleChanges()) {
        renderPolicyValidation(["Fix the selected rule fields before validating the configuration."]);
        return false;
    }
    const errors = validatePolicyConfiguration();
    renderPolicyValidation(errors);
    if (errors.length > 0) {
        policyConfigStatus.textContent = "Validation found issues in the local draft.";
        return false;
    }
    policyConfigStatus.textContent = "Validation passed. The local draft is ready for artifact preview.";
    return true;
}

function generatePolicyPreview() {
    if (!applyPolicyRuleChanges()) {
        renderPolicyValidation(["Fix the selected rule fields before generating artifacts."]);
        return;
    }
    const errors = validatePolicyConfiguration();
    renderPolicyValidation(errors);
    if (errors.length > 0) {
        policyConfigStatus.textContent = "Artifacts were not generated because validation failed.";
        return;
    }
    policyRegoPreview.textContent = generatePolicyRego();
    if (policyStaticDataEnabled.checked) {
        policyDataPreview.textContent = JSON.stringify(JSON.parse(policyStaticDataJson.value), null, 2);
    } else {
        policyDataPreview.textContent = "No static data configured; data.json will not be generated.";
    }
    policyGenerated = true;
    policyConfigStatus.textContent =
        "Policy and optional static data previews generated locally. They have not been saved or published.";
    renderPolicySummary();
}

async function loadPolicyAttributeDefinitions() {
    const requestId = ++policyAttributeRequestId;
    policyRuleListStatus.textContent = "Loading the attribute dictionary...";
    try {
        const response = await fetch(`${apiBaseUrl}/api/abac/attributes`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const definitions = await response.json();
        if (!Array.isArray(definitions)) {
            throw new Error("The attribute definition response was not a list.");
        }
        if (requestId !== policyAttributeRequestId) {
            return;
        }
        policyAttributeDefinitions = definitions;
        renderPolicyAttributeCatalogs();
        renderPolicySummary();
        const rule = selectedPolicyRule();
        if (rule) {
            renderPolicyConditionRows();
            populatePolicyOutcomeOptions(rule);
        }
        if (policyRules.length === 0) {
            policyRuleListStatus.textContent =
                `Loaded ${definitions.length} attributes. Add a rule to begin.`;
        } else {
            renderPolicyRuleList();
        }
    } catch (error) {
        if (requestId === policyAttributeRequestId) {
            policyRuleListStatus.textContent =
                `${error.message || "Unable to load attributes."} Rule conditions require the attribute dictionary.`;
        }
    }
}

function policyResourcePath(resource) {
    const resourcesById = new Map(policyResources.map((item) => [String(item.id), item]));
    const path = [];
    const visited = new Set();
    let current = resource;
    while (current && !visited.has(String(current.id))) {
        visited.add(String(current.id));
        path.unshift(current.resourceName || current.resourceCode || `Resource ${current.id}`);
        current = current.parentResourceId == null
            ? null
            : resourcesById.get(String(current.parentResourceId));
    }
    return path.join(" / ");
}

function renderPolicyResourceOptions() {
    const selectedId = policyResourceSelect.value;
    policyResourceSelect.replaceChildren(new Option("Select a resource", ""));
    [...policyResources]
        .sort((left, right) => policyResourcePath(left).localeCompare(policyResourcePath(right)))
        .forEach((resource) => {
            const path = policyResourcePath(resource);
            const label = resource.resourceType ? `${path} (${resource.resourceType})` : path;
            policyResourceSelect.append(new Option(label, String(resource.id)));
        });
    if (policyResources.some((resource) => String(resource.id) === selectedId)) {
        policyResourceSelect.value = selectedId;
    }
    renderPolicyResourceSelection();
}

function renderPolicyResourceSelection() {
    const resource = policyResources.find(
        (candidate) => String(candidate.id) === policyResourceSelect.value);
    policyResourceSelection.replaceChildren();
    if (!resource) {
        policyResourceSelection.textContent = "Select a resource to view its hierarchy and available attributes.";
        policyAttributeCatalogs.hidden = true;
        addPolicyRuleButton.disabled = true;
        renderPolicySummary();
        return;
    }
    const name = document.createElement("strong");
    name.textContent = policyResourcePath(resource);
    const details = document.createElement("span");
    details.textContent =
        `ID: ${resource.id} · Type: ${resource.resourceType || "unspecified"} · Code: ${resource.resourceCode || "—"}`;
    policyResourceSelection.append(name, details);
    policyAttributeCatalogs.hidden = false;
    addPolicyRuleButton.disabled = false;
    renderPolicyAttributeCatalogs();
    renderPolicySummary();
}

async function loadPolicyResources() {
    const requestId = ++policyResourceRequestId;
    refreshPolicyResourcesButton.disabled = true;
    policyResourceStatus.textContent = "Loading resources for the current tenant...";
    try {
        const response = await fetch(`${apiBaseUrl}/api/abac/resources`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const resources = await response.json();
        if (!Array.isArray(resources)) {
            throw new Error("The resource response was not a list.");
        }
        if (requestId !== policyResourceRequestId) {
            return;
        }
        policyResources = resources;
        renderPolicyResourceOptions();
        policyResourceStatus.textContent = resources.length === 0
            ? "No resources are registered for the current tenant."
            : `Loaded ${resources.length} resource${resources.length === 1 ? "" : "s"}.`;
        renderPolicySummary();
    } catch (error) {
        if (requestId === policyResourceRequestId) {
            policyResources = [];
            policyResourceSelect.replaceChildren(new Option("Resources unavailable", ""));
            renderPolicyResourceSelection();
            policyResourceStatus.textContent = error.message || "Unable to load resources.";
        }
    } finally {
        if (requestId === policyResourceRequestId) {
            refreshPolicyResourcesButton.disabled = false;
        }
    }
}

function renderPolicyAttributeCatalog(container, definitions, emptyMessage) {
    container.replaceChildren();
    if (definitions.length === 0) {
        const empty = document.createElement("span");
        empty.className = "policy-catalog-empty";
        empty.textContent = emptyMessage;
        container.append(empty);
        return;
    }
    definitions.forEach((definition) => {
        const chip = document.createElement("span");
        chip.className = "policy-attribute-chip";
        chip.title = definition.description || "";
        chip.textContent = `${definition.name} · ${definition.dataType}`;
        container.append(chip);
    });
}

function renderPolicyAttributeCatalogs() {
    renderPolicyAttributeCatalog(
        policyResourceAttributeList,
        policyAttributeDefinitions.filter((definition) => definition.category === "resource"),
        "No resource attributes are defined."
    );
    renderPolicyAttributeCatalog(
        policySubjectAttributeList,
        policyAttributeDefinitions.filter((definition) => definition.category === "subject"),
        "No subject attributes are defined."
    );
}

function renderPolicyMaskingRows() {
    policyColumnMaskingList.replaceChildren();
    const resourceDefinitions = policyAttributeDefinitions.filter(
        (definition) => definition.category === "resource");
    if (resourceDefinitions.length === 0) {
        const empty = document.createElement("p");
        empty.className = "policy-catalog-empty";
        empty.textContent = "Resource attributes are required before column masking can be configured.";
        policyColumnMaskingList.append(empty);
        return;
    }
    resourceDefinitions.forEach((definition) => {
        const attribute = `resource.${definition.name}`;
        const existing = editingPolicyMasking.find((item) => item.attribute === attribute);
        const row = document.createElement("label");
        row.className = "policy-mask-row";
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = Boolean(existing);
        const name = document.createElement("span");
        name.textContent = `${definition.name} (${definition.dataType})`;
        const strategy = document.createElement("select");
        policyMaskStrategies.forEach(([value, label]) => strategy.append(new Option(label, value)));
        strategy.value = existing ? existing.strategy : "redact";
        strategy.disabled = !existing;
        checkbox.addEventListener("change", () => {
            const mask = editingPolicyMasking.find((item) => item.attribute === attribute);
            if (checkbox.checked && !mask) {
                editingPolicyMasking.push({attribute, strategy: strategy.value});
            } else if (!checkbox.checked && mask) {
                editingPolicyMasking = editingPolicyMasking.filter((item) => item.attribute !== attribute);
            }
            strategy.disabled = !checkbox.checked;
            invalidatePolicyPreview();
        });
        strategy.addEventListener("change", () => {
            const mask = editingPolicyMasking.find((item) => item.attribute === attribute);
            if (mask) {
                mask.strategy = strategy.value;
                invalidatePolicyPreview();
            }
        });
        row.append(checkbox, name, strategy);
        policyColumnMaskingList.append(row);
    });
}

function populatePolicyOutcomeOptions(rule) {
    policyRowFilterAttribute.replaceChildren(new Option("Select a resource attribute", ""));
    policyAttributeDefinitions
        .filter((definition) => definition.category === "resource")
        .forEach((definition) => policyRowFilterAttribute.append(new Option(
            `${definition.name} (${definition.dataType})`,
            `resource.${definition.name}`
        )));
    policyRowFilterAttribute.value = rule && rule.rowFilter ? rule.rowFilter.attribute : "";
    policyRowFilterOperator.replaceChildren();
    policyOperatorOptions.forEach(([value, label]) =>
        policyRowFilterOperator.append(new Option(label, value)));
    policyRowFilterOperator.value = rule && rule.rowFilter ? rule.rowFilter.operator : "eq";
    policyRowFilterValue.value = rule && rule.rowFilter ? rule.rowFilter.valueText : "";
    renderPolicyMaskingRows();
}

function updatePolicyOutcomeVisibility() {
    const rowFilter = policyRuleEffect.value === "row_filtering";
    const columnMasking = policyRuleEffect.value === "column_masking";
    policyRowFilterSettings.hidden = !rowFilter;
    policyColumnMaskingSettings.hidden = !columnMasking;
    [policyRowFilterAttribute, policyRowFilterOperator].forEach((control) => {
        control.disabled = !rowFilter;
        control.required = rowFilter;
    });
    const rowFilterValueRequired = rowFilter && policyRowFilterOperator.value !== "exists";
    policyRowFilterValue.disabled = !rowFilter;
    policyRowFilterValue.required = rowFilterValueRequired;
    policyRowFilterValue.closest("label").hidden = rowFilter && !rowFilterValueRequired;
}

function populateSubjectAttributeSelects() {
    const filterSelection = subjectAttributeFilterAttributeId.value;
    const formSelection = subjectAttributeAttributeId.value;
    subjectAttributeFilterAttributeId.replaceChildren(new Option("All subject attributes", ""));
    subjectAttributeAttributeId.replaceChildren(new Option("Select an attribute", ""));
    subjectAttributeDefinitions.forEach((definition) => {
        const label = `${definition.name} (${definition.dataType}${definition.multiValued ? ", multiple" : ""})`;
        subjectAttributeFilterAttributeId.append(new Option(label, String(definition.id)));
        subjectAttributeAttributeId.append(new Option(label, String(definition.id)));
    });
    if (subjectAttributeDefinitions.some((definition) => String(definition.id) === filterSelection)) {
        subjectAttributeFilterAttributeId.value = filterSelection;
    }
    if (subjectAttributeDefinitions.some((definition) => String(definition.id) === formSelection)) {
        subjectAttributeAttributeId.value = formSelection;
    }
    filterSubjectAttributesButton.disabled = !selectedSubject;
    newSubjectAttributeButton.disabled = !selectedSubject || subjectAttributeDefinitions.length === 0;
}

async function loadSubjectAttributeDefinitions() {
    const response = await fetch(`${apiBaseUrl}/api/abac/attributes?category=subject`, {
        headers: {Authorization: `Bearer ${token}`}
    });
    if (!response.ok) {
        throw new Error(await getApiError(response));
    }
    const definitions = await response.json();
    if (!Array.isArray(definitions)) {
        throw new Error("The subject attribute definition response was not a list.");
    }
    subjectAttributeDefinitions = definitions;
    populateSubjectAttributeSelects();
}

function setSubjectRowsMessage(message) {
    subjectRows.replaceChildren();
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.className = "attribute-empty-state";
    cell.colSpan = 5;
    cell.textContent = message;
    row.append(cell);
    subjectRows.append(row);
}

function renderSubjects(subjects) {
    subjectRows.replaceChildren();
    if (subjects.length === 0) {
        setSubjectRowsMessage("No users found in your tenant.");
        return;
    }

    subjects.forEach((subject) => {
        const row = document.createElement("tr");
        [subject.userId, subject.username, subject.groupName || "—", subject.status || "—"]
            .forEach((value) => {
                const cell = document.createElement("td");
                cell.textContent = value == null ? "" : String(value);
                row.append(cell);
            });
        const actionsCell = document.createElement("td");
        const selectButton = document.createElement("button");
        selectButton.className = "secondary-button";
        selectButton.type = "button";
        const isSelected = selectedSubject
            && String(selectedSubject.userId) === String(subject.userId);
        selectButton.textContent = isSelected ? "Selected" : "Select";
        selectButton.setAttribute("aria-label", `Select subject ${subject.username || subject.userId}`);
        selectButton.addEventListener("click", () => selectSubject(subject));
        actionsCell.append(selectButton);
        row.append(actionsCell);
        subjectRows.append(row);
    });
}

function selectSubject(subject) {
    const changed = !selectedSubject || String(selectedSubject.userId) !== String(subject.userId);
    selectedSubject = subject;
    subjectAttributeSubjectId.value = String(subject.userId);
    selectedSubjectLabel.textContent =
        `Selected ${subject.username || "user"} (ID ${subject.userId}).`;
    populateSubjectAttributeSelects();
    renderSubjects(subjectsInTenant);
    if (changed) {
        resetSubjectAttributeForm();
    }
    loadSubjectAttributeValues();
}

async function loadSubjectList() {
    const requestId = ++subjectListRequestId;
    refreshSubjectsButton.disabled = true;
    subjectListStatus.textContent = "Loading users in your tenant...";
    try {
        const response = await fetch(`${apiBaseUrl}/api/permissions/users`, {
            headers: {Authorization: `Bearer ${token}`}
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const subjects = await response.json();
        if (!Array.isArray(subjects)) {
            throw new Error("The subject list response was not a list.");
        }
        if (requestId !== subjectListRequestId) {
            return;
        }

        subjectsInTenant = subjects;
        if (selectedSubject) {
            selectedSubject = subjects.find(
                (subject) => String(subject.userId) === String(selectedSubject.userId)) || null;
        }
        renderSubjects(subjects);
        subjectListStatus.textContent =
            `Loaded ${subjects.length} user${subjects.length === 1 ? "" : "s"} in your tenant.`;
        if (selectedSubject) {
            subjectAttributeSubjectId.value = String(selectedSubject.userId);
            selectedSubjectLabel.textContent =
                `Selected ${selectedSubject.username || "user"} (ID ${selectedSubject.userId}).`;
            populateSubjectAttributeSelects();
            await loadSubjectAttributeValues();
        } else {
            subjectAttributeSubjectId.value = "";
            selectedSubjectLabel.textContent = "Select a user to view or manage their attribute values.";
            resetSubjectAttributeForm();
            setSubjectAttributeRowsMessage("Select a user to load subject attribute values.");
            subjectAttributeStatus.textContent = "Select a user to load subject attribute values.";
            populateSubjectAttributeSelects();
        }
    } catch (error) {
        if (requestId === subjectListRequestId) {
            subjectsInTenant = [];
            selectedSubject = null;
            subjectAttributeSubjectId.value = "";
            resetSubjectAttributeForm();
            setSubjectRowsMessage(error.message || "Unable to load users.");
            subjectListStatus.textContent = error.message || "Unable to load users.";
            selectedSubjectLabel.textContent = "Select a user to view or manage their attribute values.";
            setSubjectAttributeRowsMessage("Unable to load subjects.");
            subjectAttributeStatus.textContent = error.message || "Unable to load users.";
            populateSubjectAttributeSelects();
        }
    } finally {
        if (requestId === subjectListRequestId) {
            refreshSubjectsButton.disabled = false;
        }
    }
}

function setSubjectAttributeRowsMessage(message) {
    subjectAttributeRows.replaceChildren();
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.className = "attribute-empty-state";
    cell.colSpan = 4;
    cell.textContent = message;
    row.append(cell);
    subjectAttributeRows.append(row);
}

function formatSubjectAttributeValue(value) {
    if (value.valueText != null) {
        return value.valueText;
    }
    if (value.valueJson != null) {
        try {
            return JSON.stringify(value.valueJson);
        } catch {
            return String(value.valueJson);
        }
    }
    return "—";
}

function renderSubjectAttributeValues(values) {
    subjectAttributeRows.replaceChildren();
    if (values.length === 0) {
        setSubjectAttributeRowsMessage("No subject attribute values found.");
        return;
    }
    values.forEach((value) => {
        const definition = subjectAttributeDefinitions.find(
            (item) => String(item.id) === String(value.attributeId));
        const row = document.createElement("tr");
        [value.id, definition ? definition.name : `Attribute #${value.attributeId}`,
            formatSubjectAttributeValue(value)].forEach((cellValue) => {
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
        editButton.setAttribute("aria-label", `Edit subject attribute value ${value.id}`);
        editButton.addEventListener("click", () => openSubjectAttributeForm(value));
        const deleteButton = document.createElement("button");
        deleteButton.className = "secondary-button";
        deleteButton.type = "button";
        deleteButton.textContent = "Delete";
        deleteButton.setAttribute("aria-label", `Delete subject attribute value ${value.id}`);
        deleteButton.addEventListener("click", () => deleteSubjectAttribute(value, deleteButton));
        actions.append(editButton, deleteButton);
        actionsCell.append(actions);
        row.append(actionsCell);
        subjectAttributeRows.append(row);
    });
}

async function loadSubjectAttributeValues() {
    const requestId = ++subjectAttributeListRequestId;
    if (!selectedSubject) {
        setSubjectAttributeRowsMessage("Select a user to load subject attribute values.");
        subjectAttributeStatus.textContent = "Select a user to load subject attribute values.";
        return;
    }
    const query = new URLSearchParams({subjectId: String(selectedSubject.userId)});
    if (subjectAttributeFilterAttributeId.value) {
        query.set("attributeId", subjectAttributeFilterAttributeId.value);
    }
    filterSubjectAttributesButton.disabled = true;
    subjectAttributeStatus.textContent = "Loading subject attribute values...";
    try {
        const response = await fetch(
            `${apiBaseUrl}/api/abac/subject-attributes?${query.toString()}`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const values = await response.json();
        if (!Array.isArray(values)) {
            throw new Error("The subject attribute value response was not a list.");
        }
        if (requestId !== subjectAttributeListRequestId) {
            return;
        }
        renderSubjectAttributeValues(values);
        subjectAttributeStatus.textContent =
            `Loaded ${values.length} subject attribute value${values.length === 1 ? "" : "s"}.`;
    } catch (error) {
        if (requestId === subjectAttributeListRequestId) {
            setSubjectAttributeRowsMessage(error.message || "Unable to load subject attribute values.");
            subjectAttributeStatus.textContent =
                error.message || "Unable to load subject attribute values.";
        }
    } finally {
        if (requestId === subjectAttributeListRequestId) {
            filterSubjectAttributesButton.disabled = !selectedSubject;
        }
    }
}

async function loadSubjectAttributes() {
    subjectAttributeStatus.textContent = "Loading subject attributes...";
    let definitionsError = null;
    try {
        await loadSubjectAttributeDefinitions();
    } catch (error) {
        definitionsError = error;
        subjectAttributeDefinitions = [];
        populateSubjectAttributeSelects();
    }
    await loadSubjectList();
    if (definitionsError) {
        subjectAttributeStatus.textContent =
            definitionsError.message || "Unable to load subject attributes.";
    }
}

function openSubjectAttributeForm(value = null) {
    if (subjectAttributeMutationInProgress || !selectedSubject
            || subjectAttributeDefinitions.length === 0) {
        return;
    }
    editingSubjectAttributeId = value ? value.id : null;
    subjectAttributeFormTitle.textContent = value
        ? "Edit subject attribute value"
        : "Add subject attribute value";
    saveSubjectAttributeButton.textContent = value ? "Save changes" : "Save value";
    subjectAttributeSubjectId.value = String(selectedSubject.userId);
    subjectAttributeAttributeId.value = value ? String(value.attributeId) : "";
    subjectAttributeValueText.value = value
        ? value.valueText != null
            ? value.valueText
            : value.valueJson != null
                ? JSON.stringify(value.valueJson, null, 2)
                : ""
        : "";
    subjectAttributeForm.hidden = false;
    subjectAttributeAttributeId.focus();
}

function resetSubjectAttributeForm() {
    editingSubjectAttributeId = null;
    subjectAttributeForm.reset();
    subjectAttributeForm.hidden = true;
    subjectAttributeFormTitle.textContent = "Add subject attribute value";
    saveSubjectAttributeButton.textContent = "Save value";
    subjectAttributeSubjectId.value = selectedSubject ? String(selectedSubject.userId) : "";
}

async function saveSubjectAttribute(event) {
    event.preventDefault();
    if (subjectAttributeMutationInProgress || !subjectAttributeForm.reportValidity()) {
        return;
    }
    if (!selectedSubject) {
        subjectAttributeStatus.textContent = "Select a user before adding an attribute value.";
        return;
    }

    const attributeId = Number(subjectAttributeAttributeId.value);
    if (!Number.isSafeInteger(attributeId) || attributeId <= 0) {
        subjectAttributeStatus.textContent = "Select a valid subject attribute.";
        return;
    }

    subjectAttributeMutationInProgress = true;
    saveSubjectAttributeButton.disabled = true;
    cancelSubjectAttributeButton.disabled = true;
    const isEditing = editingSubjectAttributeId !== null;
    const value = {
        subjectId: selectedSubject.userId,
        attributeId,
        valueText: subjectAttributeValueText.value
    };
    try {
        const path = isEditing
            ? `/api/abac/subject-attributes/${encodeURIComponent(String(editingSubjectAttributeId))}`
            : "/api/abac/subject-attributes";
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
        subjectAttributeStatus.textContent = isEditing
            ? "Subject attribute value updated."
            : "Subject attribute value created.";
        resetSubjectAttributeForm();
        await loadSubjectAttributeValues();
    } catch (error) {
        subjectAttributeStatus.textContent =
            error.message || "Unable to save the subject attribute value.";
    } finally {
        subjectAttributeMutationInProgress = false;
        saveSubjectAttributeButton.disabled = false;
        cancelSubjectAttributeButton.disabled = false;
    }
}

async function deleteSubjectAttribute(value, button) {
    if (subjectAttributeMutationInProgress
            || !window.confirm(`Delete subject attribute value ${value.id}?`)) {
        return;
    }
    subjectAttributeMutationInProgress = true;
    button.disabled = true;
    subjectAttributeStatus.textContent = `Deleting subject attribute value ${value.id}...`;
    try {
        const response = await fetch(
            `${apiBaseUrl}/api/abac/subject-attributes/${encodeURIComponent(String(value.id))}`,
            {
                method: "DELETE",
                headers: {Authorization: `Bearer ${token}`}
            }
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        subjectAttributeStatus.textContent = `Subject attribute value ${value.id} deleted.`;
        await loadSubjectAttributeValues();
    } catch (error) {
        subjectAttributeStatus.textContent =
            error.message || "Unable to delete the subject attribute value.";
        button.disabled = false;
    } finally {
        subjectAttributeMutationInProgress = false;
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
            selectedExternalDatasource = scope === "external"
                    && resource.external
                    && String(resource.resourceType).toLowerCase() === "trino_connection"
                ? resource
                : null;
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
        const visibleResources = isExternal
            ? resources.filter((resource) =>
                String(resource.resourceType).toLowerCase() === "trino_connection")
            : resources;
        renderResourceList(scope, visibleResources);
        status.textContent =
            `Loaded ${visibleResources.length} ${scope} resource${visibleResources.length === 1 ? "" : "s"}.`;
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
        [value.id, value.resourceCode, definition ? definition.name : `Attribute #${value.attributeId}`,
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

function appendTrinoResourceTreeNode(list, nodeType, node, path, datasourceResourceId) {
    const item = document.createElement("li");
    item.className = "trino-resource-tree-node";

    const choice = document.createElement("label");
    choice.className = "trino-resource-tree-choice";
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    const selection = {nodeType, ...path};
    const selectionKey = JSON.stringify(selection);
    checkbox.checked = selectedExternalTreeNodes.has(selectionKey);
    checkbox.addEventListener("change", () => {
        if (checkbox.checked) {
            selectedExternalTreeNodes.set(selectionKey, selection);
        } else {
            selectedExternalTreeNodes.delete(selectionKey);
        }
        const selectedCount = selectedExternalTreeNodes.size;
        resourceAttributeTrinoTreeStatus.textContent = selectedCount === 0
            ? "Select one or more Trino resources."
            : `${selectedCount} Trino resource${selectedCount === 1 ? "" : "s"} selected.`;
    });
    const nameElement = document.createElement("span");
    nameElement.textContent = node.type ? `${node.name} (${node.type})` : node.name;
    choice.append(checkbox, nameElement);
    item.append(choice);

    const childLevel = {catalog: "schemas", schema: "tables", table: "fields"}[nodeType];
    if (childLevel) {
        const details = document.createElement("details");
        details.className = "trino-resource-tree-children";
        const summary = document.createElement("summary");
        summary.textContent = `Load ${childLevel}`;
        const childList = document.createElement("ul");
        childList.className = "trino-resource-tree-list";
        details.append(summary, childList);
        details.addEventListener("toggle", () => {
            if (details.open && details.dataset.loaded !== "true") {
                loadTrinoResourceChildren(details, nodeType, path, datasourceResourceId, childList);
            }
        });
        item.append(details);
    }

    list.append(item);
    return item;
}

function pathLabel(path) {
    return [path.catalog, path.schema, path.table, path.field].filter(Boolean).join(".");
}

function renderTrinoResourceTree(catalogs, datasourceResourceId) {
    if (!Array.isArray(catalogs)) {
        throw new Error("The Trino metadata response was not a list of catalogs.");
    }
    if (catalogs.length === 0) {
        resourceAttributeTrinoTree.replaceChildren();
        resourceAttributeTrinoTreeStatus.textContent = "No catalogs are available for this datasource.";
        return;
    }

    const list = document.createElement("ul");
    list.className = "trino-resource-tree-list";
    catalogs.forEach((catalog) => {
        appendTrinoResourceTreeNode(
            list, "catalog", catalog, {catalog: catalog.name}, datasourceResourceId);
    });
    resourceAttributeTrinoTree.replaceChildren(list);
    resourceAttributeTrinoTreeStatus.textContent =
        "Expand a catalog, schema, or table to load children; select one or more resources.";
}

async function loadTrinoResourceChildren(details, parentType, path, datasourceResourceId, childList) {
    if (details.dataset.loading === "true" || details.dataset.loaded === "true") {
        return;
    }
    const childLevel = {catalog: "schemas", schema: "tables", table: "fields"}[parentType];
    const childType = {catalog: "schema", schema: "table", table: "field"}[parentType];
    const query = new URLSearchParams();
    ["catalog", "schema", "table"].forEach((key) => {
        if (path[key]) {
            query.set(key, path[key]);
        }
    });
    const summary = details.querySelector("summary");
    details.dataset.loading = "true";
    summary.textContent = `Loading ${childLevel}...`;
    try {
        const response = await fetch(
            `${apiBaseUrl}/api/trino/connections/${encodeURIComponent(String(datasourceResourceId))}/tree?${query}`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const children = await response.json();
        if (!Array.isArray(children)) {
            throw new Error(`The Trino ${childLevel} response was not a list.`);
        }
        if (!details.isConnected) {
            return;
        }
        childList.replaceChildren();
        children.forEach((child) => {
            const childPath = {...path};
            childPath[childType] = child.name;
            appendTrinoResourceTreeNode(
                childList, childType, child, childPath, datasourceResourceId);
        });
        details.dataset.loaded = "true";
        summary.textContent = children.length === 0
            ? `No ${childLevel} found`
            : `${children.length} ${childLevel}`;
        resourceAttributeTrinoTreeStatus.textContent =
            children.length === 0 ? `No ${childLevel} found under ${pathLabel(path)}.`
                : `Loaded ${children.length} ${childLevel} under ${pathLabel(path)}.`;
    } catch (error) {
        if (details.isConnected) {
            summary.textContent = `Unable to load ${childLevel}; close and reopen to retry`;
            resourceAttributeTrinoTreeStatus.textContent =
                error.message || `Unable to load Trino ${childLevel}.`;
        }
    } finally {
        delete details.dataset.loading;
    }
}

async function loadTrinoResourceTree() {
    const requestId = ++externalTreeRequestId;
    if (!selectedExternalDatasource) {
        refreshResourceTrinoTreeButton.disabled = false;
        resourceAttributeTrinoTreeStatus.textContent = "Select an external Trino datasource first.";
        return;
    }
    const datasourceResourceId = selectedExternalDatasource.id;
    selectedExternalTreeNodes = new Map();
    resourceAttributeTrinoTree.replaceChildren();
    resourceAttributeTrinoTreeStatus.textContent = "Loading Trino metadata...";
    refreshResourceTrinoTreeButton.disabled = true;
    try {
        const response = await fetch(
            `${apiBaseUrl}/api/trino/connections/${encodeURIComponent(String(datasourceResourceId))}/tree`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const catalogs = await response.json();
        if (requestId !== externalTreeRequestId
                || !selectedExternalDatasource
                || selectedExternalDatasource.id !== datasourceResourceId) {
            return;
        }
        renderTrinoResourceTree(catalogs, datasourceResourceId);
    } catch (error) {
        if (requestId === externalTreeRequestId) {
            resourceAttributeTrinoTree.replaceChildren();
            resourceAttributeTrinoTreeStatus.textContent =
                error.message || "Unable to load the Trino resource tree.";
        }
    } finally {
        if (requestId === externalTreeRequestId) {
            refreshResourceTrinoTreeButton.disabled = false;
        }
    }
}

async function openResourceAttributeForm(value = null) {
    if (resourceAttributeMutationInProgress || resourceAttributeDefinitions.length === 0) {
        return;
    }
    if (!value && activeResourceScope === "external" && !selectedExternalDatasource) {
        resourceAttributeStatus.textContent = "Select an external Trino datasource before adding a value.";
        return;
    }
    editingResourceAttributeId = value ? value.id : null;
    resourceAttributeUsesExternalTree = !value && activeResourceScope === "external";
    selectedExternalTreeNodes = new Map();
    resourceAttributeFormTitle.textContent = value
        ? "Edit resource attribute value"
        : "Add resource attribute value";
    saveResourceAttributeButton.textContent = value ? "Save changes" : "Save value";
    resourceAttributeResourceIdField.hidden = resourceAttributeUsesExternalTree;
    resourceAttributeResourceId.required = !resourceAttributeUsesExternalTree;
    resourceAttributeTrinoPicker.hidden = !resourceAttributeUsesExternalTree;
    resourceAttributeResourceId.value = value
        ? String(value.resourceId)
        : resourceAttributeFilterResourceId.value;
    resourceAttributeAttributeId.value = value
        ? String(value.attributeId)
        : resourceAttributeFilterAttributeId.value;
    resourceAttributeValueText.value = value && value.valueText != null ? value.valueText : "";
    resourceAttributeForm.hidden = false;
    if (resourceAttributeUsesExternalTree) {
        await loadTrinoResourceTree();
    } else {
        resourceAttributeResourceId.focus();
    }
}

function resetResourceAttributeForm() {
    editingResourceAttributeId = null;
    resourceAttributeUsesExternalTree = false;
    selectedExternalTreeNodes = new Map();
    externalTreeRequestId++;
    resourceAttributeForm.reset();
    resourceAttributeForm.hidden = true;
    resourceAttributeResourceIdField.hidden = false;
    resourceAttributeResourceId.required = true;
    resourceAttributeTrinoPicker.hidden = true;
    refreshResourceTrinoTreeButton.disabled = false;
    resourceAttributeTrinoTree.replaceChildren();
    resourceAttributeTrinoTreeStatus.textContent = "Select an external Trino datasource to load its tree.";
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
    if ((!resourceAttributeUsesExternalTree
            && (!Number.isSafeInteger(resourceId) || resourceId <= 0))
            || !Number.isSafeInteger(attributeId) || attributeId <= 0) {
        resourceAttributeStatus.textContent = "Select a valid resource and attribute.";
        return;
    }
    if (resourceAttributeUsesExternalTree
            && (!selectedExternalDatasource || selectedExternalTreeNodes.size === 0)) {
        resourceAttributeTrinoTreeStatus.textContent = "Select at least one Trino resource before saving.";
        return;
    }
    if (resourceAttributeUsesExternalTree
            && !window.confirm(
                `Replace all resources under this Trino datasource with the ${selectedExternalTreeNodes.size} selected resource(s)? Existing resource attribute values under those resources will be removed.`)) {
        return;
    }

    resourceAttributeMutationInProgress = true;
    saveResourceAttributeButton.disabled = true;
    cancelResourceAttributeButton.disabled = true;
    const isEditing = editingResourceAttributeId !== null;
    const usesExternalTree = resourceAttributeUsesExternalTree;
    const value = usesExternalTree
        ? {
            attributeId,
            valueText: resourceAttributeValueText.value,
            selections: Array.from(selectedExternalTreeNodes.values())
        }
        : {
            resourceId,
            attributeId,
            valueText: resourceAttributeValueText.value
        };
    try {
        const path = usesExternalTree
            ? `/api/trino/connections/${encodeURIComponent(String(selectedExternalDatasource.id))}/resource-attributes`
            : isEditing
                ? `/api/abac/resource-attributes/${encodeURIComponent(String(editingResourceAttributeId))}`
                : "/api/abac/resource-attributes";
        const response = await fetch(`${apiBaseUrl}${path}`, {
            method: usesExternalTree || isEditing ? "PUT" : "POST",
            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(value)
        });
        if (!response.ok) {
            throw new Error(await getApiError(response));
        }
        const savedResult = usesExternalTree ? await response.json() : null;
        resourceAttributeStatus.textContent = usesExternalTree
            ? `Recreated hierarchy for ${savedResult.resourceCount} selected Trino resource${savedResult.resourceCount === 1 ? "" : "s"}.`
            : isEditing
                ? "Resource attribute value updated."
                : "Resource attribute value created.";
        resetResourceAttributeForm();
        if (usesExternalTree) {
            resourceAttributeFilterResourceId.value = "";
        }
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
trinoQueryForm.addEventListener("submit", executeTrinoSql);
refreshUsersButton.addEventListener("click", loadUsers);
assignRoleForm.addEventListener("submit", assignSelectedRole);
assignRoleUser.addEventListener("change", updateAssignRoleSubmitState);
assignRoleRole.addEventListener("change", updateAssignRoleSubmitState);
newAttributeButton.addEventListener("click", () => openAttributeForm());
refreshAttributesButton.addEventListener("click", loadAttributeDefinitions);
attributeCategoryFilter.addEventListener("change", loadAttributeDefinitions);
attributeForm.addEventListener("submit", saveAttribute);
cancelAttributeButton.addEventListener("click", resetAttributeForm);
refreshSubjectsButton.addEventListener("click", loadSubjectList);
filterSubjectAttributesButton.addEventListener("click", loadSubjectAttributeValues);
newSubjectAttributeButton.addEventListener("click", () => openSubjectAttributeForm());
subjectAttributeForm.addEventListener("submit", saveSubjectAttribute);
cancelSubjectAttributeButton.addEventListener("click", resetSubjectAttributeForm);
refreshPolicyResourcesButton.addEventListener("click", loadPolicyResources);
policyResourceSelect.addEventListener("change", () => {
    renderPolicyResourceSelection();
    invalidatePolicyPreview();
});
policyStaticDataEnabled.addEventListener("change", () => {
    policyStaticDataEditor.hidden = !policyStaticDataEnabled.checked;
    invalidatePolicyPreview();
});
policyStaticDataJson.addEventListener("input", invalidatePolicyPreview);
addPolicyRuleButton.addEventListener("click", addPolicyRule);
policyRuleList.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-policy-rule-id]");
    if (button) {
        selectPolicyRule(Number(button.dataset.policyRuleId));
    }
});
policyRuleForm.addEventListener("submit", (event) => {
    event.preventDefault();
    applyPolicyRuleChanges();
});
policyRuleForm.addEventListener("input", invalidatePolicyPreview);
policyRuleEffect.addEventListener("change", () => {
    updatePolicyOutcomeVisibility();
    invalidatePolicyPreview();
});
policyRowFilterOperator.addEventListener("change", updatePolicyOutcomeVisibility);
[
    policyRowFilterAttribute,
    policyRowFilterOperator,
    policyRowFilterValue
].forEach((control) => {
    control.addEventListener("input", invalidatePolicyPreview);
    control.addEventListener("change", invalidatePolicyPreview);
});
addPolicyConditionButton.addEventListener("click", () => {
    if (!selectedPolicyRule()) {
        return;
    }
    editingPolicyConditions.push({
        attribute: "",
        operator: "eq",
        valueType: "constant",
        valueAttribute: "",
        valueText: ""
    });
    renderPolicyConditionRows();
    invalidatePolicyPreview();
});
deletePolicyRuleButton.addEventListener("click", deleteSelectedPolicyRule);
policyConfigName.addEventListener("input", invalidatePolicyPreview);
policyConfigEntrypoint.addEventListener("input", () => {
    renderPolicySummary();
    invalidatePolicyPreview();
});
validatePolicyConfigButton.addEventListener("click", validateCurrentPolicyDraft);
generatePolicyRegoButton.addEventListener("click", generatePolicyPreview);
defaultTemplateTab.addEventListener("click", () => activateAttributeTemplateTab(defaultTemplateTab));
tenantTemplateTab.addEventListener("click", () => activateAttributeTemplateTab(tenantTemplateTab));
attributeTemplateViews.default.refreshButton.addEventListener(
    "click", () => loadAttributeTemplateList("default"));
attributeTemplateViews.tenant.refreshButton.addEventListener(
    "click", () => loadAttributeTemplateList("tenant"));
attributeTemplateViews.default.closeDetailButton.addEventListener(
    "click", () => closeAttributeTemplateDetail("default"));
attributeTemplateViews.tenant.closeDetailButton.addEventListener(
    "click", () => closeAttributeTemplateDetail("tenant"));
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
createDatasourceForm.addEventListener("submit", createDatasource);
filterResourceAttributesButton.addEventListener("click", loadResourceAttributeValues);
refreshInternalResourcesButton.addEventListener("click", () => loadResourceList("internal"));
refreshExternalResourcesButton.addEventListener("click", () => loadResourceList("external"));
refreshResourceTrinoTreeButton.addEventListener("click", loadTrinoResourceTree);
newResourceAttributeButton.addEventListener("click", () => openResourceAttributeForm());
resourceAttributeForm.addEventListener("submit", saveResourceAttribute);
cancelResourceAttributeButton.addEventListener("click", resetResourceAttributeForm);

navItems.forEach((item) => {
    item.addEventListener("click", () => showPage(item.dataset.page));
});
renderPolicyRuleList();
showPage("assign-roles");

mobileMenuButton.addEventListener("click", () => {
    const isOpen = sidebar.classList.toggle("open");
    mobileMenuButton.setAttribute("aria-expanded", String(isOpen));
});

logoutButton.addEventListener("click", () => {
    sessionStorage.removeItem("accessToken");
    window.location.replace("./index.html");
});
