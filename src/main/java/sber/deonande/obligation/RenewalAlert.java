package sber.deonande.obligation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RenewalAlert {
    private UUID id;
    private String title;
    private LocalDate nextPaymentDate;
    private BigDecimal amount;
    private String currency;
}
