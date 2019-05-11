package com.github.upcraftlp.votifier.net;

import java.util.List;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.util.RSAUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

public class VotifierProtocol1Decoder extends ByteToMessageDecoder {
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
		int readable = buf.readableBytes();

		if (readable<256) {
			return;
		}

		byte[] block = new byte[256];
		buf.getBytes(0, block);
		try {
			block = RSAUtil.decrypt(block, RSAUtil.getKeyPair().getPrivate());
		} catch (Exception e) {
			throw new CorruptedFrameException("Could not decrypt data", e);
		}
		int position = 0;

		String opcode = readString(block, position);
		position += opcode.length()+1;
		if (!opcode.equals("VOTE")) {
			throw new CorruptedFrameException("VOTE opcode not found");
		}

		String serviceName = readString(block, position);
		position += serviceName.length()+1;
		String username = readString(block, position);
		position += username.length()+1;
		String address = readString(block, position);
		position += address.length()+1;
		String timeStamp = readString(block, position);
		position += timeStamp.length()+1;

		Vote vote = new Vote();
		vote.setServiceName(serviceName);
		vote.setUsername(username);
		vote.setAddress(address);
		vote.setTimeStamp(timeStamp);

		if (ForgeVotifier.isDebugMode()) {
			ForgeVotifier.getLogger().info("Received protocol v1 vote record -> "+vote);
		}
		list.add(vote);

		ctx.pipeline().remove(this);
	}

	private static String readString(byte[] data, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i<data.length; i++) {
			if (data[i]==10)
				break;
			builder.append((char) data[i]);
		}
		return builder.toString();
	}
}
