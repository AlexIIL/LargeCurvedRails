package alexiil.mc.mod.traincraft.api.track.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Used for all rail generation- this defines the paramaters for the internal model generator to supply valid rails for
 * any of your tracks. */
@SideOnly(Side.CLIENT)
public final class RailGeneneratorParams {
    // 4.3 is the lowest number that makes diagonal straight tracks use 3 sleepers rather than 2
    public static final double SLEEPER_COUNT_PER_METER = 4.3;
    public static final double RAIL_COUNT_PER_METER = 6;

    private TextureAtlasSprite railSprite;
    private double railGap = RAIL_COUNT_PER_METER;
    private float uMin = 1, uMax = 3;
    private boolean left = true, right = true;
    private double width = 2 / 16.0, radius = 5 / 16.0;
    private double yOffset = 0;

    public RailGeneneratorParams(TextureAtlasSprite railSprite) {
        this.railSprite = railSprite;
    }

    // FIXME: These all need comments!

    // Setters
    /** Designates the sprite that will be used for rendering the rail. */
    public RailGeneneratorParams railSprite(TextureAtlasSprite railSprite) {
        this.railSprite = railSprite;
        return this;
    }

    /** How far apart the model curves should be. This must be less than 1 (as that's the longest that a texture can be
     * used). Larger values for curves look odd, however straight sections don't need this to be low. */
    public RailGeneneratorParams railGap(double railGap) {
        this.railGap = railGap;
        return this;
    }

    /** The minimum texture U coord to use when generating rails. This */
    public RailGeneneratorParams uMin(float uMin) {
        this.uMin = uMin;
        return this;
    }

    public RailGeneneratorParams uMax(float uMax) {
        this.uMax = uMax;
        return this;
    }

    public RailGeneneratorParams left(boolean l) {
        this.left = l;
        return this;
    }

    public RailGeneneratorParams right(boolean r) {
        this.right = r;
        return this;
    }

    public RailGeneneratorParams width(double w) {
        this.width = w;
        return this;
    }

    public RailGeneneratorParams radius(double r) {
        this.radius = r;
        return this;
    }

    public RailGeneneratorParams yOffset(double y) {
        this.yOffset = y;
        return this;
    }

    // Getters
    // @formatter:off
    public TextureAtlasSprite railSprite() { return railSprite; }
    public double railGap() { return railGap; }
    public float uMin() { return uMin; }
    public float uMax() { return uMax; }
    public boolean left() { return left; }
    public boolean right() { return right; }
    public double width() { return width; }
    public double radius() { return radius; }
    public double yOffset() { return yOffset; }
    // @formatter:off
}
