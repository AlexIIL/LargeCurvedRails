package alexiil.mods.traincraft.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import alexiil.mods.traincraft.block.BlockAbstractTrack;
import alexiil.mods.traincraft.block.BlockAbstractTrack.EnumDirection;
import alexiil.mods.traincraft.block.BlockTrackCurvedFull;
import alexiil.mods.traincraft.block.BlockTrackCurvedHalf;
import alexiil.mods.traincraft.block.TCBlocks;

public class CurvedTrackJoiner extends TrackJoinerSimple {
    public CurvedTrackJoiner(Map<BlockPos, WorldBlockState> map) {
        super(map);
    }

    public static List<CurvedTrackJoiner> create(TCBlocks half, TCBlocks full) {
        return create((BlockTrackCurvedHalf) half.getBlock(), (BlockTrackCurvedFull) full.getBlock());
    }

    public static List<CurvedTrackJoiner> create(BlockTrackCurvedHalf half, BlockTrackCurvedFull full) {
        List<CurvedTrackJoiner> all = new ArrayList<>();
        // North
        {
            IBlockState halfFirst = half.getDefaultState().withProperty(BlockAbstractTrack.PROPERTY_FACING, EnumFacing.NORTH).withProperty(
                    BlockTrackCurvedHalf.PROPERTY_DIRECTION, true);
            IBlockState middle = TCBlocks.TRACK_STRAIGHT.getBlock().getDefaultState().withProperty(BlockAbstractTrack.TRACK_DIRECTION,
                    EnumDirection.NORTH_WEST);
            IBlockState halfSecond = half.getDefaultState().withProperty(BlockAbstractTrack.PROPERTY_FACING, EnumFacing.WEST).withProperty(
                    BlockTrackCurvedHalf.PROPERTY_DIRECTION, true);

            IBlockState fullTo = full.getDefaultState().withProperty(BlockAbstractTrack.PROPERTY_FACING, EnumFacing.NORTH);

            Map<BlockPos, WorldBlockState> map = new HashMap<>();
        }

        return all;
    }
}
