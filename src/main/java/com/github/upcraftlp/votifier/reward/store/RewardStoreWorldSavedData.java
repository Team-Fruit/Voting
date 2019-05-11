package com.github.upcraftlp.votifier.reward.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.IRewardStore;
import com.github.upcraftlp.votifier.api.Milestone;
import com.github.upcraftlp.votifier.api.MilestoneEvent;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.VoteReceivedEvent;
import com.github.upcraftlp.votifier.api.reward.StoredMilestone;
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

	private static final String DATA_NAME = ForgeVotifier.MODID+"_reward_data";
	private static final String DATA_REWARD_NAME = ForgeVotifier.MODID+"_reward_data";
	private static final String DATA_MILESTONE_NAME = ForgeVotifier.MODID+"_milestone_data";
	private final Map<String, List<StoredReward>> STORED_REWARDS = Maps.newHashMap();
	private final Map<String, StoredMilestone> STORED_MILESTONE = Maps.newHashMap();

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
		if (instance==null) {
			instance = new RewardStoreWorldSavedData();
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		{
			NBTTagList list = nbt.getTagList(DATA_REWARD_NAME, Constants.NBT.TAG_COMPOUND);
			STORED_REWARDS.clear();
			for (int i = 0; i<list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				String playerName = compound.getString("player");
				NBTTagList rewardList = compound.getTagList("rewards", Constants.NBT.TAG_COMPOUND);
				List<StoredReward> rewards = new ArrayList<>();
				for (int j = 0; j<rewardList.tagCount(); j++) {
					NBTTagCompound rewardTag = rewardList.getCompoundTagAt(i);
					String service = rewardTag.getString("service");
					String address = rewardTag.getString("address");
					String timestamp = rewardTag.getString("timestamp");
					int voteCount = rewardTag.getInteger("voteCount");
					rewards.add(new StoredReward(new Vote(service, playerName, address, timestamp, voteCount)));
				}
				STORED_REWARDS.put(playerName, rewards);
			}
		}
		{
			NBTTagList list = nbt.getTagList(DATA_MILESTONE_NAME, Constants.NBT.TAG_COMPOUND);
			STORED_MILESTONE.clear();
			for (int i = 0; i<list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				String uuid = compound.getString("uuid");
				NBTTagCompound milestoneTag = compound.getCompoundTag("milestone");
				int voteCount = milestoneTag.getInteger("voteCount");
				int loginCount = milestoneTag.getInteger("loginCount");
				long lastLogin = milestoneTag.getLong("lastLogin");
				StoredMilestone milestone = new StoredMilestone(new Milestone(voteCount, loginCount, lastLogin));
				STORED_MILESTONE.put(uuid, milestone);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		{
			NBTTagList list = new NBTTagList();
			for (Entry<String, List<StoredReward>> entry : STORED_REWARDS.entrySet()) {
				String playerName = entry.getKey();
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("player", playerName);
				List<StoredReward> rewards = entry.getValue();
				if (rewards==null) {
					continue;
				}
				NBTTagList playerRewardList = new NBTTagList();
				for (StoredReward reward : rewards) {
					NBTTagCompound rewardTag = new NBTTagCompound();
					rewardTag.setString("service", reward.vote.getServiceName());
					rewardTag.setString("address", reward.vote.getAddress());
					rewardTag.setString("timestamp", reward.vote.getTimeStamp());
					rewardTag.setInteger("voteCount", reward.vote.getVoteCount());
					playerRewardList.appendTag(rewardTag);
				}
				nbt.setTag("rewards", playerRewardList);
				list.appendTag(nbt);
			}
			compound.setTag(DATA_REWARD_NAME, list);
		}
		{
			NBTTagList list = new NBTTagList();
			for (Entry<String, StoredMilestone> entry : STORED_MILESTONE.entrySet()) {
				String uuid = entry.getKey();
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("uuid", uuid);
				StoredMilestone milestone = entry.getValue();
				if (milestone==null) {
					continue;
				}
				NBTTagCompound milestoneTag = new NBTTagCompound();
				milestoneTag.setInteger("voteCount", milestone.milestone.getVoteCount());
				milestoneTag.setInteger("loginCount", milestone.milestone.getLoginCount());
				milestoneTag.setLong("lastLogin", milestone.milestone.getLastLogin());
				nbt.setTag("milestone", milestoneTag);
				list.appendTag(nbt);
			}
			compound.setTag(DATA_MILESTONE_NAME, list);
		}
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
	public void storePlayerReward(String name, Vote vote) {
		if (getMaxStoredRewards()==0) {
			return; //do not store anything
		}
		ForgeVotifier.getLogger().debug("cannot find player {}, assuming they're offline and storing reward.", vote.getUsername());
		List<StoredReward> rewards = getRewardsForPlayer(name);
		while (rewards.size()>getMaxStoredRewards()) {
			rewards.remove(0); //discard old rewards
		}
		rewards.add(new StoredReward(vote));

		this.markDirty();
	}

	@Override
	public void claimRewards(String name) {
		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
		List<StoredReward> rewards = getRewardsForPlayer(name);
		if (player!=null) {
			for (int i = 0; i<Math.min(getMaxStoredRewards(), rewards.size()); i++) {
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
		if (!STORED_REWARDS.containsKey(name)) {
			STORED_REWARDS.put(name, new ArrayList<>());
		}
		return STORED_REWARDS.get(name);
	}

	@Override
	public int getVoteCount(String uuid) {
		return getMilestoneForPlayerUUID(uuid).milestone.getVoteCount();
	}

	@Override
	public int getLoginCount(String uuid) {
		return getMilestoneForPlayerUUID(uuid).milestone.getLoginCount();
	}

	@Override
	public void incrementLoginCount(String uuid) {
		StoredMilestone milestone = getMilestoneForPlayerUUID(uuid);
		long now = System.currentTimeMillis();
		long dayTimesMillis = TimeUnit.HOURS.toMillis(9);
		long nowDate = TimeUnit.MILLISECONDS.toDays(now - dayTimesMillis);
		long lastDate = TimeUnit.MILLISECONDS.toDays(milestone.milestone.getLastLogin() - dayTimesMillis);
		if (nowDate - lastDate > 0) {
			Milestone newMilestone = new Milestone(milestone.milestone.getVoteCount(), milestone.milestone.getLoginCount()+1, now);
			MilestoneEvent event = new MilestoneEvent(uuid, milestone.milestone, newMilestone);
			MinecraftForge.EVENT_BUS.post(event);
			milestone.milestone = event.getMilestone();
			this.markDirty();
		}
	}

	@Override
	public void incrementVoteCount(String uuid) {
		StoredMilestone milestone = getMilestoneForPlayerUUID(uuid);
		Milestone newMilestone = new Milestone(milestone.milestone.getVoteCount()+1, milestone.milestone.getLoginCount(), milestone.milestone.getLastLogin());
		MilestoneEvent event = new MilestoneEvent(uuid, milestone.milestone, newMilestone);
		MinecraftForge.EVENT_BUS.post(event);
		milestone.milestone = event.getMilestone();
		this.markDirty();
	}

	private StoredMilestone getMilestoneForPlayerUUID(String uuid) {
		if (!STORED_MILESTONE.containsKey(uuid)) {
			STORED_MILESTONE.put(uuid, new StoredMilestone(new Milestone(0, 0, 0)));
		}
		return STORED_MILESTONE.get(uuid);
	}
}
