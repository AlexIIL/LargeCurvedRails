package alexiil.mc.mod.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;
import alexiil.mc.mod.traincraft.block.BlockAbstractTrack;
import alexiil.mc.mod.traincraft.block.BlockTrackAscending;
import alexiil.mc.mod.traincraft.block.EnumDirection;
import alexiil.mc.mod.traincraft.client.model.Plane.Face;
import alexiil.mc.mod.traincraft.client.model.test.ModelSplitter;
import alexiil.mc.mod.traincraft.property.BlockStatePropWrapper;

public class TrackAscendingBlockModel extends TrackGenericBlockModel {
    private final BlockTrackAscending ascending;

    @Override
    public long maxCacheSize() {
        return 10 * 6 * 2;// 10 material types (atm), 6 possible axis, 2 [ascending, descending]
    }

    public TrackAscendingBlockModel(BlockTrackAscending ascending) {
        this.ascending = ascending;
    }

    @Override
    public ITrackPath path(IBlockState state) {
        return ascending.path(state);
    }

    @Override
    public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {
        // Get the material model
        IBlockState materialState = null;
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState extended = (IExtendedBlockState) state;
            BlockStatePropWrapper wrapper = extended.getValue((IUnlistedProperty<BlockStatePropWrapper>) BlockTrackAscending.MATERIAL_TYPE);
            if (wrapper != null) materialState = wrapper.state;
        }
        IBakedModel materialBaked;
        if (materialState == null) {
            materialBaked = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
        } else materialBaked = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(materialState);

        // Bake the material model
        EnumDirection dir = state.getValue(BlockTrackAscending.TRACK_DIRECTION);
        EnumFacing mf = dir.from;
        if (state.getValue(BlockTrackAscending.ASCEND_DIRECTION)) mf = dir.to;
        if (dir == EnumDirection.EAST_WEST) mf = mf.getOpposite();
        // It must be an ascending track that goes along a normal axis.
        TrackPathStraight straight = (TrackPathStraight) path;
        Vec3d planePoint = straight.interpolate(0.5).addVector(0, -BlockAbstractTrack.TRACK_HEIGHT, 0);
        Vec3d planeNormal = new Vec3d(0, ascending.length, 0);
        planeNormal = planeNormal.add(new Vec3d(BlockPos.ORIGIN.offset(mf, -1)));
        Plane plane = new Plane(planePoint, planeNormal);

        List<BakedQuad> materialQuads = new ArrayList<>(materialBaked.getQuads(materialState, null, 0));
        for (EnumFacing face : EnumFacing.values())
            materialQuads.addAll(materialBaked.getQuads(materialState, face, 0));

        List<MutableQuad> mutableMaterial = ModelSplitter.makeMutable(materialQuads, DefaultVertexFormats.BLOCK);
        List<MutableQuad> allOffsets = new ArrayList<>();
        for (BlockPos offset : ascending.slaveOffsets(straight)) {
            allOffsets.addAll(ModelSplitter.offset(mutableMaterial, new Vec3d(offset)));
        }
        List<MutableQuad>[] bisected = ModelSplitter.bisect(allOffsets, plane);
        List<MutableQuad> squishedQuadList = ModelSplitter.squashBisected(bisected, plane, Face.AWAY);
        // squishedQuadList.forEach(quad -> quad.diffuse = false);
        quads.addAll(ModelSplitter.makeVanilla(squishedQuadList, DefaultVertexFormats.BLOCK));
    }

    @Override
    protected List<BakedQuad> generateSleepers(IBlockState state, ITrackPath path) {
        boolean ascending = state.getValue(BlockTrackAscending.ASCEND_DIRECTION);
        EnumDirection dir = state.getValue(BlockAbstractTrack.TRACK_DIRECTION);
        if (dir == EnumDirection.NORTH_SOUTH) ascending = !ascending;
        return CommonModelSpriteCache.generateSleepers(path, CommonModelSpriteCache.INSTANCE.loadSleepers(), ascending);
    }
}
