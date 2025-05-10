SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS expense_category;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS expense;
DROP TABLE IF EXISTS user_accounting;
DROP TABLE IF EXISTS user_additions_log;
DROP TABLE IF EXISTS local_user;

CREATE TABLE local_user (
                            id BIGINT PRIMARY KEY,
                            first_name VARCHAR(100),
                            last_name VARCHAR(100),
                            email VARCHAR(100),
                            password VARCHAR(100),
                            role VARCHAR(50),
                            is_first_login BOOLEAN
);

CREATE TABLE user_accounting (
                                 id BIGINT PRIMARY KEY,
                                 salary_date INT,
                                 monthly_salary DECIMAL(10, 2),
                                 current_balance DECIMAL(10, 2),
                                 local_user_id BIGINT,
                                 FOREIGN KEY (local_user_id) REFERENCES local_user(id)
);

CREATE TABLE user_additions_log (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT,
                                    amount DECIMAL(10, 2),
                                    description VARCHAR(255),
                                    created_at TIMESTAMP,
                                    FOREIGN KEY (user_id) REFERENCES local_user(id)
);

CREATE TABLE expense (
                         id BIGINT PRIMARY KEY,
                         name VARCHAR(100),
                         category VARCHAR(100),
                         amount DECIMAL(10, 2),
                         date TIMESTAMP,
                         description VARCHAR(255),
                         is_recurrent BOOLEAN,
                         user_id BIGINT,
                         FOREIGN KEY (user_id) REFERENCES local_user(id)
);

CREATE TABLE category (
                          id BIGINT PRIMARY KEY,
                          name VARCHAR(100)
);

CREATE TABLE expense_category (
                                  expense_id BIGINT,
                                  category_id BIGINT,
                                  PRIMARY KEY (expense_id, category_id),
                                  FOREIGN KEY (expense_id) REFERENCES expense(id),
                                  FOREIGN KEY (category_id) REFERENCES category(id)
);


INSERT INTO local_user (
    id,
    first_name,
    last_name,
    email,
    password,
    role,
    is_first_login
) VALUES
      (1001, 'Anakin', 'Skywalker', 'anakin@example.com', '$2a$10$YNNILfxiDowj77zevkYTsO/M1tRYdlbf7oHHEVEMgyqO8gnuqn2hm', 'ROLE_USER', TRUE),
      (1002, 'Alice', 'Silva',     'alice@example.com',  '$2a$10$YNNILfxiDowj77zevkYTsO/M1tRYdlbf7oHHEVEMgyqO8gnuqn2hm','ROLE_USER', FALSE);

INSERT INTO user_accounting (
    id,
    salary_date,
    monthly_salary,
    current_balance,
    local_user_id
) VALUES
      (1001, 5, 4500.00, 2250, 1001),
      (1002, 10, 3200.00, 800.75, 1002);

INSERT INTO expense (
    id,
    name,
    category,
    amount,
    date,
    description,
    is_recurrent,
    user_id
) VALUES
      (1001, 'Groceries', 'FOOD',    300.00, '2025-05-01 14:30:00', 'Weekly grocery shopping', FALSE, 1001),
      (1002, 'Rent',      'HOUSING', 1500.00,'2025-05-01 10:00:00', 'Monthly apartment rent',  TRUE, 1001),
      (1003, 'Gym',       'HEALTH',   80.00, '2025-05-03 18:00:00', 'Monthly membership',     TRUE, 1002);

INSERT INTO user_additions_log (
    id,
    user_id,
    amount,
    description,
    created_at
) VALUES
      (1001, 1001, 500.00, 'Bonus from freelance job', '2025-05-05 12:00:00'),
      (1002, 1002, 300.00, 'Reimbursement for travel','2025-05-06 09:45:00');

SET REFERENTIAL_INTEGRITY TRUE;
