# End-to-end test of ArthGrid running locally (docker compose up --build) - Windows PowerShell version.
# Everything goes through the API Gateway on :9090, exactly like a frontend would.
#
# Usage (from the ArthGrid folder):
#   powershell -ExecutionPolicy Bypass -File scripts\smoke-test.ps1

$ErrorActionPreference = "Stop"
$GW = if ($env:GATEWAY) { $env:GATEWAY } else { "http://localhost:9090" }
$TS = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()

function Step($text) { Write-Host "`n== $text" -ForegroundColor Cyan }
function Ok($text)   { Write-Host "OK $text" -ForegroundColor Green }
function Fail($text) { Write-Host "FAILED: $text" -ForegroundColor Red; exit 1 }

function Post($path, $body) {
    try {
        return Invoke-RestMethod -Method Post -Uri "$GW$path" -ContentType "application/json" -Body ($body | ConvertTo-Json)
    } catch {
        Fail "POST $path -> $($_.Exception.Message)  (is everything up? try: docker compose ps)"
    }
}
function Get-Json($path, $headers = @{}) {
    try {
        return Invoke-RestMethod -Method Get -Uri "$GW$path" -Headers $headers
    } catch {
        Fail "GET $path -> $($_.Exception.Message)"
    }
}

Step "1. Sign up customer A (customer -> auth -> accounts, + welcome email via Kafka)"
$A = Post "/customer-service/api/v1/customers" @{
    firstName = "Asha"; lastName = "Verma"; email = "asha$TS@example.com"
    phone = "9000000001"; address = "Bhopal"; externalId = "signup-a-$TS" }
$A | Format-List
if (-not $A.accountNumber) { Fail "no account number for customer A" }
Ok "customer A user=$($A.externalId) account=$($A.accountNumber)"

Step "2. Sign up customer B"
$B = Post "/customer-service/api/v1/customers" @{
    firstName = "Ravi"; lastName = "Kumar"; email = "ravi$TS@example.com"
    phone = "9000000002"; address = "Indore"; externalId = "signup-b-$TS" }
if (-not $B.accountNumber) { Fail "no account number for customer B" }
Ok "customer B account=$($B.accountNumber)"

Step "3. Deposit 5000 into A"
$R = Post "/accounts-service/api/v1/accounts/deposit" @{ accountNumber = $A.accountNumber; amount = 5000 }
if ([decimal]$R.currentBalance -ne 5000) { Fail "deposit (balance is $($R.currentBalance))" }
Ok "deposited, balance $($R.currentBalance)"

Step "4. Transfer 1000 A -> B (payment: hold -> deposit to B -> capture -> Kafka event)"
$R = Post "/payment-service/api/v1/payments/transfer" @{
    sourceAccountNumber = $A.accountNumber; targetAccountNumber = $B.accountNumber; amount = 1000
    billerName = "Transfer to Ravi"; email = "asha$TS@example.com"; targetEmail = "ravi$TS@example.com" }
if ($R.status -ne "SUCCESS") { Fail "transfer: $($R.reason)" }
Ok "transfer done"

Step "5. Pay a 500 electricity bill from A"
$R = Post "/payment-service/api/v1/payments/bill" @{
    sourceAccountNumber = $A.accountNumber; amount = 500; billerName = "MP Electricity"
    billReferenceNumber = "EB-$TS"; email = "asha$TS@example.com" }
if ($R.status -ne "SUCCESS") { Fail "bill payment: $($R.reason)" }
Ok "bill paid"

Step "6. Balances (expect A = 3500, B = 1000)"
$RA = Get-Json "/accounts-service/api/v1/accounts/$($A.accountNumber)"
$RB = Get-Json "/accounts-service/api/v1/accounts/$($B.accountNumber)"
Write-Host "A: $($RA.currentBalance)   B: $($RB.currentBalance)"
if ([decimal]$RA.currentBalance -ne 3500 -or [decimal]$RB.currentBalance -ne 1000) { Fail "unexpected balances" }
Ok "balances correct"

Step "7. A's statement (latest 5 transactions)"
(Get-Json "/accounts-service/api/v1/accounts/$($A.accountNumber)/transactions?page=0&size=5").content |
    Format-Table transactionType, amount, description -AutoSize

Step "8. 'My profile' as customer A (local mode: X-User-Id header instead of a JWT)"
Get-Json "/customer-service/api/v1/customers/me" @{ "X-User-Id" = $A.externalId } | Format-List

Write-Host "`nAll good! Now open:" -ForegroundColor Green
Write-Host "  Emails sent by the app -> http://localhost:8025  (welcome mails, debit + credit alerts)"
Write-Host "  Kafka topics/messages  -> http://localhost:8090"
Write-Host "  Registered services    -> http://localhost:8761"
