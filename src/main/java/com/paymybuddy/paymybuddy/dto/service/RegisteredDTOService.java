package com.paymybuddy.paymybuddy.dto.service;

import com.paymybuddy.paymybuddy.dto.RegisteredDTO;
import com.paymybuddy.paymybuddy.dto.RegisteredForListDTO;
import com.paymybuddy.paymybuddy.model.Registered;

public interface RegisteredDTOService {
	RegisteredDTO registeredToDTO(Registered registered);
	RegisteredForListDTO registeredToForListDTO(Registered registered);
	Registered registeredFromDTO(RegisteredDTO registeredDTO);
}
