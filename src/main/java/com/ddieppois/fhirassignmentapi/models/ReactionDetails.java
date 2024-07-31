package com.ddieppois.fhirassignmentapi.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReactionDetails {
    private String substance;
    private List<String> manifestations;
    private String description;
}
