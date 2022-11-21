package com.paymybuddy.paymybuddy.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "com.paymybuddy.paymybuddy")
public class DateTimePatternProperties {
	private String dateStringPattern;
	private String dateTimeStringPattern;
	private String localLanguage;
}
