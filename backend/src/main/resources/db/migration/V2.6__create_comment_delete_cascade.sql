ALTER TABLE comments DROP FOREIGN KEY fk_post_id;

ALTER TABLE comments 
ADD CONSTRAINT fk_post_id 
FOREIGN KEY (post_id) 
REFERENCES posts(post_id) 
ON DELETE CASCADE;