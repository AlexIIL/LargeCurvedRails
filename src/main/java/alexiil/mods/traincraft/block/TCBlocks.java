package alexiil.mods.traincraft.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public enum TCBlocks {
    /** Bog standard straight track. Goes in any of the 4 minecraft directions. (North, East, South, West). */
    STRAIGHT_TRACK(() -> new BlockTrackStraight(), null),
    /** Track that curves from two straight sections. This is the smallest variant */
    CURVED_TRACK_2_WIDE(() -> new BlockTrackCurved(2), null),
    /** Track that curves from two straight sections. */
    CURVED_TRACK_3_WIDE(() -> new BlockTrackCurved(3), null),
    /** Track that curves from two straight sections. */
    CURVED_TRACK_4_WIDE(() -> new BlockTrackCurved(4), null),
    /** Track that curves from two straight sections. */
    CURVED_TRACK_8_WIDE(() -> new BlockTrackCurved(8), null),
    /** Meta-track that points along many pointers to the actual track piece. */
    POINTER_TRACK(() -> new BlockTrackPointer(), null),
    /** Track that slopes in a direction */
    SLOPED_TRACK(() -> null, null),
    /** Track that goes straight in a diagonal direction. (NorthEast, SouthEast, SouthWest, NorthWes). */
    DIAGONAL_TRACK(() -> null, null);

    private final Supplier<Block> supplier;
    private final Class<? extends ItemBlock> itemBlock;

    private Block block;

    private TCBlocks(Supplier<Block> blockSupplier, Class<? extends ItemBlock> itemBlock) {
        this.supplier = blockSupplier;
        this.itemBlock = itemBlock;
    }

    public static void preInit(FMLPreInitializationEvent event) {
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
