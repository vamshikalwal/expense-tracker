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

INSERT INTO budgets (id, category, monthly_limit, budget_month, user_id, created_at, updated_at)
VALUES
    (1, 'Food', 250.00, '2026-09-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Transport', 120.00, '2026-09-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Entertainment', 100.00, '2026-09-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Bills', 300.00, '2026-09-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Food', 220.00, '2026-08-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'Transport', 100.00, '2026-08-01', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO transactions (id, payment_name, amount, date, category, payment_type, card_id, bank_id, user_id, created_at, updated_at)
VALUES
    (1, 'Grocery', 50.00, '2026-09-01', 'Food', 'CARD', 1, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Restaurant', 30.00, '2026-09-03', 'Food', 'CARD', 2, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Uber', 15.00, '2026-09-05', 'Transport', 'BANK', NULL, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Movie', 40.00, '2026-09-07', 'Entertainment', 'CARD', 2, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'Electricity Bill', 120.00, '2026-09-10', 'Bills', 'BANK', NULL, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 'Coffee', 5.50, '2026-09-12', 'Food', 'CARD', 1, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'Train', 2.75, '2026-08-15', 'Transport', 'CARD', 1, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'Grocery', 60.00, '2026-08-05', 'Food', 'CARD', 2, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'Concert', 80.00, '2026-08-20', 'Entertainment', 'CARD', 2, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'Internet Bill', 50.00, '2026-08-25', 'Bills', 'BANK', NULL, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (11, 'Taxi', 12.00, '2026-09-14', 'Transport', 'CARD', 2, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (12, 'Snack', 8.25, '2026-09-15', 'Food', 'CARD', 1, NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

