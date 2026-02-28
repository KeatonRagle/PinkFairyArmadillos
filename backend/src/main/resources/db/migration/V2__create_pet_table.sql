CREATE TABLE adoptionSite (
	site_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255) NOT NULL UNIQUE,
    rating DOUBLE PRECISION,
    location VARCHAR(100)
);

CREATE TABLE pet (
	pet_id INT AUTO_INCREMENT PRIMARY KEY,
    site_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    breed VARCHAR(100) NOT NULL,
    age VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    pet_type VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    pet_status VARCHAR(50) NOT NULL,
    compatibility_score INT,
    FOREIGN KEY (site_id) REFERENCES adoptionSite(site_id)
);

CREATE TABLE featuredPets (
	pet_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

CREATE TABLE petRating (
	rating_id DOUBLE PRECISION AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    pet_id INT NOT NULL,
    rating_date DATE NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (pet_id) REFERENCES pet(pet_id)
);

CREATE TABLE reviews (
	review_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    site_id INT NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    rw_comment TEXT NOT NULL,
    rw_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (site_id) REFERENCES adoptionSite(site_id)
);

CREATE TABLE comments (
	comment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    ct_date DATE NOT NULL,
    ct_comment TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE submissions (
	submission_id INT AUTO_INCREMENT PRIMARY KEY,
    contributor_id INT NOT NULL,
    sb_date DATE NOT NULL,
    pet_info VARCHAR(255) NOT NULL,
    pet_status VARCHAR(50) NOT NULL,
    FOREIGN KEY (contributor_id) REFERENCES user(user_id)
);