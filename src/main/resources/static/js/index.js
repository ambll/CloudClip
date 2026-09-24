const button = document.getElementById("loadRoomsButton");
const roomsContainer = document.getElementById("rooms");

button.addEventListener("click", async function () {

    const response = await fetch("/rooms");

    const rooms = await response.json();

    console.log(rooms);

    roomsContainer.innerHTML = "";

    rooms.forEach(function (room) {

        const card = document.createElement("div");
        card.classList.add("room-card");

        const name = document.createElement("h2");
        name.textContent = room.name;

        const visibility = document.createElement("p");
        visibility.textContent = room.visibility === "PUBLIC"
            ? "Публичная комната"
            : "Приватная комната";

        const createdAt = document.createElement("p");
        createdAt.textContent = `Создана: ${room.createdAt}`;

        const link = document.createElement("a");
        link.textContent = "Открыть →";
        link.href = `/room/${room.roomKey}`;

        card.appendChild(name);
        card.appendChild(visibility);
        card.appendChild(createdAt);
        card.appendChild(link);

        roomsContainer.appendChild(card);
    });
});