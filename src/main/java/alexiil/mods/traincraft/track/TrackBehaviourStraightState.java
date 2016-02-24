package alexiil.mods.traincraft.track;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.block.BlockAbstractTrack;
import alexiil.mods.traincraft.block.EnumDirection;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.lib.NBTUtils;

import io.netty.buffer.ByteBuf;

public class TrackBehaviourStraightState extends TrackBehaviourStateful {
    private static final Set<BlockPos> SLAVES = ImmutableSet.of(BlockPos.ORIGIN);
    private static final String IDENTIFIER = "traincraft:track_straight";
    private EnumDirection dir = EnumDirection.EAST_WEST;
    private TrackIdentifier identifier;

    public TrackBehaviourStraightState(World world, BlockPos pos) {
        identifier = new TrackIdentifier(world.provider.getDimensionId(), pos, IDENTIFIER);
        setDir(EnumDirection.EAST_WEST);
    }

    public void setDir(EnumDirection dir) {
        this.dir = dir;
        identifier = new TrackIdentifier(identifier, IDENTIFIER + "::" + dir.name());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("dir", NBTUtils.serializeEnum(dir));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        setDir(NBTUtils.deserializeEnum(nbt.getTag("dir"), EnumDirection.class, dir));
    }

    @Override
    public void serializeBuf(ByteBuf buffer) {
        dir.serializeBuf(buffer);
    }

    @Override
    public void deserializeBuf(ByteBuf buffer) {
        setDir(EnumDirection.deserializeBuf(buffer));
    }

    @Override
    public StatefulFactory factory() {
        return Factory.INSTANCE;
    }

    @Override
    public boolean convertToNative(TileEntity owner) {
        World world = owner.getWorld();
        IBlockState state = TCBlocks.TRACK_STRAIGHT.getBlock().getDefaultState();
        state = state.withProperty(BlockAbstractTrack.TRACK_DIRECTION, dir);
        return world.setBlockState(owner.getPos(), state);
    }

    @Override
    public boolean canOverlap(TrackBehaviour otherTrack) {
        return true;
    }

    @Override
    public ITrackPath getPath() {
        return dir.path.offset(identifier.pos());
    }

    @Override
    public TrackIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void onStockPass(IRollingStock stock) {}

    @Override
    public Set<BlockPos> getSlaveOffsets() {
        return SLAVES;
    }

    public enum Factory implements StatefulFactory {
        INSTANCE;

        @Override
        public String identifier() {
            return IDENTIFIER;
        }

        @Override
        public TrackBehaviourStateful create(World world, BlockPos pos) {
            return new TrackBehaviourStraightState(world, pos);
        }
    }
}
