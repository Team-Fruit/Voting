package com.github.upcraftlp.votifier.api;

public class Milestone {
	private int voteCount;
	private int loginCount;
	private long lastLogin;

	public Milestone(int voteCount, int loginCount, long lastLogin) {
		this.voteCount = voteCount;
		this.loginCount = loginCount;
		this.lastLogin = lastLogin;
	}

	public Milestone() {
	}

	@Override
	public String toString() {
		return String.format("Milestone [voteCount=%s, loginCount=%s, lastLogin=%s]", voteCount, loginCount, lastLogin);
	}

	public int getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}
}