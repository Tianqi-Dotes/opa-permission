-- 1) Resource model
-- Purpose: Stores protected resources evaluated by ABAC policies.
-- CV/SALARY
-- BUCKET:A/B
-- resource_type:BUCKET,resource_name:A

CREATE TABLE IF NOT EXISTS test.resources (
                                         id                  BIGSERIAL PRIMARY KEY,
                                         resource_code       VARCHAR(256) NOT NULL UNIQUE,
                                         resource_type       VARCHAR(64) NOT NULL,
                                         resource_name       VARCHAR(256),
                                         owner               VARCHAR(128),
                                         status              VARCHAR(32) NOT NULL DEFAULT 'active',
                                         create_time         BIGINT NOT NULL,
                                         update_time         BIGINT NOT NULL
);

-- 2) Attribute model
-- Purpose: Central dictionary of ABAC attribute keys and types.
-- category:subject,NAME:TENANT,data_type:string
-- category:subject,NAME:ROLE,data_type:string
-- category:resource,NAME:TENANT,data_type:string
-- category:resource,NAME:ROLE,data_type:string
-- category:action,NAME:auth,data_type:string read/write
-- category:environment,NAME:env,data_type:string DEV/UAT
CREATE TABLE IF NOT EXISTS test.attribute_definitions (
                                                     id                  BIGSERIAL PRIMARY KEY,
                                                     name                VARCHAR(128) NOT NULL,
                                                     category            VARCHAR(32) NOT NULL,
                                                     data_type           VARCHAR(32) NOT NULL,
                                                     multi_valued        BOOLEAN NOT NULL DEFAULT FALSE,
                                                     description         TEXT,
                                                     create_time         BIGINT NOT NULL,
                                                     update_time         BIGINT NOT NULL,
                                                     CONSTRAINT ck_attribute_category
                                                         CHECK (category IN ('subject', 'resource', 'environment', 'action')),
                                                     CONSTRAINT ck_attribute_data_type
                                                         CHECK (data_type IN ('string', 'number', 'boolean', 'date', 'datetime', 'json'))
);

ALTER TABLE test.attribute_definitions
    DROP CONSTRAINT IF EXISTS attribute_definitions_name_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_attribute_category_name
    ON test.attribute_definitions (category, name);

-- Purpose: Stores concrete attribute values for subjects.
-- subject_id:1,attribute_id:1,value:dpaas
-- subject_id:1,attribute_id:2,value:admin
-- subject_id is expected to map to test.users.user_id; this relation is enforced by the application.
CREATE TABLE IF NOT EXISTS test.subject_attribute_values (
                                                        id                  BIGSERIAL PRIMARY KEY,
                                                        subject_id          BIGINT NOT NULL,
                                                        attribute_id        BIGINT NOT NULL,
                                                        value_text          TEXT,
                                                        value_json          JSONB,
                                                        valid_from          TIMESTAMPTZ,
                                                        valid_to            TIMESTAMPTZ,
                                                        create_time         BIGINT NOT NULL,
                                                        update_time         BIGINT NOT NULL,
                                                        CONSTRAINT ck_subject_attr_value_not_both_null
                                                            CHECK (value_text IS NOT NULL OR value_json IS NOT NULL),
                                                        CONSTRAINT ck_subject_attr_validity
                                                            CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to > valid_from)
);

CREATE INDEX IF NOT EXISTS idx_subject_attr_subject_attr
    ON test.subject_attribute_values (subject_id, attribute_id);

CREATE INDEX IF NOT EXISTS idx_subject_attr_validity
    ON test.subject_attribute_values (valid_from, valid_to);

-- Purpose: Stores concrete attribute values for resources.
-- resource_id:1,attribute_id:3,value:dpaas
-- resource_id:1,attribute_id:4,value:admin
CREATE TABLE IF NOT EXISTS test.resource_attribute_values (
                                                         id                  BIGSERIAL PRIMARY KEY,
                                                         resource_id         BIGINT NOT NULL,
                                                         attribute_id        BIGINT NOT NULL,
                                                         value_text          TEXT,
                                                         value_json          JSONB,
                                                         valid_from          TIMESTAMPTZ,
                                                         valid_to            TIMESTAMPTZ,
                                                         create_time         BIGINT NOT NULL,
                                                         update_time         BIGINT NOT NULL,
                                                         CONSTRAINT ck_resource_attr_value_not_both_null
                                                             CHECK (value_text IS NOT NULL OR value_json IS NOT NULL),
                                                         CONSTRAINT ck_resource_attr_validity
                                                             CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to > valid_from)
);

CREATE INDEX IF NOT EXISTS idx_resource_attr_resource_attr
    ON test.resource_attribute_values (resource_id, attribute_id);

CREATE INDEX IF NOT EXISTS idx_resource_attr_validity
    ON test.resource_attribute_values (valid_from, valid_to);

-- Optional: registry for environment context sources/rules
-- Purpose: Configures how runtime environment context is resolved.
-- Example row (environment_context_rules):
-- rule_code='ctx_env_from_header', context_key='env', source_type='request_header',
-- extractor_json='{"header":"x-env"}', default_value_json='"DEV"',
-- fail_strategy='use_default', priority=10, enabled=true
CREATE TABLE IF NOT EXISTS test.environment_context_rules (
                                                         id                  BIGSERIAL PRIMARY KEY,
                                                         rule_code           VARCHAR(128) NOT NULL UNIQUE,
                                                         context_key         VARCHAR(128) NOT NULL,
                                                         source_type         VARCHAR(64) NOT NULL,
                                                         scope_level         VARCHAR(32) NOT NULL DEFAULT 'global',
                                                         applies_scope_json  JSONB NOT NULL DEFAULT '{}'::JSONB,
                                                         extractor_json      JSONB NOT NULL DEFAULT '{}'::JSONB,
                                                         default_value_json  JSONB,
                                                         normalize_package   VARCHAR(256),
                                                         normalize_rule      VARCHAR(128),
                                                         fail_strategy       VARCHAR(32) NOT NULL DEFAULT 'use_default',
                                                         priority            INTEGER NOT NULL DEFAULT 100,
                                                         enabled             BOOLEAN NOT NULL DEFAULT TRUE,
                                                         description         TEXT,
                                                         create_time         BIGINT NOT NULL,
                                                         update_time         BIGINT NOT NULL,
                                                         CONSTRAINT ck_env_rule_source_type
                                                             CHECK (source_type IN ('request_header', 'jwt_claim', 'request_param', 'fixed', 'http', 'sql')),
                                                         CONSTRAINT ck_env_rule_scope_level
                                                             CHECK (scope_level IN ('global', 'tenant', 'resource')),
                                                         CONSTRAINT ck_env_rule_fail_strategy
                                                             CHECK (fail_strategy IN ('deny', 'use_default', 'skip'))
);

-- Actions are controlled values used by policies and the policy editor.
CREATE TABLE IF NOT EXISTS test.abac_actions (
    id              BIGSERIAL PRIMARY KEY,
    action_code     VARCHAR(128) NOT NULL UNIQUE,
    action_name     VARCHAR(256) NOT NULL,
    description     TEXT,
    status          VARCHAR(32) NOT NULL DEFAULT 'active',
    create_time     BIGINT NOT NULL,
    update_time     BIGINT NOT NULL,
    CONSTRAINT ck_abac_action_status
        CHECK (status IN ('active', 'disabled'))
);

-- The single configuration record contains the generated Rego file.
CREATE TABLE IF NOT EXISTS test.abac_policy_config (
    config_id               BIGINT PRIMARY KEY DEFAULT 1 CHECK (config_id = 1),
    config_name             VARCHAR(128) NOT NULL DEFAULT 'authz.rego',
    entrypoint              VARCHAR(256) NOT NULL DEFAULT 'authz/allow',
    combining_algorithm     VARCHAR(32) NOT NULL DEFAULT 'deny_overrides',
    generation_status       VARCHAR(32) NOT NULL DEFAULT 'draft',
    generated_rego          TEXT,
    content_checksum        VARCHAR(128),
    validation_report_json  JSONB,
    created_by              BIGINT,
    updated_by              BIGINT,
    generated_at            TIMESTAMPTZ,
    create_time             BIGINT NOT NULL,
    update_time             BIGINT NOT NULL,
    CONSTRAINT ck_abac_config_combining_algorithm
        CHECK (combining_algorithm IN ('deny_overrides', 'permit_overrides', 'first_applicable')),
    CONSTRAINT ck_abac_config_generation_status
        CHECK (generation_status IN ('draft', 'validated', 'generated')),
    CONSTRAINT ck_abac_config_generated_rego
        CHECK (generation_status <> 'generated' OR generated_rego IS NOT NULL)
);

INSERT INTO test.abac_policy_config (config_id)
VALUES (1)
ON CONFLICT (config_id) DO NOTHING;

-- The condition_json is a structured condition tree produced by the UI,
-- e.g. {"all":[{"attribute":"subject.department","op":"eq","value":"finance"}]}.
-- Generate Rego from this validated structure; do not concatenate user text as Rego.
CREATE TABLE IF NOT EXISTS test.abac_policy_rules (
    id                  BIGSERIAL PRIMARY KEY,
    config_id           BIGINT NOT NULL DEFAULT 1,
    rule_code           VARCHAR(128) NOT NULL,
    rule_name           VARCHAR(256) NOT NULL,
    effect              VARCHAR(16) NOT NULL,
    priority            INTEGER NOT NULL DEFAULT 100,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    condition_json      JSONB NOT NULL,
    description         TEXT,
    create_time         BIGINT NOT NULL,
    update_time         BIGINT NOT NULL,
    CONSTRAINT uq_abac_policy_rule_code
        UNIQUE (config_id, rule_code),
    CONSTRAINT ck_abac_policy_rule_effect
        CHECK (effect IN ('allow', 'deny')),
    CONSTRAINT ck_abac_policy_rule_condition
        CHECK (jsonb_typeof(condition_json) = 'object')
);

CREATE INDEX IF NOT EXISTS idx_abac_policy_rules_config_enabled_priority
    ON test.abac_policy_rules (config_id, enabled, priority);

-- Regression cases let the UI validate the current configuration.
CREATE TABLE IF NOT EXISTS test.abac_policy_test_cases (
    id                  BIGSERIAL PRIMARY KEY,
    config_id           BIGINT NOT NULL DEFAULT 1,
    case_name           VARCHAR(256) NOT NULL,
    input_json          JSONB NOT NULL,
    expected_decision   VARCHAR(16) NOT NULL,
    last_actual_result  JSONB,
    last_passed         BOOLEAN,
    last_run_at         TIMESTAMPTZ,
    created_by          BIGINT,
    create_time         BIGINT NOT NULL,
    update_time         BIGINT NOT NULL,
    CONSTRAINT uq_abac_policy_test_case
        UNIQUE (config_id, case_name),
    CONSTRAINT ck_abac_policy_test_decision
        CHECK (expected_decision IN ('allow', 'deny'))
);

-- Append-only administrative audit trail for policy edits and config generation.
CREATE TABLE IF NOT EXISTS test.abac_audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    actor_id        BIGINT,
    event_type      VARCHAR(64) NOT NULL,
    object_type     VARCHAR(64) NOT NULL,
    object_id       BIGINT,
    request_id      VARCHAR(128),
    before_json     JSONB,
    after_json      JSONB,
    create_time     BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_abac_audit_object_created
    ON test.abac_audit_logs (object_type, object_id, create_time DESC);