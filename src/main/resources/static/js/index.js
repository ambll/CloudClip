const form = document.getElementById("joinRoomForm");

form.addEventListener("submit", function (event) {

    event.preventDefault();

    const roomKey =
        document.getElementById("roomKey").value.trim();

    if (roomKey === "") {
        return;
    }

    window.location.href =
        `/room.html?roomKey=${encodeURIComponent(roomKey)}`;
});