package alexiil.mods.traincraft.component;

public class GsonComponentData {
    public double[] boxMin, boxMax;
    public String modelLocation;
    public String textureLocation;
    public EnumCompType type;
    public double wheelRadius;

    public enum EnumCompType {
        WHEEL,
        RESTING
    }
}
