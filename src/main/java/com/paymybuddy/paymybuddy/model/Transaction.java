package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
public class Transaction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private long transactionId;

	@Column(name = "date_time")
	private Date dateTime;

	@Column(name = "amount")
	private double amount;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "email_sender")
	private Registered sender;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "email_receiver")
	private Registered receiver;

	public Transaction() {
		super();
	}
	
	public Transaction(Date dateTime, double amount) {
		super();
		this.dateTime = dateTime;
		this.amount = amount;
	}
	
	public double getMonetization() {
		return BigDecimal.valueOf(amount * 0.005).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}
