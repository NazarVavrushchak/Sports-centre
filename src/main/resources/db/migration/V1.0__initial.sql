CREATE TABLE users
(
    user_id    BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL
);

CREATE TABLE training_types
(
    training_type_id   BIGSERIAL PRIMARY KEY,
    training_type_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE trainers
(
    user_id           BIGINT PRIMARY KEY,
    specialization_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (specialization_id) REFERENCES training_types (training_type_id) ON DELETE CASCADE
);

CREATE TABLE trainees
(
    user_id       BIGINT PRIMARY KEY,
    date_of_birth DATE,
    address       VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE trainings
(
    training_id       BIGSERIAL PRIMARY KEY,
    trainee_id        BIGINT       NOT NULL,
    trainer_id        BIGINT       NOT NULL,
    training_type_id  BIGINT       NOT NULL,
    training_name     VARCHAR(255) NOT NULL,
    training_date     DATE         NOT NULL,
    training_duration INT          NOT NULL,
    FOREIGN KEY (trainee_id) REFERENCES trainees (user_id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers (user_id) ON DELETE CASCADE,
    FOREIGN KEY (training_type_id) REFERENCES training_types (training_type_id) ON DELETE CASCADE
);

CREATE TABLE trainee_trainer
(
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    PRIMARY KEY (trainee_id, trainer_id),
    FOREIGN KEY (trainee_id) REFERENCES trainees (user_id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers (user_id) ON DELETE CASCADE
);