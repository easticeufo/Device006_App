package com.madongfang.api;

import java.util.List;

public class DeviceApi {

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ChannelApi> getChannels() {
		return channels;
	}

	public void setChannels(List<ChannelApi> channels) {
		this.channels = channels;
	}

	public InvestorApi getInvestor() {
		return investor;
	}

	public void setInvestor(InvestorApi investor) {
		this.investor = investor;
	}

	private String id;
	
	private List<ChannelApi> channels;

	private InvestorApi investor;
}
