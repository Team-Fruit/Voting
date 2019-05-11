package com.github.upcraftlp.votifier.api;

public class Vote extends RawVote {
	private int voteCount;

	public Vote(String serviceName, String username, String address, String timeStamp, int voteCount) {
		super(serviceName, username, address, timeStamp);
		this.voteCount = voteCount;
	}

	public Vote(RawVote rawVote, int voteCount) {
		this(rawVote.getServiceName(), rawVote.getUsername(), rawVote.getAddress(), rawVote.getTimeStamp(), voteCount);
	}

	public Vote() {
		this.voteCount = 0;
	}

	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}

	public int getVoteCount() {
		return this.voteCount;
	}
}