package alexiil.mods.traincraft.api.track.behaviour;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;

import io.netty.buffer.ByteBuf;

public class BehaviourVanillaState extends TrackBehaviourStateful {
    private final BlockRailBase rail;
    private final String name;
    private EnumRailDirection dir;
    private ITrackPath path;
    private TrackIdentifier identifier;

    public BehaviourVanillaState(String name, BlockRailBase rail, TileEntity tile) {
        this.rail = rail;
        this.name = name;
        this.dir = EnumRailDirection.EAST_WEST;
        this.path = BehaviourVanillaNative.getPath(dir);
        this.identifier = new TrackIdentifier(tile.getWorld().provider.getDimensionId(), tile.getPos(), name + "::" + dir.name());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("direction", dir.getName());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.dir = EnumRailDirection.EAST_WEST;
        this.path = BehaviourVanillaNative.getPath(dir);
        this.identifier = new TrackIdentifier(identifier.worldDim(), identifier.pos(), name + "::" + dir.name());
    }

    @Override
    public void serializeBuf(ByteBuf buffer) {
        buffer.writeByte(dir.getMetadata());
    }

    @Override
    public void deserializeBuf(ByteBuf buffer) {
        dir = EnumRailDirection.byMetadata(buffer.readByte());
        path = BehaviourVanillaNative.getPath(dir);
        identifier = new TrackIdentifier(identifier.worldDim(), identifier.pos(), name + "::" + dir.name());
    }

    @Override
    public StatefulFactory factory() {
        // TODO Auto-generated method stub
        return null;
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
}
