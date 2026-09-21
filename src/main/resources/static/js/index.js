const button = document.getElementById("loadRoomsButton");
const roomsContainer = document.getElementById("rooms");

button.addEventListener("click", async function () {

    const response = await fetch("/rooms");

    const rooms = await response.json();

    console.log(rooms);

    roomsContainer.innerHTML = "";

    rooms.forEach(function (room) {

        const element = document.createElement("div");

        element.textContent = room.name;

        roomsContainer.appendChild(element);
    });
});