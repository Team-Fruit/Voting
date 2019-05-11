package com.github.upcraftlp.votifier.reward;

import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class RewardCommand extends Reward {

    private final String command;

    public RewardCommand(int voteCount, String command) {
        super(voteCount);
        this.command = command;
    }

    @Override
    public String getType() {
        return "command";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, Vote vote) {
        if (getVoteCount() > 0 && vote.getVoteCount() != getVoteCount())
            return;
        server.commandManager.executeCommand(server, replace(this.command, vote));
    }
}
