package com.github.upcraftlp.votifier.net;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.RawVote;
import com.github.upcraftlp.votifier.util.RSAUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

public class VotifierProtocol1Decoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) {
		if (buf.readableBytes() < 256) {
			return;
		}

		byte[] block = new byte[buf.readableBytes()];
		buf.getBytes(0, block);
		// "Drain" the whole buffer
		buf.readerIndex(buf.readableBytes());

		try {
			block = RSAUtil.decrypt(block, RSAUtil.getKeyPair().getPrivate());
		} catch (Exception e) {
			throw new CorruptedFrameException("Could not decrypt data (is your key correct?)", e);
		}

		// Parse the string we received.
		String all = new String(block, StandardCharsets.UTF_8);
		String[] split = all.split("\n");
		if (split.length < 5) {
			throw new CorruptedFrameException("Not enough fields specified in vote.");
		}

		if (!split[0].equals("VOTE")) {
			throw new CorruptedFrameException("VOTE opcode not found");
		}

		// Create the vote.
		RawVote vote = new RawVote(split[1], split[2], split[3], split[4]);

		if (ForgeVotifier.isDebugMode()) {
			ForgeVotifier.getLogger().info("Received protocol v1 vote record -> "+vote);
		}

		list.add(vote);

		// We are done, remove ourselves. Why? Sometimes, we will decode multiple vote messages.
		// Netty doesn't like this, so we must remove ourselves from the pipeline. With Protocol 1,
		// ending votes is a "fire and forget" operation, so this is safe.
		ctx.pipeline().remove(this);
	}
}
