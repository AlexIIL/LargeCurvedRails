package alexiil.mc.mod.traincraft.api.track.behaviour;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.INetSerialisable;
import alexiil.mc.mod.traincraft.api.lib.MathUtil;
import alexiil.mc.mod.traincraft.api.track.model.DefaultTrackModel;
import alexiil.mc.mod.traincraft.api.track.model.ITrackModel;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;

/** This controls how a train interacts with a tracks and can get a path that a train can use to follow this track. */
public abstract class TrackBehaviour {
    /** An immutable set of just the origin point to be used for all tracks that have paths which are completly
     * contained within a single block. */
    public static final ImmutableSet<BlockPos> SINGLE_BLOCK_SLAVES = ImmutableSet.of(BlockPos.ORIGIN);

    /** Private constructor to force either of the below types to be used as they are specially coded for. */
    private TrackBehaviour() {}

    /** @return A path that will traverse over this behaviour. */
    public abstract ITrackPath getPath(World world, BlockPos pos, IBlockState state);

    /** @return An identifier that can identify this behaviour verses any other. */
    public abstract TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state);

    /** Checks to see if the appropriate block/tile for this track still exists in the world. */
    public boolean isValid(World world, BlockPos pos, IBlockState state) {
        return true;
    }

    /** Tests to see if the given track is allowed to overlap with this track. Note that you do not need to check to see
     * if the two {@link #getPath(World, BlockPos, IBlockState)} are actually allowed over each other.
     * 
     * @param otherTrack The track to test
     * @return True if you can overlap, false if not. */
    public abstract boolean canOverlap(TrackBehaviour otherTrack);

    /** @return All of the positions that this track passes over. This should include the ORIGIN as an offset. It is
     *         recommended that you create a set with {@link #createSlaveOffsets(ITrackPath)} */
    public abstract Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state);

    /** Makes a set of slave offsets for the given path. Assumes that the track has a width of 0.8. */
    public static Set<BlockPos> createSlaveOffsets(ITrackPath path) {
        Set<BlockPos> tmpSet = new HashSet<>();
        // Calculate slaves
        for (int i = 0; i < path.length() * 5; i++) {
            double offset = (i + 0.5) / path.length();
            Vec3d pos = path.interpolate(offset);
            Vec3d dir = path.direction(offset);
            dir = MathUtil.cross(dir, new Vec3d(0, 1, 0)).normalize();

            tmpSet.add(new BlockPos(pos.add(MathUtil.scale(dir, 0.4))));
            tmpSet.add(new BlockPos(pos));
            tmpSet.add(new BlockPos(pos.add(MathUtil.scale(dir, -0.4))));
        }
        // Remove the ends as curves don't work properly without these
        tmpSet.remove(new BlockPos(path.end().add(MathUtil.scale(path.direction(1), 0.01))));
        tmpSet.remove(new BlockPos(path.start().add(MathUtil.scale(path.direction(0), -0.01))));
        return ImmutableSet.copyOf(tmpSet);
    }

    /** Called once per tick by a stock to let the track intract with the stock. */
    public abstract void onMinecartPass(World world, BlockPos pos, IBlockState state, EntityMinecart minecart);

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("static-method")
    public ITrackModel getModel() {
        return DefaultTrackModel.INSTANCE;
    }

    /** A behaviour that is completly stored within a single block. It is recommended that you also provide a
     * {@link TrackBehaviourStateful} instance to use for allowing your track to overlap with other tracks. */
    public static abstract class TrackBehaviourNative extends TrackBehaviour {
        /** Attempts to convert this track into a stateful instance- that can be used to create points or a crossing. */
        public abstract TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state);
    }

    /** A behaviour that can save and load itself from inside a tile entity. If a subclass implements {@link ITickable}
     * then the wrapping tile used will call {@link ITickable#update()} every tick. getPath, getIdentifier and isValid
     * will ignore all of the arguments you give them. */
    public static abstract class TrackBehaviourStateful extends TrackBehaviour
        implements INBTSerializable<NBTTagCompound>, INetSerialisable {
        /** @return A factory that will create new instances of this behaviour for client->server or world->disk->world
         *         transfer */
        public abstract StatefulFactory factory();

        /** Attempts to convert this track (in position) to its native version (if one exists)
         * 
         * @param owner The tile entity that currently owns this state.
         * @return True if this was converted into block-only format in the world, false. */
        public abstract boolean convertToNative(TileEntity owner);

        @Override
        public final ITrackPath getPath(World world, BlockPos pos, IBlockState state) {
            return getPath();
        }

        public abstract ITrackPath getPath();

        @Override
        public final TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
            return getIdentifier();
        }

        public abstract TrackIdentifier getIdentifier();

        @Override
        public final Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state) {
            return getSlaveOffsets();
        }

        public abstract Set<BlockPos> getSlaveOffsets();

        @Override
        public final void onMinecartPass(World world, BlockPos pos, IBlockState state, EntityMinecart cart) {
            onMinecartPass(cart);
        }

        public abstract void onMinecartPass(EntityMinecart cart);

        // Block related methods
        public void onNeighbourChange(TileEntity owner) {}
    }

    public interface StatefulFactory {
        /** @return A globally unique identifier for this factory. It is recommended this is in the form
         *         "modid:moduniquename" to ensure this is globally unique. */
        String identifier();

        /** @return A new instance of the state that can be loaded from either {@link NBTTagCompound} of a
         *         {@link ByteBuf} or is usable in its default configuration */
        TrackBehaviourStateful create(World world, BlockPos pos);
    }
}
