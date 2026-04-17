CREATE TABLE pet_image (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    pet_id INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);