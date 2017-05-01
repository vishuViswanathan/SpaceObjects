package GeneralElements.link;

import GeneralElements.DarkMatter;
import GeneralElements.Constants;
import mvUtils.physics.Vector3dMV;

import javax.vecmath.Vector3d;

/**
 * Created by M Viswanathan on 08 Feb 2015
 * For inter item Gravity, contact force etc.
 */
public class InterItem extends Influence {
    boolean gravityON = false;
    boolean elasticityON = false;
    boolean isSticky = false;
    double totalMass;
    boolean theyAreStuck = false;
//    double lastVelocityI1, lastVelocityI2;  // approaching velocities
    double limitDistance;
    double factorLbyR;
    double diffE; // e1 - e2
    double r1xE1LessE1;
    double r2E1PLusR1E2;
    double e1;
    double e2;
    boolean equalE = false;
    boolean oneIsASurface = false;

    public InterItem (DarkMatter itemOne, DarkMatter itemTwo, boolean gravityON) {
        type = Type.INTERITEM;
        hasDetails = false;
        item1 = itemOne;
        item2 = itemTwo;
        if (itemOne.boundaryItem) {
            oneIsASurface = true;
            if (itemTwo.boundaryItem)
                valid = false;
        }
        else if (itemTwo.boundaryItem) {
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
                e2 = item2.getECompression();
                if (e2 > 0)
                    elasticityON = true;
                double r2 = item2.getDia() / 2;
                limitDistance = r2;
                factorLbyR = 1 / (r2 / e2);
            } else {
                this.gravityON = gravityON;
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
                } else  {
                    if (e1 == -1 || e2 == -1) {
                        totalMass = item1.mass + item2.mass;
                        isSticky = true;
                    }
                    else
                        factorLbyR = 0;
                }
            }
        }
    }

    /**
     * for inter-item elastic forces the following sre considered
     * 1) the object's elasticity is isotropic
     * 2) the simple spring-elastic force is multiplied by 1/(1-compression/radius) to make the force asymptotic to reach infinity at 0 distance
     * 3) compression is the distance of the distorted surface from the original.
     * 4) the case e1 == e2 is handled differently since the general formula causes DIV0 error
     * 5) Depending on time step, can cause error due to a large incremental position
     * @return
     */
    @Override
    public boolean evalForce(double deltaT, boolean bFinal) {
        if (oneIsASurface)
            return getBoundaryForce();
        boolean retVal = true;
        Vector3d nowForce = null;
        Vector3d distVect = new Vector3d();
        distVect.sub(item2.status.pos, item1.status.pos); // vector item1 towards item2
        double distance = distVect.length();
        double compression = limitDistance - distance;
        if (isSticky)
            nowForce = getStickingForce(distVect, distance, compression, deltaT, bFinal);
        else {
            distVect = new Vector3d();
            distVect.sub(item2.status.pos, item1.status.pos); // vector item1 towards item2
            distance = distVect.length();
            nowForce = new Vector3d();
            if (compression > 0) {
                if (elasticityON) {
                    double force;
                    nowForce.set(distVect);
                    if (equalE) {
                        double compFraction = 1 - compression / limitDistance;
                        force = -compression * factorLbyR * (1 / compFraction); // negated since it is a repulsion
                    } else {
                        double a = r1xE1LessE1;
                        double b = r2E1PLusR1E2 - compression * diffE;
                        double c = -e2 * compression;
                        double m1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
                        force = -m1 * e1 / (1 - m1); // negated since it is a repulsion
                    }
                    double ratio = force / distance;
                    if (Double.isNaN(ratio)) {
                        retVal = false;
                    } else {
                        nowForce.scale(ratio);
                    }
                }
            }
        }
        if (gravityON) {
            double gForceValue = (item1.mass / distance) * Constants.G * (item2.mass / distance); // attraction force
            double ratio = gForceValue / distance;
            if (Double.isNaN(ratio)) {
                retVal = false;
            } else {
                Vector3d gForce = new Vector3d(distVect);
                gForce.scale(ratio);
                nowForce.add(gForce);
             }
        }
        if (retVal) {
            item1.addToForce(nowForce);
            item2.subtractFromForce(nowForce);
        }
        return retVal;
    }

    /**
     *
     * @param distVect
     * @param distance // vector item1 towards item2
     * @param compression
     * @return
     */

    Vector3d getStickingForce(Vector3d distVect, double distance, double compression, double deltaT, boolean bFinal) {
        Vector3d nowForce = new Vector3dMV();
        if ((compression > 0 && distance > 0) || theyAreStuck) {
            Vector3d uDistanceVect = new Vector3dMV(1/distance, distVect);
            double nowApproachVelI1 = uDistanceVect.dot(item1.status.velocity);
            double nowApproachVelI2 = uDistanceVect.dot(item2.status.velocity);
            double netVelocity = (nowApproachVelI1 * item1.mass + nowApproachVelI2 * item2.mass) / totalMass;
            if (theyAreStuck) {
                double accI1 = (nowApproachVelI1 - netVelocity) / deltaT;
                nowForce = new Vector3dMV(- item1.mass * accI1, uDistanceVect);
                // this works out to be exact opposite when nowForce is evaluated with accI2 and item2.mass
            }
            else
                theyAreStuck = bFinal; // once stuck, always stuck
        }
        return nowForce;
    }

    boolean getBoundaryForce() {
        boolean retVal = true;
        Vector3d distVect = item1.distanceVector(item2.status.pos);
        double distance = distVect.length();
        double compression = limitDistance - distance;
        if (compression > 0) {
            distVect.scale(1 / distance);
            double compFraction = 1 - compression / limitDistance;  // note the compressions is negative;
            double force = -compression * factorLbyR * (1 / compFraction); // negated since it is a repulsion
            double alignedVelocity = distVect.dot(item2.status.velocity);
            if (alignedVelocity > 0)
                force *= item1.getCollisionLossFactor();
//            if (item1.canStick()) // item1 is the boundary item
//                force += item1.getStickingForce(item2.getStickingArea(distance)); // item1 is the boundary item
            Vector3d nowForce = new Vector3d(distVect);
            nowForce.negate();
            nowForce.scale(force);
            item2.addToForce(nowForce);
            retVal = true;
        }
        return retVal;
    }

    void trace(String msg) {
        System.out.println("InterItem:" + msg);
    }
}
