package com.github.upcraftlp.votifier.net;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.RawVote;
import com.github.upcraftlp.votifier.config.TokenParser;
import com.github.upcraftlp.votifier.util.GsonInst;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;

import javax.crypto.Mac;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.UUID;

public class VotifierProtocol2VoteDecoder extends io.netty.handler.codec.MessageToMessageDecoder<String> {
    protected void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
        JsonObject voteMessage = GsonInst.gson.fromJson(s, JsonObject.class);
        VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();

        // Deserialize the payload.
        String payload = voteMessage.get("payload").getAsString();
        JsonObject votePayload = GsonInst.gson.fromJson(payload, JsonObject.class);

        // Verify challenge.
        if (!votePayload.get("challenge").getAsString().equals(session.getChallenge())) {
            throw new CorruptedFrameException("Challenge is not valid");
        }

        Key key = TokenParser.tokens.get(votePayload.get("serviceName").getAsString());

        if (key == null) {
            key = TokenParser.tokens.get("default");
            if (key == null) {
                throw new RuntimeException("Unknown service '" + votePayload.get("serviceName").getAsString() + "'");
            }
        }

		// Verify signature.
        String sigHash = voteMessage.get("signature").getAsString();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        mac.update(voteMessage.get("payload").getAsString().getBytes(StandardCharsets.UTF_8));
        String computed = DatatypeConverter.printBase64Binary(mac.doFinal());

        if (!sigHash.equals(computed)) {
            throw new CorruptedFrameException("Signature is not valid (invalid token?)");
        }

		// Stopgap: verify the "uuid" field is valid, if provided.
		if (votePayload.has("uuid")) {
			UUID.fromString(votePayload.get("uuid").getAsString());
		}

		if (votePayload.get("username").getAsString().length() > 16) {
			throw new CorruptedFrameException("Username too long");
		}

        RawVote vote = new RawVote();
        vote.setServiceName(votePayload.get("serviceName").getAsString());
        vote.setUsername(votePayload.get("username").getAsString());
        vote.setAddress(votePayload.get("address").getAsString());
        vote.setTimeStamp(votePayload.get("timestamp").getAsString());

        if (ForgeVotifier.isDebugMode()) {
            ForgeVotifier.getLogger().info("Received protocol v2 vote record -> " + vote);
        }

        list.add(vote);

        ctx.pipeline().remove(this);
    }
}
