package sber.deonande.obligation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Информация об обязательстве (подписке или платеже)")
public class ObligationResponse {
    @Schema(description = "Уникальный идентификатор обязательства", example = "e2e01901-a077-4128-b5fa-b796787d939d")
    private UUID id;
    
    @Schema(description = "Название", example = "Netflix")
    private String title;
    
    @Schema(description = "Сумма платежа", example = "299.50")
    private BigDecimal amount;
    
    @Schema(description = "Валюта (3 буквы)", example = "RUB")
    private String currency;
    
    @Schema(description = "Категория", example = "SUBSCRIPTION")
    private Category category;
    
    @Schema(description = "Периодичность платежа (null для разовых)", example = "MONTHLY")
    private Recurrence recurrence;
    
    @Schema(description = "Дата следующего списания", example = "2024-05-15")
    private LocalDate nextPaymentDate;
    
    @Schema(description = "Текущий статус", example = "ACTIVE")
    private Status status;
    
    @Schema(description = "Дата и время создания записи", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата и время последнего обновления", example = "2024-01-02T15:30:00")
    private LocalDateTime updatedAt;
}
