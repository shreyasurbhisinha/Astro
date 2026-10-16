package com.astro.kundli;

import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class TimeZoneConfig {

    @Bean
    @Lazy
    TimeZoneEngine timeZoneEngine() {
        return TimeZoneEngine.initialize();
    }
}
