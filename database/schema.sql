CREATE DATABASE IF NOT EXISTS employee_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE employee_management;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS departments (
  department_id INT AUTO_INCREMENT PRIMARY KEY,
  department_name VARCHAR(100) NOT NULL UNIQUE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS employees (
  employee_id INT AUTO_INCREMENT PRIMARY KEY,
  department_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  position VARCHAR(100),
  email VARCHAR(255) NOT NULL,
  FOREIGN KEY (department_id)
    REFERENCES departments(department_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS login (
  login_id VARCHAR(50) PRIMARY KEY,
  password VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO departments (department_name)
VALUES
  ('営業部'),
  ('開発部'),
  ('総務部');

-- 学習・ローカル動作用デモアカウント
INSERT INTO login (login_id, password)
VALUES ('admin', 'admin123');

INSERT INTO employees (
  department_id,
  name,
  position,
  email
)
SELECT
  department_id,
  '山田 太郎',
  '主任',
  'taro@example.com'
FROM departments
WHERE department_name = '開発部';

INSERT INTO employees (
  department_id,
  name,
  position,
  email
)
SELECT
  department_id,
  '佐藤 花子',
  '担当',
  'hanako@example.com'
FROM departments
WHERE department_name = '営業部';

