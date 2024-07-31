package com.ddieppois.fhirassignmentapi.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AllergyIntoleranceResponse {
    private String id;
    private String code;
    private String type;
    private String clinicalStatus;
    private String verificationStatus;
    private String note;
    private List<ReactionDetails> reactions;
}
