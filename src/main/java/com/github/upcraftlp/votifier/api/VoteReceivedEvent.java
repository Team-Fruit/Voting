package com.github.upcraftlp.votifier.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class VoteReceivedEvent extends PlayerEvent {

	private final Vote vote;

	public VoteReceivedEvent(EntityPlayerMP player, Vote vote) {
		super(player);
		this.vote = vote;
	}

	public Vote getVote() {
		return this.vote;
	}
}
