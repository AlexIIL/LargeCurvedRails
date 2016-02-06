package alexiil.mods.traincraft;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mods.traincraft.item.TCItems;

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
                'I', Items.iron_ingot,
                'S', Items.stick
                // @formatter:on
        );

        GameRegistry.addShapedRecipe(new ItemStack(TCItems.TRACK_STRAIGHT_DIAG.getItem(), 16),
                // @formatter:off
                "I  ",
                "SI ",
                "ISI",
                'I', Items.iron_ingot,
                'S', Items.stick
                // @formatter:on
        );
    }

    private static void addTrainRecipies() {}
}
