package alexiil.mods.traincraft.item;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mods.traincraft.TCTabs;
import alexiil.mods.traincraft.block.TCBlocks;

public enum TCItems {
    /* Tracks */
    TRACK_STRAIGHT_AXIS(() -> new ItemTrackStraight(TCBlocks.STRAIGHT_TRACK.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_STRAIGHT_DIAG(() -> new ItemTrackDiagonal(TCBlocks.STRAIGHT_TRACK.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_3(() -> new ItemTrackAscendingAxis(TCBlocks.ASCENDING_TRACK_3_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_4(() -> new ItemTrackAscendingAxis(TCBlocks.ASCENDING_TRACK_4_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_6(() -> new ItemTrackAscendingAxis(TCBlocks.ASCENDING_TRACK_6_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_8(() -> new ItemTrackAscendingAxis(TCBlocks.ASCENDING_TRACK_8_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_AXIS_12(() -> new ItemTrackAscendingAxis(TCBlocks.ASCENDING_TRACK_12_LONG.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_3(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_4(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_6(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_8(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_ASCENDING_DIAG_12(() -> null, () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_2(() -> new ItemTrackCurved(TCBlocks.CURVED_TRACK_2_WIDE.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_3(() -> new ItemTrackCurved(TCBlocks.CURVED_TRACK_3_WIDE.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_4(() -> new ItemTrackCurved(TCBlocks.CURVED_TRACK_4_WIDE.getBlock()), () -> TCTabs.TRAINCRAFT),
    TRACK_CURVED_8(() -> new ItemTrackCurved(TCBlocks.CURVED_TRACK_8_WIDE.getBlock()), () -> TCTabs.TRAINCRAFT),
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
    
    
    
    
    
    
    
    // For some reason items don't get the texture. Hmm.
    
    
    
    
    
    
    
    
    
    
    
    

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
