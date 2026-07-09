package sber.deonande.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    private UUID id;
    private UUID obligationId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paidAt;
}
