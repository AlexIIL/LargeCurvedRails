package alexiil.mods.traincraft.api.track.behaviour;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mods.traincraft.api.track.path.TrackPathStraight;
import alexiil.mods.traincraft.api.train.IRollingStock;

public class BehaviourVanillaNative extends TrackBehaviourNative {
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

    private final BlockRailBase rail;

    private ITrackPath currentPath;
    private TrackIdentifier currentIdentifier;

    public BehaviourVanillaNative(BlockRailBase rail) {
        this.rail = rail;
    }

    public static ITrackPath getPath(EnumRailDirection dir) {
        return pathMap.get(dir);
    }

    @Override
    public TrackBehaviour readFromWorld(World world, BlockPos pos, IBlockState state) {
        EnumRailDirection dir = state.getValue(rail.getShapeProperty());
        currentPath = pathMap.get(dir);
        currentPath = currentPath.offset(pos);
        return this;
    }

    @Override
    public ITrackPath getPath() {
        return currentPath;
    }

    @Override
    public TrackIdentifier getIdentifier() {
        return currentIdentifier;
    }

    @Override
    public void onStockPass(IRollingStock stock) {}
}
