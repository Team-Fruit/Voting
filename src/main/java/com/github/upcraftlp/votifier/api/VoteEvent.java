package com.github.upcraftlp.votifier.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class VoteEvent extends Event {

	private final String username;
    private final String service;
    private final String address;
    private final String timestamp;

    public VoteEvent(String username, String service, String address, String timestamp) {
        this.username = username;
        this.service = service;
        this.address = address;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getServiceDescriptor() {
        return service;
    }

    public String getRemoteAddress() {
        return address;
    }
}
