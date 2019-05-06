package com.github.upcraftlp.votifier.command;

import com.github.upcraftlp.votifier.api.reward.Reward;
import com.github.upcraftlp.votifier.config.VotifierConfig;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandVote extends CommandTreeBase {

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

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);
        if(args.length == 0) {
            playerMP.sendMessage(TextComponentUtils.processComponent(server, ITextComponent.Serializer.jsonToComponent(Reward.replace(VotifierConfig.voteCommand, sender.getName(), "")), playerMP));
        }
        else {
            super.execute(server, sender, args);
        }
    }
}