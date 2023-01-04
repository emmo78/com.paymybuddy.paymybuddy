package com.paymybuddy.paymybuddy.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration property date time pattern and local for DTO services 
 * @author Olivier MOREL
 *
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "com.paymybuddy.paymybuddy")
public class DateTimePatternProperties {
	private String dateStringPattern;
	private String dateTimeStringPattern;
	private String localLanguage;
}
