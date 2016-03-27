package alexiil.mc.mod.traincraft;

import java.util.function.Supplier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public enum TCTabs {
    TRAINCRAFT("traincraft", () -> Items.minecart);

    public final TCTab tab;
    private final Supplier<Item> itemSupplier;
    private Item shownItem = Items.minecart;

    private TCTabs(String name, Supplier<Item> itemSupplier) {
        this.tab = new TCTab(name);
        this.itemSupplier = itemSupplier;
    }

    public static void preInit() {
        for (TCTabs tcTab : values()) {
            Item shown = tcTab.itemSupplier.get();
            if (shown != null) tcTab.shownItem = shown;
        }
    }

    public class TCTab extends CreativeTabs {
        public TCTab(String label) {
            super(label);
        }

        @Override
        public Item getTabIconItem() {
            return TCTabs.this.shownItem;
        }
    }
}
