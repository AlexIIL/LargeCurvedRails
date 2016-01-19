package alexiil.mods.traincraft.item;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mods.traincraft.TCTabs;
import alexiil.mods.traincraft.block.TCBlocks;

public enum TCItems {
    TRACK_STRAIGHT_AXIS(() -> new ItemTrackStraight(TCBlocks.STRAIGHT_TRACK.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_STRAIGHT_DIAG(() -> new ItemTrackDiagonal(TCBlocks.STRAIGHT_TRACK.getBlock()), () -> TCTabs.TRAINCRAFT);

    private final Supplier<Item> supplier;
    private final Supplier<TCTabs> tabSupplier;
    private Item item;

    private TCItems(Supplier<Item> itemSupplier, Supplier<TCTabs> tabSupplier) {
        this.supplier = itemSupplier;
        this.tabSupplier = tabSupplier;
    }

    public static void preInit() {
        for (TCItems enumItem : values()) {
            enumItem.item = enumItem.supplier.get();
            // TODO: Remove this when all items are added.
            if (enumItem.item == null) continue;
            String name = enumItem.name().toLowerCase(Locale.ROOT);
            enumItem.item.setUnlocalizedName(name);
            enumItem.item.setRegistryName(new ResourceLocation("traincraft", name));
            GameRegistry.registerItem(enumItem.item);

            enumItem.item.setCreativeTab(enumItem.tabSupplier.get().tab);
        }
    }

    public Item getItem() {
        return item;
    }
}
