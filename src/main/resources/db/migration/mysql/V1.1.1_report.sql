CREATE TABLE zigg.report
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime              NOT NULL,
    updated_at      datetime              NOT NULL,
    report_message  VARCHAR(255)          NULL,
    report_specific VARCHAR(255)          NULL,
    report_type     ENUM                  NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE zigg.user_ignore_users
(
    user_user_id         BIGINT NOT NULL,
    ignore_users_user_id BIGINT NOT NULL
);

ALTER TABLE zigg.user_ignore_users
    ADD CONSTRAINT FKjolg5o0j9tuii3mrdmy2s9nky FOREIGN KEY (user_user_id) REFERENCES zigg.user (user_id) ON DELETE NO ACTION;

CREATE INDEX FKjolg5o0j9tuii3mrdmy2s9nky ON zigg.user_ignore_users (user_user_id);

ALTER TABLE zigg.user_ignore_users
    ADD CONSTRAINT FKknqlixj6ickv7d7ax01u89md7 FOREIGN KEY (ignore_users_user_id) REFERENCES zigg.user (user_id) ON DELETE NO ACTION;

CREATE INDEX FKknqlixj6ickv7d7ax01u89md7 ON zigg.user_ignore_users (ignore_users_user_id);
