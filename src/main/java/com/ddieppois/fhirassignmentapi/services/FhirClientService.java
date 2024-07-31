package com.ddieppois.fhirassignmentapi.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.springframework.stereotype.Component;

@Component
public class FhirClientService {

    private IGenericClient client;

    public synchronized IGenericClient getClient() {
        if (this.client == null) {
            FhirContext fhirContext = FhirContext.forR4();
            this.client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
            this.client.registerInterceptor(new LoggingInterceptor(false));
        }
        return this.client;
    }
}
