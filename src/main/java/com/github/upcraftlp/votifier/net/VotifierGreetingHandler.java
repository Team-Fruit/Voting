package com.github.upcraftlp.votifier.net;

import java.nio.charset.StandardCharsets;

import com.github.upcraftlp.votifier.ForgeVotifier;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class VotifierGreetingHandler
		extends ChannelInboundHandlerAdapter {
	public void channelActive(ChannelHandlerContext ctx)
			throws Exception {
		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();
		String version = "VOTIFIER "+ForgeVotifier.VERSION+" "+session.getChallenge()+"\n";
		ctx.writeAndFlush(Unpooled.copiedBuffer(version, StandardCharsets.UTF_8));
	}
}