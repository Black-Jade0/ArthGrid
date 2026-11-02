package com.Prakhar.Payment_Service.Service;

import com.Prakhar.Payment_Service.Client.AccountsClient;
import com.Prakhar.Payment_Service.DTO.BillPaymentRequest;
import com.Prakhar.Payment_Service.DTO.DepositRequest; // <--- Make sure ye DTO bana ho
import com.Prakhar.Payment_Service.DTO.HoldRequest;
import com.Prakhar.Payment_Service.Entity.BillPayment;
import com.Prakhar.Payment_Service.Entity.Status;
import com.Prakhar.Payment_Service.Event.PaymentEvent;
import com.Prakhar.Payment_Service.Repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountsClient accountsClient;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public BillPayment payBill(BillPaymentRequest paymentRequest) {
        String transactionId = UUID.randomUUID().toString(); // TransactionId

        // Initial Entry (Initiated)
        BillPayment payment = BillPayment.builder()
                .sourceAccountNumber(paymentRequest.getSourceAccountNumber())
                .amount(paymentRequest.getAmount())
                .billerName(paymentRequest.getBillerName())
                .billReferenceNumber(paymentRequest.getBillReferenceNumber())
                .status(Status.INITIATED)
                .transactionReferenceId(transactionId)
                .build();
        paymentRepository.save(payment);

        boolean holdCaptured = false;
        try {
            // Basic checks before touching any money
            if (paymentRequest.getAmount() == null || paymentRequest.getAmount().signum() <= 0) {
                throw new RuntimeException("Amount must be more than 0");
            }
            if (paymentRequest.getSourceAccountNumber() != null
                    && paymentRequest.getSourceAccountNumber().equals(paymentRequest.getTargetAccountNumber())) {
                throw new RuntimeException("You cannot send money to your own account");
            }
            boolean isTransfer = paymentRequest.getTargetAccountNumber() != null && !paymentRequest.getTargetAccountNumber().isEmpty();
            String holdText = isTransfer
                    ? "Sent to " + (paymentRequest.getTargetName() != null ? paymentRequest.getTargetName() : paymentRequest.getTargetAccountNumber())
                    : "Bill: " + paymentRequest.getBillerName();

            //  HOLD MONEY (Source Account) - Ye sabke liye same rahega
            HoldRequest holdRequest = HoldRequest.builder()
                    .accountNumber(paymentRequest.getSourceAccountNumber())
                    .amount(paymentRequest.getAmount())
                    .description(holdText)
                    .referenceId(transactionId)
                    .build();

            accountsClient.createHold(holdRequest);
            payment.setStatus(Status.PENDING);
            paymentRepository.save(payment);

            //  CHECK - Transfer hai ya Bill Payment?
            if (isTransfer) {

                // ================= CASE A: TRANSFER (Paisa Bhejna) =================

                // 1. Source se paisa pakka kato (Capture) - FIRST, so money can never be created out of thin air
                accountsClient.captureHold(transactionId);
                holdCaptured = true;

                // 2. Target account mein paisa daalo (Deposit)
                String receivedText = "Received from " + (paymentRequest.getSenderName() != null ? paymentRequest.getSenderName() : paymentRequest.getSourceAccountNumber());
                DepositRequest depositReq = new DepositRequest(paymentRequest.getTargetAccountNumber(), paymentRequest.getAmount(), receivedText);
                try {
                    accountsClient.deposit(depositReq);
                } catch (Exception depositError) {
                    // Hold is already captured, so put the money back into the sender's account
                    accountsClient.deposit(new DepositRequest(paymentRequest.getSourceAccountNumber(), paymentRequest.getAmount(), "Refund: transfer failed"));
                    throw depositError;
                }



                payment.setReason("Transfer Complete to " + paymentRequest.getTargetAccountNumber());

            } else {

                // ================= CASE B: BILL PAYMENT (Purana Logic) =================

                // Biller validation logic
                boolean billerSuccess = paymentRequest.getAmount().doubleValue() <= 100000;
                if (!billerSuccess) {
                    throw new RuntimeException("Payment Rejected: Amount limit exceeded");
                }

                // Source se paisa pakka kato (Capture)
                accountsClient.captureHold(transactionId);
                payment.setReason("Bill Payment Complete");
            }

            // STEP 3: SUCCESS & KAFKA EVENT
            payment.setStatus(Status.SUCCESS);

            PaymentEvent event = PaymentEvent.builder()
                    .eventType("Payment_Completed")
                    .sourceAccountNumber(paymentRequest.getSourceAccountNumber())
                    .transactionId(transactionId)
                    .amount(paymentRequest.getAmount())
                    .billerName(paymentRequest.getBillerName())
                    .status(Status.SUCCESS)
                    .email(paymentRequest.getEmail())
                    .targetEmail(paymentRequest.getTargetEmail())
                    .targetAccountNumber(paymentRequest.getTargetAccountNumber())
                    .senderName(paymentRequest.getSenderName())
                    .targetName(paymentRequest.getTargetName())
                    .build();

            kafkaTemplate.send("payment-events", transactionId, event);
            System.out.println("Message sent to kafka for " + transactionId);

        } catch (Exception e) {
            // ERROR HANDLING (Hold Release karna)
            if (holdCaptured) {
                payment.setReason("Failed: " + e.getMessage() + " -> money refunded to sender");
            } else if (payment.getStatus() == Status.PENDING) {
                System.out.println("Releasing Hold due to error: " + e.getMessage());
                try {
                    accountsClient.releaseHold(transactionId);
                    payment.setReason("Failed: " + e.getMessage() + " -> Hold Released");
                } catch (Exception exception) {
                    payment.setReason("Critical: Failed to release Hold -> " + exception.getMessage());
                }
            } else {
                payment.setReason("Failed: " + e.getMessage());
            }
            payment.setStatus(Status.FAILURE);
        }

        return paymentRepository.save(payment);
    }
}