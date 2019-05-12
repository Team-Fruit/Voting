package com.github.upcraftlp.votifier.reward;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.github.upcraftlp.votifier.api.RewardException;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class RewardRandom extends Reward {

	List<Pair<Float, List<Reward>>> rewards;

	public RewardRandom(Predicate<Vote> predicate, List<Pair<Float, List<Reward>>> rewards) {
		super(predicate);
		this.rewards = rewards;
	}

	@Override
	public String getType() {
		return "random";
	}

	@Override
	public void activate(MinecraftServer server, EntityPlayer player, Vote vote) throws RewardException {
		if (!getPredicate().test(vote))
			return;

		float total = 0;
		for (Pair<Float, List<Reward>> reward : rewards)
			total += Math.max(0, reward.getLeft());

		float rand = RandomUtils.nextFloat(0, total);
		float current = 0;
		List<Reward> rewardItems = null;
		for (Pair<Float, List<Reward>> reward : rewards)
			if ((current += Math.max(0, reward.getLeft()))>rand)
				rewardItems = reward.getRight();

		if (rewardItems!=null)
			for (Reward rewardItem : rewardItems)
				rewardItem.activate(server, player, vote);
	}
}
