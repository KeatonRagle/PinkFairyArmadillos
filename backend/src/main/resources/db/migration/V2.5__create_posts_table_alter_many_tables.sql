CREATE TABLE posts (
	post_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_date DATETIME NOT NULL,
    post_content TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

ALTER TABLE comments MODIFY ct_date DATETIME NOT NULL;
ALTER TABLE comments ADD post_id INT NOT NULL;
ALTER TABLE comments ADD CONSTRAINT fk_post_id FOREIGN KEY (post_id) REFERENCES posts(post_id);

ALTER TABLE adoption_site ADD user_id INT NOT NULL;
ALTER TABLE adoption_site ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user(user_id);
ALTER TABLE adoption_site ADD COLUMN submitted_at DATE NOT NULL;

ALTER TABLE user ADD COLUMN is_banned BOOLEAN NOT NULL;
ALTER TABLE user ADD COLUMN requested_contributor BOOLEAN NOT NULL;

ALTER TABLE pet ADD COLUMN created_at DATE NOT NULL;