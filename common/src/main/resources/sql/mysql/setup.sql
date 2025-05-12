CREATE TABLE IF NOT EXISTS $table_prefixaxcoins_currencies (id INTEGER AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS $table_prefixaxcoins_users (id INTEGER AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), uuid VARCHAR(36));

CREATE TABLE IF NOT EXISTS $table_prefixaxcoins_currency_data (user_id INTEGER, currency_id INTEGER, amount LONGTEXT, PRIMARY KEY (user_id, currency_id), INDEX $table_prefixaxcoins_idx_user_currency (user_id, currency_id));