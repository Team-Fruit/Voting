package com.github.upcraftlp.votifier.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class VoteEvent extends Event {

	private final Vote vote;

	public VoteEvent(final Vote vote) {
		this.vote = vote;
	}

	public Vote getVote() {
		return this.vote;
	}
}
