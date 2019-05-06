package com.github.upcraftlp.votifier.reward;

import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;

public class RewardChat extends Reward {

    private final boolean broadcastMessage;
    private final String messageRaw;
    private boolean parseAsTellraw;

    public RewardChat(String raw, boolean broadcastMessage, boolean parseAsTellraw) {
        this.broadcastMessage = broadcastMessage;
        this.messageRaw = raw;
        this.parseAsTellraw = parseAsTellraw;
    }

    @Override
    public String getType() {
        return "chat";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, String timestamp, String service, String address) throws RewardException {
        String msg = replace(messageRaw, player.getName(), service);
        if(this.parseAsTellraw) {
            try {
                ITextComponent textComponent = ITextComponent.Serializer.jsonToComponent(msg);
                if(textComponent == null) {
                    throw new CommandException("parsed component from message was null: " + msg);
                }
                if(this.broadcastMessage) {
                    for(EntityPlayerMP playerMP : server.getPlayerList().getPlayers()) {
                        playerMP.sendMessage(TextComponentUtils.processComponent(server, textComponent, playerMP));
                    }
                }
                else {
                    player.sendMessage(TextComponentUtils.processComponent(server, textComponent, player));
                }
            }
            catch (CommandException e) {
                throw new RewardException("error parsing chat reward!", e);
            }
        }
        else {
            String[] messages = msg.split("\n");
            for(String messageString : messages) {
                ITextComponent textComponent = new TextComponentString(messageString);
                if(this.broadcastMessage) {
                    server.getPlayerList().sendMessage(textComponent);
                }
                else {
                    player.sendMessage(textComponent);
                }
            }
        }
    }
}
