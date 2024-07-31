package com.ddieppois.fhirassignmentapi.services;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.ddieppois.fhirassignmentapi.models.AllergyIntoleranceResponse;
import com.ddieppois.fhirassignmentapi.models.ReactionDetails;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FhirPatientService {

    @Autowired
    private FhirClientService fhirClientService;

    @Cacheable("patientsWithAllergies")
    public List<Patient> searchPatientsWithAllergies() {
        IGenericClient client = fhirClientService.getClient();
        // Search for AllergyIntolerance resources, only fetching the first page of results
        Bundle bundle = client
                .search()
                .forResource(AllergyIntolerance.class)
                .returnBundle(Bundle.class)
                .execute();

        // Use a Set to store unique patient references
        Set<String> patientReferences = new HashSet<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            AllergyIntolerance allergy = (AllergyIntolerance) entry.getResource();
            if (allergy.hasPatient()) {
                String patientReference = allergy.getPatient().getReference();
                patientReferences.add(patientReference);
            }
        }

        // Retrieve Patient resources using the collected patient references
        List<Patient> patients = new ArrayList<>();
        for (String patientReference : patientReferences) {
            Patient patient = client
                    .read()
                    .resource(Patient.class)
                    .withUrl(patientReference)
                    .execute();
            patients.add(patient);
        }
        return patients;
    }

    public List<AllergyIntolerance> getAllergiesForPatient(String patientId) {
        Bundle bundle = fhirClientService.getClient().
                search()
                .forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<AllergyIntolerance> allergies = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            allergies.add((AllergyIntolerance) entry.getResource());
        }
        return allergies;
    }

    public void updateAllergiesForPatient(String patientId, List<AllergyIntoleranceResponse> updatedAllergies, List<AllergyIntoleranceResponse> deletedAllergies) {
        IGenericClient client = fhirClientService.getClient();

        for (AllergyIntoleranceResponse allergyResponse : updatedAllergies) {
            AllergyIntolerance allergy = retrieveAndUpdateAllergy(allergyResponse);
            if (allergy.hasId()) {
                client
                        .update()
                        .resource(allergy)
                        .execute();
            } else {
                // Set the patient reference for new allergies
                allergy.setPatient(new Reference("Patient/" + patientId));
                client
                        .create()
                        .resource(allergy)
                        .execute();
            }
        }

        for (AllergyIntoleranceResponse allergyResponse : deletedAllergies) {
            client
                    .delete()
                    .resourceById(new IdType("AllergyIntolerance", allergyResponse.getId()))
                    .execute();
        }
    }

    private AllergyIntolerance retrieveAndUpdateAllergy(AllergyIntoleranceResponse response) {
        IGenericClient client = fhirClientService.getClient();
        AllergyIntolerance allergy;

        if (StringUtils.isNoneBlank(response.getId())) {
            allergy = client.read().resource(AllergyIntolerance.class).withId(response.getId()).execute();
        } else {
            allergy = new AllergyIntolerance();
        }

        if (response.getCode() != null) {
            Coding coding = new Coding();
            coding.setDisplay(response.getCode());
            CodeableConcept code = new CodeableConcept();
            code.addCoding(coding);
            allergy.setCode(code);
        }

        if (response.getType() != null) {
            allergy.setType(AllergyIntolerance.AllergyIntoleranceType.fromCode(response.getType()));
        }

        if (response.getClinicalStatus() != null) {
            Coding clinicalCoding = new Coding();
            clinicalCoding.setDisplay(response.getClinicalStatus());
            CodeableConcept clinicalStatus = new CodeableConcept();
            clinicalStatus.addCoding(clinicalCoding);
            allergy.setClinicalStatus(clinicalStatus);
        }

        if (response.getVerificationStatus() != null) {
            Coding verificationCoding = new Coding();
            verificationCoding.setDisplay(response.getVerificationStatus());
            CodeableConcept verificationStatus = new CodeableConcept();
            verificationStatus.addCoding(verificationCoding);
            allergy.setVerificationStatus(verificationStatus);
        }

        if (response.getNote() != null) {
            Annotation note = new Annotation();
            note.setText(response.getNote());
            allergy.addNote(note);
        }

        if (response.getReactions() != null) {
            for (ReactionDetails reactionDetail : response.getReactions()) {
                AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = new AllergyIntolerance.AllergyIntoleranceReactionComponent();

                if (reactionDetail.getSubstance() != null) {
                    Coding substanceCoding = new Coding();
                    substanceCoding.setDisplay(reactionDetail.getSubstance());
                    CodeableConcept substance = new CodeableConcept();
                    substance.addCoding(substanceCoding);
                    reaction.setSubstance(substance);
                }

                if (reactionDetail.getManifestations() != null) {
                    for (String manifestation : reactionDetail.getManifestations()) {
                        Coding manifestationCoding = new Coding();
                        manifestationCoding.setDisplay(manifestation);
                        CodeableConcept manifestationConcept = new CodeableConcept();
                        manifestationConcept.addCoding(manifestationCoding);
                        reaction.addManifestation(manifestationConcept);
                    }
                }

                if (reactionDetail.getDescription() != null) {
                    reaction.setDescription(reactionDetail.getDescription());
                }

                allergy.addReaction(reaction);
            }
        }

        return allergy;
    }
}
