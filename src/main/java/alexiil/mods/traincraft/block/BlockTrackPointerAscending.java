package alexiil.mods.traincraft.block;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.tile.TileTrackAscendingPointer;

public class BlockTrackPointerAscending extends BlockTrackPointer {
    public static final IProperty<EnumMaterialType> PROPERTY_MATERIAL_TYPE = PropertyEnum.create("material", EnumMaterialType.class);

    public BlockTrackPointerAscending() {
        super(PROPERTY_MATERIAL_TYPE);
    }

    @Override
    protected BlockPos findMaster(IBlockAccess access, BlockPos pos, IBlockState state) throws IllegalPathException {
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            BlockPos masterPos = pos.offset(face);
            IBlockState masterState = access.getBlockState(masterPos);
            Block masterBlock = masterState.getBlock();
            if (masterBlock instanceof BlockTrackAscending) {
                BlockTrackAscending asc = (BlockTrackAscending) masterBlock;
                if (asc.isSlave(access, masterPos, masterState, pos, state)) return masterPos;
            }
        }
        // We failed. Update this block to remove it.
        return pos;
    }

    public IBlockState getSupportingMaterial(IBlockAccess access, BlockPos pos, IBlockState state) {
        if (!hasTileEntity(state)) return state.getValue(PROPERTY_MATERIAL_TYPE).state;
        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TileTrackAscendingPointer) return ((TileTrackAscendingPointer) tile).getMaterialState();
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (hasTileEntity(state)) return new TileTrackAscendingPointer();
        return null;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(PROPERTY_MATERIAL_TYPE) == EnumMaterialType.OTHER;
    }

    public enum EnumMaterialType implements IStringSerializable {
        // @formatter:off
        /** 0 */ COBBLESTONE (Blocks.cobblestone.getDefaultState()),
        /** 1 */ ANDERSITE   (Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE)),
        /** 2 */ DIORITE     (Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE)),
        /** 3 */ GRANITE     (Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE)),
        /** 4 */ OAK         (Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK)),
        /** 5 */ SPRUCE      (Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE)),
        /** 6 */ BIRCH       (Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.BIRCH)),
        /** 7 */ JUNGLE      (Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.JUNGLE)),
        /** 8 */ ACACIA      (Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA)),
        /** 9 */ DARK_OAK    (Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK)),
        /** A */ UNUSED_A    (null),
        /** B */ UNUSED_B    (null),
        /** C */ UNUSED_C    (null),
        /** D */ UNUSED_D    (null),
        /** E */ UNUSED_E    (null),
        /** F */ OTHER       (Blocks.cobblestone.getDefaultState());
        // @formatter:on

        public final IBlockState state;

        EnumMaterialType(IBlockState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

    }
}
