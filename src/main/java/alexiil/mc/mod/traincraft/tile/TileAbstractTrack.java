package alexiil.mc.mod.traincraft.tile;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;

public abstract class TileAbstractTrack extends TileEntity {
    public abstract List<BehaviourWrapper> getWrappedBehaviours();
}
