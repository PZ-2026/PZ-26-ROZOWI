CREATE TABLE refresh_tokens (
    token_id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);
