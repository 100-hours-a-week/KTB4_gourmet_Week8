const API_BASE_URL = "http://localhost:8080";

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

function clearLoginStorage() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userId");
    localStorage.removeItem("email");
    localStorage.removeItem("nickname");
    localStorage.removeItem("profileImage");
}

async function apiFetch(path, options = {}) {
    const accessToken = getAccessToken();

    const headers = new Headers(options.headers || {});

    if (accessToken) {
        headers.set("Authorization", `Bearer ${accessToken}`);
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
        credentials: "include"
    });

    if (response.status === 204) {
        return null;
    }

    const data = await response.json().catch(function () {
        return null;
    });

    if (!response.ok) {
        if (response.status === 401) {
            clearLoginStorage();
            alert("로그인이 필요합니다.");
            window.location.href = "./login.html";
            return null;
        }

        throw data ?? new Error("API 요청에 실패했습니다.");
    }

    return data;
}