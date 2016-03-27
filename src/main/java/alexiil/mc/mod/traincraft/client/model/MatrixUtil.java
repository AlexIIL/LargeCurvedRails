package alexiil.mc.mod.traincraft.client.model;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import net.minecraft.util.Vec3;

import alexiil.mc.mod.traincraft.TrainCraft;

public class MatrixUtil {
    /** Rotates towards the given vector from (0, 0, 1) by firstly rotating around the X axis and the the Y axis. */
    public static Matrix4f rotateTo(Vec3 direction) {
        direction = direction.normalize();

        float xRot = (float) Math.asin(direction.yCoord);
        Matrix4f matrixRotX = new Matrix4f();
        matrixRotX.rotX(-xRot);

        // DO STUFF

        float yRot = (float) Math.atan2(direction.xCoord, direction.zCoord);
        Matrix4f matrixRotY = new Matrix4f();
        matrixRotY.rotY(yRot);

        Matrix4f total = new Matrix4f();
        total.setIdentity();
        total.mul(matrixRotY);
        total.mul(matrixRotX);

        return total;
    }

    public static Matrix4f translation(Vec3 pos) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.setTranslation(new Vector3f((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord));
        return matrix;
    }

    // FIXME: this is probably broken in some way
    public static Matrix3d rotate(Vector3d x, Vector3d y) {
        Matrix3d m = new Matrix3d();
        m.setRow(0, x);
        m.setRow(2, normalise(cross_product(x, y)));
        m.setRow(1, normalise(cross_product(row(m, 2), x)));
        return m;
    }

    // Ugly as f*** because javax vecmath don't return new objects.
    private static Vector3d row(Matrix3d m, int row) {
        Vector3d vec = new Vector3d();
        m.getRow(row, vec);
        return vec;
    }

    private static Vector3d normalise(Vector3d vec) {
        Vector3d v2 = new Vector3d(vec);
        v2.normalize();
        return v2;
    }

    private static Vector3d cross_product(Vector3d x, Vector3d y) {
        Vector3d vec = new Vector3d();
        vec.cross(x, y);
        return vec;
    }
}
