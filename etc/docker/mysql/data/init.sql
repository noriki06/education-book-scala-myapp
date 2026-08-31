--
-- Copyright IxiaS, Inc. All Rights Reserved.
--
-- Runs once on first container start (docker-entrypoint-initdb.d).
-- Creates the application database and user used by application.conf.
--
-- The database is named `myapp`, not the scaffold's `app`, so that connecting
-- to the wrong port fails loudly instead of quietly opening the other repo's
-- database. Port and database name are changed together on purpose.
--

CREATE DATABASE IF NOT EXISTS `myapp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'myapp'@'%' IDENTIFIED BY 'pass';
GRANT ALL PRIVILEGES ON `myapp`.* TO 'myapp'@'%';
FLUSH PRIVILEGES;
