package alexiil.mc.mod.traincraft.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

public class TileTrackAscendingPointer extends TileEntity {
    private IBlockState state;

    public TileTrackAscendingPointer() {}

    public IBlockState getMaterialState() {
        return state;
    }

    public void setMaterial(IBlockState state) {
        if (state == null) throw new NullPointerException();
        this.state = state;
    }

}
