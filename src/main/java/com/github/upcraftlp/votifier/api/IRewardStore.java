package com.github.upcraftlp.votifier.api;

public interface IRewardStore {

    int getOutStandingRewardCount(String playerNmae);

    int getMaxStoredRewards();

    void storePlayerReward(Vote vote);

    void claimRewards(String name);
}
