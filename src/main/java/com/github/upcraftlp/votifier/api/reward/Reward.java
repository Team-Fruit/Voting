package com.github.upcraftlp.votifier.api.reward;

import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.Vote;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class Reward {

    public static String replace(String input, Vote vote) {
        return input.replace("@PLAYER@", vote.getUsername()).replace("@SERVICE@", vote.getServiceName()).replace("@COUNT@", String.valueOf(vote.getVoteCount()));
    }

    private int voteCount;

    public Reward(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getVoteCount() {
        return this.voteCount;
    }

    public abstract String getType();

    public abstract void activate(MinecraftServer server, EntityPlayer player, Vote vote) throws RewardException;
}
