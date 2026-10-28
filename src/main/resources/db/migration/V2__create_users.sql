CREATE TABLE users (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         varchar(255) NOT NULL UNIQUE,
    password_hash varchar(100) NOT NULL,
    role          varchar(20)  NOT NULL DEFAULT 'USER'
                  CHECK (role IN ('USER', 'ADMIN')),
    created_at    timestamptz  NOT NULL DEFAULT now()
);