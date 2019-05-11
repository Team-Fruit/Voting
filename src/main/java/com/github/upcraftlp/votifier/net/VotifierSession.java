package com.github.upcraftlp.votifier.net;

import com.github.upcraftlp.votifier.util.TokenUtil;

import io.netty.util.AttributeKey;

public class VotifierSession {
	public static final AttributeKey<VotifierSession> KEY = AttributeKey.newInstance("votifier_session");
	private ProtocolVersion version;
	private final String challenge;

	public VotifierSession() {
		this.version = ProtocolVersion.UNKNOWN;

		this.challenge = TokenUtil.newToken();
	}

	public void setVersion(ProtocolVersion version) {
		if (this.version!=ProtocolVersion.UNKNOWN) {
			throw new IllegalStateException("Protocol version already switched");
		} else {
			this.version = version;
		}
	}

	public ProtocolVersion getVersion() {
		return this.version;
	}

	public String getChallenge() {
		return this.challenge;
	}

	public static enum ProtocolVersion {
		UNKNOWN,
		ONE,
		TWO;
	}
}
