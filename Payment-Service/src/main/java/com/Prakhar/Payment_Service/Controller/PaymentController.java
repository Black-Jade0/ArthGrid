package com.Prakhar.Payment_Service.Controller;

import com.Prakhar.Payment_Service.DTO.BillPaymentRequest;
import com.Prakhar.Payment_Service.Entity.BillPayment;
import com.Prakhar.Payment_Service.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/bill")
    public ResponseEntity<BillPayment> payBill(@RequestBody BillPaymentRequest request)
    {
        return ResponseEntity.ok(paymentService.payBill(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<BillPayment> transferMoney(@RequestBody BillPaymentRequest request) {
        return ResponseEntity.ok(paymentService.payBill(request));
    }


}
