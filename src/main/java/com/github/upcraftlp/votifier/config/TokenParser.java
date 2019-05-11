package com.github.upcraftlp.votifier.config;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.util.KeyCreator;
import com.github.upcraftlp.votifier.util.TokenUtil;
import com.google.common.base.Charsets;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class TokenParser {

	public static Map<String, Key> tokens = new HashMap<>();

    public static void init(FMLPreInitializationEvent event) {
        File configDir = new File(event.getModConfigurationDirectory(), "votifier/tokens");
        if(!configDir.exists()) {
            setupDefaultRewards(configDir);
        }
        File[] tokenFiles = configDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".txt"));
        if(tokenFiles == null) {
            //if this returns null, something is seriously wrong.
            throw new IllegalStateException("error initializing votifier, could not list files for " + configDir.getAbsolutePath());
        }
        int regCount = 0;
        for(File tokenFile : tokenFiles) {
            try {
            	String service = FilenameUtils.getBaseName(tokenFile.getName());
                String token = FileUtils.readFileToString(tokenFile, Charsets.UTF_8);
                tokens.put(service, KeyCreator.createKeyFrom(token));
                regCount++;
            }
            catch (IOException e) {
                ForgeVotifier.getLogger().error("error parsing txt file " + tokenFile.getName() + "!", e);
            }
            ForgeVotifier.getLogger().info("Votifier registered a total of {} tokens in {} files!", regCount, tokenFiles.length);
        }
    }

    private static void setupDefaultRewards(File rewardsDir) {
        File defaultConfig = new File(rewardsDir, "default.txt");
        try {
            FileUtils.forceMkdir(rewardsDir);
            //noinspection ConstantConditions
            FileUtils.write(defaultConfig, TokenUtil.newToken(), Charsets.UTF_8);
        }
        catch (IOException e) {
            ForgeVotifier.getLogger().error("Exception setting up the default reward config!", e);
        }
    }
}
