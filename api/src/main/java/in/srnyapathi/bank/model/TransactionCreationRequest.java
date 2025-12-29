package in.srnyapathi.bank.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreationRequest {

    @JsonProperty("account_id")
    @NotNull
    private Long accountId;

    @JsonProperty("operation_type_id")
    @NotNull
    private Long operationTypeId;

    @JsonProperty("amount")
    @NotNull
    private BigDecimal amount;
}
