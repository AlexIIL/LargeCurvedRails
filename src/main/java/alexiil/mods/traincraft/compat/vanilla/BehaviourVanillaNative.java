package alexiil.mods.traincraft.compat.vanilla;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mods.traincraft.api.track.path.TrackPathStraight;
import alexiil.mods.traincraft.api.train.IRollingStock;

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

    public BehaviourVanillaNative(BlockRailBase rail) {
        this.rail = rail;
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
        return new TrackIdentifier(world.provider.getDimensionId(), pos, "native");
    }

    @Override
    public void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {}

    public static class Normal extends BehaviourVanillaNative {
        public static final Normal INSTANCE = new Normal();

        private Normal() {
            super((BlockRailBase) Blocks.rail);
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
            super((BlockRailBase) Blocks.activator_rail);
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
            super((BlockRailBase) Blocks.detector_rail);
        }

        @Override
        public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
            EnumRailDirection dir = world.getBlockState(pos).getValue(rail.getShapeProperty());
            // Perhaps this could be for saving tracks with block underneath?
            if (dir.isAscending()) return null;
            return BehaviourVanillaState.Factory.DETECTOR.create(world, pos).setDir(dir);
        }
    }

    public static class Speed extends BehaviourVanillaNative {
        public static final Speed INSTANCE = new Speed();

        private Speed() {
            super((BlockRailBase) Blocks.golden_rail);
        }

        @Override
        public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
            EnumRailDirection dir = world.getBlockState(pos).getValue(rail.getShapeProperty());
            // Perhaps this could be for saving tracks with block underneath?
            if (dir.isAscending()) return null;
            return BehaviourVanillaState.Factory.SPEED.create(world, pos).setDir(dir);
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
