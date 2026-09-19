package com.groupa.digitalbackendapplication.domain.dto.request;

import com.groupa.digitalbackendapplication.domain.enums.PersonalAccountType;

public record SecondaryAccountCreationRequest(PersonalAccountType type) {
}
