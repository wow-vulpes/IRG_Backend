package sber.deonande.obligation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class UpcomingResponse {
    private List<ObligationResponse> obligations;
    private Map<String, BigDecimal> totals;
    private List<RenewalAlert> renewalAlerts;
}
