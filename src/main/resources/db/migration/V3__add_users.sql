CREATE TABLE app_users (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           username VARCHAR(100) NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,
                           enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT uk_app_users_username UNIQUE (username)
);

CREATE TABLE app_user_roles (
                                user_id BIGINT NOT NULL,
                                role_name VARCHAR(50) NOT NULL,
                                PRIMARY KEY (user_id, role_name),
                                CONSTRAINT fk_app_user_roles_user
                                    FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);


INSERT INTO app_users (username, password_hash, enabled)
VALUES ('admin', '$2a$12$npI8NOBL/hJVja7X/5Dizu6VkF2rrC/2KQcv8E1kQlZzXLRM0r7bq', TRUE);

INSERT INTO app_user_roles (user_id, role_name)
SELECT id, 'ROLE_ADMIN'
FROM app_users
WHERE username = 'admin';