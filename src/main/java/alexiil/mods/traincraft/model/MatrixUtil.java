package alexiil.mods.traincraft.model;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.TrainCraft;

public class MatrixUtil {
    public static Matrix4f rotateTo(Vec3 direction) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Vector3f from = new Vector3f(0, 0, 1);
        Vector3f to = new Vector3f((float) direction.xCoord, (float) direction.yCoord, (float) direction.zCoord);

        Vector3f around = new Vector3f();
        around.cross(from, to);
        around.normalize();

        // P = wanted
        // D = current

        float pDotD = from.dot(to);
        Vector3f fromPerp = new Vector3f(1, 0, 0);
        float pPerpDotD = fromPerp.dot(to);

        float angle = (float) Math.atan2(pPerpDotD, pDotD);

        matrix.setRotation(new AxisAngle4f(around, angle));

        return matrix;
    }

    public static Matrix4f translation(Vec3 pos) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.setTranslation(new Vector3f((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord));
        TrainCraft.trainCraftLog.info("\n\n" + pos + " -> \n" + matrix);
        return matrix;
    }
}
