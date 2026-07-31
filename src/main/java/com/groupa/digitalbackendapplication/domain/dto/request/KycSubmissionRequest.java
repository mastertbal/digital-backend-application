package com.groupa.digitalbackendapplication.domain.dto.request;

import com.groupa.digitalbackendapplication.domain.enums.KycDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class KycSubmissionRequest {
//    @NotNull(message = "Please select document type")
    private KycDocumentType documentType;
//    @NotBlank(message = "Field cannot be empty")
    private String submittedValue;
}
