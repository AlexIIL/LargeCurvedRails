package alexiil.mc.mod.traincraft;

import java.util.function.Supplier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum TCTabs {
    TRAINCRAFT("traincraft", () -> new ItemStack(Items.MINECART));

    public final TCTab tab;
    private final Supplier<ItemStack> itemSupplier;
    private ItemStack shownItem = new ItemStack(Items.MINECART);

    private TCTabs(String name, Supplier<ItemStack> itemSupplier) {
        this.tab = new TCTab(name);
        this.itemSupplier = itemSupplier;
    }

    public static void preInit() {
        for (TCTabs tcTab : values()) {
            ItemStack shown = tcTab.itemSupplier.get();
            if (shown != null) tcTab.shownItem = shown;
        }
    }

    public class TCTab extends CreativeTabs {
        public TCTab(String label) {
            super(label);
        }

        @Override
        public ItemStack getTabIconItem() {
            return TCTabs.this.shownItem;
        }
    }
}
