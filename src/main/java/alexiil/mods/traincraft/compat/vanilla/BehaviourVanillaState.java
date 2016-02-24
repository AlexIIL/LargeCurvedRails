package alexiil.mods.traincraft.compat.vanilla;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.lib.NBTUtils;

import io.netty.buffer.ByteBuf;

public abstract class BehaviourVanillaState extends TrackBehaviourStateful {
    protected final BlockRailBase rail;
    private final String name;
    private final Set<BlockPos> slaves = ImmutableSet.of(BlockPos.ORIGIN);
    private EnumRailDirection dir;
    private ITrackPath path;
    private TrackIdentifier identifier;

    public BehaviourVanillaState(String name, BlockRailBase rail, World world, BlockPos pos) {
        this.rail = rail;
        this.name = name;
        this.dir = EnumRailDirection.EAST_WEST;
        this.path = BehaviourVanillaNative.getPath(dir).offset(pos);
        this.identifier = new TrackIdentifier(world.provider.getDimensionId(), pos, name + "::" + dir.name());
    }

    public BehaviourVanillaState setDir(EnumRailDirection dir) {
        this.dir = dir;
        this.identifier = new TrackIdentifier(identifier.worldDim(), identifier.pos(), name + "::" + dir.name());
        return this;
    }

    public EnumRailDirection getDir() {
        return dir;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("direction", NBTUtils.serializeEnum(dir));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.dir = NBTUtils.deserializeEnum(nbt.getTag("direction"), EnumRailDirection.class, dir);
        this.path = BehaviourVanillaNative.getPath(dir).offset(identifier.pos());
        this.identifier = new TrackIdentifier(identifier.worldDim(), identifier.pos(), name + "::" + dir.name());
    }

    @Override
    public void serializeBuf(ByteBuf buffer) {
        buffer.writeByte(dir.getMetadata());
    }

    @Override
    public void deserializeBuf(ByteBuf buffer) {
        // TODO: Investigate Should this send over the dim and pos?
        dir = EnumRailDirection.byMetadata(buffer.readByte());
        path = BehaviourVanillaNative.getPath(dir).offset(identifier.pos());
        identifier = new TrackIdentifier(identifier.worldDim(), identifier.pos(), name + "::" + dir.name());
    }

    @Override
    public ITrackPath getPath() {
        return path;
    }

    @Override
    public TrackIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void onStockPass(IRollingStock stock) {}

    @Override
    public boolean convertToNative(TileEntity owner) {
        return owner.getWorld().setBlockState(owner.getPos(), rail.getDefaultState().withProperty(rail.getShapeProperty(), dir));
    }

    @Override
    public boolean canOverlap(TrackBehaviour otherTrack) {
        return true;
    }

    @Override
    public Set<BlockPos> getSlaveOffsets() {
        return slaves;
    }

    public enum Factory implements StatefulFactory {
        NORMAL("normal", (ident, world, pos) -> new Normal(ident, world, pos)),
        ACTIVATOR("activator", (ident, world, pos) -> new Activator(ident, world, pos)),
        DETECTOR("detector", (ident, world, pos) -> new Detector(ident, world, pos)),
        GOLDEN("golden", (ident, world, pos) -> new Speed(ident, world, pos));

        private final String ident;
        private final TriFunction<String, World, BlockPos, BehaviourVanillaState> factory;

        private Factory(String ident, TriFunction<String, World, BlockPos, BehaviourVanillaState> factory) {
            this.ident = "traincraft:vanilla_" + ident;
            this.factory = factory;
        }

        @Override
        public String identifier() {
            return ident;
        }

        @Override
        public BehaviourVanillaState create(World world, BlockPos pos) {
            return factory.apply(ident, world, pos);
        }
    }

    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    public static class Normal extends BehaviourVanillaState {
        public Normal(String name, World world, BlockPos pos) {
            super(name, (BlockRailBase) Blocks.rail, world, pos);
        }

        @Override
        public StatefulFactory factory() {
            return Factory.NORMAL;
        }
    }

    /* TODO: Maybe redstone functionality should be provided via a capability for our own tracks to take advantage of as
     * well? */
    public static abstract class Redstone extends BehaviourVanillaState {
        protected byte redstonePower = 0;

        public Redstone(String name, BlockRailBase rail, World world, BlockPos pos) {
            super(name, rail, world, pos);
        }

        @Override
        public void deserializeBuf(ByteBuf buffer) {
            super.deserializeBuf(buffer);
            redstonePower = buffer.readByte();
        }

        @Override
        public void serializeBuf(ByteBuf buffer) {
            super.serializeBuf(buffer);
            buffer.writeByte(redstonePower);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            redstonePower = nbt.getByte("redstone");
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = super.serializeNBT();
            nbt.setByte("redstone", redstonePower);
            return nbt;
        }
    }

    public static class Activator extends Redstone {
        public Activator(String name, World world, BlockPos pos) {
            super(name, (BlockRailPowered) Blocks.activator_rail, world, pos);
        }

        @Override
        public StatefulFactory factory() {
            return Factory.ACTIVATOR;
        }

        @Override
        public boolean convertToNative(TileEntity owner) {
            IBlockState state = rail.getDefaultState().withProperty(rail.getShapeProperty(), getDir());
            state = state.withProperty(BlockRailPowered.POWERED, this.redstonePower > 0);
            return owner.getWorld().setBlockState(owner.getPos(), state);
        }
    }

    public static class Detector extends Redstone {
        public Detector(String name, World world, BlockPos pos) {
            super(name, (BlockRailDetector) Blocks.detector_rail, world, pos);
        }

        @Override
        public StatefulFactory factory() {
            return Factory.DETECTOR;
        }

        @Override
        public boolean convertToNative(TileEntity owner) {
            IBlockState state = rail.getDefaultState().withProperty(rail.getShapeProperty(), getDir());
            state = state.withProperty(BlockRailDetector.POWERED, this.redstonePower > 0);
            return owner.getWorld().setBlockState(owner.getPos(), state);
        }
    }

    public static class Speed extends Redstone {
        public Speed(String name, World world, BlockPos pos) {
            super(name, (BlockRailPowered) Blocks.golden_rail, world, pos);
        }

        @Override
        public StatefulFactory factory() {
            return Factory.GOLDEN;
        }

        @Override
        public boolean convertToNative(TileEntity owner) {
            IBlockState state = rail.getDefaultState().withProperty(rail.getShapeProperty(), getDir());
            state = state.withProperty(BlockRailPowered.POWERED, this.redstonePower > 0);
            return owner.getWorld().setBlockState(owner.getPos(), state);
        }
    }
}
