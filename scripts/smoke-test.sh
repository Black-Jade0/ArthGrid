#!/usr/bin/env bash
# End-to-end test of ArthGrid running locally (docker compose up --build).
# Everything goes through the API Gateway on :9090, exactly like a frontend would.
#
# Usage:   bash scripts/smoke-test.sh
# Needs:   curl  (Mac/Linux/Git Bash on Windows)

set -u
GW="${GATEWAY:-http://localhost:9090}"
TS=$(date +%s)

# pull a "field":"value" (or "field":number) out of a JSON response, no jq needed
json() { echo "$2" | sed -n "s/.*\"$1\":\"\{0,1\}\([^\",}]*\).*/\1/p" | head -1; }

step() { printf "\n\033[1;34m== %s\033[0m\n" "$1"; }
fail() { printf "\033[1;31mFAILED: %s\033[0m\n" "$1"; exit 1; }
ok()   { printf "\033[1;32mOK\033[0m %s\n" "$1"; }

step "1. Sign up customer A (customer -> auth -> accounts, + welcome email via Kafka)"
A=$(curl -s -X POST "$GW/customer-service/api/v1/customers" -H 'Content-Type: application/json' -d "{
  \"firstName\":\"Asha\",\"lastName\":\"Verma\",\"email\":\"asha$TS@example.com\",
  \"phone\":\"9000000001\",\"address\":\"Bhopal\",\"externalId\":\"signup-a-$TS\"}")
echo "$A"
ACC_A=$(json accountNumber "$A"); USER_A=$(json externalId "$A")
[ -n "$ACC_A" ] || fail "no account number for customer A (is everything up? try: docker compose ps)"
ok "customer A user=$USER_A account=$ACC_A"

step "2. Sign up customer B"
B=$(curl -s -X POST "$GW/customer-service/api/v1/customers" -H 'Content-Type: application/json' -d "{
  \"firstName\":\"Ravi\",\"lastName\":\"Kumar\",\"email\":\"ravi$TS@example.com\",
  \"phone\":\"9000000002\",\"address\":\"Indore\",\"externalId\":\"signup-b-$TS\"}")
ACC_B=$(json accountNumber "$B")
[ -n "$ACC_B" ] || fail "no account number for customer B: $B"
ok "customer B account=$ACC_B"

step "3. Deposit 5000 into A"
R=$(curl -s -X POST "$GW/accounts-service/api/v1/accounts/deposit" -H 'Content-Type: application/json' \
  -d "{\"accountNumber\":\"$ACC_A\",\"amount\":5000}")
echo "$R"; [ "$(json currentBalance "$R")" = "5000" ] || [ "$(json currentBalance "$R")" = "5000.00" ] || fail "deposit"
ok "deposited"

step "4. Transfer 1000 A -> B (payment: hold -> deposit to B -> capture -> Kafka event)"
R=$(curl -s -X POST "$GW/payment-service/api/v1/payments/transfer" -H 'Content-Type: application/json' -d "{
  \"sourceAccountNumber\":\"$ACC_A\",\"targetAccountNumber\":\"$ACC_B\",\"amount\":1000,
  \"billerName\":\"Transfer to Ravi\",\"email\":\"asha$TS@example.com\",\"targetEmail\":\"ravi$TS@example.com\"}")
echo "$R"; [ "$(json status "$R")" = "SUCCESS" ] || fail "transfer"
ok "transfer done"

step "5. Pay a 500 electricity bill from A"
R=$(curl -s -X POST "$GW/payment-service/api/v1/payments/bill" -H 'Content-Type: application/json' -d "{
  \"sourceAccountNumber\":\"$ACC_A\",\"amount\":500,\"billerName\":\"MP Electricity\",
  \"billReferenceNumber\":\"EB-$TS\",\"email\":\"asha$TS@example.com\"}")
echo "$R"; [ "$(json status "$R")" = "SUCCESS" ] || fail "bill payment"
ok "bill paid"

step "6. Balances (expect A = 3500, B = 1000)"
RA=$(curl -s "$GW/accounts-service/api/v1/accounts/$ACC_A"); echo "A: $RA"
RB=$(curl -s "$GW/accounts-service/api/v1/accounts/$ACC_B"); echo "B: $RB"

step "7. A's statement (latest 5 transactions)"
curl -s "$GW/accounts-service/api/v1/accounts/$ACC_A/transactions?page=0&size=5"; echo

step "8. 'My profile' as customer A (local mode: X-User-Id header instead of a JWT)"
curl -s "$GW/customer-service/api/v1/customers/me" -H "X-User-Id: $USER_A"; echo

printf "\n\033[1;32mAll good!\033[0m Now open:\n"
echo "  Emails sent by the app -> http://localhost:8025  (welcome mails, debit + credit alerts)"
echo "  Kafka topics/messages  -> http://localhost:8090"
echo "  Registered services    -> http://localhost:8761"
