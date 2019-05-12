package com.github.upcraftlp.votifier.reward;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.upcraftlp.votifier.ForgeVotifier;
import com.github.upcraftlp.votifier.api.Vote;
import com.github.upcraftlp.votifier.api.reward.Reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class RewardItem extends Reward {

    private final Supplier<ItemStack> itemStack;
    private final String nbtRaw;

    public RewardItem(Predicate<Vote> predicate, Supplier<Item> item, int count, @Nullable String nbtString) {
        this(predicate, item, count, 0, nbtString);
    }

    public RewardItem(Predicate<Vote> predicate, Supplier<Item> item, int count, int meta, @Nullable String nbtString) {
    	super(predicate);
        itemStack = ()->new ItemStack(item.get(), count, meta);
        nbtRaw = nbtString;
    }

    @Override
    public String getType() {
        return "item";
    }

    @Override
    public void activate(MinecraftServer server, EntityPlayer player, Vote vote) {
        if (!getPredicate().test(vote))
            return;
        try {
            ItemStack ret = itemStack.get().copy();
            if(ret.hasDisplayName()) {
                ret.setStackDisplayName(replace(ret.getDisplayName(), vote));
            }
            if(nbtRaw != null) {
                try {
                    NBTTagCompound nbt = JsonToNBT.getTagFromJson(replace(nbtRaw, vote));
                    ret.setTagCompound(nbt);
                }
                catch (NBTException e) {
                    ForgeVotifier.getLogger().error("unable to parse NBT string: {}", nbtRaw);
                }
            }
            player.inventory.addItemStackToInventory(ret);
        }
        catch (RuntimeException e) {
            ForgeVotifier.getLogger().error("unable to find item", e);
        }
    }
}
