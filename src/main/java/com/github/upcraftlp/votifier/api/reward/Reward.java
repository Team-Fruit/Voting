package com.github.upcraftlp.votifier.api.reward;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.Vote;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class Reward {

    public static String replace(String input, Vote vote) {
        String formattedString = "";
        if (input.contains("@TIME@")) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(vote.getTimeStamp()) * 1000), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            formattedString = zonedDateTime.format(formatter);
        }
        return input.replace("@PLAYER@", vote.getUsername()).replace("@SERVICE@", vote.getServiceName()).replace("@COUNT@", String.valueOf(vote.getVoteCount())).replace("@TIME@", formattedString);
    }

    private Predicate<Vote> predicate;

    public Reward(Predicate<Vote> predicate) {
        this.predicate = predicate;
    }

    public Predicate<Vote> getPredicate() {
        return this.predicate;
    }

    public abstract String getType();

    public abstract void activate(MinecraftServer server, EntityPlayer player, Vote vote) throws RewardException;
}
