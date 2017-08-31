package GeneralElements;

import GeneralElements.Display.ItemGraphic;
import com.sun.j3d.utils.universe.ViewingPlatform;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;
import time.timePlan.JetTimeController;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by mviswanathan on 02-08-2017.
 */
public interface ItemInterface {
    enum ItemType {
        SPHERE("Sphere"), // default spherical object
        SURFACE("Surface"),
        VMRL("from VMRL file");

        private final String typeName;

        ItemType(String actionName) {
            this.typeName = actionName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ItemType getEnum(String text) {
            ItemType retVal = null;
            if (text != null) {
                for (ItemType b : ItemType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    enum EditResponse {CHANGED, NOTCHANGED, DELETE, CANCEL, OK}

    enum ColType {
        SLNO("SlNo."),
        NAME("Name"),
        DETAILS("Details");

        private final String typeName;

        ColType(String typeName) {
            this.typeName = typeName;
        }

        public String getValue() {
            return typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }

        public static ColType getEnum(String text) {
            ColType retVal = null;
            if (text != null) {
                for (ColType b : ColType.values()) {
                    if (text.equalsIgnoreCase(b.typeName)) {
                        retVal = b;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    ItemType getItemType();

    String getVrmlFile();

    AxisAngle4d getSpinAxis();

    boolean isLightSrc();

    Vector3d getMiAsVector();

    Vector3d getOneByMI();

    void setJetController(JetTimeController jetController);

    void setMomentsOfInertia(double mIxx, double mIyy, double mIzz) throws Exception;

    boolean takeBasicFrom(DarkMatter fromItem);

    double getGM();

    void noteTotalGM(double totalGM);

    static Item getNewItem(ItemSpace theSpace, String theName, Window theParent) {
        Item theItem = null;
        Item.ItemBasic dlg = new Item.ItemBasic(theSpace, theName);
        dlg.setLocationRelativeTo(theParent);
        dlg.setVisible(true);
        if (dlg.getResponse() == Item.EditResponse.OK) {
            Item.ItemType selectedType = dlg.getSelectedType();
            switch (selectedType) {
                case SURFACE:
                    theItem = new Surface(theParent, theName);
                    break;
                case VMRL:
                    theItem = new Item(theName, 10000, "VRML\\rocket.wrl", theParent);
                    break;
                case SPHERE:
                    theItem = new Item(theParent, theName);
                    break;
            }
        }
        return theItem;
    }

    static class ItemBasic extends JDialog {
        JComboBox jcItem = new JComboBox(Item.ItemType.values());
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ItemSpace theSpace;
        String theName;
        Item.EditResponse response = Item.EditResponse.CANCEL;

        ItemBasic(ItemSpace theSpace, String theName) {
            setModal(true);
            this.theSpace = theSpace;
            this.theName = theName;
            jcItem.setSelectedItem(Item.ItemType.SPHERE);
            setTitle("Selection Object Type");
            MultiPairColPanel jp = new MultiPairColPanel("Selection Object Type");
            jp.addItemPair("Selected Type", jcItem);
            jp.addBlank();
            jp.addItemPair(cancel, ok);
            add(jp);
            pack();
            ActionListener li = e -> {
                Object src = e.getSource();
                if (src == ok)
                    response = Item.EditResponse.OK;
                closeThisWindow();
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }

        Item.ItemType getSelectedType() {
            return (Item.ItemType) jcItem.getItemAt(jcItem.getSelectedIndex());
        }

        Item.EditResponse getResponse() {
            return response;
        }
    }

    static Item getItemFromXML(String xmlStr, Window parent) {
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "itemType", 0);
        boolean done = false;
        Item theItem = null;
        if (vp.val.length() > 2) {
            Item.ItemType nowType = Item.ItemType.getEnum(vp.val);
            if (nowType != null) {
                switch (nowType) {
                    case SURFACE:
                        theItem = new Surface(xmlStr, parent);
                        done = true;
                        break;
                    case VMRL:
                    case SPHERE:
                        theItem = new Item(xmlStr, parent);
                        done = true;
                        break;
                }
            }
        }
        if (!done)
            theItem = new Item(xmlStr, parent);
        return theItem;
    }

    static String[] getColHeader() {
        Item.ColType[] values = Item.ColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    static int[] getColumnWidths() {
        Item.ColType[] values = Item.ColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(Item.ColType colType) {
        switch (colType) {
            case SLNO:
                return 30;
            case NAME:
                return 100;
            case DETAILS:
                return 630;
        }
        return 0;
    }

    ItemSpace getSpace();

    String getName();

    ItemStat getStatus();

    Vector3d getVelocity();

    Point3d getPos();

    String getImageName();

    Object[] getRowData(int slNo);

    void setbFixedLocation(boolean set);

    void setRefreshInterval(double interval, double nextRefresh);

    EditResponse editItem(String title, InputControl inpC, Component c);

    void showItem(String title, InputControl inpC, Component c);

    void editItemKeepingPosition(String title, InputControl inpC, Component c);

    EditResponse editItem(InputControl inpC, Component c);

    boolean hasAnyAccessories();

    Window showControlPanel(InputControl inpC, Component parent);

    void setLocalForces();

    void initStartForce();

    void addTojetForce(Vector3d addForce);

    void addToJetTorque(Vector3d addTorque);

    void addToTorque(Vector3d angularAcceleration);

    void subtractFromTorque(Vector3d angularAcceleration);

    void setStartConditions(double duration, double nowT);

    void addToAngularVel(Vector3d angularVel);

    void subtractFromVel(Vector3d angularVel);

    void setImage(String imageName);

    void noteInput();

    void initPosEtc(Point3d pos, Vector3d velocity);

    void setInitialAcceleration(Vector3d acc);

    void initAngularPosEtc(Vector3d angularPos, Vector3d angularVelocity);

    void setSpin(AxisAngle4d spinAxis, double spinPeriod);

    ItemGraphic createItemGraphic(Group grp, RenderingAttributes orbitAtrib) throws Exception;

    void setItemDisplayAttribute(RenderingAttributes itemAttribute);

    void attachPlatform(ViewingPlatform platform);

    void detachPlatform();

    void setScale(double scale);

    void updateOrbitAndPos() throws Exception;

    void enableLightSrc(boolean ena);

    //    =========================== calculations ======================

    boolean updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception;

    Transform3D globalToItem();

    Transform3D itemToGlobal();

    StringBuilder statusStringForCSV(double posFactor, double velFactor);

    StringBuilder dataInXML();

    boolean takeFromXML(String xmlStr) throws NumberFormatException;

}

