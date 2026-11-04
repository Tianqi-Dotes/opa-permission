const pages = {
    "assign-roles": {
        title: "Assign Roles",
        description: "Manage role assignments for users in your workspace.",
        featureTitle: "Role assignment",
        featureDescription: "Select a workspace tool from the menu to get started. Feature forms will appear here.",
        symbol: "AR"
    },
    "test-permissions": {
        title: "Test Permissions",
        description: "Check whether a user is allowed to access a resource.",
        featureTitle: "Permission testing",
        featureDescription: "Test a user's access against the active role and permission rules.",
        symbol: "TP"
    },
    "assign-policy": {
        title: "Assign Policy",
        description: "Manage policies and associate them with the appropriate identities.",
        featureTitle: "Policy assignment",
        featureDescription: "Policy assignment tools will be available here.",
        symbol: "AP"
    },
    "query-trino": {
        title: "Query Trino",
        description: "Run read-only queries against your connected Trino catalogs.",
        featureTitle: "Trino query",
        featureDescription: "The query workspace will be available here.",
        symbol: "QT"
    },
    "create-trino-table": {
        title: "Create Trino Table",
        description: "Create and manage tables in your Trino data catalogs.",
        featureTitle: "Table management",
        featureDescription: "Table management tools will be available here.",
        symbol: "CT"
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
const breadcrumbPage = document.querySelector("#breadcrumb-page");
const pageDescription = document.querySelector("#page-description");
const featureTitle = document.querySelector("#feature-title");
const featureDescription = document.querySelector("#feature-description");
const featureSymbol = document.querySelector("#feature-symbol");
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
    breadcrumbPage.textContent = page.title;
    pageDescription.textContent = page.description;
    featureTitle.textContent = page.featureTitle;
    featureDescription.textContent = page.featureDescription;
    featureSymbol.textContent = page.symbol;
    const isAssignRolesPage = pageKey === "assign-roles";
    const isPermissionTestPage = pageKey === "test-permissions";
    featureCard.hidden = isAssignRolesPage || isPermissionTestPage;
    infoRow.hidden = isAssignRolesPage || isPermissionTestPage;
    assignRolePanel.hidden = !isAssignRolesPage;
    permissionTestPanel.hidden = !isPermissionTestPage;
    if (isAssignRolesPage) {
        loadUsers();
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
