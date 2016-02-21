package alexiil.mods.traincraft.api.track.behaviour;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.INBTSerializable;

import alexiil.mods.traincraft.api.INetSerialisable;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;

import io.netty.buffer.ByteBuf;

/** This controls how a train interacts with a tracks and can get a path that a train can use to follow this track. */
public abstract class TrackBehaviour {
    /** Private constructor to force either of the below types to be used as they are specially coded for. */
    private TrackBehaviour() {}

    /** @return A path that will traverse over this behaviour. So long as a rolling stock is traversing this path
     *         {@link #onStockPass(IRollingStock)} will be called. */
    public abstract ITrackPath getPath();

    /** @return An identifier that can identify this behaviour verses any other. */
    public abstract TrackIdentifier getIdentifier();

    /** Called once per tick by a stock to let the track intract with the stock. */
    public abstract void onStockPass(IRollingStock stock);

    /** A behaviour that is completly stored within a single block. It is recommended that you also provide a
     * {@link TrackBehaviourStateful} instance to use for allowing your track to overlap with other tracks. */
    public static abstract class TrackBehaviourNative extends TrackBehaviour {
        public TrackBehaviourNative() {}

        /** Sets up this object so that it can return the correct values for {@link #getPath()},
         * {@link #getIdentifier()} and correctly applies {@link #onStockPass(IRollingStock)}. This should be called
         * everytime that you use this object from a different place.
         * 
         * @return An object that can be used to get the path, identifer and fire events on. This may or may not be the
         *         same object as this. */
        public abstract TrackBehaviour readFromWorld(World world, BlockPos pos, IBlockState state);
    }

    /** A behaviour that can save and load itself from inside a tile entity. */
    public static abstract class TrackBehaviourStateful extends TrackBehaviour implements INBTSerializable<NBTTagCompound>, INetSerialisable {
        /** @return A factory that will create new instances of this behaviour for client->server or world->disk->world
         *         transfer */
        public abstract StatefulFactory factory();
    }

    public interface StatefulFactory {
        /** @return A globally unique identifier for this factory. It is recommended this is in the form
         *         "modid:moduniquename" to ensure this is globally unique. */
        String identifier();

        /** @return A new instance of the state that can be loaded from either {@link NBTTagCompound} of a
         *         {@link ByteBuf} */
        TrackBehaviourStateful create(TileEntity tile);
    }
}
