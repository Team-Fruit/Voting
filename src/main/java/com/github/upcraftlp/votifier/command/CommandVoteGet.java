package com.github.upcraftlp.votifier.command;

import com.github.upcraftlp.votifier.command.CommandVote.Level;
import com.github.upcraftlp.votifier.config.VotifierConfig;
import com.github.upcraftlp.votifier.reward.store.RewardStoreWorldSavedData;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandVoteGet extends CommandBase {
    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/vote get";
    }

    public final Level level = Level.ALL;

    @Override
    public int getRequiredPermissionLevel() {
        return level.requiredPermissionLevel;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return level.permissionChecker.checkPermission(server, sender, this);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);
        int outstandingRewards = RewardStoreWorldSavedData.get(playerMP.getServerWorld()).getOutStandingRewardCount(playerMP.getName());
        if(outstandingRewards == 0) {
            sender.sendMessage(new TextComponentString(VotifierConfig.noOutstandingRewardsMessage));
        }
        else {
            sender.sendMessage(CommandVote.getOutstandingRewardsText(outstandingRewards));
        }
    }
}
