CREATE TABLE IF NOT EXISTS $table_prefixaxcoins_currencies (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS $table_prefixaxcoins_users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(16), uuid UUID);

CREATE TABLE IF NOT EXISTS $table_prefixaxcoins_currency_data (user_id INT, currency_id INT, amount VARCHAR(1000000), PRIMARY KEY (user_id, currency_id));