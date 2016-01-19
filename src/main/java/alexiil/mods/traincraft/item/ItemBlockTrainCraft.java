package alexiil.mods.traincraft.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public abstract class ItemBlockTrainCraft extends ItemBlock {
    private CreativeTabs tab;

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
}
