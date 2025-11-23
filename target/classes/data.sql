-- Insert sample users (password is 'password123' hashed with BCrypt)
-- Using MERGE to avoid duplicate key errors on re-initialization
MERGE INTO users (id, username, email, password) KEY(username) VALUES
(1, 'john_trader', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JqMqZqZ8qZ8qZ8qZ8qZ8qZ8qZ8qZ'),
(2, 'jane_investor', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JqMqZqZ8qZ8qZ8qZ8qZ8qZ8qZ8qZ'),
(3, 'bob_daytrader', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JqMqZqZ8qZ8qZ8qZ8qZ8qZ8qZ8qZ'),
(4, 'alice_longterm', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JqMqZqZ8qZ8qZ8qZ8qZ8qZ8qZ8qZ'),
(5, 'charlie_swing', 'charlie@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8JqMqZqZ8qZ8qZ8qZ8qZ8qZ8qZ8qZ');

-- Insert sample accounts
MERGE INTO accounts (user_id, account_number, balance, account_type, status) KEY(account_number) VALUES
(1, 'ACC001', 50000.00, 'CASH', 'ACTIVE'),
(1, 'ACC002', 100000.00, 'MARGIN', 'ACTIVE'),
(2, 'ACC003', 75000.00, 'CASH', 'ACTIVE'),
(2, 'ACC004', 200000.00, 'MARGIN', 'ACTIVE'),
(3, 'ACC005', 30000.00, 'CASH', 'ACTIVE'),
(3, 'ACC006', 150000.00, 'MARGIN', 'ACTIVE'),
(4, 'ACC007', 500000.00, 'CASH', 'ACTIVE'),
(4, 'ACC008', 250000.00, 'MARGIN', 'ACTIVE'),
(5, 'ACC009', 80000.00, 'CASH', 'ACTIVE'),
(5, 'ACC010', 120000.00, 'MARGIN', 'ACTIVE');

-- Insert sample stocks
INSERT INTO stocks (symbol, name, sector, current_price) VALUES
-- Technology
('AAPL', 'Apple Inc.', 'Technology', 178.50),
('MSFT', 'Microsoft Corporation', 'Technology', 385.20),
('GOOGL', 'Alphabet Inc.', 'Technology', 142.75),
('NVDA', 'NVIDIA Corporation', 'Technology', 495.30),
('TSLA', 'Tesla Inc.', 'Technology', 242.80),
-- Finance
('JPM', 'JPMorgan Chase & Co.', 'Finance', 158.90),
('BAC', 'Bank of America Corp.', 'Finance', 34.25),
('GS', 'Goldman Sachs Group Inc.', 'Finance', 385.60),
-- Healthcare
('JNJ', 'Johnson & Johnson', 'Healthcare', 162.40),
('PFE', 'Pfizer Inc.', 'Healthcare', 28.75),
('UNH', 'UnitedHealth Group Inc.', 'Healthcare', 524.30),
-- Energy
('XOM', 'Exxon Mobil Corporation', 'Energy', 112.50),
('CVX', 'Chevron Corporation', 'Energy', 158.20),
-- Consumer
('AMZN', 'Amazon.com Inc.', 'Consumer', 178.35),
('WMT', 'Walmart Inc.', 'Consumer', 165.80),
('HD', 'Home Depot Inc.', 'Consumer', 385.90),
('NKE', 'Nike Inc.', 'Consumer', 108.45),
('SBUX', 'Starbucks Corporation', 'Consumer', 98.20),
('MCD', 'McDonald''s Corporation', 'Consumer', 295.75),
('DIS', 'Walt Disney Company', 'Consumer', 112.30);

-- Insert sample orders
INSERT INTO orders (account_id, stock_id, order_type, side, quantity, price, status) VALUES
-- Pending orders
(1, 1, 'LIMIT', 'BUY', 50, 175.00, 'PENDING'),
(1, 2, 'MARKET', 'BUY', 25, NULL, 'PENDING'),
(2, 3, 'LIMIT', 'SELL', 100, 145.00, 'PENDING'),
(3, 4, 'STOP_LOSS', 'SELL', 20, 490.00, 'PENDING'),
(4, 5, 'LIMIT', 'BUY', 30, 240.00, 'PENDING'),
(5, 6, 'MARKET', 'BUY', 100, NULL, 'PENDING'),
(6, 7, 'LIMIT', 'BUY', 200, 33.50, 'PENDING'),
(7, 8, 'LIMIT', 'SELL', 10, 390.00, 'PENDING'),
(8, 9, 'MARKET', 'BUY', 50, NULL, 'PENDING'),
(9, 10, 'LIMIT', 'BUY', 150, 28.00, 'PENDING'),
-- Executed orders
(1, 14, 'MARKET', 'BUY', 100, 177.50, 'EXECUTED'),
(2, 14, 'MARKET', 'SELL', 100, 177.50, 'EXECUTED'),
(3, 15, 'LIMIT', 'BUY', 50, 165.00, 'EXECUTED'),
(4, 15, 'LIMIT', 'SELL', 50, 165.00, 'EXECUTED'),
(5, 1, 'MARKET', 'BUY', 75, 178.00, 'EXECUTED'),
(6, 1, 'MARKET', 'SELL', 75, 178.00, 'EXECUTED'),
(7, 2, 'LIMIT', 'BUY', 40, 384.50, 'EXECUTED'),
(8, 2, 'LIMIT', 'SELL', 40, 384.50, 'EXECUTED'),
(9, 6, 'MARKET', 'BUY', 80, 158.50, 'EXECUTED'),
(10, 6, 'MARKET', 'SELL', 80, 158.50, 'EXECUTED'),
-- Cancelled orders
(1, 3, 'LIMIT', 'BUY', 60, 140.00, 'CANCELLED'),
(2, 4, 'LIMIT', 'SELL', 30, 500.00, 'CANCELLED'),
(3, 5, 'STOP_LOSS', 'SELL', 25, 235.00, 'CANCELLED'),
(4, 7, 'LIMIT', 'BUY', 150, 33.00, 'CANCELLED'),
(5, 8, 'MARKET', 'BUY', 15, NULL, 'CANCELLED'),
(6, 9, 'LIMIT', 'BUY', 70, 160.00, 'CANCELLED'),
(7, 10, 'LIMIT', 'SELL', 100, 29.00, 'CANCELLED'),
(8, 11, 'MARKET', 'BUY', 20, NULL, 'CANCELLED'),
(9, 12, 'LIMIT', 'BUY', 90, 111.00, 'CANCELLED'),
(10, 13, 'LIMIT', 'SELL', 60, 159.00, 'CANCELLED');

-- Insert sample trades (matching executed orders)
INSERT INTO trades (buy_order_id, sell_order_id, stock_id, quantity, price) VALUES
(11, 12, 14, 100, 177.50),
(13, 14, 15, 50, 165.00),
(15, 16, 1, 75, 178.00),
(17, 18, 2, 40, 384.50),
(19, 20, 6, 80, 158.50);

-- Insert portfolio holdings
INSERT INTO portfolio_holdings (account_id, stock_id, quantity, average_price, current_value) VALUES
-- Account 1 holdings
(1, 14, 100, 177.50, 17835.00),
-- Account 3 holdings
(3, 15, 50, 165.00, 8290.00),
-- Account 5 holdings
(5, 1, 75, 178.00, 13387.50),
-- Account 7 holdings
(7, 2, 40, 384.50, 15408.00),
(7, 11, 100, 523.00, 52430.00),
(7, 16, 200, 385.00, 77180.00),
-- Account 9 holdings
(9, 6, 80, 158.50, 12712.00),
(9, 12, 150, 112.00, 16875.00),
(9, 19, 300, 295.00, 88725.00),
-- Account 10 holdings
(10, 1, 200, 177.00, 35700.00),
(10, 2, 100, 383.00, 38520.00),
(10, 3, 150, 141.50, 21412.50),
(10, 4, 50, 492.00, 24765.00),
(10, 5, 75, 241.00, 18210.00);

-- Insert sample watchlists
INSERT INTO watchlists (user_id, name) VALUES
(1, 'Tech Stocks'),
(1, 'My Favorites'),
(2, 'High Growth'),
(3, 'Day Trading'),
(4, 'Long Term Holds'),
(5, 'Dividend Stocks');

-- Insert watchlist items
INSERT INTO watchlist_items (watchlist_id, stock_id) VALUES
-- John's Tech Stocks
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
-- John's Favorites
(2, 1), (2, 14), (2, 6),
-- Jane's High Growth
(3, 4), (3, 5), (3, 14),
-- Bob's Day Trading
(4, 1), (4, 2), (4, 5), (4, 14),
-- Alice's Long Term
(5, 1), (5, 2), (5, 9), (5, 11), (5, 16),
-- Charlie's Dividend Stocks
(6, 6), (6, 9), (6, 15), (6, 19);

-- Insert historical market data
INSERT INTO market_data (stock_id, price, volume, timestamp) VALUES
-- AAPL recent data
(1, 178.50, 52000000, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
(1, 177.80, 48000000, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(1, 178.20, 51000000, CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
-- MSFT recent data
(2, 385.20, 28000000, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
(2, 384.50, 26000000, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(2, 385.00, 27500000, CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
-- GOOGL recent data
(3, 142.75, 31000000, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
(3, 142.30, 29000000, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(3, 142.50, 30000000, CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
-- NVDA recent data
(4, 495.30, 45000000, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
(4, 493.80, 43000000, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(4, 494.50, 44000000, CURRENT_TIMESTAMP - INTERVAL '3' HOUR),
-- TSLA recent data
(5, 242.80, 95000000, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
(5, 241.50, 92000000, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(5, 242.00, 93500000, CURRENT_TIMESTAMP - INTERVAL '3' HOUR);
