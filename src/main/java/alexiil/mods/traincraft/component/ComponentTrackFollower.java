package alexiil.mods.traincraft.component;

import java.util.Collections;

import net.minecraft.util.ResourceLocation;

/** Follows a track directly. This essentially creates the "parent" for the motion (and as such it cannot have
 * sub-components). */
public class ComponentTrackFollower extends Component {
    public ComponentTrackFollower(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        super(Collections.emptyMap(), modelLocation, textureLocation);
    }
}
