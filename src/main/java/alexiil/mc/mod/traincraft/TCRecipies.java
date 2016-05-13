package alexiil.mc.mod.traincraft;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mc.mod.traincraft.item.TCItems;

public class TCRecipies {
    public static void init() {
        addTrackRecipies();
        addTrainRecipies();
    }

    private static void addTrackRecipies() {
        GameRegistry.addShapedRecipe(new ItemStack(TCItems.TRACK_STRAIGHT_AXIS.getItem(), 16),
                // @formatter:off
                "ISI",
                "ISI",
                "ISI",
                'I', Items.IRON_INGOT,
                'S', Items.STICK
                // @formatter:on
        );

        GameRegistry.addShapedRecipe(new ItemStack(TCItems.TRACK_STRAIGHT_DIAG.getItem(), 16),
                // @formatter:off
                "I  ",
                "SI ",
                "ISI",
                'I', Items.IRON_INGOT,
                'S', Items.STICK
                // @formatter:on
        );
    }

    private static void addTrainRecipies() {}
}
