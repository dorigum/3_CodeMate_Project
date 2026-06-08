CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    main_tech_stack VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
);

CREATE TABLE study (
    id BIGINT NOT NULL AUTO_INCREMENT,
    host_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content CLOB NOT NULL,
    category VARCHAR(20) NOT NULL,
    meeting_type VARCHAR(20) NOT NULL,
    location VARCHAR(100),
    max_member_count INTEGER NOT NULL,
    current_member_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_study PRIMARY KEY (id),
    CONSTRAINT fk_study_host FOREIGN KEY (host_id) REFERENCES users (id)
);

CREATE TABLE tech_stacks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_tech_stacks PRIMARY KEY (id),
    CONSTRAINT uk_tech_stacks_name UNIQUE (name)
);

CREATE TABLE study_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    study_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_study_members PRIMARY KEY (id),
    CONSTRAINT uk_study_members_study_user UNIQUE (study_id, user_id),
    CONSTRAINT fk_study_members_study FOREIGN KEY (study_id) REFERENCES study (id),
    CONSTRAINT fk_study_members_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE study_tech_stacks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    study_id BIGINT NOT NULL,
    tech_stack_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_study_tech_stacks PRIMARY KEY (id),
    CONSTRAINT uk_study_tech_stacks_study_tech UNIQUE (study_id, tech_stack_id),
    CONSTRAINT fk_study_tech_stacks_study FOREIGN KEY (study_id) REFERENCES study (id),
    CONSTRAINT fk_study_tech_stacks_tech FOREIGN KEY (tech_stack_id) REFERENCES tech_stacks (id)
);

CREATE INDEX idx_study_host_id ON study (host_id);
CREATE INDEX idx_study_members_user_id ON study_members (user_id);
CREATE INDEX idx_study_tech_stacks_tech_stack_id ON study_tech_stacks (tech_stack_id);
