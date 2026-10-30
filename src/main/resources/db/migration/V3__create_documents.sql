CREATE TABLE documents (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id      uuid          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filename      varchar(255)  NOT NULL,
    content_type  varchar(100)  NOT NULL,
    size_bytes    bigint        NOT NULL,
    status        varchar(20)   NOT NULL DEFAULT 'UPLOADED'
                  CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY', 'FAILED')),
    error_message varchar(500),
    page_count    int,
    chunk_count   int,
    created_at    timestamptz   NOT NULL DEFAULT now()
);

CREATE INDEX documents_owner_idx ON documents (owner_id);

CREATE TABLE document_chunks (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id uuid        NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    owner_id    uuid        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    chunk_index int         NOT NULL,
    page        int,
    content     text        NOT NULL,
    UNIQUE (document_id, chunk_index)
);

CREATE INDEX document_chunks_owner_idx ON document_chunks (owner_id);