package alexiil.mods.traincraft.tile;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;

public abstract class TileAbstractTrack extends TileEntity {
    public abstract List<TrackBehaviourStateful> getBehaviours();
}
