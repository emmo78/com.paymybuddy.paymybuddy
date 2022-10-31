package com.paymybuddy.paymybuddy.service;

import java.time.LocalDate;

public interface AdminService {
	double billARegisteredForAPeriod(LocalDate beginDate, LocalDate endDate, String email);
	//will use TransanctionRepository : feeSumForARegisteredBetweenDate(beginDate: Timestamp, endDate: Timestamp, email: String): double;
	//and see TransactionRepositoryIT : testFeeSumForARegisteredBetweenDateShouldReturnTen() for example using Calendar
	double getAllFeesSumForAPeriod(LocalDate beginDate, LocalDate endDate);
	//will use TransanctionRepository : feeSumBetweenDate(beginDate: Timestamp, endDate: Timestamp): double;
}
