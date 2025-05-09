-- 1) disable foreign-key checks so we can insert the circular references
SET REFERENTIAL_INTEGRITY FALSE;

-- 2) local_user (note: user_accounting_id points to accounting.id)
INSERT INTO local_user (
    id,
    first_name,
    last_name,
    email,
    password,
    role,
    is_first_login,
    user_accounting_id
) VALUES
      (1001, 'Anakin', 'Skywalker', 'anakin@example.com', '$2a$10$YNNILfxiDowj77zevkYTsO/M1tRYdlbf7oHHEVEMgyqO8gnuqn2hm', 'ROLE_USER', TRUE, 1001),
      (1002, 'Alice', 'Silva',     'alice@example.com',  '$2a$10$YNNILfxiDowj77zevkYTsO/M1tRYdlbf7oHHEVEMgyqO8gnuqn2hm','ROLE_USER', FALSE, 1002);

-- 3) user_accounting (note: salaryDate, monthly_salary, current_balance, local_user_id)
INSERT INTO user_accounting (
    id,
    salary_date,
    monthly_salary,
    current_balance,
    local_user_id
) VALUES
      (1001, 5, 4500.00, 2250, 1001),
      (1002, 10, 3200.00, 800.75, 1002);

-- 4) expense (column names all match your @Column mappings)
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

-- 5) user_additions_log
INSERT INTO user_additions_log (
    id,
    user_id,
    amount,
    description,
    created_at
) VALUES
      (1001, 1001, 500.00, 'Bonus from freelance job', '2025-05-05 12:00:00'),
      (1002, 1002, 300.00, 'Reimbursement for travel','2025-05-06 09:45:00');

-- 6) re-enable foreign-key checks
SET REFERENTIAL_INTEGRITY TRUE;
