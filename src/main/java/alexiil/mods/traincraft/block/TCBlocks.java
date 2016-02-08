package alexiil.mods.traincraft.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;

public enum TCBlocks {
    /** Bog standard straight track. Goes in any of the 4 minecraft directions (North, East, South, West) and all
     * diagonal directions (NorthEast, SouthEast, SouthWest, NorthWest). */
    STRAIGHT_TRACK(() -> new BlockTrackStraight()),
    /** Track that curves from two straight sections. This is the smallest variant */
    CURVED_TRACK_2_WIDE(() -> new BlockTrackCurved(2)),
    /** Track that curves from two straight sections. */
    CURVED_TRACK_3_WIDE(() -> new BlockTrackCurved(3)),
    /** Track that curves from two straight sections. */
    CURVED_TRACK_4_WIDE(() -> new BlockTrackCurved(4)),
    /** Track that curves from two straight sections. */
    CURVED_TRACK_8_WIDE(() -> new BlockTrackCurved(8)),
    /** Meta-track that points along many pointers to the actual track piece. */
    POINTER_TRACK(() -> new BlockTrackPointer()),
    /** Track that ascends in a direction. */
    ASCENDING_TRACK_3_LONG(() -> new BlockTrackAscending(3)),
    ASCENDING_TRACK_4_LONG(() -> new BlockTrackAscending(4)),
    ASCENDING_TRACK_6_LONG(() -> new BlockTrackAscending(6)),
    ASCENDING_TRACK_8_LONG(() -> new BlockTrackAscending(8)),
    ASCENDING_TRACK_12_LONG(() -> new BlockTrackAscending(12)),
    ASCENDING_TRACK_POINTER(() -> new BlockTrackAscendingPointer()),
    TRACK_MULTIPLE_POINTER(() -> new BlockTrackMultiplePointer()),
    TRACK_MULTIPLE(() -> new BlockTrackMultiple());

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
