-- Validation seed data for Expense Tracker
-- Run after schema.sql on an empty database

INSERT INTO users (id, name, email, password, created_at, updated_at)
VALUES
    (1, 'John Doe', 'john@example.com', '$2a$10$slYQmyNdGzin7olVN3DOCOYygZH.0rHG46o8bgFfxqSv3xiBwXGyO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cards (id, name, user_id, created_at, updated_at)
VALUES
    (1, 'Debit Card', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Credit Card', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO banks (id, name, user_id, created_at, updated_at)
VALUES
    (1, 'SBI', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'HDFC', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO transactions (id, payment_name, amount, date, category, payment_type, card_id, bank_id, user_id, created_at, updated_at)
VALUES
    (1, 'Grocery', 50.00, '2026-09-01', 'Food', 'CARD', 1, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Restaurant', 30.00, '2026-09-03', 'Food', 'CARD', 2, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Uber', 15.00, '2026-09-05', 'Transport', 'BANK', NULL, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

