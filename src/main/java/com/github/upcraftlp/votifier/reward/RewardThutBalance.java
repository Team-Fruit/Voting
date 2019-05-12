package com.github.upcraftlp.votifier.reward;

import java.util.function.Predicate;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.economy.EconomyManager;

public class RewardThutBalance extends Reward {

    private final int amount;

    public RewardThutBalance(Predicate<Vote> predicate, int amount) {
        super(predicate);
        this.amount = amount;
    }

    @Override
    public String getType() {
        return "thut_pay";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, Vote vote) {
        if (!getPredicate().test(vote))
            return;
        if(ForgeVotifier.isThutessentialsLoaded()) {
            EconomyManager.addBalance(player, this.amount);
        }
        else {
            server.sendMessage(new TextComponentString("Thut essentials not loaded!"));
        }
    }
}
