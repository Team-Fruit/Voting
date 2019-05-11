package com.github.upcraftlp.votifier.command;

import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;
import com.github.upcraftlp.votifier.config.VotifierConfig;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandVote extends CommandTreeBase {
    public static class Level
    {
        public static final Level ALL = new Level(0, (server, sender, command) -> true);
        public static final Level OP_OR_SP = new Level(2, (server, sender, command) -> server.isSinglePlayer() || sender.canUseCommand(2, command.getName()));
        public static final Level OP = new Level(2, (server, sender, command) -> sender.canUseCommand(2, command.getName()));
        public static final Level STRONG_OP_OR_SP = new Level(4, (server, sender, command) -> server.isSinglePlayer() || sender.canUseCommand(4, command.getName()));
        public static final Level STRONG_OP = new Level(4, (server, sender, command) -> sender.canUseCommand(4, command.getName()));
        public static final Level SERVER = new Level(4, (server, sender, command) -> sender instanceof MinecraftServer);

        public interface PermissionChecker
        {
            boolean checkPermission(MinecraftServer server, ICommandSender sender, ICommand command);
        }

        public final int requiredPermissionLevel;
        public final PermissionChecker permissionChecker;

        public Level(int l, PermissionChecker p)
        {
            requiredPermissionLevel = l;
            permissionChecker = p;
        }
    }

    public CommandVote() {
        addSubcommand(new CommandVoteClaim());
        addSubcommand(new CommandVoteGet());
        addSubcommand(new CommandVoteGive());
        addSubcommand(new CommandTreeHelp(this));
    }

    public static ITextComponent getOutstandingRewardsText(int rewardsOutstanding) {
        return ITextComponent.Serializer.jsonToComponent(VotifierConfig.outstandingRewardsMessage.replace("@REWARDS@", String.valueOf(rewardsOutstanding)));
    }

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/vote [claim|get]";
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
        if(args.length == 0) {
            playerMP.sendMessage(TextComponentUtils.processComponent(server, ITextComponent.Serializer.jsonToComponent(
                    Reward.replace(VotifierConfig.voteCommand, new Vote("", sender.getName(), "", "", 0))), playerMP));
        }
        else {
            super.execute(server, sender, args);
        }
    }
}