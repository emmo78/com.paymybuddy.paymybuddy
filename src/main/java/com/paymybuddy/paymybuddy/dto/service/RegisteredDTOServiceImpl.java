package com.paymybuddy.paymybuddy.dto.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

@Service
public class RegisteredDTOServiceImpl implements RegisteredDTOService {
		
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private DateTimePatternProperties dateStringPattern;

	@Override
	public RegisteredDTO registeredToDTO(Registered registered) {
		Converter<LocalDate, String> dateToString = new AbstractConverter<LocalDate, String>() {
			@Override
			protected String convert(LocalDate date) {
				return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));//dateStringPattern.getDateStringPattern()
			}	
		};
		modelMapper.typeMap(Registered.class, RegisteredDTO.class).addMappings(mapper -> {
			mapper.using(dateToString).map(Registered::getBirthDate, RegisteredDTO::setBirthDate);
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
				return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));//dateStringPattern.getDateStringPattern()
			}	
		};
		modelMapper.typeMap(RegisteredDTO.class, Registered.class).addMappings(mapper -> 
			mapper.using(stringToDate).map(RegisteredDTO::getBirthDate, Registered::setBirthDate));
		return modelMapper.map(registeredDTO, Registered.class);
	}

}
