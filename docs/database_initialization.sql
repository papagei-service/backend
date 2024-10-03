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
    username VARCHAR(64) PRIMARY KEY,
    hashed_password VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL
);

-- Define collections table
CREATE TABLE collections (
	id BIGSERIAL PRIMARY KEY,
	title TEXT NOT NULL,
	description TEXT,
	subject_type VARCHAR(32) NOT NULL,
	subject_language VARCHAR(2),
	native_language VARCHAR(2),
	owner_username VARCHAR(64) NOT NULL,
	CONSTRAINT owner_username_fk FOREIGN KEY (owner_username) REFERENCES users(username)
);

-- Define cards table
CREATE TABLE cards (
	id BIGSERIAL PRIMARY KEY,
	front_title TEXT NOT NULL,
	front_description TEXT,
	front_example TEXT,
	back_title TEXT NOT NULL,
	back_description TEXT,
	back_example TEXT,
	next_time_at TIMESTAMP DEFAULT now() NOT NULL,
	current_interval_ms BIGINT NOT NULL,
	collection_id BIGINT NOT NULL,
	CONSTRAINT collection_id_fk FOREIGN KEY (collection_id) REFERENCES collections(id)
);