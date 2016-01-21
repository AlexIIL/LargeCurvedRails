package alexiil.mods.traincraft.model;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;

import alexiil.mods.traincraft.TrainCraft;

public class VoidStateMapper extends StateMapperBase {
    public static final VoidStateMapper INSTANCE = new VoidStateMapper();

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        ModelResourceLocation mrl = new ModelResourceLocation(Block.blockRegistry.getNameForObject(state.getBlock()), "normal");
        TrainCraft.trainCraftLog.info(mrl.toString());
        return mrl;
    }
}
