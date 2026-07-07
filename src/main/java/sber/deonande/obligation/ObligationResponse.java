package sber.deonande.obligation;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ObligationResponse {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private Category category;
    private Recurrence recurrence;
    private LocalDate nextPaymentDate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
