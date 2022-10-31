package com.paymybuddy.paymybuddy.dto.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

@Service
public class RegisteredDTOServiceImpl implements RegisteredDTOService {
		
	@Autowired
	private ModelMapper modelMapper;

	@Override
	public RegisteredDTO registeredToDTO(Registered registered) {
		modelMapper.typeMap(Registered.class, RegisteredDTO.class).addMappings(mapper -> mapper.skip(RegisteredDTO::setPassword));
		return modelMapper.map(registered, RegisteredDTO.class);
	}

	@Override
	public RegisteredForListDTO registeredToForListDTO(Registered registered) {
		return modelMapper.map(registered, RegisteredForListDTO.class);
	}

	@Override
	public Registered registeredFromDTO(RegisteredDTO registeredDTO) {
		return modelMapper.map(registeredDTO, Registered.class);
	}

}
