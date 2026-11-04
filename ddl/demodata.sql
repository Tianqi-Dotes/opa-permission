CREATE TABLE test.customers (
                           customer_id   BIGINT PRIMARY KEY,
                           customer_name VARCHAR(100) NOT NULL,
                           segent        VARCHAR(30) NOT NULL,
                           risk_rating   VARCHAR(10) NOT NULL
                               CHECK (risk_rating IN ('LOW', 'MEDIUM', 'HIGH')),
                           occupation    VARCHAR(100),
                           onboard_date  DATE NOT NULL,
                           country_code  VARCHAR(2) NOT NULL
                               CHECK (country_code ~ '^[A-Z]{2}$')
    );

INSERT INTO test.customers
(customer_id, customer_name, segent, risk_rating, occupation, onboard_date, country_code)
VALUES
    (10001, 'Alex Morgan',   'RETAIL',          'LOW',    'Teacher',           '2022-03-14', 'US'),
    (10002, 'Jamie Chen',    'PREMIUM',         'MEDIUM', 'Software Engineer', '2021-11-02', 'SG'),
    (10003, 'Taylor Singh',  'PRIVATE_BANKING','LOW',    'Business Owner',    '2020-06-25', 'GB'),
    (10004, 'Riley Kim',     'RETAIL',          'HIGH',   'Consultant',        '2023-01-09', 'CN'),
    (10005, 'Jordan Garcia', 'PREMIUM',         'LOW',    'Doctor',            '2019-08-17', 'CA'),
    (10006, 'Casey Patel',   'RETAIL',          'MEDIUM', 'Accountant',        '2024-02-20', 'AU'),
    (10007, 'Avery Wilson',  'PRIVATE_BANKING','MEDIUM', 'Executive',         '2018-12-05', 'US'),
    (10008, 'Samira Ahmed',  'PREMIUM',         'HIGH',   'Import Trader',     '2022-09-30', 'CN'),
    (10009, 'Noah Brown',    'RETAIL',          'LOW',    'Nurse',             '2023-07-12', 'CN'),
    (10010, 'Mina Park',     'PREMIUM',         'MEDIUM', 'Researcher',        '2020-04-03', 'JP');