package alexiil.mods.traincraft.block;

import java.util.Locale;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.ITrackPath;

/** This "points" to a different block that contains all of the actual information regarding the track path. */
public class BlockTrackPointer extends BlockAbstractTrack {
    // 0: X-1
    // 1: X+1
    // 2: Z-1
    // 3: Z+1
    // 4: Y-1
    // 5: Y+1
    // 6: X-2
    // 7: X+2
    // 8: Z-2
    // 9: Z+2
    // A: X-1,Z-1
    // B: X+1,Z-1
    // C: X-1,Z+1
    // D: X+1,Z+1
    // E: UNUSED
    // F: UNUSED
    public enum EnumOffset implements IStringSerializable {
        XN1(-1, 0, 0),
        XP1(1, 0, 0),
        YN1(0, -1, 0),
        YP1(0, 1, 0),
        ZN1(0, 0, -1),
        ZP1(0, 0, 1),
        XN2(-2, 0, 0),
        XP2(2, 0, 0),
        ZN2(0, 0, -2),
        ZP2(0, 0, 2),
        XN1_ZN1(-1, 0, -1),
        XP1_ZN1(1, 0, -1),
        XN1_ZP1(-1, 0, 1),
        XP1_ZP1(1, 0, 1);

        public final BlockPos offset;
        private final String dispName;

        private EnumOffset(int x, int y, int z) {
            this.offset = new BlockPos(x, y, z);
            dispName = name().toLowerCase(Locale.ROOT).replace("_", "_with_").replace("N", "_negative_").replace("P", "_positive_");
        }

        @Override
        public String getName() {
            return dispName;
        }
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        return null;
    }
}
