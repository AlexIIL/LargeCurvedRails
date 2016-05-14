package alexiil.mc.mod.traincraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import alexiil.mc.mod.traincraft.api.track.ITrackBlock;

public abstract class BlockAbstractTrack extends Block implements ITrackBlock {

    public static final PropertyEnum<EnumDirection> TRACK_DIRECTION = PropertyEnum.create("facing", EnumDirection.class);
    public static final PropertyEnum<EnumFacing> PROPERTY_FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final float TRACK_HEIGHT = 2 / 16.0f;

    public BlockAbstractTrack() {
        super(Material.CIRCUITS);
    }

    @Override
    public boolean getUseNeighborBrightness(IBlockState state) {
        return true;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
