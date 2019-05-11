package com.github.upcraftlp.votifier.api;

public class Vote {
	private String serviceName;
	private String username;
	private String address;
	private String timeStamp;

	public Vote(String serviceName, String username, String address, String timeStamp) {
		this.serviceName = serviceName;
		this.username = username;
		this.address = address;
		this.timeStamp = timeStamp;
	}

	public Vote() {
	}

	public String toString() {
		return "Vote (from:"+this.serviceName+" username:"+this.username+" address:"+this.address+" timeStamp:"+this.timeStamp+")";
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setUsername(String username) {
		this.username = username.length()<=16 ? username : username.substring(0, 16);
	}

	public String getUsername() {
		return this.username;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return this.address;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTimeStamp() {
		return this.timeStamp;
	}
}