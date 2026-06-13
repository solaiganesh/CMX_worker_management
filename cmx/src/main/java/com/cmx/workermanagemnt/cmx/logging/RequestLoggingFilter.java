package com.cmx.workermanagemnt.cmx.logging;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		long start = System.currentTimeMillis();

		try {
			chain.doFilter(request, response);
		}
		finally {
			long duration = System.currentTimeMillis() - start;
			log.info("{} {} -> {} ({} ms)", httpRequest.getMethod(), maskUri(httpRequest.getRequestURI()),
					httpResponse.getStatus(), duration);
		}
	}

	private String maskUri(String uri) {
		return uri;
	}
}
