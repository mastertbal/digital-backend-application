package com.groupa.digitalbackendapplication.domain.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;

public class UpgradeRequest {
    @Pattern(regexp = "[0-9]{11}", message = "Invalid nin")
    private String nin;

    @Pattern(regexp = "[0-9]{11}", message = "Invalid bvn")
    private String bvn;
}
