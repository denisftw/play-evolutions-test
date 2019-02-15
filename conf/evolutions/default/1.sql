# --- !Ups

CREATE TABLE users
(
    user_id UUID PRIMARY KEY,
    user_code VARCHAR(250) NOT NULL,
    password VARCHAR(250) NOT NULL
);

INSERT INTO users VALUES ('e62742bf-858f-4d9d-8dee-e1ecde5d0a2e', 'stest', '$2a$10$niF.amAexQMHaevqlkganeSjvMHfTq/OdISyj8/5BQy1FHvlbi3Ne');

# --- !Downs

DROP TABLE users;
