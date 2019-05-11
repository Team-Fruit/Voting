package com.github.upcraftlp.votifier.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class MilestoneEvent extends Event {

	private final String uuid;
	private final Milestone lastMilestone;
	private Milestone milestone;

	public MilestoneEvent(final String uuid, final Milestone lastMilestone, final Milestone milestone) {
		this.uuid = uuid;
		this.lastMilestone = lastMilestone;
		this.milestone = milestone;
	}

	public String getUUID() {
		return this.uuid;
	}

	public Milestone getLastMilestone() {
		return this.lastMilestone;
	}

	public Milestone getMilestone() {
		return this.milestone;
	}

	public void setMilestone(final Milestone milestone) {
		this.milestone = milestone;
	}
}
