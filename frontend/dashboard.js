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
    const isCreateBucketPage = pageKey === "create-bucket";
    featureCard.hidden = isAssignRolesPage || isPermissionTestPage
        || isAttributeDictionaryPage || isCreateBucketPage;
    infoRow.hidden = isAssignRolesPage || isPermissionTestPage
        || isAttributeDictionaryPage || isCreateBucketPage;
    assignRolePanel.hidden = !isAssignRolesPage;
    permissionTestPanel.hidden = !isPermissionTestPage;
    attributeDictionaryPanel.hidden = !isAttributeDictionaryPage;
    createBucketPanel.hidden = !isCreateBucketPage;
    if (isAssignRolesPage) {
        loadUsers();
    }
    if (isAttributeDictionaryPage) {
        loadAttributeDefinitions();
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
            `Bucket "${bucketName}" was registered as ${resource.resourceCode}.`;
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
        emptyCell.colSpan = 6;
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
            definition.description || "—"
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
        editButton.addEventListener("click", () => openAttributeForm(definition));
        const deleteButton = document.createElement("button");
        deleteButton.className = "secondary-button";
        deleteButton.type = "button";
        deleteButton.textContent = "Delete";
        deleteButton.setAttribute("aria-label", `Delete ${definition.name}`);
        deleteButton.addEventListener("click", () => deleteAttribute(definition, deleteButton));
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
createBucketForm.addEventListener("submit", createBucket);

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
