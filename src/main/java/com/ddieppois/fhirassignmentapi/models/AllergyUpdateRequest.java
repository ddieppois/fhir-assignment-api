package com.ddieppois.fhirassignmentapi.models;

import lombok.Data;
import org.hl7.fhir.r4.model.AllergyIntolerance;

import java.util.List;

@Data
public class AllergyUpdateRequest {
    private List<AllergyIntoleranceResponse> updatedAllergies;
    private List<AllergyIntoleranceResponse> deletedAllergies;
}

