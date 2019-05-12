package com.github.upcraftlp.votifier.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jline.utils.Log;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.RewardCreatedEvent;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;
import com.github.upcraftlp.votifier.reward.RewardChat;
import com.github.upcraftlp.votifier.reward.RewardCommand;
import com.github.upcraftlp.votifier.reward.RewardItem;
import com.github.upcraftlp.votifier.reward.RewardRandom;
import com.github.upcraftlp.votifier.reward.RewardThutBalance;
import com.github.upcraftlp.votifier.util.RangeFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Predicates;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import net.minecraft.command.CommandBase;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;

public class RewardParser {

    public static void init(File configDir, Consumer<Reward> rewardRegistry, String defaultName) {
        if(!configDir.exists()) {
            setupDefaultRewards(configDir, defaultName);
        }
        File[] jsonFiles = configDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
        if(jsonFiles == null) {
            //if this returns null, something is seriously wrong.
            throw new IllegalStateException("error initializing votifier, could not list files for " + configDir.getAbsolutePath());
        }
        JsonParser parser = new JsonParser();
        int regCount = 0;
        for(File jsonFile : jsonFiles) {
            try {
                JsonObject root = parser.parse(new JsonReader(new InputStreamReader(new FileInputStream(jsonFile), Charsets.UTF_8))).getAsJsonObject();
                if(!root.has("rewards")) {
                    ForgeVotifier.getLogger().error("cannot parse reward file {}!", jsonFile.getName());
                    continue;
                }
                JsonArray rewardArray = root.get("rewards").getAsJsonArray();
                for(int i = 0; i < rewardArray.size(); i++) {
                    JsonObject object = rewardArray.get(i).getAsJsonObject();
                    Reward reward = parseReward(object);
                    if(reward != null) {
                        rewardRegistry.accept(reward);
                        regCount++;
                    }
                    else {
                        String type = object.get("type").getAsString();
                        ForgeVotifier.getLogger().warn("ignoring unknown votifier reward type: {}", type);
                    }
                }
            }
            catch (FileNotFoundException e) {
                ForgeVotifier.getLogger().error("error parsing reward file " + jsonFile.getName() + "!", e);
            }
            ForgeVotifier.getLogger().info("Votifier registered a total of {} rewards in {} files!", regCount, jsonFiles.length);
        }
    }

    private static Reward parseReward(JsonObject object) {
        String type = object.get("type").getAsString();

        Predicate<Vote> predicate = Predicates.alwaysTrue();
        if (object.has("votecount")) {
            int votecount = object.get("votecount").getAsInt();
            predicate = predicate.and(vote -> votecount == vote.getVoteCount());
        } else if (object.has("votecountrange")) {
            try {
                Range<Float> votecount = RangeFactory.from(object.get("votecountrange").getAsString());
                predicate = predicate.and(vote -> votecount.contains((float) vote.getVoteCount()));
            } catch (IllegalArgumentException e) {
            }
        }
        if (object.has("time") && object.has("timeformat") && object.has("timemask")) {
            String time = object.get("time").getAsString();
            String timeformat = object.get("timeformat").getAsString();
            int timemask = object.get("timemask").getAsInt();
            SimpleDateFormat inputFormat = new SimpleDateFormat(timeformat);
            Date time1d = null;
            try {
                time1d = inputFormat.parse(time);
            } catch (ParseException e) {
                ForgeVotifier.getLogger().error("Parse Error", e);
            }
            if (time1d != null) {
                ZonedDateTime time1 = ZonedDateTime.ofInstant(time1d.toInstant(), ZoneId.systemDefault());
                predicate = predicate.and(vote -> {
                    if (StringUtils.isNullOrEmpty(vote.getTimeStamp()))
                        return false;
                    ZonedDateTime time2 = null;
                    try {
                        // 2019-05-12 21:05:37 +0900
                        LocalDateTime time3 = LocalDateTime.parse(vote.getTimeStamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxxx"));
                        //Instant instant = Instant.parse(vote.getTimeStamp());
                        time2 = ZonedDateTime.of(time3, ZoneId.systemDefault());
                    } catch (DateTimeParseException e) {
                        Log.warn("Timestamp Parse Exception", e);
                    }
                    if (time2 == null)
                        return false;
                    //ZonedDateTime time2 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
                    return
                            ((timemask & 0b1000000 /* 64 */) == 0 || time1.getYear() == time2.getYear()) &&
                            ((timemask & 0b0100000 /* 32 */) == 0 || time1.getMonth() == time2.getMonth()) &&
                            ((timemask & 0b0010000 /* 16 */) == 0 || time1.getDayOfMonth() == time2.getDayOfMonth()) &&
                            ((timemask & 0b0001000 /* 8  */) == 0 || time1.getDayOfWeek() == time2.getDayOfWeek()) &&
                            ((timemask & 0b0000100 /* 4  */) == 0 || time1.getHour() == time2.getHour()) &&
                            ((timemask & 0b0000010 /* 2  */) == 0 || time1.getMinute() == time2.getMinute()) &&
                            ((timemask & 0b0000001 /* 1  */) == 0 || time1.getSecond() == time2.getSecond());
                });
            }
        }

        Reward reward;
        switch(type) {
            case "command":
            {
                String commandRaw = object.get("command").getAsString();
                reward = new RewardCommand(predicate, commandRaw);
            }
            break;

            case "chat":
            {
                boolean broadcast = object.has("broadcast") && object.get("broadcast").getAsBoolean();
                boolean parseAsTellraw = object.has("tellraw") && object.get("tellraw").getAsBoolean();
                String msgRaw = object.get("message").getAsString();
                reward = new RewardChat(predicate, msgRaw, broadcast, parseAsTellraw);
            }
            break;

            case "item":
            {
                String name = object.get("name").getAsString();
                Supplier<Item> item = ()->{
                    try {
                        return CommandBase.getItemByText(null, name);
                    } catch(NumberInvalidException e) {
                        throw new RuntimeException("Reward item is invalid.", e);
                    }
                };
                int count = object.has("count") ? object.get("count").getAsInt() : 1;
                int meta = object.has("damage") ? object.get("damage").getAsInt() : 0;
                String nbtRaw = object.has("nbt") ? object.get("nbt").getAsString() : null;
                reward = new RewardItem(predicate, item, count, meta, nbtRaw);
            }
            break;

            case "random": {
                List<Pair<Float, List<Reward>>> rewards = new ArrayList<>();
                JsonArray rrandoms = object.get("random").getAsJsonArray();
                for (JsonElement rrandom : rrandoms) {
                    JsonObject rrandom1 = rrandom.getAsJsonObject();
                    float rprob = rrandom1.has("probability") ? rrandom1.get("probability").getAsFloat() : 1;
                    List<Reward> rreward = new ArrayList<>();
                    JsonArray rrewards = rrandom1.get("rewards").getAsJsonArray();
                    for (JsonElement rreward1 : rrewards) {
                        Reward rreward2 = parseReward(rreward1.getAsJsonObject());
                        rreward.add(rreward2);
                    }
                    rewards.add(Pair.of(rprob, rreward));
                }
                reward = new RewardRandom(predicate, rewards);
            }
            break;

            case "thut_pay":
            {
                if(ForgeVotifier.isThutessentialsLoaded()) {
                    int amount = object.get("amount").getAsInt();
                    reward = new RewardThutBalance(predicate, amount);
                }
                else {
                    ForgeVotifier.getLogger().error("found reward thut_pay, but thut essentials is not loaded!");
                    reward = null;
                }
            }
            break;

            default: //allow for custom rewards from other mods
            {
                RewardCreatedEvent rewardEvent = new RewardCreatedEvent(type, object);
                MinecraftForge.EVENT_BUS.post(rewardEvent);
                reward = rewardEvent.getRewardResult();
            }
            break;
        }
        return reward;
    }

    private static void setupDefaultRewards(File rewardsDir, String defaultName) {
        File defaultConfig = new File(rewardsDir, defaultName);
        try {
            FileUtils.forceMkdir(rewardsDir);
            //noinspection ConstantConditions
            FileUtils.copyToFile(MinecraftServer.class.getClassLoader().getResourceAsStream("assets/" + ForgeVotifier.MODID + "/reward/" + defaultName), defaultConfig);
        }
        catch (IOException e) {
            ForgeVotifier.getLogger().error("Exception setting up the default reward config!", e);
        }
    }
}
