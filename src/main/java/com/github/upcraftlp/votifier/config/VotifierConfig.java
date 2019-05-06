package com.github.upcraftlp.votifier.config;

import static com.github.upcraftlp.votifier.ForgeVotifier.*;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author UpcraftLP
 */
@Config(modid = MODID, name = "votifier/ForgeVotifier") //--> /config/votifier/ForgeVotifier.cfg
public class VotifierConfig {

    @Config.RequiresWorldRestart
    @Config.RangeInt(min = 0, max = 65535)
    @Config.Name("ListenerPort")
    @Config.Comment({ "The port for votifier to listen on,", "make sure your server provider allwos the port!", "Default: 8192" })
    public static int port = 8192;

    @Config.RequiresWorldRestart
    @Config.Name("Host Address")
    @Config.Comment({ "The Server's Host address if different from the address set in the server.properties file.", "Leave EMPTY for default value of 0.0.0.0" })
    public static String host = "";

    @Config.Name("Vote Command")
    @Config.Comment({ "the text that is shown when a player types /vote", "must be formatted in /tellraw nbt format" })
    public static String voteCommand = "{\"text\":\"Vote here!\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"Curseforge\",\"color\":\"aqua\"}]},\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft.curseforge.com/projects/293830\"}}";

    @Config.RequiresWorldRestart
    @Config.Name("Vote Command Enabled")
    @Config.Comment({ "Whether or not the /vote command will be available", "WARNING: disabling this will also prevent players from claiming their reward" })
    public static boolean voteCommandEnabled = true;

    @Config.RangeInt(min = 0, max = 100)
    @Config.Name("Offline Reward Count")
    @Config.Comment({ "How many rewards a player can receive while offline, must be claimed via \"/vote claim\"", "set to 0 to disable" })
    public static int maxOfflineRewards = 5;

    @Config.Name("debug mode")
    @Config.Comment("enable more verbose output of what's going on")
    public static boolean debugMode = false;

    @Config.RequiresMcRestart
    @Config.Name("Update-Checker")
    @Config.Comment({ "configure the update checker" })
    public static Updates updates = new Updates();

    @Config.Name("Vote Message")
    @Config.Comment({ "the text that is broadcasted when a player vote", "must be formatted in /tellraw nbt format" })
    public static String voteMessage = "{\"text\":\"Voted!\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"Curseforge\",\"color\":\"aqua\"}]},\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft.curseforge.com/projects/293830\"}}";

    @Config.Name("Outstanding Rewards Message")
    @Config.Comment({ "the text that is shown when a player join", "must be formatted in /tellraw nbt format" })
    public static String outstandingRewardsMessage = "[\"\",{\"text\":\"You have @REWARDS@ rewards outstanding, use \"},{\"text\":\"/vote claim\",\"color\":\"green\"},{\"text\":\" to claim or \"},{\"text\":\"click here\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/vote claim\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"click to vote\",\"italic\":false,\"color\":\"aqua\"}}},{\"text\":\" to claim them!\"}]";

    @Config.Name("No Outstanding Rewards Message")
    @Config.Comment({ "the text that is shown when a player types /vote but no rewards remained" })
    public static String noOutstandingRewardsMessage = "You have no outstanding rewards!";

    public static class Updates {

        @Config.Name("Enable Update Checker")
        @Config.Comment({ "whether to announce updates to opped players", "Note: available updates will be logged to console regardless" })
        public boolean enableUpdateChecker = true;

        @Config.Name("Show Beta Updates")
        @Config.Comment("whether or not to also show beta updates")
        public boolean showBetaUpdates = false;
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class Handler {

        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MODID))
                ConfigManager.load(MODID, Config.Type.INSTANCE);
        }
    }
}
