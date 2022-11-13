package com.paymybuddy.paymybuddy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;

@Service
public class RequestServiceImpl implements RequestService {

	@Override
	public String requestToString(WebRequest request) {
		StringBuffer parameters = new StringBuffer(request.getDescription(false)+"?"); 
		request.getParameterMap().forEach((p,v) -> {
			parameters.append(p+"=");
			int i=0;
			while (i<(v.length-1)) {
				parameters.append(v[i]+",");
				i++;}
			parameters.append(v[i]+"&");
		});
		int length = parameters.length();
		parameters.delete(length-1, length);
		return parameters.toString();
	}
}