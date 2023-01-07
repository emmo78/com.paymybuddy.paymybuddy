package com.paymybuddy.paymybuddy.dto.service;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

/**
 * service interface for converting DTOs to the model and vice versa
 * use DateTimePatternProperties
 * @author Olivier MOREL
 *
 */
public interface RegisteredDTOService {
	RegisteredDTO registeredToDTO(Registered registered);
	RegisteredForListDTO registeredToForListDTO(Registered registered);
	Registered registeredFromDTO(RegisteredDTO registeredDTO);
}
