package com.paymybuddy.paymybuddy.dto.service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.paymybuddy.paymybuddy.configuration.DateTimePatternProperties;
import com.paymybuddy.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.paymybuddy.exception.ParseRuntimeException;
import com.paymybuddy.paymybuddy.model.Transaction;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TransactionDTOServiceImpl implements TransactionDTOService {
	
	private final ModelMapper modelMapper;
	
	private final DateTimePatternProperties dateStringPattern;
	
	@Override
	public TransactionDTO transactionToDTOSender(Transaction transaction) {
		Converter<LocalDateTime, String> dateTimeToString = new AbstractConverter<LocalDateTime, String>() {
			@Override
			protected String convert(LocalDateTime dateTime) {
				return dateTime.format(DateTimeFormatter.ofPattern(dateStringPattern.getDateTimeStringPattern()));
			}	
		};
		Converter<Double, String> amountFeeToNegString = new AbstractConverter<Double, String>() {
			@Override
			protected String convert(Double amountFee) {
				amountFee = 0 - amountFee;
				return String.format(new Locale(dateStringPattern.getLocalLanguage()) ,"%.2f", amountFee);
			}
		};
		
		modelMapper.typeMap(Transaction.class, TransactionDTO.class).addMappings(mapper -> {
			mapper.using(dateTimeToString).map(Transaction::getDateTime, TransactionDTO::setDateTime);
			mapper.using(amountFeeToNegString).map(Transaction::getAmount, TransactionDTO::setAmount);
			mapper.using(amountFeeToNegString).map(Transaction::getFee, TransactionDTO::setFee);
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
	
	@Override
	public Transaction transactionFromNewTransactionDTO(TransactionDTO transactionDTO) throws MappingException{
		Converter<String, Double> stringToDouble = new AbstractConverter<String, Double>() {
			@Override
			protected Double convert(String amountString) {
				Double amount = null;
				try { 
					amount = NumberFormat.getInstance(new Locale(dateStringPattern.getLocalLanguage())).parse(amountString).doubleValue();
				} catch (ParseException e) {
					throw new ParseRuntimeException(e.getMessage());
				}
				return amount;
			}
		};
		
		modelMapper.typeMap(TransactionDTO.class, Transaction.class).addMappings(mapper -> {
			mapper.using(stringToDouble).map(TransactionDTO::getAmount, Transaction::setAmount);
			mapper.skip(Transaction::setDateTime);
			mapper.skip(Transaction::setFee);
		});
		
		Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
		transaction.setDateTime(LocalDateTime.now());
		transaction.monetize();
		return transaction;
	}	
}
