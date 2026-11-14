const apiBaseUrl = (window.API_BASE_URL || "http://localhost:1111").replace(/\/+$/, "");

const form = document.querySelector("#login-form");
const usernameInput = document.querySelector("#username");
const passwordInput = document.querySelector("#password");
const passwordToggle = document.querySelector("#password-toggle");
const submitButton = document.querySelector("#submit-button");
const buttonLabel = document.querySelector(".button-label");
const message = document.querySelector("#form-message");

passwordToggle.addEventListener("click", () => {
    const showPassword = passwordInput.type === "password";
    passwordInput.type = showPassword ? "text" : "password";
    passwordToggle.textContent = showPassword ? "Hide" : "Show";
    passwordToggle.setAttribute("aria-label", showPassword ? "Hide password" : "Show password");
    passwordToggle.setAttribute("aria-pressed", String(showPassword));
    passwordInput.focus();
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    message.textContent = "";
    message.classList.remove("success");

    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    if (!username || !password) {
        message.textContent = "Enter your username and password.";
        (!username ? usernameInput : passwordInput).focus();
        return;
    }

    submitButton.disabled = true;
    buttonLabel.textContent = "Signing in...";

    try {
        const response = await fetch(`${apiBaseUrl}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
        const responseText = await response.text();
        let result = {};
        if (responseText) {
            try {
                result = JSON.parse(responseText);
            } catch {
                throw new Error("The authentication service returned an unrecognized response.");
            }
        }

        if (!response.ok) {
            if (response.status === 401) {
                throw new Error("Incorrect username or password.");
            }
            throw new Error(result.message || `Sign-in failed (HTTP ${response.status}).`);
        }
        if (!result.accessToken) {
            throw new Error("The login response is missing an accessToken.");
        }

        sessionStorage.setItem("accessToken", result.accessToken);
        window.location.assign("./dashboard.html");
    } catch (error) {
        message.textContent = error instanceof TypeError
            ? "Unable to connect to the authentication service. Check the service URL and CORS settings."
            : error.message || "Sign-in failed. Please try again later.";
    } finally {
        submitButton.disabled = false;
        buttonLabel.textContent = "Sign in";
    }
});
