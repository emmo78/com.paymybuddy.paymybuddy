package com.paymybuddy.paymybuddy.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class RequestServiceTest {
	
	private RequestService requestService;
	private MockHttpServletRequest requestMock;
	private WebRequest request;
	
	@BeforeEach
	public void setUpPerTest() {
		requestService = new RequestServiceImpl();
	}
	
	@AfterEach
	public void undefPerTest() {
		requestService = null;
	}
	
	@Test
	@Tag("RequestServiceTest")
	@DisplayName("upperCasingFirstLetter test should Capitalize the first letter and lowercase the rest")
	public void upperCasingFirstLetterTest() {
		//GIVEN
		String totest = "tOTEST";
		//WHEN
		String result = requestService.upperCasingFirstLetter(totest);
		//THEN
		assertThat(result).isEqualTo("Totest");
	}
	
	@Test
	@Tag("RequestServiceTest")
	@DisplayName("requestToString test chain a WebRequest uri+parameters into a String")
	public void requestToStringTest() {
		//GIVEN
		requestMock = new MockHttpServletRequest();
		requestMock.setServerName("http://localhost:8080");
		requestMock.setRequestURI("/phoneAlert");
		String[] params = {"1", "2"};
		requestMock.setParameter("firestation", params);
		request = new ServletWebRequest(requestMock);
		//WHEN
		String parameters = requestService.requestToString(request);
		//THEN
		assertThat(parameters).isEqualTo("uri=/phoneAlert?firestation=1,2");
	}
}
