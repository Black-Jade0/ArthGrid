-- ArthGrid: queries to show that a transfer really happened in all 4 databases.
-- Run in Adminer (http://localhost:8091 -> "SQL command") or MySQL Workbench (localhost:3307, root/root).

-- 1) bank_db: registered customers (real emails + their account number)
-- (password_hash and pin_hash show that passwords/PINs are stored encrypted, never as plain text)
SELECT id, first_name, last_name, email, phone, city, state, account_number, kyc_status,
       LEFT(password_hash, 20) AS password_hash, LEFT(pin_hash, 20) AS pin_hash, created_at
FROM bank_db.customers ORDER BY id DESC;

-- 2) account_db: every account and its current balance
SELECT account_number, current_balance, currency, status
FROM account_db.accounts ORDER BY id DESC;

-- 3) account_db: the ledger (opening balance, money sent, money received)
SELECT account_number, transaction_type, amount, description, time_stamp
FROM account_db.accounts_transactions ORDER BY id DESC;

-- 4) account_db: holds (money reserved first, then captured)
SELECT reference_id, amount, status, description, created_at
FROM account_db.accounts_holds ORDER BY created_at DESC;

-- 5) payment_db: each transfer and whether it succeeded
SELECT transaction_reference_id, source_account_number, amount, biller_name,
       bill_reference_number AS remark, status, reason
FROM payment_db.bill_payments ORDER BY id DESC;

-- 6) notification_db: which emails were sent, to whom
SELECT * FROM notification_db.notification_entry ORDER BY id DESC;

-- 7) One transfer followed across all databases (paste a transaction_reference_id from query 5)
-- SET @ref = 'paste-id-here';
-- SELECT * FROM payment_db.bill_payments WHERE transaction_reference_id = @ref;
-- SELECT * FROM account_db.accounts_holds WHERE reference_id = @ref;
-- SELECT * FROM account_db.accounts_transactions WHERE reference_id = @ref;
-- SELECT * FROM notification_db.notification_entry WHERE transaction_id = @ref;
