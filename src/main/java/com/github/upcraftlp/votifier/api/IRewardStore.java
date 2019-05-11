package com.github.upcraftlp.votifier.api;

public interface IRewardStore {

    int getOutStandingRewardCount(String playerNmae);

    int getMaxStoredRewards();

    void storePlayerReward(String name, Vote vote);

    void claimRewards(String name);

    int getVoteCount(String uuid);

    int getLoginCount(String uuid);

    void incrementLoginCount(String uuid);

	void incrementVoteCount(String uuid);
}
