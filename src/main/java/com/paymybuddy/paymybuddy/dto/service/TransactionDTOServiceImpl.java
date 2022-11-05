package com.paymybuddy.paymybuddy.dto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.model.Transaction;

@Service
public class TransactionDTOServiceImpl implements TransactionDTOService {
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private DateTimePatternProperties dateStringPattern;
	
	@Override
	public TransactionDTO transactionToDTO(Transaction transaction) {
		Converter<LocalDateTime, String> dateTimeToString = new AbstractConverter<LocalDateTime, String>() {
			@Override
			protected String convert(LocalDateTime dateTimme) {
				return dateTimme.format(DateTimeFormatter.ofPattern(dateStringPattern.getDateTimeStringPattern()));
			}	
		};
		Converter<Double, String> doubleToString = new AbstractConverter<Double, String>() {
			@Override
			protected String convert(Double balance) {
				return String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", balance);
			}
		};
		modelMapper.typeMap(Transaction.class, TransactionDTO.class).addMappings(mapper -> {
			mapper.using(dateTimeToString).map(Transaction::getDateTime, TransactionDTO::setDateTime);
			mapper.using(doubleToString).map(Transaction::getAmount, TransactionDTO::setAmount);
			mapper.using(doubleToString).map(Transaction::getFee, TransactionDTO::setFee);
			mapper.skip(TransactionDTO::setReceiver);
		});
		return modelMapper.map(transaction, TransactionDTO.class);
	}

	@Override
	public TransactionDTO transactionToDTOSender(Transaction transaction) {
		TransactionDTO transactionDTO = transactionToDTO(transaction);
		transactionDTO.setReceiver(false);
		return transactionDTO;
	}

	@Override
	public TransactionDTO transactionToDTOReceiver(Transaction transaction) {
		TransactionDTO transactionDTO = transactionToDTO(transaction);
		transactionDTO.setReceiver(true);
		return transactionDTO;
	}

}
