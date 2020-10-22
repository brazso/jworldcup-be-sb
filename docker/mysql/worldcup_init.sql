CREATE USER IF NOT EXISTS 'worldcup'@'%' IDENTIFIED BY 'worldcup.org';

CREATE DATABASE IF NOT EXISTS worldcup CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON worldcup.* TO 'worldcup'@'%';

CREATE DATABASE IF NOT EXISTS worldcup_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON worldcup_test.* TO 'worldcup'@'%';

FLUSH PRIVILEGES;
