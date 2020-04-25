package mvTesting;

import mvUtils.physics.Vector3dMV;

import javax.vecmath.Vector3d;

/**
 * Created by mviswanathan on 17-03-2020.
 */
public class Test extends Vector3d{
    public Test() {

    }

    public Test(double x, double y, double z) {
       super(x, y, z);
    }

    Vector3d componetAlong(Vector3d along) {
        Vector3d projection = new Vector3d(along);
        projection.normalize();
        double lenOfProjection = this.dot(projection);
        projection.scale(lenOfProjection);
        return projection;
    }


    public static void main(String[] args) {
        Vector3dMV vA= new Vector3dMV(10, 0, 0);
        Vector3d vAB= new Vector3d(-8, 8, 0);
        double angle = vA.angle(vAB);
        System.out.println(angle * 180 / Math.PI );
        System.out.println(" "+ vA.projectionLength(vAB));

//        Test vect = new Test(-10, -10, 10);
//        System.out.println("vect " + vect + ", Length " +  vect.length());
//        Vector3d allignTo = new Vector3d(-10, -10, 0);
//        System.out.println("allignTo " + allignTo);
//        System.out.println("angle " + vect.angle(allignTo) * 180 / Math.PI);
//        Vector3d projection = vect.componetAlong(allignTo);
//        System.out.println("projection " + projection+ ", Length " +  projection.length());
     }
}
