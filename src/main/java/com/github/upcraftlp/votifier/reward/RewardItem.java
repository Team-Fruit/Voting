package com.github.upcraftlp.votifier.reward;

import javax.annotation.Nullable;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class RewardItem extends Reward {

    private final ItemStack itemStack;
    private final String nbtRaw;

    public RewardItem(Item item, int count, @Nullable String nbtString) {
        this(item, count, 0, nbtString);
    }

    public RewardItem(Item item, int count, int meta, @Nullable String nbtString) {
        itemStack = new ItemStack(item, count, meta);
        nbtRaw = nbtString;
    }

    @Override
    public String getType() {
        return "item";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, String timestamp, String service, String address) {
        ItemStack ret = itemStack.copy();
        if(ret.hasDisplayName()) {
            ret.setStackDisplayName(replace(ret.getDisplayName(), player.getName(), service));
        }
        if(nbtRaw != null) {
            try {
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(replace(nbtRaw, player.getName(), service));
                ret.setTagCompound(nbt);
            }
            catch (NBTException e) {
                ForgeVotifier.getLogger().error("unable to parse NBT string: {}", nbtRaw);
            }
        }
        player.inventory.addItemStackToInventory(ret);
    }
}
