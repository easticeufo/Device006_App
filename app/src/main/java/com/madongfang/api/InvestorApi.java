package com.madongfang.api;

public class InvestorApi {

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCashierUsername() {
		return cashierUsername;
	}

	public void setCashierUsername(String cashierUsername) {
		this.cashierUsername = cashierUsername;
	}

	public String getCashierPassword() {
		return cashierPassword;
	}

	public void setCashierPassword(String cashierPassword) {
		this.cashierPassword = cashierPassword;
	}

	private Integer id;
	
	private String cashierUsername;
	
	private String cashierPassword;
	
}
