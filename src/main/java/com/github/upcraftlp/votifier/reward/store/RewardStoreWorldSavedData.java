package com.github.upcraftlp.votifier.reward.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.IRewardStore;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.VoteReceivedEvent;
import com.github.upcraftlp.votifier.api.reward.StoredReward;
import com.github.upcraftlp.votifier.config.VotifierConfig;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class RewardStoreWorldSavedData extends WorldSavedData implements IRewardStore {

    private static final String DATA_NAME = ForgeVotifier.MODID + "_reward_data";
    private final Map<String, List<StoredReward>> STORED_REWARDS = Maps.newHashMap();

    public RewardStoreWorldSavedData() {
        this(DATA_NAME);
    }

    public RewardStoreWorldSavedData(String name) {
        super(name);
    }

    public static RewardStoreWorldSavedData get() {
        return get(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
    }

    public static RewardStoreWorldSavedData get(World world) {
        MapStorage storage = world.getMapStorage();
        RewardStoreWorldSavedData instance = (RewardStoreWorldSavedData) storage.getOrLoadData(RewardStoreWorldSavedData.class, DATA_NAME);
        if(instance == null) {
            instance = new RewardStoreWorldSavedData();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList(DATA_NAME, Constants.NBT.TAG_COMPOUND);
        STORED_REWARDS.clear();
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            String playerName = compound.getString("player");
            NBTTagList rewardList = compound.getTagList("rewards", Constants.NBT.TAG_COMPOUND);
            List<StoredReward> rewards = new ArrayList<>();
            for(int j = 0; j < rewardList.tagCount(); j++) {
                NBTTagCompound rewardTag = rewardList.getCompoundTagAt(i);
                String service = rewardTag.getString("service");
                String address = rewardTag.getString("address");
                String timestamp = rewardTag.getString("timestamp");
                rewards.add(new StoredReward(new Vote(service, playerName, address, timestamp)));
            }
            STORED_REWARDS.put(playerName, rewards);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for(String playerName : STORED_REWARDS.keySet()) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("player", playerName);
            List<StoredReward> rewards = STORED_REWARDS.get(playerName);
            if(rewards == null) {
                continue;
            }
            NBTTagList playerRewardList = new NBTTagList();
            for(StoredReward reward : rewards) {
                NBTTagCompound rewardTag = new NBTTagCompound();
                rewardTag.setString("service", reward.vote.getServiceName());
                rewardTag.setString("address", reward.vote.getAddress());
                rewardTag.setString("timestamp", reward.vote.getTimeStamp());
                playerRewardList.appendTag(rewardTag);
            }
            nbt.setTag("rewards", playerRewardList);
            list.appendTag(nbt);
        }
        compound.setTag(DATA_NAME, list);
        return compound;
    }

    @Override
    public int getOutStandingRewardCount(String playerName) {
        return Math.min(getRewardsForPlayer(playerName).size(), getMaxStoredRewards());
    }

    @Override
    public int getMaxStoredRewards() {
        return VotifierConfig.maxOfflineRewards;
    }

    @Override
    public void storePlayerReward(Vote vote) {
        if(getMaxStoredRewards() == 0) {
            return; //do not store anything
        }
        ForgeVotifier.getLogger().debug("cannot find player {}, assuming they're offline and storing reward.", vote.getUsername());
        List<StoredReward> rewards = getRewardsForPlayer(vote.getUsername());
        while(rewards.size() > getMaxStoredRewards()) {
            rewards.remove(0); //discard old rewards
        }
        rewards.add(new StoredReward(vote));
        this.markDirty();
    }

    @Override
    public void claimRewards(String name) {
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
        List<StoredReward> rewards = getRewardsForPlayer(name);
        if(player != null) {
            for(int i = 0; i < Math.min(getMaxStoredRewards(), rewards.size()); i++) {
                StoredReward reward = rewards.get(i);
                MinecraftForge.EVENT_BUS.post(new VoteReceivedEvent(player, reward.vote));
            }
        }
        ForgeVotifier.getLogger().debug("player {} claimed their {} outstanding rewards", name, rewards.size());
        rewards.clear();
        this.markDirty();
    }

    private List<StoredReward> getRewardsForPlayer(String name) {
        name = name.toLowerCase(Locale.ROOT);
        if(!STORED_REWARDS.containsKey(name)) {
            STORED_REWARDS.put(name, new ArrayList<>());
        }
        return STORED_REWARDS.get(name);
    }
}
