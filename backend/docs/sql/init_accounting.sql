CREATE TABLE IF NOT EXISTS account_books (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category_id BIGINT,
    title VARCHAR(100) NOT NULL,
    note TEXT,
    transaction_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_account_books_owner_id ON account_books (owner_id);
CREATE INDEX IF NOT EXISTS idx_account_books_relationship_id ON account_books (relationship_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account_book_id ON transactions (account_book_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_time ON transactions (transaction_time);

INSERT INTO transaction_categories (name, type, icon, sort_order, status)
SELECT item.name, item.type, item.icon, item.sort_order, 'ACTIVE'
FROM (
    VALUES
        ('餐饮', 'EXPENSE', 'food', 10),
        ('交通', 'EXPENSE', 'transport', 20),
        ('购物', 'EXPENSE', 'shopping', 30),
        ('住房', 'EXPENSE', 'home', 40),
        ('娱乐', 'EXPENSE', 'entertainment', 50),
        ('医疗', 'EXPENSE', 'medical', 60),
        ('学习', 'EXPENSE', 'study', 70),
        ('旅行', 'EXPENSE', 'travel', 80),
        ('其他', 'EXPENSE', 'other', 90),
        ('工资', 'INCOME', 'salary', 10),
        ('奖金', 'INCOME', 'bonus', 20),
        ('红包', 'INCOME', 'gift', 30),
        ('兼职', 'INCOME', 'part-time', 40),
        ('其他', 'INCOME', 'other', 50)
) AS item(name, type, icon, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM transaction_categories c
    WHERE c.name = item.name AND c.type = item.type
);
