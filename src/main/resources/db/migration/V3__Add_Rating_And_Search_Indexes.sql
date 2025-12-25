-- Add rating fields to hotels table
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS average_rating DECIMAL(2,1) DEFAULT 0.0;
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS total_reviews INT DEFAULT 0;

-- Add indexes for search optimization
CREATE INDEX IF NOT EXISTS idx_hotels_city ON hotels(city);
CREATE INDEX IF NOT EXISTS idx_hotels_rating ON hotels(average_rating);
CREATE INDEX IF NOT EXISTS idx_rooms_price ON rooms(price_per_night);
