package alexiil.mods.traincraft.component;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.component.ComponentResting;
import alexiil.mods.traincraft.api.component.IComponent;

public class ComponentCart extends ComponentResting {
    public ComponentCart(IRollingStock stock, IComponent childFront, IComponent childBack, double frontBack) {
        super(stock, childFront, childBack, frontBack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        childFront.render(stock, partialTicks);
        childBack.render(stock, partialTicks);

        GlStateManager.disableTexture2D();
        GlStateManager.color(0, 1, 0);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        return new ComponentCart(stock, childFront.createNew(stock), childBack.createNew(stock), frontBack);
    }
}
