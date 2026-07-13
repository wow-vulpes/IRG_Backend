package sber.deonande.obligation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Запрос на создание нового обязательства (подписки/платежа)")
public class ObligationCreateRequest {
    @NotBlank
    @Schema(description = "Название (например, Netflix)", example = "Netflix")
    private String title;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @Schema(description = "Сумма платежа", example = "299.50")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    @Schema(description = "Валюта (3 буквы)", example = "RUB")
    private String currency;

    @NotNull
    @Schema(description = "Категория", example = "SUBSCRIPTION")
    private Category category;

    @Schema(description = "Периодичность (оставить пустым для разовых платежей)", example = "MONTHLY")
    private Recurrence recurrence;

    @Schema(description = "Дата следующего списания (ГГГГ-ММ-ДД)", example = "2024-05-15")
    private LocalDate nextPaymentDate;
}
