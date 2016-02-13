package alexiil.mods.traincraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.train.AlignmentFailureException;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public abstract class ItemPlacableTrain extends Item {

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        EntityRollingStockBase entity = createRollingStock(world);
        try {
            Vec3 lookVec = player.getLookVec().normalize();
            Vec3 lookFrom = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            entity.alignFromPlayer(lookVec, lookFrom, false);
            if (!world.isRemote) world.spawnEntityInWorld(entity);
        } catch (AlignmentFailureException afe) {
            // In the future this will display a notification to the player that something went wrong, but for the
            // moment we will leave it as-is
            return stack;
        }
        return stack;
    }

    public abstract EntityRollingStockBase createRollingStock(World world);
}
