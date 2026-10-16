CREATE TABLE chart_snapshots (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    calculation_settings JSONB NOT NULL,
    chart_data JSONB NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_chart_snapshots_profile UNIQUE (profile_id),
    CONSTRAINT fk_chart_snapshots_profile FOREIGN KEY (profile_id) REFERENCES birth_profiles (id) ON DELETE CASCADE
);
