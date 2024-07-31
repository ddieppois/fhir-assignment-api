package com.ddieppois.fhirassignmentapi.configuration;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    @Scheduled(fixedRate = 600000)
    @CacheEvict(value = "patientsWithAllergies", allEntries = true)
    public void clearPatientsWithAllergiesCache() {
        // This method will automatically clear the cache every 10 minutes
    }
}
