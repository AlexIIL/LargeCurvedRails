package alexiil.mods.traincraft.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;

public enum TCBlocks {
    TRACK_STRAIGHT(() -> new BlockTrackStraight()),
    /* 45 degree turns */
    TRACK_CURVED_HALF_3_RADIUS(() -> new BlockTrackCurvedHalf(1, 2)),
    TRACK_CURVED_HALF_5_RADIUS(() -> new BlockTrackCurvedHalf(1.5, 3.5)),
    TRACK_CURVED_HALF_7_RADIUS(() -> new BlockTrackCurvedHalf(2, 5)),
    TRACK_CURVED_HALF_9_RADIUS(() -> new BlockTrackCurvedHalf(2.5, 6.5)),
    TRACK_CURVED_HALF_11_RADIUS(() -> new BlockTrackCurvedHalf(3, 8)),
    /* 90 degree turns */
    TRACK_CURVED_FULL_3_RADIUS(() -> new BlockTrackCurvedFull(3.5)),
    TRACK_CURVED_FULL_5_RADIUS(() -> new BlockTrackCurvedFull(5.5)),
    TRACK_CURVED_FULL_7_RADIUS(() -> new BlockTrackCurvedFull(7.5)),
    TRACK_CURVED_FULL_9_RADIUS(() -> new BlockTrackCurvedFull(9.5)),
    TRACK_CURVED_FULL_11_RADIUS(() -> new BlockTrackCurvedFull(11.5)),
    /* Ascending tracks */
    TRACK_ASCENDING_3_LONG(() -> new BlockTrackAscending(3)),
    TRACK_ASCENDING_4_LONG(() -> new BlockTrackAscending(4)),
    TRACK_ASCENDING_6_LONG(() -> new BlockTrackAscending(6)),
    TRACK_ASCENDING_8_LONG(() -> new BlockTrackAscending(8)),
    TRACK_ASCENDING_12_LONG(() -> new BlockTrackAscending(12)),
    /* Pointers */
    TRACK_POINTER(() -> new BlockTrackPointer()),
    TRACK_POINTER_ASCENDING(() -> new BlockTrackPointerAscending()),
    // TRACK_POINTER_MULTIPLE(() -> new BlockTrackMultiplePointer()),
    // TRACK_MULTIPLE(() -> new BlockTrackMultiple()),
    ;

    private final Supplier<Block> supplier;
    private final Class<? extends ItemBlock> itemBlock;

    private Block block;

    private TCBlocks(Supplier<Block> blockSupplier) {
        this(blockSupplier, null);
    }

    private TCBlocks(Supplier<Block> blockSupplier, Class<? extends ItemBlock> itemBlock) {
        this.supplier = blockSupplier;
        this.itemBlock = itemBlock;
    }

    public static void preInit() {
        for (TCBlocks tcBlock : values()) {
            tcBlock.block = tcBlock.supplier.get();
            // TODO: Remove this when all blocks are added.
            if (tcBlock.block == null) continue;
            String name = tcBlock.name().toLowerCase(Locale.ROOT);
            tcBlock.block.setUnlocalizedName(name + ".name");
            tcBlock.block.setRegistryName(new ResourceLocation("traincraft", name));
            GameRegistry.registerBlock(tcBlock.getBlock(), tcBlock.itemBlock);
        }
    }

    public Block getBlock() {
        return block;
    }
}
