package com.github.upcraftlp.votifier.command;

import com.github.upcraftlp.votifier.command.CommandVote.Level;
import com.github.upcraftlp.votifier.config.VotifierConfig;
import com.github.upcraftlp.votifier.reward.store.RewardStoreWorldSavedData;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandVoteClaim extends CommandBase {
    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/vote claim";
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
        RewardStoreWorldSavedData wsd = RewardStoreWorldSavedData.get(playerMP.getServerWorld());
        if(wsd.getOutStandingRewardCount(playerMP.getName()) == 0) {
            throw new CommandException(VotifierConfig.noOutstandingRewardsMessage);
        }
        else {
            wsd.claimRewards(playerMP.getName());
        }
    }
}
