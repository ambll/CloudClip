const params = new URLSearchParams(window.location.search);

const roomKey = params.get("roomKey");


// =========================
// Ошибки комнаты
// =========================

function showRoomError(message) {

    const errorMessage =
        document.getElementById("errorMessage");

    const roomContent =
        document.getElementById("roomContent");

    errorMessage.textContent = message;
    errorMessage.style.display = "block";

    roomContent.style.display = "none";
}


function handleRoomError(response) {

    if (response.status === 404) {
        showRoomError("Комната не найдена");
        return true;
    }

    if (response.status === 410) {
        showRoomError("Срок действия комнаты истёк");
        return true;
    }

    return false;
}


// =========================
// Пароль комнаты
// =========================

function showPasswordModal() {

    const passwordModal =
        document.getElementById("passwordModal");

    passwordModal.style.display = "flex";

    document.getElementById("passwordInput").focus();
}


function hidePasswordModal() {

    const passwordModal =
        document.getElementById("passwordModal");

    passwordModal.style.display = "none";
}


function showPasswordError(message) {

    const passwordError =
        document.getElementById("passwordError");

    passwordError.textContent = message;
    passwordError.style.display = "block";
}


function hidePasswordError() {

    const passwordError =
        document.getElementById("passwordError");

    passwordError.textContent = "";
    passwordError.style.display = "none";
}


// =========================
// Загрузка комнаты
// =========================

async function loadRoom() {

    const response =
        await fetch(`/rooms/${roomKey}`);

    if (!response.ok) {

        if (response.status === 404) {
            showRoomError("Комната не найдена");
        } else if (response.status === 410) {
            showRoomError("Срок действия комнаты истёк");
        } else {
            showRoomError("Не удалось загрузить комнату");
        }

        return false;
    }

    const room = await response.json();

    document.getElementById("roomName").textContent =
        room.name;

    const visibility =
        room.visibility === "PUBLIC"
            ? "Публичная комната"
            : "Приватная комната";

    document.getElementById("roomVisibility").textContent =
        visibility;

    if (room.expiresAt === null) {

        document.getElementById("roomExpiration").textContent =
            "Без срока действия";

    } else {

        document.getElementById("roomExpiration").textContent =
            `Действует до: ${room.expiresAt}`;
    }

    return true;
}


// =========================
// Загрузка items
// =========================

async function loadItems() {

    const response =
        await fetch(`/rooms/${roomKey}/items`);

    if (response.status === 403) {

        showPasswordModal();

        return;
    }

    if (!response.ok) {

        if (handleRoomError(response)) {
            return;
        }

        showRoomError(
            "Не удалось загрузить содержимое комнаты"
        );

        return;
    }

    const items = await response.json();

    const itemsContainer =
        document.getElementById("items");

    itemsContainer.innerHTML = "";

    items.forEach(function (item) {

        const card =
            document.createElement("div");

        card.classList.add("item-card");


        if (item.type === "TEXT") {

            const text =
                document.createElement("p");

            text.textContent = item.text;

            card.appendChild(text);
        }


        if (item.type === "FILE") {

            const fileName =
                document.createElement("p");

            fileName.textContent =
                item.fileName;


            const downloadLink =
                document.createElement("a");

            downloadLink.textContent =
                "Скачать";

            downloadLink.href =
                item.downloadUrl;


            card.appendChild(fileName);
            card.appendChild(downloadLink);
        }


        const deleteButton =
            document.createElement("button");

        deleteButton.textContent =
            "Удалить";


        deleteButton.addEventListener(
            "click",
            async function () {

                const response =
                    await fetch(
                        `/rooms/${roomKey}/items/${item.id}`,
                        {
                            method: "DELETE"
                        }
                    );


                if (!response.ok) {

                    if (handleRoomError(response)) {
                        return;
                    }

                    if (response.status === 403) {
                        showPasswordModal();
                        return;
                    }

                    alert("Не удалось удалить item");

                    return;
                }


                await loadItems();
            }
        );


        card.appendChild(deleteButton);

        itemsContainer.appendChild(card);
    });
}


// =========================
// Доступ к комнате
// =========================

const passwordForm =
    document.getElementById("passwordForm");


passwordForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        hidePasswordError();


        const passwordInput =
            document.getElementById("passwordInput");

        const password =
            passwordInput.value;


        const response =
            await fetch(
                `/rooms/${roomKey}/access`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        password: password
                    })
                }
            );


        if (response.status === 403) {

            showPasswordError("Неверный пароль");

            passwordInput.focus();

            return;
        }


        if (!response.ok) {

            showPasswordError(
                "Не удалось получить доступ к комнате"
            );

            return;
        }


        hidePasswordModal();

        passwordInput.value = "";

        await loadItems();
    }
);


// =========================
// Добавление текста
// =========================

const addTextButton =
    document.getElementById("addTextButton");


addTextButton.addEventListener(
    "click",
    async function () {

        const textInput =
            document.getElementById("textInput");

        const text =
            textInput.value;


        if (text.trim() === "") {

            alert("Введите текст");

            return;
        }


        const response =
            await fetch(
                `/rooms/${roomKey}/items/text`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        text: text
                    })
                }
            );


        if (!response.ok) {

            if (handleRoomError(response)) {
                return;
            }


            if (response.status === 403) {

                showPasswordModal();

                return;
            }


            alert("Не удалось добавить текст");

            return;
        }


        textInput.value = "";

        await loadItems();
    }
);


// =========================
// Добавление файла
// =========================

const addFileButton =
    document.getElementById("addFileButton");


addFileButton.addEventListener(
    "click",
    async function () {

        const fileInput =
            document.getElementById("fileInput");

        const file =
            fileInput.files[0];


        if (!file) {
            return;
        }


        const formData =
            new FormData();

        formData.append("file", file);


        const response =
            await fetch(
                `/rooms/${roomKey}/items/file`,
                {
                    method: "POST",

                    body: formData
                }
            );


        if (!response.ok) {

            if (handleRoomError(response)) {
                return;
            }


            if (response.status === 403) {

                showPasswordModal();

                return;
            }


            if (response.status === 413) {

                alert("Файл слишком большой");

            } else {

                alert("Не удалось загрузить файл");
            }


            return;
        }


        fileInput.value = "";

        await loadItems();
    }
);


// =========================
// Меню комнаты
// =========================

const roomMenuButton =
    document.getElementById("roomMenuButton");

const roomMenu =
    document.getElementById("roomMenu");


roomMenuButton.addEventListener(
    "click",
    function (event) {

        event.stopPropagation();

        if (roomMenu.style.display === "none") {

            roomMenu.style.display = "flex";

        } else {

            roomMenu.style.display = "none";
        }
    }
);


document.addEventListener(
    "click",
    function (event) {

        if (
            !roomMenu.contains(event.target) &&
            !roomMenuButton.contains(event.target)
        ) {
            roomMenu.style.display = "none";
        }
    }
);


// =========================
// Удаление комнаты
// =========================

const deleteRoomButton =
    document.getElementById("deleteRoomButton");

const deleteRoomModal =
    document.getElementById("deleteRoomModal");

const deleteRoomForm =
    document.getElementById("deleteRoomForm");

const cancelDeleteRoomButton =
    document.getElementById("cancelDeleteRoomButton");

const ownerPasswordInput =
    document.getElementById("ownerPasswordInput");

const deleteRoomError =
    document.getElementById("deleteRoomError");


function showDeleteRoomModal() {

    deleteRoomError.textContent = "";
    deleteRoomError.style.display = "none";

    ownerPasswordInput.value = "";

    deleteRoomModal.style.display = "flex";

    ownerPasswordInput.focus();
}


function hideDeleteRoomModal() {

    deleteRoomModal.style.display = "none";

    ownerPasswordInput.value = "";

    deleteRoomError.textContent = "";
    deleteRoomError.style.display = "none";
}


deleteRoomButton.addEventListener(
    "click",
    function () {

        roomMenu.style.display = "none";

        showDeleteRoomModal();
    }
);


cancelDeleteRoomButton.addEventListener(
    "click",
    function () {

        hideDeleteRoomModal();
    }
);


deleteRoomForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        deleteRoomError.textContent = "";
        deleteRoomError.style.display = "none";


        const ownerPassword =
            ownerPasswordInput.value;


        const response =
            await fetch(
                `/rooms/${roomKey}`,
                {
                    method: "DELETE",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        password: ownerPassword
                    })
                }
            );


        if (response.status === 403) {

            deleteRoomError.textContent =
                "Неверный пароль владельца";

            deleteRoomError.style.display =
                "block";

            ownerPasswordInput.focus();

            return;
        }


        if (response.status === 404) {

            hideDeleteRoomModal();

            showRoomError("Комната не найдена");

            return;
        }


        if (response.status === 410) {

            hideDeleteRoomModal();

            showRoomError(
                "Срок действия комнаты истёк"
            );

            return;
        }


        if (!response.ok) {

            deleteRoomError.textContent =
                "Не удалось удалить комнату";

            deleteRoomError.style.display =
                "block";

            return;
        }


        window.location.href = "/";
    }
);


// =========================
// Инициализация
// =========================

async function initializeRoom() {

    const roomLoaded =
        await loadRoom();

    if (!roomLoaded) {
        return;
    }

    await loadItems();
}


initializeRoom();