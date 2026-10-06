const params = new URLSearchParams(window.location.search);

const roomKey = params.get("roomKey");

let currentRoom = null;


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

    currentRoom = room;

    document.getElementById("roomName").textContent =
        room.name;

    updateExpirationText(room);
    updateRoomMenu(room);

    return true;
}


// =========================
// Информация о комнате
// =========================

function updateExpirationText(room) {

    const expirationElement =
        document.getElementById("roomExpiration");

    if (room.expiresAt === null) {

        expirationElement.textContent =
            "Без срока действия";

        return;
    }

    const date = new Date(room.expiresAt);

    if (Number.isNaN(date.getTime())) {

        expirationElement.textContent =
            `Действует до: ${room.expiresAt}`;

        return;
    }

    expirationElement.textContent =
        `Действует до: ${date.toLocaleString("ru-RU")}`;
}


// =========================
// Динамическое меню комнаты
// =========================

function updateRoomMenu(room) {

    const roomPasswordActions =
        document.getElementById("roomPasswordActions");

    const expirationActions =
        document.getElementById("expirationActions");

    roomPasswordActions.innerHTML = "";
    expirationActions.innerHTML = "";


    // Пароль комнаты

    const passwordButton =
        document.createElement("button");

    passwordButton.type = "button";

    passwordButton.textContent =
        room.passwordProtected
            ? "Изменить пароль комнаты"
            : "Добавить пароль комнаты";

    passwordButton.addEventListener(
        "click",
        function () {

            roomMenu.style.display = "none";

            showRoomPasswordModal();
        }
    );

    roomPasswordActions.appendChild(passwordButton);


    // Удаление пароля комнаты

    if (room.passwordProtected) {

        const removePasswordButton =
            document.createElement("button");

        removePasswordButton.type = "button";

        removePasswordButton.textContent =
            "Удалить пароль комнаты";

        removePasswordButton.addEventListener(
            "click",
            function () {

                roomMenu.style.display = "none";

                showRemoveRoomPasswordModal();
            }
        );

        roomPasswordActions.appendChild(
            removePasswordButton
        );
    }


    // Срок действия

    const expirationButton =
        document.createElement("button");

    expirationButton.type = "button";

    expirationButton.textContent =
        room.expiresAt === null
            ? "Добавить срок действия"
            : "Изменить срок действия";

    expirationButton.addEventListener(
        "click",
        function () {

            roomMenu.style.display = "none";

            showExpirationModal();
        }
    );

    expirationActions.appendChild(
        expirationButton
    );


    // Сделать бессрочной

    if (room.expiresAt !== null) {

        const removeExpirationButton =
            document.createElement("button");

        removeExpirationButton.type = "button";

        removeExpirationButton.textContent =
            "Сделать бессрочной";

        removeExpirationButton.addEventListener(
            "click",
            function () {

                roomMenu.style.display = "none";

                removeExpiration();
            }
        );

        expirationActions.appendChild(
            removeExpirationButton
        );
    }
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


    const items =
        await response.json();

    const itemsContainer =
        document.getElementById("items");

    itemsContainer.innerHTML = "";


    items.forEach(function (item) {

        const card =
            document.createElement("div");

        card.classList.add("item-card");


        // TEXT

        if (item.type === "TEXT") {

            const text =
                document.createElement("p");

            text.textContent =
                item.text;

            card.appendChild(text);
        }


        // FILE

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


        // DELETE

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

                    alert(
                        "Не удалось удалить item"
                    );

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
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        password: password
                    })
                }
            );


        if (response.status === 403) {

            showPasswordError(
                "Неверный пароль"
            );

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
                        "Content-Type":
                            "application/json"
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


            alert(
                "Не удалось добавить текст"
            );

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

        formData.append(
            "file",
            file
        );


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

                alert(
                    "Не удалось загрузить файл"
                );
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


// =========================
// Переименование комнаты
// =========================

const renameRoomButton =
    document.getElementById("renameRoomButton");

const renameRoomModal =
    document.getElementById("renameRoomModal");

const renameRoomForm =
    document.getElementById("renameRoomForm");

const cancelRenameRoomButton =
    document.getElementById("cancelRenameRoomButton");

const newRoomNameInput =
    document.getElementById("newRoomNameInput");

const renameOwnerPasswordInput =
    document.getElementById(
        "renameOwnerPasswordInput"
    );

const renameRoomError =
    document.getElementById(
        "renameRoomError"
    );


function showRenameRoomModal() {

    renameRoomError.textContent = "";

    renameRoomError.style.display =
        "none";

    newRoomNameInput.value = "";

    renameOwnerPasswordInput.value = "";

    renameRoomModal.style.display =
        "flex";

    newRoomNameInput.focus();
}


function hideRenameRoomModal() {

    renameRoomModal.style.display =
        "none";

    newRoomNameInput.value = "";

    renameOwnerPasswordInput.value = "";

    renameRoomError.textContent = "";

    renameRoomError.style.display =
        "none";
}


renameRoomButton.addEventListener(
    "click",
    function () {

        roomMenu.style.display = "none";

        showRenameRoomModal();
    }
);


cancelRenameRoomButton.addEventListener(
    "click",
    function () {

        hideRenameRoomModal();
    }
);


renameRoomForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        renameRoomError.textContent = "";

        renameRoomError.style.display =
            "none";


        const newName =
            newRoomNameInput.value;

        const ownerPassword =
            renameOwnerPasswordInput.value;


        const response =
            await fetch(
                `/rooms/${roomKey}/name`,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        ownerPassword:
                            ownerPassword,

                        newName:
                            newName
                    })
                }
            );


        if (response.status === 403) {

            renameRoomError.textContent =
                "Неверный пароль владельца";

            renameRoomError.style.display =
                "block";

            renameOwnerPasswordInput.focus();

            return;
        }


        if (response.status === 404) {

            hideRenameRoomModal();

            showRoomError(
                "Комната не найдена"
            );

            return;
        }


        if (response.status === 410) {

            hideRenameRoomModal();

            showRoomError(
                "Срок действия комнаты истёк"
            );

            return;
        }


        if (!response.ok) {

            renameRoomError.textContent =
                "Не удалось переименовать комнату";

            renameRoomError.style.display =
                "block";

            return;
        }


        document.getElementById(
            "roomName"
        ).textContent = newName;


        hideRenameRoomModal();
    }
);


// Закрытие меню при клике вне него

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
// Пароль комнаты
// =========================

const roomPasswordModal =
    document.getElementById(
        "roomPasswordModal"
    );

const roomPasswordForm =
    document.getElementById(
        "roomPasswordForm"
    );

const roomPasswordModalTitle =
    document.getElementById(
        "roomPasswordModalTitle"
    );

const roomPasswordModalDescription =
    document.getElementById(
        "roomPasswordModalDescription"
    );

const roomPasswordOwnerInput =
    document.getElementById(
        "roomPasswordOwnerInput"
    );

const newRoomPasswordInput =
    document.getElementById(
        "newRoomPasswordInput"
    );

const cancelRoomPasswordButton =
    document.getElementById(
        "cancelRoomPasswordButton"
    );

const roomPasswordError =
    document.getElementById(
        "roomPasswordError"
    );


function showRoomPasswordModal() {

    roomPasswordError.textContent = "";

    roomPasswordError.style.display =
        "none";

    roomPasswordOwnerInput.value = "";

    newRoomPasswordInput.value = "";


    if (currentRoom.passwordProtected) {

        roomPasswordModalTitle.textContent =
            "Изменить пароль комнаты";

    } else {

        roomPasswordModalTitle.textContent =
            "Добавить пароль комнаты";
    }


    roomPasswordModalDescription.textContent =
        "Введите пароль владельца и новый пароль комнаты.";


    roomPasswordModal.style.display =
        "flex";

    roomPasswordOwnerInput.focus();
}


function hideRoomPasswordModal() {

    roomPasswordModal.style.display =
        "none";

    roomPasswordOwnerInput.value = "";

    newRoomPasswordInput.value = "";

    roomPasswordError.textContent = "";

    roomPasswordError.style.display =
        "none";
}


cancelRoomPasswordButton.addEventListener(
    "click",
    hideRoomPasswordModal
);


roomPasswordForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const ownerPassword =
            roomPasswordOwnerInput.value;

        const newPassword =
            newRoomPasswordInput.value;


        roomPasswordError.textContent = "";

        roomPasswordError.style.display =
            "none";


        if (newPassword.trim() === "") {

            roomPasswordError.textContent =
                "Введите новый пароль комнаты";

            roomPasswordError.style.display =
                "block";

            newRoomPasswordInput.focus();

            return;
        }


        const response =
            await fetch(
                `/rooms/${roomKey}/password`,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        ownerPassword:
                            ownerPassword,

                        newPassword:
                            newPassword
                    })
                }
            );


        if (response.status === 403) {

            roomPasswordError.textContent =
                "Неверный пароль владельца";

            roomPasswordError.style.display =
                "block";

            roomPasswordOwnerInput.focus();

            return;
        }


        if (response.status === 404) {

            hideRoomPasswordModal();

            showRoomError(
                "Комната не найдена"
            );

            return;
        }


        if (response.status === 410) {

            hideRoomPasswordModal();

            showRoomError(
                "Срок действия комнаты истёк"
            );

            return;
        }


        if (!response.ok) {

            roomPasswordError.textContent =
                "Не удалось изменить пароль комнаты";

            roomPasswordError.style.display =
                "block";

            return;
        }


        currentRoom.passwordProtected =
            true;

        hideRoomPasswordModal();

        updateRoomMenu(currentRoom);
    }
);


// =========================
// Удаление пароля комнаты
// =========================

const removeRoomPasswordModal =
    document.getElementById(
        "removeRoomPasswordModal"
    );

const removeRoomPasswordForm =
    document.getElementById(
        "removeRoomPasswordForm"
    );

const removeRoomPasswordOwnerInput =
    document.getElementById(
        "removeRoomPasswordOwnerInput"
    );

const cancelRemoveRoomPasswordButton =
    document.getElementById(
        "cancelRemoveRoomPasswordButton"
    );

const removeRoomPasswordError =
    document.getElementById(
        "removeRoomPasswordError"
    );


function showRemoveRoomPasswordModal() {

    removeRoomPasswordError.textContent = "";

    removeRoomPasswordError.style.display =
        "none";

    removeRoomPasswordOwnerInput.value =
        "";

    removeRoomPasswordModal.style.display =
        "flex";

    removeRoomPasswordOwnerInput.focus();
}


function hideRemoveRoomPasswordModal() {

    removeRoomPasswordModal.style.display =
        "none";

    removeRoomPasswordOwnerInput.value =
        "";

    removeRoomPasswordError.textContent =
        "";

    removeRoomPasswordError.style.display =
        "none";
}


cancelRemoveRoomPasswordButton.addEventListener(
    "click",
    hideRemoveRoomPasswordModal
);


removeRoomPasswordForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const response =
            await fetch(
                `/rooms/${roomKey}/password`,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        ownerPassword:
                            removeRoomPasswordOwnerInput.value,

                        newPassword:
                            null
                    })
                }
            );


        if (response.status === 403) {

            removeRoomPasswordError.textContent =
                "Неверный пароль владельца";

            removeRoomPasswordError.style.display =
                "block";

            removeRoomPasswordOwnerInput.focus();

            return;
        }


        if (response.status === 404) {

            hideRemoveRoomPasswordModal();

            showRoomError(
                "Комната не найдена"
            );

            return;
        }


        if (response.status === 410) {

            hideRemoveRoomPasswordModal();

            showRoomError(
                "Срок действия комнаты истёк"
            );

            return;
        }


        if (!response.ok) {

            removeRoomPasswordError.textContent =
                "Не удалось удалить пароль комнаты";

            removeRoomPasswordError.style.display =
                "block";

            return;
        }


        currentRoom.passwordProtected =
            false;

        hideRemoveRoomPasswordModal();

        updateRoomMenu(currentRoom);

        await loadItems();
    }
);


// =========================
// Пароль владельца
// =========================

const changeOwnerPasswordButton =
    document.getElementById(
        "changeOwnerPasswordButton"
    );

const ownerPasswordModal =
    document.getElementById(
        "ownerPasswordModal"
    );

const ownerPasswordForm =
    document.getElementById(
        "ownerPasswordForm"
    );

const currentOwnerPasswordInput =
    document.getElementById(
        "currentOwnerPasswordInput"
    );

const newOwnerPasswordInput =
    document.getElementById(
        "newOwnerPasswordInput"
    );

const cancelOwnerPasswordButton =
    document.getElementById(
        "cancelOwnerPasswordButton"
    );

const ownerPasswordError =
    document.getElementById(
        "ownerPasswordError"
    );


function showOwnerPasswordModal() {

    ownerPasswordError.textContent = "";

    ownerPasswordError.style.display =
        "none";

    currentOwnerPasswordInput.value =
        "";

    newOwnerPasswordInput.value =
        "";

    ownerPasswordModal.style.display =
        "flex";

    currentOwnerPasswordInput.focus();
}


function hideOwnerPasswordModal() {

    ownerPasswordModal.style.display =
        "none";

    currentOwnerPasswordInput.value =
        "";

    newOwnerPasswordInput.value =
        "";

    ownerPasswordError.textContent =
        "";

    ownerPasswordError.style.display =
        "none";
}


changeOwnerPasswordButton.addEventListener(
    "click",
    function () {

        roomMenu.style.display = "none";

        showOwnerPasswordModal();
    }
);


cancelOwnerPasswordButton.addEventListener(
    "click",
    hideOwnerPasswordModal
);


ownerPasswordForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const response =
            await fetch(
                `/rooms/${roomKey}/owner-password`,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        ownerPassword:
                            currentOwnerPasswordInput.value,

                        newPassword:
                            newOwnerPasswordInput.value
                    })
                }
            );


        if (response.status === 403) {

            ownerPasswordError.textContent =
                "Неверный текущий пароль владельца";

            ownerPasswordError.style.display =
                "block";

            currentOwnerPasswordInput.focus();

            return;
        }


        if (response.status === 404) {

            hideOwnerPasswordModal();

            showRoomError(
                "Комната не найдена"
            );

            return;
        }


        if (response.status === 410) {

            hideOwnerPasswordModal();

            showRoomError(
                "Срок действия комнаты истёк"
            );

            return;
        }


        if (!response.ok) {

            ownerPasswordError.textContent =
                "Не удалось изменить пароль владельца";

            ownerPasswordError.style.display =
                "block";

            return;
        }


        hideOwnerPasswordModal();

        alert("Пароль владельца изменён");
    }
);


// =========================
// Срок действия комнаты
// =========================

const expirationModal =
    document.getElementById(
        "expirationModal"
    );

const expirationForm =
    document.getElementById(
        "expirationForm"
    );

const expirationInput =
    document.getElementById(
        "expirationInput"
    );

const expirationOwnerPasswordInput =
    document.getElementById(
        "expirationOwnerPasswordInput"
    );

const cancelExpirationButton =
    document.getElementById(
        "cancelExpirationButton"
    );

const expirationError =
    document.getElementById(
        "expirationError"
    );


function showExpirationModal() {

    expirationError.textContent = "";

    expirationError.style.display =
        "none";

    expirationOwnerPasswordInput.value =
        "";


    if (currentRoom.expiresAt !== null) {

        const date =
            new Date(currentRoom.expiresAt);


        if (!Number.isNaN(date.getTime())) {

            expirationInput.value =
                toDateTimeLocalValue(date);

        } else {

            expirationInput.value = "";
        }

    } else {

        expirationInput.value = "";
    }


    expirationModal.style.display =
        "flex";

    expirationInput.focus();
}


function hideExpirationModal() {

    expirationModal.style.display =
        "none";

    expirationInput.value = "";

    expirationOwnerPasswordInput.value =
        "";

    expirationError.textContent = "";

    expirationError.style.display =
        "none";
}


function toDateTimeLocalValue(date) {

    const year =
        date.getFullYear();

    const month =
        String(date.getMonth() + 1)
            .padStart(2, "0");

    const day =
        String(date.getDate())
            .padStart(2, "0");

    const hours =
        String(date.getHours())
            .padStart(2, "0");

    const minutes =
        String(date.getMinutes())
            .padStart(2, "0");


    return `${year}-${month}-${day}T${hours}:${minutes}`;
}


cancelExpirationButton.addEventListener(
    "click",
    hideExpirationModal
);


expirationForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        expirationError.textContent = "";

        expirationError.style.display =
            "none";


        if (!expirationInput.value) {

            expirationError.textContent =
                "Выберите дату и время";

            expirationError.style.display =
                "block";

            expirationInput.focus();

            return;
        }


        const selectedDate =
            new Date(expirationInput.value);


        if (Number.isNaN(
            selectedDate.getTime()
        )) {

            expirationError.textContent =
                "Некорректная дата";

            expirationError.style.display =
                "block";

            return;
        }


        if (selectedDate <= new Date()) {

            expirationError.textContent =
                "Дата окончания должна быть в будущем";

            expirationError.style.display =
                "block";

            return;
        }


        /*
         * ВАЖНО:
         *
         * Backend использует LocalDateTime.
         * Поэтому не используем toISOString(),
         * иначе JavaScript переведёт время в UTC.
         *
         * datetime-local уже содержит нужное
         * локальное время.
         */

        const expiresAt =
            expirationInput.value + ":00";


        const response =
            await fetch(
                `/rooms/${roomKey}/expiration`,
                {
                    method: "PATCH",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        ownerPassword:
                            expirationOwnerPasswordInput.value,

                        expiresAt:
                            expiresAt
                    })
                }
            );


        if (response.status === 403) {

            expirationError.textContent =
                "Неверный пароль владельца";

            expirationError.style.display =
                "block";

            expirationOwnerPasswordInput.focus();

            return;
        }


        if (response.status === 404) {

            hideExpirationModal();

            showRoomError(
                "Комната не найдена"
            );

            return;
        }


        if (response.status === 410) {

            hideExpirationModal();

            showRoomError(
                "Срок действия комнаты истёк"
            );

            return;
        }


        if (!response.ok) {

            expirationError.textContent =
                "Не удалось изменить срок действия";

            expirationError.style.display =
                "block";

            return;
        }


        currentRoom.expiresAt =
            expiresAt;

        hideExpirationModal();

        updateExpirationText(
            currentRoom
        );

        updateRoomMenu(
            currentRoom
        );
    }
);


// =========================
// Удаление срока действия
// =========================

async function removeExpiration() {

    const ownerPassword =
        window.prompt(
            "Введите пароль владельца:"
        );


    if (ownerPassword === null) {
        return;
    }


    const response =
        await fetch(
            `/rooms/${roomKey}/expiration`,
            {
                method: "PATCH",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({
                    ownerPassword:
                        ownerPassword,

                    expiresAt:
                        null
                })
            }
        );


    if (response.status === 403) {

        alert(
            "Неверный пароль владельца"
        );

        return;
    }


    if (response.status === 404) {

        showRoomError(
            "Комната не найдена"
        );

        return;
    }


    if (response.status === 410) {

        showRoomError(
            "Срок действия комнаты истёк"
        );

        return;
    }


    if (!response.ok) {

        alert(
            "Не удалось изменить срок действия"
        );

        return;
    }


    currentRoom.expiresAt = null;

    updateExpirationText(
        currentRoom
    );

    updateRoomMenu(
        currentRoom
    );
}


// =========================
// Удаление комнаты
// =========================

const deleteRoomButton =
    document.getElementById(
        "deleteRoomButton"
    );

const deleteRoomModal =
    document.getElementById(
        "deleteRoomModal"
    );

const deleteRoomForm =
    document.getElementById(
        "deleteRoomForm"
    );

const cancelDeleteRoomButton =
    document.getElementById(
        "cancelDeleteRoomButton"
    );

const ownerPasswordInput =
    document.getElementById(
        "ownerPasswordInput"
    );

const deleteRoomError =
    document.getElementById(
        "deleteRoomError"
    );


function showDeleteRoomModal() {

    deleteRoomError.textContent = "";

    deleteRoomError.style.display =
        "none";

    ownerPasswordInput.value = "";

    deleteRoomModal.style.display =
        "flex";

    ownerPasswordInput.focus();
}


function hideDeleteRoomModal() {

    deleteRoomModal.style.display =
        "none";

    ownerPasswordInput.value = "";

    deleteRoomError.textContent = "";

    deleteRoomError.style.display =
        "none";
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

        deleteRoomError.style.display =
            "none";


        const ownerPassword =
            ownerPasswordInput.value;


        const response =
            await fetch(
                `/rooms/${roomKey}`,
                {
                    method: "DELETE",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        password:
                            ownerPassword
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

            showRoomError(
                "Комната не найдена"
            );

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