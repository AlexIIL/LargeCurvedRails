package alexiil.mc.mod.traincraft.compat.vanilla;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mc.mod.traincraft.api.track.model.DefaultTrackModel;
import alexiil.mc.mod.traincraft.api.track.model.ITrackModel;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;

public abstract class BehaviourVanillaNative extends TrackBehaviourNative {
    private static final Map<EnumRailDirection, ITrackPath> pathMap = new EnumMap<>(EnumRailDirection.class);

    static {
        double trackHeight = 2 / 16.0;

        Vec3 north = new Vec3(0.5, trackHeight, 0);
        Vec3 south = new Vec3(0.5, trackHeight, 1);
        Vec3 west = new Vec3(0, trackHeight, 0.5);
        Vec3 east = new Vec3(1, trackHeight, 0.5);

        Vec3 up = new Vec3(0, 1, 0);

        BlockPos from = new BlockPos(0, 0, 0);

        pathMap.put(EnumRailDirection.NORTH_SOUTH, new TrackPathStraight(north, south, from));
        pathMap.put(EnumRailDirection.EAST_WEST, new TrackPathStraight(east, west, from));
        pathMap.put(EnumRailDirection.ASCENDING_EAST, new TrackPathStraight(west, east.add(up), from));
        pathMap.put(EnumRailDirection.ASCENDING_WEST, new TrackPathStraight(east, west.add(up), from));
        pathMap.put(EnumRailDirection.ASCENDING_NORTH, new TrackPathStraight(south, north.add(up), from));
        pathMap.put(EnumRailDirection.ASCENDING_SOUTH, new TrackPathStraight(north, south.add(up), from));

        pathMap.put(EnumRailDirection.SOUTH_EAST, TrackPath2DArc.createDegrees(from, new Vec3(1, trackHeight, 1), 0.5, 180, 270));
        pathMap.put(EnumRailDirection.SOUTH_WEST, TrackPath2DArc.createDegrees(from, new Vec3(0, trackHeight, 1), 0.5, 270, 360));
        pathMap.put(EnumRailDirection.NORTH_WEST, TrackPath2DArc.createDegrees(from, new Vec3(0, trackHeight, 0), 0.5, 0, 90));
        pathMap.put(EnumRailDirection.NORTH_EAST, TrackPath2DArc.createDegrees(from, new Vec3(1, trackHeight, 0), 0.5, 90, 180));
    }

    protected final BlockRailBase rail;
    private final String uniqueName;

    public BehaviourVanillaNative(BlockRailBase rail, String name) {
        this.rail = rail;
        this.uniqueName = name;
    }

    public static ITrackPath getPath(EnumRailDirection dir) {
        return pathMap.get(dir);
    }

    public ITrackPath getDefaultPath(IBlockState state) {
        EnumRailDirection dir = state.getValue(rail.getShapeProperty());
        return pathMap.get(dir);
    }

    @Override
    public ITrackPath getPath(World world, BlockPos pos, IBlockState state) {
        EnumRailDirection dir = state.getValue(rail.getShapeProperty());
        return pathMap.get(dir).offset(pos);
    }

    @Override
    public TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
        EnumRailDirection dir = state.getValue(rail.getShapeProperty());
        return new TrackIdentifier(world.provider.getDimensionId(), pos, "traincraft:vanilla_" + uniqueName + ":" + dir.getName());
    }

    @Override
    public void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {}

    @Override
    public boolean canOverlap(TrackBehaviour otherTrack) {
        return true;
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state) {
        return TrackBehaviour.SINGLE_BLOCK_SLAVES;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ITrackModel getModel() {
        // FIXME: This should be different for all the different types
        return DefaultTrackModel.INSTANCE;
    }

    public static class Normal extends BehaviourVanillaNative {
        public static final Normal INSTANCE = new Normal();

        private Normal() {
            super((BlockRailBase) Blocks.rail, "normal");
        }

        @Override
        public BehaviourVanillaState convertToStateful(World world, BlockPos pos, IBlockState state) {
            EnumRailDirection dir = world.getBlockState(pos).getValue(rail.getShapeProperty());
            // Perhaps this could be for saving tracks with block underneath?
            if (dir.isAscending()) return null;
            return BehaviourVanillaState.Factory.NORMAL.create(world, pos).setDir(dir);
        }
    }

    public static class Activator extends BehaviourVanillaNative {
        public static final Activator INSTANCE = new Activator();

        private Activator() {
            super((BlockRailBase) Blocks.activator_rail, "activator");
        }

        @Override
        public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
            EnumRailDirection dir = world.getBlockState(pos).getValue(rail.getShapeProperty());
            // Perhaps this could be for saving tracks with block underneath?
            if (dir.isAscending()) return null;
            return BehaviourVanillaState.Factory.ACTIVATOR.create(world, pos).setDir(dir);
        }
    }

    public static class Detector extends BehaviourVanillaNative {
        public static final Detector INSTANCE = new Detector();

        private Detector() {
            super((BlockRailBase) Blocks.detector_rail, "detector");
        }

        @Override
        public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
            EnumRailDirection dir = world.getBlockState(pos).getValue(rail.getShapeProperty());
            // Perhaps this could be for saving tracks with block underneath?
            if (dir.isAscending()) return null;
            return BehaviourVanillaState.Factory.DETECTOR.create(world, pos).setDir(dir);
        }
    }

    public static class Golden extends BehaviourVanillaNative {
        public static final Golden INSTANCE = new Golden();

        private Golden() {
            super((BlockRailBase) Blocks.golden_rail, "golden");
        }

        @Override
        public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
            EnumRailDirection dir = world.getBlockState(pos).getValue(rail.getShapeProperty());
            // Perhaps this could be for saving tracks with block underneath?
            if (dir.isAscending()) return null;
            return BehaviourVanillaState.Factory.GOLDEN.create(world, pos).setDir(dir);
        }

        @Override
        public void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {
            double momentum = stock.momentum();
            if (momentum < 0) {
                momentum -= 20;
            } else if (momentum > 0) {
                momentum += 20;
            } else return;
            stock.setSpeed(momentum / stock.weight());
        }
    }
}
