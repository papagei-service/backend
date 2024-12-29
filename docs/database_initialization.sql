-- Drop tables if exists
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS collections;
DROP TABLE IF EXISTS users;

-- Define sessions table
CREATE TABLE sessions (
    id VARCHAR(64) PRIMARY KEY,
    value TEXT NOT NULL
);

-- Define users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) UNIQUE,
    hashed_password VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL
);

-- Define collections table
CREATE TABLE collections (
	id BIGSERIAL PRIMARY KEY,
	title TEXT NOT NULL,
	description TEXT,
	native_language_iso_639_1 VARCHAR(2),
	foreign_language_iso_639_1 VARCHAR(2),
	owner_id BIGSERIAL NOT NULL,
	CONSTRAINT owner_id_fk FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Define cards table
CREATE TABLE cards (
	id BIGSERIAL PRIMARY KEY,
	native_language_value TEXT NOT NULL,
	native_language_value_description TEXT,
	native_language_value_example TEXT,
	foreign_language_value TEXT NOT NULL,
	foreign_language_value_description TEXT,
	foreign_language_value_example TEXT,
	next_time_at TIMESTAMP DEFAULT now() NOT NULL,
	correct_answers_in_row INT DEFAULT 0 NOT NULL,
	collection_id BIGINT NOT NULL,
	CONSTRAINT collection_id_fk FOREIGN KEY (collection_id) REFERENCES collections(id)
);