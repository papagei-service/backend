CREATE TABLE IF NOT EXISTS sessions (
    id VARCHAR(64) PRIMARY KEY,
    "value" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    hashed_password VARCHAR(128) NOT NULL,
    display_name VARCHAR(48) NOT NULL,
    salt VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS collections (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NULL,
    known_language_iso_639_1 VARCHAR(2) NULL,
    learning_language_iso_639_1 VARCHAR(2) NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_collections_owner_id
        FOREIGN KEY (owner_id)
            REFERENCES users(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    known_language_text TEXT NOT NULL,
    learning_language_text TEXT NOT NULL,
    notes TEXT NOT NULL,
    last_answered_at TIMESTAMP NULL,
    show_next_time_at TIMESTAMP NULL,
    correct_answers_in_row INT DEFAULT 0 NOT NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_cards_owner_id
        FOREIGN KEY (owner_id)
            REFERENCES users(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS examples (
    id BIGSERIAL PRIMARY KEY,
    known_language_text TEXT NOT NULL,
    learning_language_text TEXT NOT NULL,
    card_id BIGINT NOT NULL,
    CONSTRAINT fk_examples_card_id
        FOREIGN KEY (card_id)
            REFERENCES cards(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS collections_cards (
    id BIGSERIAL PRIMARY KEY,
    collection_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    CONSTRAINT fk_collections_cards_collection_id
        FOREIGN KEY (collection_id)
            REFERENCES collections(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_collections_cards_card_id
        FOREIGN KEY (card_id)
            REFERENCES cards(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);