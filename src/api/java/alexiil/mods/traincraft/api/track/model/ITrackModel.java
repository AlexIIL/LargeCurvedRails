package alexiil.mods.traincraft.api.track.model;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.track.path.ITrackPath;

@SideOnly(Side.CLIENT)
public interface ITrackModel {
    /** @return A list of the rail generation paramaters for the internal model generator to auto-generate rails. It is
     *         highly recommended that you use this rather than your own custom version. */
    List<RailGeneneratorParams> getRailGen();

    List<ISleeperGen> getSleeperGen();

    /** @return A list of extra components that will generate alongside the path. Most tracks don't need this. */
    default List<IModelComponent> getExtraComponents() {
        return Collections.emptyList();
    }

    @SideOnly(Side.CLIENT)
    public interface IModelComponent {
        List<BakedQuad> generate(ITrackPath path);
    }

    @SideOnly(Side.CLIENT)
    public interface ISleeperGen {
        // TODO: Think about how best to do this!
        /* Perhaps this should be done with a "get sleeper model base" method, and then be used as arguments for an
         * internal sleeper generator (as sleepers need to be more flexible to allow for proper points + crossings) */
    }
}
