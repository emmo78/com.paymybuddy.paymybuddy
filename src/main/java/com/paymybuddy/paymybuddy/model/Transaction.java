package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class Transaction implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private long transactionId;
	
	@Column(name = "date_time")
	private Date dateTime;
	
	@Column(name = "amont")
	private double amont;
	
	@Column(name = "email_sender")
	private String emailSender;
	
	@Column(name = "email_receiver")
	private String emailReceiver;
	
	public double getMonetization() {
		return BigDecimal.valueOf(amont * 0.005).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
}
