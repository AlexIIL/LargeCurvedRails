package alexiil.mods.traincraft.component;

public class GsonRollingStock {
    public GsonComponent component;
    public double connectionOffsetFront, connectionOffsetBack;

    public static class GsonComponent {
        public String identifier;
        public GsonComponent frontUnder;
        public GsonComponent backUnder;
        public double origin, attachPointUp;
        public double[] boundingBoxMin, boundingBoxMax;
    }
}
