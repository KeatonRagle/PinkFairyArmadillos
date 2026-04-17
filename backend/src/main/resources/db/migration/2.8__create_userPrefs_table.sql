CREATE TABLE userPreferences (
	pref_id INT AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL,
    pref_trait VARCHAR(50) NOT NULL,
    pref_value VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

ALTER TABLE user MODIFY requested_contributor VARCHAR(5) NOT NULL;