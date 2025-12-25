-- Add booking management fields
ALTER TABLE bookings ADD COLUMN cancellation_reason TEXT;
ALTER TABLE bookings ADD COLUMN cancelled_at TIMESTAMP;
ALTER TABLE bookings ADD COLUMN modified_at TIMESTAMP;
ALTER TABLE bookings ADD COLUMN original_check_in DATE;
ALTER TABLE bookings ADD COLUMN original_check_out DATE;

-- Add index for modified bookings
CREATE INDEX idx_bookings_modified ON bookings(modified_at);
CREATE INDEX idx_bookings_cancelled ON bookings(cancelled_at);
