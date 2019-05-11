package com.github.upcraftlp.votifier.command;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.VoteEvent;
import com.github.upcraftlp.votifier.api.VoteReceivedEvent;
import com.github.upcraftlp.votifier.api.reward.RewardStore;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandVoteGive extends CommandBase {
    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/vote give <playername>";
    }

    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String service = "ForgeVotifierTest";
        String username = args.length == 0 ? sender.getName() : args[0];
        String address = "";
        String timestamp = Long.toString(System.nanoTime() / 1_000_000L);

        ForgeVotifier.getLogger().info("[{}] received test vote from {} (service: {})", timestamp, username, service);
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        Vote vote = new Vote(service, username, address, timestamp);
        MinecraftForge.EVENT_BUS.post(new VoteEvent(vote));
        EntityPlayerMP player = playerList.getPlayerByUsername(username);
        if(player != null) {
            MinecraftForge.EVENT_BUS.post(new VoteReceivedEvent(player, vote));
        }
        else {
            RewardStore.getStore().storePlayerReward(vote);
        }
    }
}
