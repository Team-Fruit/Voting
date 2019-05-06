package com.github.upcraftlp.votifier.api.reward;

import com.github.upcraftlp.votifier.api.RewardException;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class Reward {

    public static String replace(String input, String username, String service) {
        return input.replace("@PLAYER@", username).replace("@SERVICE@", service);
    }

    @Deprecated
    public static String replace(String input, ICommandSender username, String service) {
        return replace(input, username.getName(), service);
    }

    public abstract String getType();

    public abstract void activate(MinecraftServer server, EntityPlayer player, String timestamp, String service, String address) throws RewardException;
}
