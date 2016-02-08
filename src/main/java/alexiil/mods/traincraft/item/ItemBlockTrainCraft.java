package alexiil.mods.traincraft.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public abstract class ItemBlockTrainCraft extends ItemBlock {
    private CreativeTabs tab;
    protected String unlocalizedName;

    public ItemBlockTrainCraft(Block block) {
        super(block);
    }

    @Override
    public Item setCreativeTab(CreativeTabs tab) {
        this.tab = tab;
        return this;
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return tab;
    }

    @Override
    public ItemBlock setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
        return super.setUnlocalizedName(unlocalizedName);
    }

    @Override
    public String getUnlocalizedName() {
        return getUnlocalizedName(null);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + unlocalizedName;
    }
}
