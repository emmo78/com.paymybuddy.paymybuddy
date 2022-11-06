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
	public TransactionDTO transactionToDTOSender(Transaction transaction) {
		Converter<LocalDateTime, String> dateTimeToString = new AbstractConverter<LocalDateTime, String>() {
			@Override
			protected String convert(LocalDateTime dateTimme) {
				return dateTimme.format(DateTimeFormatter.ofPattern(dateStringPattern.getDateTimeStringPattern()));
			}	
		};
		Converter<Double, String> amountToNegString = new AbstractConverter<Double, String>() {
			@Override
			protected String convert(Double amount) {
				amount = 0 - amount;
				return String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", amount);
			}
		};
		Converter<Double, String> feeToString = new AbstractConverter<Double, String>() {
			@Override
			protected String convert(Double fee) {
				return String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", fee);
			}
		};

		modelMapper.typeMap(Transaction.class, TransactionDTO.class).addMappings(mapper -> {
			mapper.using(dateTimeToString).map(Transaction::getDateTime, TransactionDTO::setDateTime);
			mapper.using(amountToNegString).map(Transaction::getAmount, TransactionDTO::setAmount);
			mapper.using(feeToString).map(Transaction::getFee, TransactionDTO::setFee);
			mapper.skip(TransactionDTO::setReceiver);
		});
		TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
		transactionDTO.setReceiver(false);
		return transactionDTO;
	}

	@Override
	public TransactionDTO transactionToDTOReceiver(Transaction transaction) {
		Converter<LocalDateTime, String> dateTimeToString = new AbstractConverter<LocalDateTime, String>() {
			@Override
			protected String convert(LocalDateTime dateTimme) {
				return dateTimme.format(DateTimeFormatter.ofPattern(dateStringPattern.getDateTimeStringPattern()));
			}	
		};
		Converter<Double, String> amountToString = new AbstractConverter<Double, String>() {
			@Override
			protected String convert(Double amount) {
				return String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", amount);
			}
		};
		modelMapper.typeMap(Transaction.class, TransactionDTO.class).addMappings(mapper -> {
			mapper.using(dateTimeToString).map(Transaction::getDateTime, TransactionDTO::setDateTime);
			mapper.using(amountToString).map(Transaction::getAmount, TransactionDTO::setAmount);
			mapper.skip(TransactionDTO::setFee);
			mapper.skip(TransactionDTO::setReceiver);
		});
		TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
		transactionDTO.setFee("0.00");
		transactionDTO.setReceiver(true);
		return transactionDTO;
	}
}
