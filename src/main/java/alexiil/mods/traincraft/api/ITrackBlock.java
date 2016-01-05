package alexiil.mods.traincraft.api;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

/** Designates that a train can go from one section to another of this track block. Used to handle positioning trains on
 * tracks. */
public interface ITrackBlock {
    /** Gets an {@link ITrackPath} given the approximate direction you wish to go in.
     * 
     * @return A path that starts at the middle of the bottom of the track and ends at the middle of the bottom of the
     *         track */
    ITrackPath path(IBlockAccess access, BlockPos pos, EnumFacing approximateDirection);
}
