package alexiil.mods.traincraft.block;

import java.util.function.Predicate;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public abstract class BlockSeperatedTrack extends BlockAbstractTrack {
    public static final PropertyBool PROPERTY_MASTER = PropertyBool.create("master");

    public BlockSeperatedTrack(IProperty<?>... properties) {
        super(properties);
    }

    public BlockPos findMaster(IBlockAccess access, BlockPos pos, int maxTries, Predicate<IBlockState> isAnotherBlock, EnumFacing... directions) {
        int tries = maxTries;
        BlockPos toTry = pos;
        while (tries-- > 0) {
            IBlockState state = access.getBlockState(toTry);
            boolean isMaster = state.getValue(PROPERTY_MASTER);
            if (isMaster) return toTry;

            for (EnumFacing face : directions) {
                BlockPos offset = toTry.offset(face);
                IBlockState offsetState = access.getBlockState(offset);
                if (offsetState.getBlock() != this) continue;
                if (isAnotherBlock.test(offsetState)) toTry = offset;
            }
            if (toTry == pos) return null;
        }
        return pos;
    }
}
