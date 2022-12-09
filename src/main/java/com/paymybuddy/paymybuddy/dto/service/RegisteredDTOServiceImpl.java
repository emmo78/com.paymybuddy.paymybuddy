package com.paymybuddy.paymybuddy.dto.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RegisteredDTOServiceImpl implements RegisteredDTOService {
		
	private final ModelMapper modelMapper;
	
	private final DateTimePatternProperties dateStringPattern;
	
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public RegisteredDTO registeredToDTO(Registered registered) {
		Converter<LocalDate, String> dateToString = new AbstractConverter<LocalDate, String>() {
			@Override
			protected String convert(LocalDate birthDate) {
				return birthDate.format(DateTimeFormatter.ofPattern(dateStringPattern.getDateStringPattern()));
			}	
		};
		Converter<Double, String> doubleToString = new AbstractConverter<Double, String>() {
			@Override
			protected String convert(Double balance) {
				return String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", balance);
			}
			
		};
		modelMapper.typeMap(Registered.class, RegisteredDTO.class).addMappings(mapper -> {
			mapper.using(dateToString).map(Registered::getBirthDate, RegisteredDTO::setBirthDate);
			mapper.using(doubleToString).map(Registered::getBalance, RegisteredDTO::setBalance);
			mapper.skip(RegisteredDTO::setPassword);
			});
		return modelMapper.map(registered, RegisteredDTO.class);
	}

	@Override
	public RegisteredForListDTO registeredToForListDTO(Registered registered) {
		return modelMapper.map(registered, RegisteredForListDTO.class);
	}

	@Override
	public Registered registeredFromDTO(RegisteredDTO registeredDTO) {
		Converter<String, LocalDate> stringToDate = new AbstractConverter<String, LocalDate>() {
			@Override
			protected LocalDate convert(String stringDate) {
				//LocalDate implements TemporalAdjuster so 02/31/1991 -> 02/28/1991
				return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(dateStringPattern.getDateStringPattern()));
			}	
		};
		Converter<String, String> toLowerCase = new AbstractConverter<String, String>() {
			protected String convert(String email) {
				return email.toLowerCase();
			}
		};
		Converter<String, String> encodePW = new AbstractConverter<String, String>() {
			protected String convert(String passwd) {
				return passwd == null ? null : passwordEncoder.encode(passwd);
			}
		};
		modelMapper.typeMap(RegisteredDTO.class, Registered.class).addMappings(mapper -> {
			mapper.using(stringToDate).map(RegisteredDTO::getBirthDate, Registered::setBirthDate);
			mapper.using(toLowerCase).map(RegisteredDTO::getEmail, Registered::setEmail);
			mapper.using(encodePW).map(RegisteredDTO::getPassword, Registered::setPassword);
		});
		return modelMapper.map(registeredDTO, Registered.class);
	}

}
