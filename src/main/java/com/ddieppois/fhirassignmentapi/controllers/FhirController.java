package com.ddieppois.fhirassignmentapi.controllers;

import com.ddieppois.fhirassignmentapi.models.AllergyIntoleranceResponse;
import com.ddieppois.fhirassignmentapi.models.AllergyUpdateRequest;
import com.ddieppois.fhirassignmentapi.models.PatientResponse;
import com.ddieppois.fhirassignmentapi.models.ReactionDetails;
import com.ddieppois.fhirassignmentapi.services.FhirPatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Annotation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class FhirController {

    private final FhirPatientService fhirPatientService;

    @GetMapping("/with-allergies")
    public ResponseEntity<List<PatientResponse>> getPatientsWithAllergies() {
        val patients = fhirPatientService.searchPatientsWithAllergies();
        List<PatientResponse> patientResponses = patients.stream()
                .filter(patient -> StringUtils.isNoneEmpty(patient.getNameFirstRep().getGivenAsSingleString())
                        && StringUtils.isNoneEmpty(patient.getNameFirstRep().getFamily()))
                .map(patient -> PatientResponse.builder()
                        .id(patient.getIdElement().getIdPart())
                        .firstName(patient.getNameFirstRep().getGivenAsSingleString())
                        .lastName(patient.getNameFirstRep().getFamily())
                        .build())
                .toList();
        return ResponseEntity.ok(patientResponses);
    }

    @GetMapping("/{patientId}/allergies")
    public ResponseEntity<List<AllergyIntoleranceResponse>> getAllergiesForPatient(@PathVariable String patientId) {
        List<AllergyIntolerance> allergies = fhirPatientService.getAllergiesForPatient(patientId);
        List<AllergyIntoleranceResponse> allergyResponses = allergies.stream()
                .map(allergy -> AllergyIntoleranceResponse.builder()
                        .id(allergy.getIdElement().getIdPart())
                        .clinicalStatus(allergy.hasClinicalStatus() ? allergy.getClinicalStatus().getCodingFirstRep().getDisplay() : null)
                        .verificationStatus(allergy.hasVerificationStatus() ? allergy.getVerificationStatus().getCodingFirstRep().getDisplay() : null)
                        .type(allergy.hasType() ? allergy.getType().toCode() : null)
                        .code(allergy.hasCode() ? allergy.getCode().getCodingFirstRep().getDisplay() : null)
                        .note(allergy.hasNote() ? allergy.getNote().stream().map(Annotation::getText).collect(Collectors.joining(", ")) : null)
                        .reactions(allergy.getReaction().stream().map(reaction -> ReactionDetails.builder()
                                .substance(reaction.hasSubstance() ? reaction.getSubstance().getCodingFirstRep().getDisplay() : null)
                                .manifestations(reaction.getManifestation().stream().map(manifestation -> manifestation.getCodingFirstRep().getDisplay()).toList())
                                .description(reaction.hasDescription() ? reaction.getDescription() : null)
                                .build()).toList())
                        .build())
                .toList();
        return ResponseEntity.ok(allergyResponses);
    }

    @PostMapping(value = "/{patientId}/allergies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateAllergiesForPatient(@PathVariable String patientId, @RequestBody AllergyUpdateRequest allergyUpdateRequest) {
        fhirPatientService.updateAllergiesForPatient(patientId, allergyUpdateRequest.getUpdatedAllergies(), allergyUpdateRequest.getDeletedAllergies());
        return ResponseEntity.ok().build();
    }

}
