package GeneralElements.link;

import Applications.ItemMovementsApp;
import GeneralElements.DarkMatter;
import GeneralElements.ItemSpace;
import GeneralElements.localActions.LocalAction;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Vector;

/**
 * Created by M Viswanathan on 27 Sep 2014
 */
public class LinkWithMass extends InfluenceDef  {
    int nElements;
    double massPerM;
    double surfAreaPerM;
    double projectedAreaPerM;
    Vector<DarkMatter> massElements;
    Vector<ItemLink> allLinks;
    Window parent;
    ItemSpace space;
    Vector<LocalAction> localActions;
    boolean elementsSet = false;

    static int defElements = 50;
    static double defE = 1000;
    static double defMassPerM = 0.1;

    public LinkWithMass(DarkMatter item1, DarkMatter item2, double initialLenFactor, int nElements,
                        double massPerM, double eExpansion) {
        super(item1, item2, initialLenFactor, eExpansion, eExpansion);
        noteBasicData(massPerM, nElements);
        elementsSet = false;
    }

    public LinkWithMass(DarkMatter item1, DarkMatter item2, double initialLenFactor, double eExpansion) {
        super(item1, item2, initialLenFactor, eExpansion, eExpansion);
        noteBasicData(defMassPerM, defElements);
        elementsSet = false;
    }

    void noteBasicData(double massPerM, int nElements) {
        type = Type.ROPE;
        this.massPerM = massPerM;
        allLinks = new Vector<ItemLink>(nElements);
        this.nElements = (nElements / 2) * 2; // make it even
        this.parent = item1.parentW;
        this.space = item1.space;
        localActions = new Vector<LocalAction>();
    }

    public void addLocalAction(LocalAction action) {
        localActions.add(action);
    }

    public void setStartConditions(double duration, double nowT) {
        for (DarkMatter mat:massElements)
            mat.setMatterStartConditions(duration, nowT);
    }

    public void initStartForces() {
        if (!elementsSet)
            setAllElements();
        for (DarkMatter mat:massElements)
            mat.initNetForce();
    }

    public void setLocalForces() {
        for (DarkMatter mat:massElements)
            mat.setMatterLocalForces();
    }

    public void setGravityLinks(boolean bSet) {
        if (bSet)
            showMessage("Not Ready for inter-gravity on links");
    }

    public void updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {  // deltaT is time is seconds
        for (DarkMatter mat:massElements)
            mat.updatePAndV(deltaT, nowT, bFinal);
    }

    public boolean setAllElements() {
        int pairs = nElements / 2;
        allLinks = new Vector<ItemLink>(); // (nElements);
        nElements = (nElements / 2) * 2; // make it even
        massElements = new Vector<DarkMatter>(); //(nElements - 1); // the massElements are 1 less
        double uMass = massPerM * freeLen / (nElements - 1);
        Vector3d distVect = new Vector3d();
        Vector3d item1Pos = new Vector3d(item1.status.pos);
        distVect.sub(item2.status.pos, item1Pos);
        double distance = distVect.length();
        double stPitch = distance / nElements;
        double stPitch2 = Math.pow(stPitch, 2);
        double oneLinkLen = freeLen / nElements;
        double oneLinkLen2 = Math.pow(oneLinkLen, 2);
        double uSurfaceArea = surfAreaPerM * oneLinkLen;
        double uProjectedArea = projectedAreaPerM * oneLinkLen;
        boolean retVal = true;
        double initialLenF = 1;
        if (distance > 0) {
            // create mass elements and position them uniformly between item1 and item2, but at an oneLinkLen
            // in zigzag fashion if required
            // mark along x axis in xy plane
            Vector<Point3d> points = new Vector<Point3d>();
            double x = 0;
            double y = 0;
            if (oneLinkLen > stPitch)
                y = Math.sqrt(oneLinkLen2 - stPitch2);
            else
                initialLenF = oneLinkLen / stPitch;
            double z = 0;
            boolean invert = false;
            for (int i = 0; i < pairs - 1; i++) {
                // the first point is offset from x axis
                x += stPitch;
                points.add(new Point3d(x, y * ((invert) ? -1: 1), z));
                // the next point is on the x axis
                x += stPitch;
                points.add(new Point3d(x, 0, z));
                invert = !invert;
            }
            // for the last pair, take only the offset point since the next point will be item2
            x += stPitch;
            points.add(new Point3d(x, y, z));
            // build a transform matrix
            Vector3d vectToTransform = new Vector3d(distVect);
            // shift base to 0, 0, 0
            Transform3D trFwd = new Transform3D();
            Vector3d trPosVect = new Vector3d(-item1Pos.x, -item1Pos.y, -item1Pos.z);
            trFwd.setTranslation(trPosVect);
            trFwd.transform(vectToTransform);
            // find angle of the xz-plane projection wrt x axis
            double yRot = rotAngle(vectToTransform.getZ(), vectToTransform.getX());
            // rotate it on y axis
            Transform3D yRotFwd = new Transform3D();
            yRotFwd.rotY(yRot);
            yRotFwd.transform(vectToTransform);
            // find angle of xy-plane projection wrt x axis (for rotation about z axis
            double zRot = rotAngle(vectToTransform.getY(), vectToTransform.getX());
            // rotation around z axis
            Transform3D zRotFwd = new Transform3D();
            zRotFwd.rotZ(zRot);
            zRotFwd.transform(vectToTransform);
            // with this rotation the vectToTransform would have been aligned with x axis with start point at origin
            // prepare the reverse transform
            // rotation about z axis
            // prepare reverse transforms
            Transform3D zRevRot = new Transform3D();
            zRevRot.rotZ(zRot); // in reverse direction
            // rotation around y axis
            Transform3D yRotRev = new Transform3D();
            yRotRev.rotY(-yRot); // in reverse direction
//            trRev.mul(yRotRev);
            // translate to original location of distVect.
            Transform3D trRev = new Transform3D();
            trRev.setTranslation(item1Pos);
            DarkMatter oneElem;
            int i = 0;
            for (Point3d point : points) {
                zRevRot.transform(point);
                yRotRev.transform(point);
                trRev.transform(point);
                // oneElem dia is set at 0.1, but of no consequence since projected and surface areas are set subsequently
                oneElem = new DarkMatter("pa" + i++, uMass, 0.1, Color.BLACK, parent);
                oneElem.setSpace(space);
                oneElem.initPosEtc(point, new Vector3d(0, 0, 0));
                oneElem.setProjectedArea(uProjectedArea);
                oneElem.setSurfaceArea(uSurfaceArea);
                for (LocalAction action : localActions)
                    oneElem.addLocalAction(action);
                massElements.add(oneElem);
            }
            // create rods between the points
            DarkMatter lastItem = item1;
            for (DarkMatter oneItem : massElements) {
                allLinks.add(new ItemLink(lastItem, oneItem, new Rod(lastItem, oneItem,
                        initialLenF, eExpansion), space));
                lastItem = oneItem;
            }
            allLinks.add(new ItemLink(lastItem, item2, new Rod(lastItem, item2,
                    initialLenF, eExpansion), space));
            assignLocalActions();
        }
        else {
//            showMessage("Free Length " + freeLen + " is less than distance " + distance + "\nLink is NOT created!");
            showMessage("Distance is less than or equal to 0 " + distance + "\nLink is NOT created!");
            retVal = false;
        }
        elementsSet = retVal;
        return retVal;
    }

    void assignLocalActions() {
        for (LocalAction la: localActions)
            for (DarkMatter m: massElements)
                m.addLocalAction((LocalAction)la.clone());
    }

    // numerator and denominator for tan of the angle (for anticlockwise rotation
    double rotAngle(double numerator, double denominator) {
        double retVal = (denominator == 0) ? ((numerator < 0) ? -Math.PI/2 : Math.PI/2) : Math.atan(numerator / denominator);
        if (denominator < 0)
            retVal += Math.PI;
        return retVal;
    }

    public boolean addLinksDisplay(Group grp, RenderingAttributes linkAttrib) {
        boolean retVal = super.addLinksDisplay(grp, linkAttrib);
        for (ItemLink l: allLinks)
            l.addLinksDisplay(grp, linkAttrib);
        return retVal;
    }

    public void updateDisplay() {
        super.updateDisplay();
        for (ItemLink l: allLinks)
            l.updateDisplay();

    }

//    LocalActionsTable localActionTable;

    @Override
    public JPanel detailsPanel() {
        JPanel outerP = new JPanel(new BorderLayout());
        outerP.add(lwmDetailsPanel(), BorderLayout.WEST);
//        DarkMatter dummyItem = new Item(item1.parentW);
//        for (LocalAction la:localActions)
//            dummyItem.addLocalAction(la);
//        localActionTable = new LocalActionsTable(dummyItem, item1);
//        outerP.add(localActionTable.getLocalActionPanel(), BorderLayout.EAST);
        return outerP;
    }

    @Override
    public boolean takeDataFromUI() {
        boolean retVal = false;
        if (lwmTakeDataFromUI()) {
////            if (localActionTable.getEditResponse() == Item.EditResponse.CHANGED) {
//                localActions.clear();
//                DarkMatter dummyItem = localActionTable.item;
//                for (LocalAction la : dummyItem.getLocalActions())
//                    localActions.add(la);
////            }
            setAllElements();
            retVal = true;
        }
        return retVal;
    }

    public JPanel lwmDetailsPanel() {
        return null;
    }

    public boolean lwmTakeDataFromUI() {
        return false;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = super.dataInXML();
        xmlStr.append(XMLmv.putTag("massPerM", massPerM)).append(XMLmv.putTag("nElements", nElements));
        xmlStr.append(XMLmv.putTag("nLocalActions", localActions.size()));
        int a = 0;
        for (LocalAction action: localActions) {
            xmlStr.append(XMLmv.putTag("a#" + ("" + a).trim(), action.dataInXML().toString()));
            a++;
        }
        return xmlStr;
    }

    public boolean set(String xmlStr) throws NumberFormatException {
        boolean retVal = true;
        super.set(xmlStr);
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "massPerM", 0);
        massPerM = Double.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "nElements", 0);
        nElements = Integer.valueOf(vp.val);
        vp = XMLmv.getTag(xmlStr, "nLocalActions", vp.endPos);
        if (vp.val.length() > 0) {
            int nActions = Integer.valueOf(vp.val);
            DarkMatter dummyItem = item1;
            try {
                for (int a = 0; a < nActions; a++) {
                    vp = XMLmv.getTag(xmlStr, "a#" + ("" + a).trim(), vp.endPos);
                    addLocalAction(LocalAction.getLocalAction(dummyItem, vp.val));
                }
            } catch (Exception e) {
                retVal = false;
                e.printStackTrace();
            }
        }
        elementsSet = false;
        return retVal;
    }

    @Override
    public boolean evalForce(double deltaT, boolean bFinal) {
//        if (!elementsSet)
//            setAllElements();
        boolean retVal = true;
        for (ItemLink l: allLinks)
            if (!l.evalForce(deltaT, bFinal)) {
                retVal = false;
                break;
            }
        return retVal;
    }

    void showMessage(String msg) {
        ItemMovementsApp.showMessage("LinkWithMass:" + msg);
    }

}
