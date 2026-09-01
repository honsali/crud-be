ALTER TABLE account ADD COLUMN password_hash VARCHAR(500);

UPDATE account a
SET password_hash = c.password_hash
FROM account_credential c
WHERE c.account_id = a.id;

ALTER TABLE account ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE account RENAME COLUMN active TO activated;
ALTER TABLE account DROP COLUMN display_name;
ALTER TABLE account DROP COLUMN email;

DROP TABLE account_credential;
