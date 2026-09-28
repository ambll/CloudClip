const form = document.getElementById("createRoomForm");

form.addEventListener("submit", async function (event) {

    event.preventDefault();

    const name = document.getElementById("name").value;
    const password = document.getElementById("password").value;
    const visibility = document.getElementById("visibility").value;
    const expiresAtValue = document.getElementById("expiresAt").value;

    const roomData = {
        name,
        password: password === ""
                ? null
                : password,
        visibility,
        expiresAt: expiresAtValue === ""
            ? null
            : expiresAtValue
    };

    try {
        const response = await fetch("/rooms", {
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