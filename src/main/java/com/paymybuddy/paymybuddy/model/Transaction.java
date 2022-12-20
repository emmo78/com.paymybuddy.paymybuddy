package com.paymybuddy.paymybuddy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class Transaction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	@EqualsAndHashCode.Include
	private long transactionId;

	@Column(name = "date_time")
	private LocalDateTime dateTime;

	@Column(name = "amount")
	private double amount;
	
	@Column(name = "fee")
	private double fee;
	
	@Column(name = "description")
	private String description;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "email_sender")
	private Registered sender;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "email_receiver")
	private Registered receiver;

	public void monetize() {
		fee = BigDecimal.valueOf(amount * 0.005).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}
