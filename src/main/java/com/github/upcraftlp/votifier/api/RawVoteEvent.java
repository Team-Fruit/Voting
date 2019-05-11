package com.github.upcraftlp.votifier.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class RawVoteEvent extends Event {

	private final RawVote vote;

	public RawVoteEvent(final RawVote vote) {
		this.vote = vote;
	}

	public RawVote getVote() {
		return this.vote;
	}
}
