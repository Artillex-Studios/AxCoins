SELECT $table_prefixaxcoins_users.id, $table_prefixaxcoins_users.name, $table_prefixaxcoins_users.uuid, $table_prefixaxcoins_currency_data.currency_id, $table_prefixaxcoins_currency_data.amount
FROM $table_prefixaxcoins_users
WHERE $table_prefixaxcoins_users.uuid = ?
AND $table_prefixaxcoins_currency_data.user_id = $table_prefixaxcoins_users.id;
