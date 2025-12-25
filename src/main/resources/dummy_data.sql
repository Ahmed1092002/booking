-- =============================================
-- Dummy Data for Hotel Booking System (PostgreSQL)
-- =============================================

-- 1. Insert Users (Password is 'password' encoded with BCrypt)
-- Hash: $2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIZn.o9bfqwyb5uch.Obys.
INSERT INTO users (email, password, full_name) VALUES
('admin@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIZn.o9bfqwyb5uch.Obys.', 'System Admin'),
('seller1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIZn.o9bfqwyb5uch.Obys.', 'John Seller'),
('seller2@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIZn.o9bfqwyb5uch.Obys.', 'Sarah Host'),
('user1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIZn.o9bfqwyb5uch.Obys.', 'Alice Traveler'),
('user2@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIZn.o9bfqwyb5uch.Obys.', 'Bob Guest');

-- 2. Assign Roles
-- ID 1: ADMIN, ID 2-3: SELLER, ID 4-5: CUSTOMER (Assumed IDs based on insertion order)
INSERT INTO user_roles (user_id, role) VALUES
((SELECT id FROM users WHERE email = 'admin@example.com'), 'ROLE_ADMIN'),
((SELECT id FROM users WHERE email = 'seller1@example.com'), 'ROLE_SELLER'),
((SELECT id FROM users WHERE email = 'seller2@example.com'), 'ROLE_SELLER'),
((SELECT id FROM users WHERE email = 'user1@example.com'), 'ROLE_CUSTOMER'),
((SELECT id FROM users WHERE email = 'user2@example.com'), 'ROLE_CUSTOMER');

-- 3. Insert Hotels
INSERT INTO hotels (name, city, address, google_map_url, seller_id, average_rating) VALUES
('Grand Plaza Hotel', 'New York', '123 Broadway St, NY', 'https://maps.google.com/?q=Grand+Plaza+NY', (SELECT id FROM users WHERE email = 'seller1@example.com'), 4.5),
('Ocean View Resort', 'Miami', '456 Ocean Dr, FL', 'https://maps.google.com/?q=Ocean+View+Miami', (SELECT id FROM users WHERE email = 'seller1@example.com'), 4.8),
('Mountain Retreat', 'Denver', '789 Alpine Way, CO', 'https://maps.google.com/?q=Mountain+Retreat+CO', (SELECT id FROM users WHERE email = 'seller2@example.com'), 4.2),
('Urban Loft', 'San Francisco', '101 Tech Blvd, CA', 'https://maps.google.com/?q=Urban+Loft+SF', (SELECT id FROM users WHERE email = 'seller2@example.com'), 4.0);

-- 4. Insert Hotel Amenities
INSERT INTO hotel_amenities (hotel_id, amenity) VALUES
((SELECT id FROM hotels WHERE name = 'Grand Plaza Hotel'), 'WiFi'),
((SELECT id FROM hotels WHERE name = 'Grand Plaza Hotel'), 'Gym'),
((SELECT id FROM hotels WHERE name = 'Grand Plaza Hotel'), 'Restaurant'),
((SELECT id FROM hotels WHERE name = 'Ocean View Resort'), 'Pool'),
((SELECT id FROM hotels WHERE name = 'Ocean View Resort'), 'Spa'),
((SELECT id FROM hotels WHERE name = 'Ocean View Resort'), 'Beach Access'),
((SELECT id FROM hotels WHERE name = 'Mountain Retreat'), 'Skiing'),
((SELECT id FROM hotels WHERE name = 'Mountain Retreat'), 'Fireplace'),
((SELECT id FROM hotels WHERE name = 'Urban Loft'), 'WiFi'),
((SELECT id FROM hotels WHERE name = 'Urban Loft'), 'Workspace');

-- 5. Insert Rooms
INSERT INTO rooms (name, price_per_night, capacity, view_type, has_kitchen, is_available, hotel_id) VALUES
-- Grand Plaza
('Deluxe King', 250.00, 2, 'City View', false, true, (SELECT id FROM hotels WHERE name = 'Grand Plaza Hotel')),
('Executive Suite', 450.00, 4, 'City View', true, true, (SELECT id FROM hotels WHERE name = 'Grand Plaza Hotel')),
-- Ocean View
('Standard Room', 180.00, 2, 'Garden View', false, true, (SELECT id FROM hotels WHERE name = 'Ocean View Resort')),
('Ocean Suite', 500.00, 4, 'Ocean View', true, true, (SELECT id FROM hotels WHERE name = 'Ocean View Resort')),
-- Mountain Retreat
('Cozy Cabin', 300.00, 6, 'Mountain View', true, true, (SELECT id FROM hotels WHERE name = 'Mountain Retreat')),
-- Urban Loft
('Studio', 150.00, 2, 'Street View', true, true, (SELECT id FROM hotels WHERE name = 'Urban Loft'));

-- 6. Insert Bookings
INSERT INTO bookings (check_in_date, check_out_date, total_price, status, user_id, room_id) VALUES
('2025-01-10', '2025-01-15', 1250.00, 'CONFIRMED', (SELECT id FROM users WHERE email = 'user1@example.com'), (SELECT id FROM rooms WHERE name = 'Deluxe King')),
('2025-02-01', '2025-02-05', 2000.00, 'PENDING', (SELECT id FROM users WHERE email = 'user2@example.com'), (SELECT id FROM rooms WHERE name = 'Ocean Suite')),
('2024-12-01', '2024-12-05', 600.00, 'COMPLETED', (SELECT id FROM users WHERE email = 'user1@example.com'), (SELECT id FROM rooms WHERE name = 'Studio'));

-- 7. Insert Reviews
INSERT INTO reviews (rating, comment, created_at, hotel_id, user_id) VALUES
(5, 'Absolutely amazing stay!', NOW(), (SELECT id FROM hotels WHERE name = 'Grand Plaza Hotel'), (SELECT id FROM users WHERE email = 'user1@example.com')),
(4, 'Great location but a bit noisy.', NOW(), (SELECT id FROM hotels WHERE name = 'Urban Loft'), (SELECT id FROM users WHERE email = 'user1@example.com'));

-- 8. Insert Discount Codes
INSERT INTO discount_codes (code, type, discount_value, valid_from, valid_until, max_uses, current_uses, min_booking_amount, active) VALUES
('WELCOME2025', 'PERCENTAGE', 10.00, '2025-01-01', '2025-12-31', 1000, 0, 100.00, true),
('SUMMER50', 'FIXED', 50.00, '2025-06-01', '2025-08-31', 500, 0, 300.00, true);

-- 9. Insert Loyalty Points
INSERT INTO loyalty_points (user_id, total_points, available_points) VALUES
((SELECT id FROM users WHERE email = 'user1@example.com'), 500, 500),
((SELECT id FROM users WHERE email = 'user2@example.com'), 200, 200);

-- End of Dummy Data
