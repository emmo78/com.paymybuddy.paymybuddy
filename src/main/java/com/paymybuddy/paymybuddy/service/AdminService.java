package com.paymybuddy.paymybuddy.service;

import java.time.LocalDate;

/**
 * Interface to bill a registered or to sum all fees over a period
 * @author Olivier MOREL
 *
 */
public interface AdminService {
	double billARegisteredForAPeriod(LocalDate beginDate, LocalDate endDate, String email);
	//will use TransanctionRepository : feeSumForARegisteredBetweenDate(beginDate: LocalDateTime, endDate: LocalDateTime, email: String): double;
	//and see TransactionRepositoryIT : testFeeSumForARegisteredBetweenDateShouldReturnTen() for example using Calendar
	double getAllFeesSumForAPeriod(LocalDate beginDate, LocalDate endDate);
	//will use TransactionRepository : feeSumBetweenDate(beginDate: LocalDateTime, endDate: LocalDateTime): double;
}
