package com.github.upcraftlp.votifier.command;

import com.github.upcraftlp.votifier.api.RawVote;
import com.github.upcraftlp.votifier.api.RawVoteEvent;
import com.github.upcraftlp.votifier.command.CommandVote.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

public class CommandVoteGive extends CommandBase {
    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/vote give <playername>";
    }

    public final Level level = Level.OP;

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
        String service = args.length >= 2 ? args[1] : "ForgeVotifierTest";
        String username = args.length >= 1 ? args[0] : sender.getName();
        String address = "";
        String timestamp = Long.toString(System.nanoTime() / 1_000_000L);

        RawVote vote = new RawVote(service, username, address, timestamp);
        MinecraftForge.EVENT_BUS.post(new RawVoteEvent(vote));
    }
}
