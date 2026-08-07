CREATE TABLE IF NOT EXISTS offer_question_banks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(30) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_offer_question_banks_name UNIQUE (name),
    CONSTRAINT uk_offer_question_banks_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS offer_categories (
    id BIGSERIAL PRIMARY KEY,
    bank_id BIGINT NOT NULL REFERENCES offer_question_banks(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_offer_categories_bank_type_name UNIQUE (bank_id, type, name)
);

CREATE TABLE IF NOT EXISTS offer_questions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    bank_id BIGINT NOT NULL REFERENCES offer_question_banks(id),
    category_id BIGINT NOT NULL REFERENCES offer_categories(id),
    difficulty VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    answer TEXT NOT NULL,
    source VARCHAR(100),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_offer_questions_bank_type_title UNIQUE (bank_id, type, title)
);

CREATE INDEX IF NOT EXISTS idx_offer_categories_bank_id ON offer_categories (bank_id);
CREATE INDEX IF NOT EXISTS idx_offer_questions_bank_id ON offer_questions (bank_id);
CREATE INDEX IF NOT EXISTS idx_offer_questions_category_id ON offer_questions (category_id);
CREATE INDEX IF NOT EXISTS idx_offer_questions_type ON offer_questions (type);

INSERT INTO offer_question_banks (name, code, sort)
VALUES
    ('Java', 'JAVA', 1),
    ('Python', 'PYTHON', 2),
    ('前端', 'FRONTEND', 3)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name, sort = EXCLUDED.sort, updated_at = CURRENT_TIMESTAMP;

INSERT INTO offer_categories (bank_id, name, type, sort)
SELECT bank.id, category.name, category.type, category.sort
FROM offer_question_banks bank
JOIN (
    VALUES
        ('JAVA', 'Java基础', 'THEORY', 1),
        ('JAVA', 'Java集合', 'THEORY', 2),
        ('JAVA', 'Java并发', 'THEORY', 3),
        ('JAVA', 'JVM', 'THEORY', 4),
        ('JAVA', 'Spring', 'THEORY', 5),
        ('JAVA', 'Spring MVC', 'THEORY', 6),
        ('JAVA', 'Spring Boot', 'THEORY', 7),
        ('JAVA', 'MyBatis', 'THEORY', 8),
        ('JAVA', 'MySQL', 'THEORY', 9),
        ('JAVA', 'Redis', 'THEORY', 10),
        ('JAVA', 'RabbitMQ', 'THEORY', 11),
        ('JAVA', 'RocketMQ', 'THEORY', 12),
        ('JAVA', 'Kafka', 'THEORY', 13),
        ('JAVA', '微服务', 'THEORY', 14),
        ('JAVA', '分布式', 'THEORY', 15),
        ('JAVA', 'Linux', 'THEORY', 16),
        ('JAVA', '计算机网络', 'THEORY', 17),
        ('JAVA', '数组', 'ALGORITHM', 1),
        ('JAVA', '字符串', 'ALGORITHM', 2),
        ('JAVA', '哈希表', 'ALGORITHM', 3),
        ('JAVA', '链表', 'ALGORITHM', 4),
        ('JAVA', '栈', 'ALGORITHM', 5),
        ('JAVA', '队列', 'ALGORITHM', 6),
        ('JAVA', '双指针', 'ALGORITHM', 7),
        ('JAVA', '滑动窗口', 'ALGORITHM', 8),
        ('JAVA', '二分查找', 'ALGORITHM', 9),
        ('JAVA', '二叉树', 'ALGORITHM', 10),
        ('JAVA', '图', 'ALGORITHM', 11),
        ('JAVA', 'DFS', 'ALGORITHM', 12),
        ('JAVA', 'BFS', 'ALGORITHM', 13),
        ('JAVA', '动态规划', 'ALGORITHM', 14),
        ('JAVA', '贪心', 'ALGORITHM', 15),
        ('JAVA', '堆', 'ALGORITHM', 16),
        ('JAVA', '回溯', 'ALGORITHM', 17),
        ('JAVA', '前缀和', 'ALGORITHM', 18),
        ('PYTHON', 'Python基础', 'THEORY', 1),
        ('PYTHON', '面向对象', 'THEORY', 2),
        ('PYTHON', '标准库', 'THEORY', 3),
        ('PYTHON', 'Django', 'THEORY', 4),
        ('PYTHON', 'Flask', 'THEORY', 5),
        ('PYTHON', '数据分析', 'THEORY', 6),
        ('PYTHON', '爬虫', 'THEORY', 7),
        ('FRONTEND', 'HTML/CSS', 'THEORY', 1),
        ('FRONTEND', 'JavaScript', 'THEORY', 2),
        ('FRONTEND', 'TypeScript', 'THEORY', 3),
        ('FRONTEND', 'React', 'THEORY', 4),
        ('FRONTEND', 'Vue', 'THEORY', 5),
        ('FRONTEND', 'Node.js', 'THEORY', 6),
        ('FRONTEND', '工程化', 'THEORY', 7),
        ('FRONTEND', '浏览器', 'THEORY', 8)
) AS category(bank_code, name, type, sort) ON bank.code = category.bank_code
ON CONFLICT (bank_id, type, name) DO UPDATE
SET sort = EXCLUDED.sort, updated_at = CURRENT_TIMESTAMP;
