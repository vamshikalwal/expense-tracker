-- Expense Tracker Database Schema

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create cards table
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create banks table
CREATE TABLE IF NOT EXISTS banks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id_bank (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_name VARCHAR(255) NOT NULL,
    amount DOUBLE NOT NULL,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    payment_type ENUM('CARD', 'BANK') NOT NULL,
    card_id BIGINT,
    bank_id BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id_trans (user_id),
    INDEX idx_card_id (card_id),
    INDEX idx_bank_id (bank_id),
    INDEX idx_date (date),
    INDEX idx_category (category),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE SET NULL,
    FOREIGN KEY (bank_id) REFERENCES banks(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample Data (Optional - uncomment to insert)
/*
-- Insert sample user
INSERT INTO users (name, email, password) VALUES 
('John Doe', 'john@example.com', '$2a$10$slYQmyNdGzin7olVN3DOCOYygZH.0rHG46o8bgFfxqSv3xiBwXGyO'); -- password: password123

-- Insert sample cards
INSERT INTO cards (name, user_id) VALUES 
('Debit Card', 1),
('Credit Card', 1);

-- Insert sample banks
INSERT INTO banks (name, user_id) VALUES 
('SBI', 1),
('HDFC', 1);

-- Insert sample transactions
INSERT INTO transactions (payment_name, amount, date, category, payment_type, card_id, user_id) VALUES 
('Grocery', 50.00, CURDATE(), 'Food', 'CARD', 1, 1),
('Restaurant', 30.00, CURDATE(), 'Food', 'CARD', 2, 1),
('Uber', 15.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Transport', 'BANK', NULL, 1);
*/

-- Verify tables
SHOW TABLES;
