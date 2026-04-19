DROP TABLE user_pref;

ALTER TABLE user_preferences ADD COLUMN created_at DATE DEFAULT (CURRENT_DATE);
ALTER TABLE user_preferences ADD CONSTRAINT make_pref_strict CHECK (pref_trait IN ('BREED', 'GENDER', 'AGE_MIN', 'AGE_MAX', 'SIZE'));