package com.github.upcraftlp.votifier.net;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import javax.crypto.Mac;
import javax.xml.bind.DatatypeConverter;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.config.TokenParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.netty.channel.ChannelHandlerContext;

public class VotifierProtocol2VoteDecoder extends io.netty.handler.codec.MessageToMessageDecoder<String> {
	protected void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
		JsonObject voteMessage = new Gson().fromJson(s, JsonObject.class);
		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();

		if (!voteMessage.get("challenge").getAsString().equals(session.getChallenge())) {
			throw new RuntimeException("Challenge is not valid");
		}

		JsonObject votePayload = voteMessage.get("payload").getAsJsonObject();

		Key key = TokenParser.tokens.get(votePayload.get("serviceName").getAsString());

		if (key==null) {
			key = TokenParser.tokens.get("default");
			if (key==null) {
				throw new RuntimeException("Unknown service '"+votePayload.get("serviceName").getAsString()+"'");
			}
		}

		String sigHash = voteMessage.get("signature").getAsString();
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		mac.update(voteMessage.get("payload").getAsString().getBytes(StandardCharsets.UTF_8));
		String computed = DatatypeConverter.printBase64Binary(mac.doFinal());

		if (!sigHash.equals(computed)) {
			throw new RuntimeException("Signature is not valid (invalid token?)");
		}

		Vote vote = new Vote();
		vote.setServiceName(votePayload.get("serviceName").getAsString());
		vote.setUsername(votePayload.get("username").getAsString());
		vote.setAddress(votePayload.get("address").getAsString());
		vote.setTimeStamp(votePayload.get("timestamp").getAsString());

		if (ForgeVotifier.isDebugMode()) {
			ForgeVotifier.getLogger().info("Received protocol v2 vote record -> "+vote);
		}
		list.add(vote);

		ctx.pipeline().remove(this);
	}
}
