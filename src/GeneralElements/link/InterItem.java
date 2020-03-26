package GeneralElements.link;

import GeneralElements.*;
import mvUtils.physics.Vector3dMV;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 08 Feb 2015
 * For inter item Gravity, contact force etc.
 */
public class InterItem extends Influence {
    boolean elasticityON = false;
    boolean isSticky = false;
    double totalMass;
    double limitDistance;
    double factorLbyR;
    double diffE; // e1 - e2
    double r1xE1LessE1;
    double r2E1PLusR1E2;
    double e1;
    double e2;
    boolean equalE = false;
    boolean oneIsASurface = false;
    Vector3d surfaceNormal;
    ItemSpace space;
    boolean collisionOn = false;
    public InterItem(DarkMatter itemOne, DarkMatter itemTwo, ItemSpace space) {
        type = Type.INTERITEM;
        this.space = space;
        collisionOn = space.bInterItemCollisionOn;
        hasDetails = false;
        item1 = itemOne;
        item2 = itemTwo;
        if (itemOne.boundaryItem) {
            oneIsASurface = true;
            if (itemTwo.boundaryItem)
                valid = false;
        } else if (itemTwo.boundaryItem) {
            if (oneIsASurface) // already
                valid = false;
            else {
                oneIsASurface = true;
                // make item1 as boundary item always
                item1 = itemTwo;
                item2 = itemOne;
            }
        }
        if (valid) {
            if (oneIsASurface) {
                surfaceNormal = new Vector3d(item1.getNormal());
                surfaceNormal.normalize();
                e2 = item2.getECompression();
                if (e2 > 0)
                    elasticityON = true;
                double r2 = item2.getDia() / 2;
                limitDistance = r2;
                factorLbyR = 1 / (r2 / e2);
            } else {
                double r1 = item1.getDia() / 2;
                double r2 = item2.getDia() / 2;
                limitDistance = r1 + r2;
                e1 = item1.getECompression();
                e2 = item2.getECompression();
                if (e1 > 0 && e2 > 0) {
                    equalE = (e1 == e2);
                    if (!equalE) {
                        diffE = e1 - e2;
                        r1xE1LessE1 = r1 * diffE;
                        r2E1PLusR1E2 = r2 * e1 + r1 * e2;
                    } else
                        factorLbyR = 1 / (r1 / e1 + r2 / e2);
                    elasticityON = true;
                } else {
                    if (e1 == -1 || e2 == -1) {
                        totalMass = item1.mass + item2.mass;
                        isSticky = true;
                    } else
                        factorLbyR = 0;
                }
            }
        }
    }

    public InterItem(DarkMatter itemOne, DarkMatter itemTwo) {
        this(itemOne, itemTwo, null);
    }
    /**
     * for inter-item elastic forces the following sre considered
     * 1) the object's elasticity is isotropic
     * 2) the simple spring-elastic force is multiplied by 1/(1-compression/radius) to make the force asymptotic to reach infinity at 0 distance
     * 3) compression is the distance of the distorted surface from the original.
     * 4) the case e1 == e2 is handled differently since the general formula causes DIV0 error
     * 5) Depending on time step, can cause error due to a large incremental position
     *
     * @return
     */
    @Override
    public boolean evalForce(double deltaT, boolean bFinal) {
        if (oneIsASurface)
            return getBoundaryForce();
        boolean retVal = true;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos); // vector item1 towards item2
        double distance = distVect.length();
        double compression = limitDistance - distance;
        Vector3d nowForce = new Vector3d();
        if (compression > 0) {
            if (elasticityON && collisionOn) {
                distVect.normalize();
                double v1Before = item1.status.velocity.projectionLength(distVect);
                double v2Before = item2.status.velocity.projectionLength(distVect);
                double mass1 = item1.mass;
                double mass2 = item2.mass;
                double v1After = (v1Before * (mass1 - mass2) + 2 * mass2 * v2Before) /
                        (mass1 + mass2);
                double v2After = v1After + v1Before - v2Before;
                item1.addToAddVelocity(new Vector3dMV(v1After - v1Before, distVect));
                item2.addToAddVelocity(new Vector3dMV(v2After - v2Before, distVect));


//
//                double force;
//                nowForce.set(distVect);
//                if (equalE) {
//                    double compFraction = 1 - compression / limitDistance;
//                    force = -compression * factorLbyR * (1 / compFraction); // negated since it is a repulsion
//                } else {
//                    double a = r1xE1LessE1;
//                    double b = r2E1PLusR1E2 - compression * diffE;
//                    double c = -e2 * compression;
//                    double m1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
//                    force = -m1 * e1 / (1 - m1); // negated since it is a repulsion
//                }
//                double ratio = force / distance;
//                if (Double.isNaN(ratio)) {
//                    retVal = false;
//                } else {
//                    nowForce.scale(ratio / 2);  // 20200317 take the force halfway
//                }
            }
        }
//        if (retVal) {
//            item1.addToForce(nowForce);
//            item2.subtractFromForce(nowForce);
//        }
        return retVal;
    }

    boolean getBoundaryForce() {
        boolean retVal;
        Vector3d distVect = item1.distanceVector(item2.status.pos);
        // distance vector is normal to surface
        double distance = distVect.length();
        double compression = limitDistance - distance;
        if (compression > 0) {
            Vector3d projection = new Vector3d(surfaceNormal);
            double lenOfProjection = (item2.getStatus().velocity).dot(projection);
            projection.scale(-2 * lenOfProjection);
            item2.addToAddVelocity(projection);


//            distVect.scale(1 / distance);
//            double compFraction = 1 - compression / limitDistance;  // note the compressions is negative;
//            double force = -compression * factorLbyR * (1 / compFraction); // negated since it is a repulsion
//            double alignedVelocity = distVect.dot(item2.status.velocity);
//            if (alignedVelocity > 0)
//                force *= item1.getCollisionLossFactor();
////            if (item1.canStick()) // item1 is the boundary item
////                force += item1.getStickingEffect(item2.getStickingArea(distance)); // item1 is the boundary item
//            // the nowForce is normal to the surface 'item1'
//            Vector3d nowForce = new Vector3d(distVect);
//            nowForce.negate();
////            nowForce.scale(force); // 20200323 no halfway NOT good speed explodes
//            nowForce.scale(force / 2); // 20200317 take the force halfway
//            item2.addToForce(nowForce);
        }
        retVal = true;
        return retVal;
    }

    Vector3d componetAlong(Vector3d vect, Vector3d along) {
        Vector3d projection = new Vector3d(along);
        projection.normalize();
        double lenOfProjection = vect.dot(projection);
        projection.scale(lenOfProjection);
        return projection;
    }


    void trace(String msg) {
        System.out.println("InterItem:" + msg);
    }
}
