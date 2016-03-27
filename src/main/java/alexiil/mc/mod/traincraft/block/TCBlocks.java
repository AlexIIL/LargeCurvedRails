package alexiil.mc.mod.traincraft.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mc.mod.traincraft.tile.TileTrackMultiple;
import alexiil.mc.mod.traincraft.tile.TileTrackMultiplePoints;
import alexiil.mc.mod.traincraft.track.Curve;

public enum TCBlocks {
    TRACK_STRAIGHT(() -> new BlockTrackStraight()),
    /* 45 degree turns */
    TRACK_CURVED_HALF_3_RADIUS(() -> new BlockTrackCurvedHalf(Curve.RADIUS_3, 1, 2)),
    TRACK_CURVED_HALF_5_RADIUS(() -> new BlockTrackCurvedHalf(Curve.RADIUS_5, 1.5, 3.5)),
    TRACK_CURVED_HALF_7_RADIUS(() -> new BlockTrackCurvedHalf(Curve.RADIUS_7, 2, 5)),
    TRACK_CURVED_HALF_9_RADIUS(() -> new BlockTrackCurvedHalf(Curve.RADIUS_9, 2.5, 6.5)),
    TRACK_CURVED_HALF_11_RADIUS(() -> new BlockTrackCurvedHalf(Curve.RADIUS_11, 3, 8)),
    /* 90 degree turns */
    TRACK_CURVED_FULL_3_RADIUS(() -> new BlockTrackCurvedFull(Curve.RADIUS_3, 3.5)),
    TRACK_CURVED_FULL_5_RADIUS(() -> new BlockTrackCurvedFull(Curve.RADIUS_5, 5.5)),
    TRACK_CURVED_FULL_7_RADIUS(() -> new BlockTrackCurvedFull(Curve.RADIUS_7, 7.5)),
    TRACK_CURVED_FULL_9_RADIUS(() -> new BlockTrackCurvedFull(Curve.RADIUS_9, 9.5)),
    TRACK_CURVED_FULL_11_RADIUS(() -> new BlockTrackCurvedFull(Curve.RADIUS_11, 11.5)),
    /* Ascending tracks */
    TRACK_ASCENDING_3_LONG(() -> new BlockTrackAscending(3)),
    TRACK_ASCENDING_4_LONG(() -> new BlockTrackAscending(4)),
    TRACK_ASCENDING_6_LONG(() -> new BlockTrackAscending(6)),
    TRACK_ASCENDING_8_LONG(() -> new BlockTrackAscending(8)),
    TRACK_ASCENDING_12_LONG(() -> new BlockTrackAscending(12)),
    /* Pointers */
    TRACK_POINTER(() -> new BlockTrackPointer()),
    TRACK_POINTER_ASCENDING(() -> new BlockTrackPointerAscending()),
    TRACK_MULTIPLE(() -> new BlockTrackMultiple()),;

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

    public static void init() {
        // Special case tile entities
        GameRegistry.registerTileEntity(TileTrackMultiple.class, "traincraft.track.multiple");
        GameRegistry.registerTileEntity(TileTrackMultiple.Tickable.class, "traincraft.track.multiple.tickable");
        GameRegistry.registerTileEntity(TileTrackMultiplePoints.class, "traincraft.track.multiple.points");
        GameRegistry.registerTileEntity(TileTrackMultiplePoints.Tickable.class, "traincraft.track.multiple.points.tickable");
    }

    public Block getBlock() {
        return block;
    }
}
