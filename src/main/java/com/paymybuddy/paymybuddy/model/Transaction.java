package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	@EqualsAndHashCode.Include
	private long transactionId;

	@Column(name = "date_time")
	private Timestamp dateTime;

	@Column(name = "amount")
	private double amount;
	
	@Column(name = "fee")
	private double fee;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "email_sender")
	private Registered sender;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "email_receiver")
	private Registered receiver;

	public Transaction() {
	}
	
	public Transaction(Timestamp dateTime, double amount) {
		this.dateTime = dateTime;
		this.amount = amount;
		monetize();
	}
	
	public void monetize() {
		fee = BigDecimal.valueOf(amount * 0.005).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}
