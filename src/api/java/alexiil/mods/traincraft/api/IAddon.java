package alexiil.mods.traincraft.api;

public interface IAddon {
    void preInit();

    void init();

    void postInit();

    void enable();

    boolean canEnable();

    void disable();

    String getName();
}
