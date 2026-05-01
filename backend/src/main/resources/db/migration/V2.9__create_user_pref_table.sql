CREATE TABLE user_pref(
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER NOT NULL REFERENCES user(user_id) ON DELETE CASCADE,
    preference_type     VARCHAR(20) NOT NULL CHECK (preference_type IN ('BREED', 'GENDER', 'AGE_MIN', 'AGE_MAX', 'SIZE')),
    value               VARCHAR(100) NOT NULL,
    created_at          DATE DEFAULT (CURRENT_DATE)
)