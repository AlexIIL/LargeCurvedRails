package alexiil.mods.traincraft.component;

public class GsonRollingStock {
    public GsonComponent[] components;

    public static class GsonComponent {
        public String identifier;
        public GsonComponent[] components;
        public double origin, attachPointUp;
        public double[] boundingBoxMin, boundingBoxMax;
    }
}
