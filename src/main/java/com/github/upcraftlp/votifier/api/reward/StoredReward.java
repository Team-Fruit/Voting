package com.github.upcraftlp.votifier.api.reward;

import com.github.upcraftlp.votifier.api.Vote;

public class StoredReward {

    public final Vote vote;

    public StoredReward(Vote vote) {
        this.vote = vote;
    }
}
