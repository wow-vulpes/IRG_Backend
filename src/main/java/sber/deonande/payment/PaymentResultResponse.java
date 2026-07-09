package sber.deonande.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sber.deonande.obligation.ObligationResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultResponse {
    private ObligationResponse obligation;
    private PaymentResponse payment;
}
