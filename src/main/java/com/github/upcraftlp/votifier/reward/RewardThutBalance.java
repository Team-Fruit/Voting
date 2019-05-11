package com.github.upcraftlp.votifier.reward;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.economy.EconomyManager;

public class RewardThutBalance extends Reward {

    private final int amount;

    public RewardThutBalance(int voteCount, int amount) {
        super(voteCount);
        this.amount = amount;
    }

    @Override
    public String getType() {
        return "thut_pay";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, Vote vote) {
        if (getVoteCount() > 0 && vote.getVoteCount() != getVoteCount())
            return;
        if(ForgeVotifier.isThutessentialsLoaded()) {
            EconomyManager.addBalance(player, this.amount);
        }
        else {
            server.sendMessage(new TextComponentString("Thut essentials not loaded!"));
        }
    }
}
