SELECT $table_prefixaxcoins_users.id, $table_prefixaxcoins_users.uuid, $table_prefixaxcoins_currency_data.currency_id, $table_prefixaxcoins_currency_data.amount
FROM $table_prefixaxcoins_users
LEFT JOIN $table_prefixaxcoins_currency_data ON $table_prefixaxcoins_currency_data.user_id = $table_prefixaxcoins_users.id
WHERE $table_prefixaxcoins_users.uuid = ?;
