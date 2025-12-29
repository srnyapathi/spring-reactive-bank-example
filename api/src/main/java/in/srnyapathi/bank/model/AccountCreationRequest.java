package in.srnyapathi.bank.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCreationRequest {
    @JsonProperty("document_number")
    @NotBlank(message = "Document number cannot be empty")
    private String documentNumber;
}
