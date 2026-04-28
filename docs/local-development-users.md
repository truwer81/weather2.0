For manual local testing, example non-admin users can be inserted into the local database.

- `user1` / `User1Test!`
- `user2` / `User2Test!`

These accounts are intended for local development only and should not be used in shared or production environments.

```sql
INSERT INTO app_users (username, password_hash, enabled)
SELECT 'user1', '$2a$12$XWfc2p14XqQe8Fct.O8P2.1/yILxu1vEkoQ76O1TXpX.WkNjDZFGW', TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'user1');

INSERT INTO app_users (username, password_hash, enabled)
SELECT 'user2', '$2a$12$4tRbPusFkyB8gIzUQf8vxujElAG4cvFfNIPm5yzXNbOjPgpOC5d3q', TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'user2');
```