package alexiil.mods.traincraft.item;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mods.traincraft.TCTabs;
import alexiil.mods.traincraft.block.TCBlocks;

public enum TCItems {
    /* Tracks */
    TRACK_STRAIGHT_AXIS(() -> new ItemTrackStraight(TCBlocks.TRACK_STRAIGHT.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_STRAIGHT_DIAG(() -> new ItemTrackDiagonal(TCBlocks.TRACK_STRAIGHT.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_3(() -> new ItemTrackAscendingAxis(TCBlocks.TRACK_ASCENDING_3_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_4(() -> new ItemTrackAscendingAxis(TCBlocks.TRACK_ASCENDING_4_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_6(() -> new ItemTrackAscendingAxis(TCBlocks.TRACK_ASCENDING_6_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_8(() -> new ItemTrackAscendingAxis(TCBlocks.TRACK_ASCENDING_8_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_12(() -> new ItemTrackAscendingAxis(TCBlocks.TRACK_ASCENDING_12_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_3(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_4(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_6(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_8(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_12(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_HALF_3(() -> new ItemTrackCurved(TCBlocks.TRACK_CURVED_HALF_3_RADIUS, TCBlocks.TRACK_CURVED_FULL_3_RADIUS), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_HALF_5(() -> new ItemTrackCurved(TCBlocks.TRACK_CURVED_HALF_5_RADIUS, TCBlocks.TRACK_CURVED_FULL_5_RADIUS), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_HALF_7(() -> new ItemTrackCurved(TCBlocks.TRACK_CURVED_HALF_7_RADIUS, TCBlocks.TRACK_CURVED_FULL_7_RADIUS), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_HALF_9(() -> new ItemTrackCurved(TCBlocks.TRACK_CURVED_HALF_9_RADIUS, TCBlocks.TRACK_CURVED_FULL_9_RADIUS), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_HALF_11(() -> new ItemTrackCurved(TCBlocks.TRACK_CURVED_HALF_11_RADIUS, TCBlocks.TRACK_CURVED_FULL_11_RADIUS), () -> TCTabs.TRAINCRAFT),
    /* Trains */
    TRAIN_SMALL_STEAM_LOCOMOTIVE(() -> new ItemTrainLocomotiveSteamSmall(), () -> TCTabs.TRAINCRAFT),
    /* Rolling stock */
    STOCK_SMALL_CART(() -> new ItemTrainCartSmall(), () -> TCTabs.TRAINCRAFT),
    /* Misc */
    MISC_STAKE(() -> null, () -> TCTabs.TRAINCRAFT);

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
            enumItem.item.setUnlocalizedName("traincraft." + name);
            enumItem.item.setRegistryName(new ResourceLocation("traincraft", name));
            GameRegistry.registerItem(enumItem.item);
        }
    }

    public static void init() {
        for (TCItems enumItem : values()) {
            if (enumItem.item == null) continue;
            enumItem.item.setCreativeTab(enumItem.tabSupplier.get().tab);
        }
    }

    public Item getItem() {
        return item;
    }
}
