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
		buf.retain();
		buf.readerIndex(0);
		short readMagic = buf.readShort();

		buf.readerIndex(0);

		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();

		if (readMagic==PROTOCOL_2_MAGIC) {
			ctx.pipeline().addAfter("protocolDifferentiator", "protocol2LengthDecoder", new LengthFieldBasedFrameDecoder(1024, 2, 2, 0, 4));
			ctx.pipeline().addAfter("protocol2LengthDecoder", "protocol2StringDecoder", new StringDecoder(StandardCharsets.UTF_8));
			ctx.pipeline().addAfter("protocol2StringDecoder", "protocol2VoteDecoder", new VotifierProtocol2VoteDecoder());
			ctx.pipeline().addAfter("protocol2VoteDecoder", "protocol2StringEncoder", new StringEncoder(StandardCharsets.UTF_8));
			session.setVersion(VotifierSession.ProtocolVersion.TWO);
			ctx.pipeline().remove(this);
		} else if (readable==256) {
			ctx.pipeline().addAfter("protocolDifferentiator", "protocol1Handler", new VotifierProtocol1Decoder());
			session.setVersion(VotifierSession.ProtocolVersion.ONE);
			ctx.pipeline().remove(this);
		} else {
			throw new CorruptedFrameException("Unrecognized protocol (missing 0x733A header or 256-byte v1 block)");
		}
	}
}
