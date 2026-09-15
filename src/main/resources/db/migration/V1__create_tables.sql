CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    room_key VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    password_hash VARCHAR(255),
    visibility VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    text TEXT,
    file_name VARCHAR(255),
    file_path VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_items_room
        FOREIGN KEY (room_id)
        REFERENCES rooms(id)
        ON DELETE CASCADE
);