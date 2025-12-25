-- Create blocked_dates table
CREATE TABLE blocked_dates (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(50) NOT NULL,
    notes TEXT,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- Create seasonal_pricing table
CREATE TABLE seasonal_pricing (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    season_name VARCHAR(100),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_blocked_dates_room ON blocked_dates(room_id);
CREATE INDEX idx_blocked_dates_dates ON blocked_dates(start_date, end_date);
CREATE INDEX idx_seasonal_pricing_room ON seasonal_pricing(room_id);
CREATE INDEX idx_seasonal_pricing_dates ON seasonal_pricing(start_date, end_date);
