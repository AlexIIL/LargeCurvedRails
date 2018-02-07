package alexiil.mc.mod.traincraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.track.ITrackBlock;

public abstract class BlockAbstractTrack extends BlockTraincraft implements ITrackBlock {

    public static final PropertyEnum<EnumDirection> TRACK_DIRECTION = PropertyEnum.create("facing", EnumDirection.class);
    public static final PropertyEnum<EnumFacing> PROPERTY_FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final float TRACK_HEIGHT = 2 / 16.0f;

    public BlockAbstractTrack(IProperty<?>... properties) {
        super(Material.circuits, properties);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

}