package com.github.upcraftlp.votifier.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.MilestoneEvent;
import com.github.upcraftlp.votifier.api.RawVote;
import com.github.upcraftlp.votifier.api.RawVoteEvent;
import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.VoteEvent;
import com.github.upcraftlp.votifier.api.VoteReceivedEvent;
import com.github.upcraftlp.votifier.api.reward.Reward;
import com.github.upcraftlp.votifier.api.reward.RewardStore;
import com.github.upcraftlp.votifier.command.CommandVote;
import com.github.upcraftlp.votifier.config.VotifierConfig;
import com.github.upcraftlp.votifier.util.ModUpdateHandler;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.SERVER, modid = ForgeVotifier.MODID)
public class VoteEventHandler {

    private static final List<Reward> REWARDS = new LinkedList<>();
    private static final List<Reward> MILESTONES = new LinkedList<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void voteMade(VoteReceivedEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for (Iterator<Reward> iterator = REWARDS.iterator(); iterator.hasNext();) {
            Reward reward = iterator.next();
            try {
                reward.activate(server, event.getEntityPlayer(), event.getVote());
            }
            catch (RewardException e) {
                ForgeVotifier.getLogger().error("Error executing votifier reward, removing reward from reward list!", e);
                iterator.remove();
            }
        }
    }

    public static final Consumer<Reward> AddReward = reward -> {
        REWARDS.add(reward);
    };

    public static final Consumer<Reward> AddMilestone = reward -> {
        MILESTONES.add(reward);
    };

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void voted(RawVoteEvent event) {
        RawVote vote = event.getVote();
        ForgeVotifier.getLogger().info("[{}] received vote from {} (service: {})", vote.getTimeStamp(), vote.getUsername(), vote.getServiceName());

        GameProfile profile = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getGameProfileForUsername(event.getVote().getUsername());
        int voteCount = 0;
        if (profile != null) {
            String uuid = profile.getId().toString();
            RewardStore.getStore().incrementVoteCount(uuid);
            voteCount = RewardStore.getStore().getVoteCount(uuid);
        }

        MinecraftForge.EVENT_BUS.post(new VoteEvent(new Vote(vote, voteCount)));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void voted(VoteEvent event) {
        Vote vote = event.getVote();

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (!StringUtils.isNullOrEmpty(VotifierConfig.voteMessage))
            for(EntityPlayerMP playerMP : server.getPlayerList().getPlayers())
                playerMP.sendMessage(ITextComponent.Serializer.jsonToComponent(
                    Reward.replace(VotifierConfig.voteMessage, vote)));

        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
        EntityPlayerMP player = playerList.getPlayerByUsername(vote.getUsername());
        if(player != null) {
            MinecraftForge.EVENT_BUS.post(new VoteReceivedEvent(player, vote));
        }
        else {
            RewardStore.getStore().storePlayerReward(vote.getUsername(), vote);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        GameProfile profile = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getGameProfileForUsername(event.player.getName());
        int loginCount = 0;
        if (profile != null) {
            String uuid = profile.getId().toString();
            RewardStore.getStore().incrementLoginCount(uuid);
            loginCount = RewardStore.getStore().getLoginCount(uuid);
        }

        int rewardCount = RewardStore.getStore().getOutStandingRewardCount(event.player.getName());
        if(rewardCount > 0) {
            event.player.sendMessage(CommandVote.getOutstandingRewardsText(rewardCount));
        }
        if(VotifierConfig.updates.enableUpdateChecker && server.getPlayerList().getOppedPlayers().getPermissionLevel(event.player.getGameProfile()) == server.getOpPermissionLevel()) { //player is opped
            ForgeVersion.CheckResult result = ModUpdateHandler.getResult();
            if(VotifierConfig.updates.enableUpdateChecker && ModUpdateHandler.hasUpdate(result)) {
                event.player.sendMessage(new TextComponentString("There's an update available for " + ForgeVotifier.MODNAME + ", check the server log for details!"));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMilestone(MilestoneEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (event.getLastMilestone().getLoginCount() != event.getMilestone().getLoginCount()) {
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            EntityPlayerMP player = null;
            try {
                player = playerList.getPlayerByUUID(UUID.fromString(event.getUUID()));
            } catch (IllegalArgumentException e) {
            }

            if (player != null)
                for (Iterator<Reward> iterator = MILESTONES.iterator(); iterator.hasNext();) {
                    Reward reward = iterator.next();
                    try {
                        reward.activate(server, player, new Vote("Login", player.getName(), "", "", event.getMilestone().getLoginCount()));
                    }
                    catch (RewardException e) {
                        ForgeVotifier.getLogger().error("Error executing votifier milestone, removing reward from milestone list!", e);
                        iterator.remove();
                    }
                }
        }
    }
}
