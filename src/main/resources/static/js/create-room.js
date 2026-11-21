function getCsrfToken() {
    const cookie = document.cookie
        .split("; ")
        .find(row => row.startsWith("XSRF-TOKEN="));

    return cookie
        ? decodeURIComponent(cookie.substring("XSRF-TOKEN=".length))
        : null;
}

async function apiFetch(url, options = {}) {
    const method = (options.method || "GET").toUpperCase();

    const headers = new Headers(options.headers || {});

    if (!["GET", "HEAD", "OPTIONS", "TRACE"].includes(method)) {
        const csrfToken = getCsrfToken();

        if (!csrfToken) {
            throw new Error("CSRF token not found");
        }

        headers.set("X-XSRF-TOKEN", csrfToken);
    }

    return fetch(url, {
        ...options,
        headers
    });
}


const form = document.getElementById("createRoomForm");

form.addEventListener("submit", async function (event) {

    event.preventDefault();

    const name = document.getElementById("name").value;
    const password = document.getElementById("password").value;
    const ownerPassword = document.getElementById("ownerPassword").value;
    const expiresAtValue = document.getElementById("expiresAt").value;

    const roomData = {
        name,
        password: password === ""
            ? null
            : password,
        ownerPassword,
        expiresAt: expiresAtValue === ""
            ? null
            : expiresAtValue
    };

    try {
        const response = await apiFetch("/rooms", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(roomData)
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message);
        }

        const room = await response.json();

        window.location.href =
            `/room.html?roomKey=${room.roomKey}`;

    } catch (error) {
        console.error(error);

        document.getElementById("errorMessage").textContent =
            "Не удалось создать комнату";
    }
});