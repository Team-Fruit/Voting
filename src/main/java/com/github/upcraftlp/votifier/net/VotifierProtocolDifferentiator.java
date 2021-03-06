package com.github.upcraftlp.votifier.net;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class VotifierProtocolDifferentiator extends ByteToMessageDecoder {
	private static final short PROTOCOL_2_MAGIC = 29498;

	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
		int readable = buf.readableBytes();
		if (readable < 2) {
			// Some retarded voting sites (PMC?) seem to send empty buffers for no good reason.
			return;
		}
		short readMagic = buf.readShort();

		buf.readerIndex(0);

		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();

		if (readMagic==PROTOCOL_2_MAGIC) {
			session.setVersion(VotifierSession.ProtocolVersion.TWO);
			ctx.pipeline().addAfter("protocolDifferentiator", "protocol2LengthDecoder", new LengthFieldBasedFrameDecoder(1024, 2, 2, 0, 4));
			ctx.pipeline().addAfter("protocol2LengthDecoder", "protocol2StringDecoder", new StringDecoder(StandardCharsets.UTF_8));
			ctx.pipeline().addAfter("protocol2StringDecoder", "protocol2VoteDecoder", new VotifierProtocol2VoteDecoder());
			ctx.pipeline().addAfter("protocol2VoteDecoder", "protocol2StringEncoder", new StringEncoder(StandardCharsets.UTF_8));
			ctx.pipeline().remove(this);
		} else if (readable==256) {
			session.setVersion(VotifierSession.ProtocolVersion.ONE);
			ctx.pipeline().addAfter("protocolDifferentiator", "protocol1Handler", new VotifierProtocol1Decoder());
			ctx.pipeline().remove(this);
		} else {
			throw new CorruptedFrameException("Unrecognized protocol (missing 0x733A header or 256-byte v1 block)");
		}
	}
}
