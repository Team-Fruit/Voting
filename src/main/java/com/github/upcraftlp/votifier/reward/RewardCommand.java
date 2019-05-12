package com.github.upcraftlp.votifier.reward;

import java.util.function.Predicate;

import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class RewardCommand extends Reward {

    private final String command;

    public RewardCommand(Predicate<Vote> predicate, String command) {
        super(predicate);
        this.command = command;
    }

    @Override
    public String getType() {
        return "command";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, Vote vote) {
        if (!getPredicate().test(vote))
            return;
        server.commandManager.executeCommand(server, replace(this.command, vote));
    }
}
