package alexiil.mods.traincraft.track;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.block.BlockTrackCurvedHalf;
import alexiil.mods.traincraft.lib.NBTUtils;

import io.netty.buffer.ByteBuf;

public class TrackBehaviourCurvedHalfState extends TrackBehaviourStateful {
    public static final String IDENTIFIER = "traincraft:track_curved_half";

    private final Curve.HalfFactory factory;
    private EnumFacing facing;
    private boolean positive;
    private TrackIdentifier identifier;

    public TrackBehaviourCurvedHalfState(World world, BlockPos pos, Curve.HalfFactory factory) {
        this.factory = factory;
        identifier = new TrackIdentifier(world.provider.getDimensionId(), pos, IDENTIFIER);
        setDir(EnumFacing.NORTH, false);
    }

    public void setDir(EnumFacing face, boolean b) {
        if (face.getAxis() == Axis.Y) throw new IllegalArgumentException("Not allowed to curve in the Y direction!");
        facing = face;
        positive = b;
        identifier = new TrackIdentifier(identifier, factory.identifier() + "::" + facing.name() + "," + positive);
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public boolean getPositive() {
        return positive;
    }

    // Serialization
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("facing", NBTUtils.serializeEnum(facing));
        nbt.setBoolean("positive", positive);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        EnumFacing facing = NBTUtils.deserializeEnum(nbt, EnumFacing.class, this.facing);
        boolean positive = nbt.getBoolean("positive");
        setDir(facing, positive);
    }

    @Override
    public void serializeBuf(ByteBuf buffer) {
        buffer.writeByte(facing.getHorizontalIndex());
        buffer.writeBoolean(positive);
    }

    @Override
    public void deserializeBuf(ByteBuf buffer) {
        EnumFacing facing = EnumFacing.getHorizontal(buffer.readByte() & 0b11);
        boolean positive = buffer.readBoolean();
        setDir(facing, positive);
    }

    @Override
    public Curve.HalfFactory factory() {
        return factory;
    }

    @Override
    public boolean convertToNative(TileEntity owner) {
        IBlockState state = factory.parent().halfBlock.getDefaultState();
        state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_DIRECTION, positive);
        state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_FACING, facing);
        return owner.getWorld().setBlockState(owner.getPos(), state);
    }

    @Override
    public boolean canOverlap(TrackBehaviourStateful otherTrack) {
        return true;
    }

    @Override
    public ITrackPath getPath() {
        return factory.getPath(facing, positive).offset(identifier.pos());
    }

    @Override
    public TrackIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void onStockPass(IRollingStock stock) {}

    @Override
    public Set<BlockPos> getSlaveOffsets() {
        return factory.getSlaves(facing, positive);
    }
}
