package com.github.upcraftlp.votifier.net;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.VoteEvent;
import com.github.upcraftlp.votifier.net.VotifierSession.ProtocolVersion;
import com.google.gson.JsonObject;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VoteInboundHandler extends SimpleChannelInboundHandler<Vote> {
	protected void channelRead0(ChannelHandlerContext ctx, Vote vote) throws Exception {
		//System.out.println(vote.getAddress()+" "+vote.getUsername());

		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> { //ensure we are not handling the event on the network thread
			ForgeVotifier.getLogger().info("[{}] received vote from {} (service: {})", vote.getTimeStamp(), vote.getUsername(), vote.getServiceName());
			MinecraftForge.EVENT_BUS.post(new VoteEvent(vote));
		});

		VotifierSession session = (VotifierSession) ctx.channel().attr(VotifierSession.KEY).get();

		if (session.getVersion()==ProtocolVersion.ONE) {
			ctx.close();
		} else {
			JsonObject object = new JsonObject();
			object.addProperty("status", "ok");
			ctx.writeAndFlush(object.toString()).addListener(ChannelFutureListener.CLOSE);
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
			ctx.writeAndFlush(object.toString()).addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.close();
		}
	}
}
