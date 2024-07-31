package com.ddieppois.fhirassignmentapi.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientResponse {
    private String id;
    private String firstName;
    private String lastName;

    public PatientResponse(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
