/**
 * 
 */
package com.eazybytes.accounts.controller;

import com.eazybytes.accounts.model.*;
import com.eazybytes.accounts.service.client.CardFeignClient;
import com.eazybytes.accounts.service.client.LoanFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eazybytes.accounts.config.AccountsServiceConfig;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.List;

/**
 * @author Eazy Bytes
 *
 */

@RestController
@Slf4j
public class AccountsController {
	@Autowired
	private AccountsRepository accountsRepository;
	@Autowired
	AccountsServiceConfig accountsConfig;
	@Autowired
	CardFeignClient cardFeignClient;
	@Autowired
	LoanFeignClient loanFeignClient;
	@PostMapping("/myAccount")
	@Timed(value = "getAccountDetails.time", description = "get time request of get account detail")
	public Accounts getAccountDetails(@RequestBody Customer customer) {

		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		if (accounts != null) {
			return accounts;
		} else {
			return null;
		}

	}

	@PostMapping("/myCustomerDetails")
	@CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerDetailsFallback")
	@Retry(name = "retryForCustomerDetails", fallbackMethod = "myCustomerDetailsFallback")
	@Timed(value = "myCustomerDetails", description = "get time taken of request get my customer detail information")
	public CustomerDetails myCustomerDetails(@RequestHeader("eazybank-correlation-id") String correlationId, @RequestBody Customer customer) {
		log.info("myCustomerDetails() method started");
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		List<Loans> loans = loanFeignClient.getLoansDetails(correlationId, customer);
		List<Cards> cards = cardFeignClient.getCardDetails(correlationId, customer);

		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setCards(cards);
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		log.info("myCustomerDetails() method ended");
		return customerDetails;
	}

	public CustomerDetails myCustomerDetailsFallback(@RequestHeader("eazybank-correlation-id") String correlationId, Customer customer, Throwable t) {
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		List<Loans> loans = loanFeignClient.getLoansDetails(correlationId, customer);
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		return customerDetails;
	}

	@GetMapping("/account/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountsConfig.getMsg(), accountsConfig.getBuildVersion(),
				accountsConfig.getMailDetails(), accountsConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}

	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallBack")
	public String sayHello() {
		return "Hello, Welcome to EazyBank";
	}

	private String sayHelloFallBack(Throwable t) {
		return "Hi, Welcome Back";
	}
}
