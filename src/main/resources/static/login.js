const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");

document.getElementById("userQuick").addEventListener("click", () => {
    usernameInput.value = "user";
    passwordInput.value = "user123";
    login();
});

document.getElementById("adminQuick").addEventListener("click", () => {
    usernameInput.value = "admin";
    passwordInput.value = "admin123";
    login();
});

document.getElementById("loginBtn").addEventListener("click", login);

async function login() {
    setLoginNotice("登录中...", "");
    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            username: usernameInput.value,
            password: passwordInput.value
        })
    });
    if (!response.ok) {
        const body = await response.json().catch(() => ({message: "登录失败"}));
        setLoginNotice(body.message || "登录失败", "error");
        return;
    }
    const user = await response.json();
    window.location.href = user.role === "ADMIN" ? "/admin.html" : "/";
}

function setLoginNotice(message, type) {
    const notice = document.getElementById("loginNotice");
    notice.textContent = message;
    notice.className = `notice ${type}`;
}
