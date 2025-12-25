-- Optimizing Search Performance with Indexes

-- Index for searching hotels by city (Used in searchHotels)
CREATE INDEX idx_hotels_city ON hotels(city);

-- Index for checking room availability overlaps (Used in createBooking)
CREATE INDEX idx_bookings_room_dates ON bookings(room_id, check_in_date, check_out_date);

-- Index for searching users by email (Used in Login/Register)
CREATE INDEX idx_users_email ON users(email);
