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
    passwordToggle.textContent = showPassword ? "隐藏" : "显示";
    passwordToggle.setAttribute("aria-label", showPassword ? "隐藏密码" : "显示密码");
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
        message.textContent = "请输入用户名和密码。";
        (!username ? usernameInput : passwordInput).focus();
        return;
    }

    submitButton.disabled = true;
    buttonLabel.textContent = "正在登录…";

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
                throw new Error("认证服务返回了无法识别的响应。");
            }
        }

        if (!response.ok) {
            if (response.status === 401) {
                throw new Error("用户名或密码不正确。");
            }
            throw new Error(result.message || `登录失败（${response.status}）。`);
        }
        if (!result.accessToken) {
            throw new Error("登录响应中没有 accessToken。");
        }

        sessionStorage.setItem("accessToken", result.accessToken);
        message.textContent = "登录成功，访问令牌已保存到当前标签页。";
        message.classList.add("success");
        passwordInput.value = "";
    } catch (error) {
        message.textContent = error instanceof TypeError
            ? "无法连接认证服务，请检查服务地址和跨域配置。"
            : error.message || "登录失败，请稍后重试。";
    } finally {
        submitButton.disabled = false;
        buttonLabel.textContent = "登录";
    }
});
