CREATE TABLE buildings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    estate_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6)
);

CREATE TABLE staircases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL
);

CREATE TABLE apartments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staircase_id UUID NOT NULL REFERENCES staircases(id) ON DELETE CASCADE,
    number VARCHAR(50) NOT NULL,
    current_balance DECIMAL(12,2) DEFAULT 0.00
);

CREATE TABLE meter_readings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apartment_id UUID NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    meter_type VARCHAR(100) NOT NULL,
    value DECIMAL(12,4) NOT NULL,
    reading_date DATE NOT NULL
);

CREATE TABLE user_apartments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    apartment_id UUID NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    UNIQUE (user_id, apartment_id)
);

CREATE TABLE ticket_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_number VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'NOWE',
    category_id UUID NOT NULL REFERENCES ticket_categories(id),
    author_id UUID NOT NULL REFERENCES users(id),
    assigned_to_id UUID REFERENCES users(id),
    apartment_id UUID REFERENCES apartments(id) ON DELETE SET NULL,
    staircase_id UUID REFERENCES staircases(id) ON DELETE SET NULL,
    building_id UUID REFERENCES buildings(id) ON DELETE SET NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    CHECK (
        (CASE WHEN apartment_id IS NOT NULL THEN 1 ELSE 0 END) + 
        (CASE WHEN staircase_id IS NOT NULL THEN 1 ELSE 0 END) + 
        (CASE WHEN building_id IS NOT NULL THEN 1 ELSE 0 END) <= 1
    )
);

CREATE TABLE ticket_media (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    file_url VARCHAR(255) NOT NULL,
    media_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL,
    changed_by UUID NOT NULL REFERENCES users(id),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE announcements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) DEFAULT 'OGLOSZENIE',
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id UUID NOT NULL REFERENCES users(id),
    target_building_id UUID REFERENCES buildings(id) ON DELETE CASCADE,
    target_staircase_id UUID REFERENCES staircases(id) ON DELETE CASCADE,
    target_apartment_id UUID REFERENCES apartments(id) ON DELETE CASCADE,
    planned_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (
        (CASE WHEN target_building_id IS NOT NULL THEN 1 ELSE 0 END) + 
        (CASE WHEN target_staircase_id IS NOT NULL THEN 1 ELSE 0 END) + 
        (CASE WHEN target_apartment_id IS NOT NULL THEN 1 ELSE 0 END) <= 1
    )
);

CREATE TABLE resolutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    author_id UUID NOT NULL REFERENCES users(id),
    end_date TIMESTAMP NOT NULL
);

CREATE TABLE resolution_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resolution_id UUID NOT NULL REFERENCES resolutions(id) ON DELETE CASCADE,
    option_text VARCHAR(255) NOT NULL,
    UNIQUE (id, resolution_id)
);

CREATE TABLE resolution_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resolution_id UUID NOT NULL,
    option_id UUID NOT NULL,
    voter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    voted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (option_id, resolution_id) REFERENCES resolution_options(id, resolution_id) ON DELETE CASCADE,
    UNIQUE (resolution_id, voter_id)
);

CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apartment_id UUID NOT NULL REFERENCES apartments(id) ON DELETE RESTRICT,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description TEXT NOT NULL,
    transaction_date DATE NOT NULL,
    recorded_by_id UUID NOT NULL REFERENCES users(id)
);

CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    owner_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    apartment_id UUID REFERENCES apartments(id) ON DELETE SET NULL,
    ticket_id UUID REFERENCES tickets(id) ON DELETE SET NULL,
    resolution_id UUID REFERENCES resolutions(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
