package com.github.upcraftlp.votifier.net;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.RawVote;
import com.github.upcraftlp.votifier.api.RawVoteEvent;
import com.github.upcraftlp.votifier.net.VotifierSession.ProtocolVersion;
import com.github.upcraftlp.votifier.util.GsonInst;
import com.google.gson.JsonObject;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VoteInboundHandler extends SimpleChannelInboundHandler<RawVote> {
	protected void channelRead0(ChannelHandlerContext ctx, RawVote vote) throws Exception {
		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();
		//System.out.println(vote.getAddress()+" "+vote.getUsername());

		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> { //ensure we are not handling the event on the network thread
			MinecraftForge.EVENT_BUS.post(new RawVoteEvent(vote));
		});

		if (session.getVersion()==ProtocolVersion.ONE) {
			ctx.close();
		} else {
			JsonObject object = new JsonObject();
			object.addProperty("status", "ok");
			ctx.writeAndFlush(GsonInst.gson.toJson(object) + "\r\n").addListener(ChannelFutureListener.CLOSE);
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();

		ForgeVotifier.getLogger().error("Exception while processing vote from "+ctx.channel().remoteAddress(), cause);

		if (session.getVersion()==ProtocolVersion.TWO) {
			JsonObject object = new JsonObject();
			object.addProperty("status", "error");
			object.addProperty("cause", cause.getClass().getSimpleName());
			object.addProperty("error", cause.getMessage());
			ctx.writeAndFlush(GsonInst.gson.toJson(object) + "\r\n").addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.close();
		}
	}
}
