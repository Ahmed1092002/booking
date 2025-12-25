-- Create hotel_images table
CREATE TABLE hotel_images (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);

-- Create room_images table
CREATE TABLE room_images (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_hotel_images_hotel ON hotel_images(hotel_id);
CREATE INDEX idx_hotel_images_primary ON hotel_images(hotel_id, is_primary);
CREATE INDEX idx_room_images_room ON room_images(room_id);
CREATE INDEX idx_room_images_primary ON room_images(room_id, is_primary);
