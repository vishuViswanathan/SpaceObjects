package GeneralElements.link;

import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import display.InputControl;
import display.NumberTextField;
import display.TextLabel;
import evaluations.EvalOnce;
import mvXML.ValAndPos;
import mvXML.XMLmv;
import mvmath.FramedPanel;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * Created by M Viswanathan on 20 May 2014
 */
public class ItemLink implements EvalOnce {
    public DarkMatter item1;
    public DarkMatter item2;
    Influence inf;
    boolean valid = false;
    InputControl control;
    final ItemSpace space;

    public ItemLink(DarkMatter item1, DarkMatter item2, Influence inf, ItemSpace space) {
        this(space);
        this.control = space.getInputControl();
        this.item1 = item1;
        this.item2 = item2;
        this.inf = inf;
        item1.addInfluence(this);
        item2.addInfluence(this);
        valid = true;
    }

    public ItemLink(DarkMatter item1, DarkMatter item2,  ItemSpace space) {
        this(item1, item2, new Gravity(item1, item2), space);
    }

    public ItemLink(DarkMatter item1, DarkMatter item2, Influence.Type type, ItemSpace space) {
        this(space);
        tlItem1.setText("" + item1);
        tlItem2.setText("" + item2);
        tlLinkType.setText("" + type);
        this.item1 = item1;
        this.item2 = item2;
        inf = Influence.createInfluence(item1, item2, type);
        enableRequired();
    }

    public ItemLink(ItemSpace space) {
        this.space = space;
        this.control = space.getInputControl();
        tlItem1 = new TextLabel("Item1");
        tlItem2 = new TextLabel("Item2");
        tlLinkType = new TextLabel("Type");
        ntFreeLength = new NumberTextField(control, freeLen, 8, false, 0, 1e20, "##0.00000E00", "Free Length in m", false);
        ntkCompression = new NumberTextField(control, kCompression, 8, false, 0, 1e20, "##0.00000E00", "k Compression in N/m", false);
        ntkExpansion = new NumberTextField(control, kExpansion, 8, false, 0, 1e20, "##0.00000E00", "k Expansion in N/m", false);
        bDelete = new JButton("X");
        bDelete.addActionListener(new DeleteButtListener(this, space));
    }

    public Influence getInfluence() {
        return inf;
    }

    public void setLocalForces() {
        inf.setLocalForces();
    }

    public void setGravityLinks(boolean bSet) {
        inf.setGravityLinks(bSet);
    }

    public void initStartForce() {
        inf.initStartForces();
    }

    public ItemLink(LinkedList<Item> allItems, ItemLink oldLink, ItemSpace space) {
        this(space);
//        this.slNo = slNo;
        if (oldLink.isValid(allItems)) {
//            cbLinkType.setSelectedItem(oldLink.inf.getType());
            tlLinkType.setText("" + oldLink.inf.getType());
            enableRequired();
            item1 = oldLink.item1;
            tlItem1.setText("" + item1);

            item2 = oldLink.item2;
            tlItem2.setText("" + oldLink.item2);

            ntFreeLength.setData(oldLink.inf.getFreeLength());
            ntkCompression.setData(oldLink.inf.getKCompression());
            ntkExpansion.setData(oldLink.inf.getKExpansion());
        }
    }

    public ItemLink(String xmlStr, ItemSpace space) throws NumberFormatException {
        this(space);
        takeFromXML(xmlStr);
    }

    @Override
    public void evalOnce() {
        evalForce();
    }

    // dummy not used
    public void evalOnce(double detaT, double nowT){}


    public boolean addLinksDisplay(Group grp, RenderingAttributes linkAttrib) {
        return inf.addLinksDisplay(grp, linkAttrib);
    }

    public void updateDisplay() {
        inf.updateDisplay();
    }

    public ItemLink getInfFromUI() {
        if (createInfluence())
            return new ItemLink(item1, item2, inf, space);
        else
            return null;
    }

    public boolean isGravity() {
        return (inf.getType() == Influence.Type.GRAVITY);
    }


    public boolean isValid(LinkedList<Item> allItems) {
        boolean retVal = (allItems.contains(item1) && allItems.contains(item2));
        retVal &= (inf != null);
        return retVal;
    }

    public boolean createInfluence() {
        Influence.Type type = Influence.Type.getEnum(tlLinkType.getText()); //.Type)(cbLinkType.getSelectedItem());
        item1 = space.getItem(tlItem1.getText());
        item2 = space.getItem(tlItem2.getText());
        valid = false;
        if (item1 != null && item2 != null) {
            freeLen = ntFreeLength.getData();
            kCompression = ntkCompression.getData();
            kExpansion = ntkExpansion.getData();
             switch (type) {
                case GRAVITY:
                    inf = new Gravity(item1, item2);
                    valid = true;
                case SPRING:
                    if (freeLen > 0 && kCompression > 0 && kExpansion > 0) {
                        inf = new Spring(item1, item2, freeLen, kCompression, kExpansion, true);
                        valid = true;
                    }
                    break;
//                case ROPE:
//                    if (freeLen > 0 && kExpansion > 0) {
//                        inf = new Rope(item1, item2, freeLen, kExpansion, true);
//                        valid = true;
//                    }
//                    break;
                 case ROD:
                     if (freeLen > 0 && kCompression > 0) {
                         inf = new Rod(item1, item2, freeLen, kCompression, true);
                         valid = true;
                     }
                     break;
            }
        }
        return valid;
    }


    public boolean evalForce() {
        return inf.evalForce();
    }

    public void updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception{
        inf.updatePosAndVel(deltaT, nowT, bFinal);
    }

    public void setStartConditions() {
        inf.setStartConditions();
    }

    static int cellHeight = 30;
    static Dimension[] allDim = {new Dimension(20, cellHeight),
            new Dimension(70, cellHeight),
            new Dimension(100, cellHeight),
            new Dimension(100, cellHeight),
//            new Dimension(120, cellHeight),
//            new Dimension(120, cellHeight),
//            new Dimension(120, cellHeight),
            new Dimension(50, cellHeight)};

    public static JPanel colHeader() {
        JPanel outerPan = new FramedPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        int col = 0;
        col = addBasicColHeads(outerPan, gbc, col);
        JPanel jp;
        gbc.gridx++;
        jp = new JPanel();
//        jp.setPreferredSize(allDim[col]);
        jp.add(new JLabel("Delete"));
        outerPan.add(jp, gbc);
        return outerPan;
    }

    static int addBasicColHeads(JPanel outerPan, GridBagConstraints gbc, int col) {
        JPanel jp;
        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(new JLabel("#"));
        outerPan.add(jp, gbc);
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(new JLabel("Link Type"));
        outerPan.add(jp, gbc);
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(new JLabel("Item 1"));
        outerPan.add(jp, gbc);
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(new JLabel("Item 2"));
        outerPan.add(jp, gbc);
        gbc.gridx++;
//
//        jp = new JPanel();
//        jp.setPreferredSize(allDim[col++]);
//        jp.add(new JLabel("Free Length (m)"));
//        outerPan.add(jp, gbc);
//        gbc.gridx++;
//
//        jp = new JPanel();
//        jp.setPreferredSize(allDim[col++]);
//        jp.add(new JLabel("K Compress (N/m)"));
//        outerPan.add(jp, gbc);
//        gbc.gridx++;
//
//        jp = new JPanel();
//        jp.setPreferredSize(allDim[col++]);
//        jp.add(new JLabel("K Expand (N/m)"));
//        outerPan.add(jp, gbc);
        return col;
    }

//    JComboBox cbLinkType = new JComboBox(Influence.Type.values());
    TextLabel tlItem1, tlItem2, tlLinkType;
    NumberTextField ntFreeLength, ntkCompression, ntkExpansion;
    JButton bDelete;
    double freeLen = 0, kCompression = 0, kExpansion = 0;

    public JPanel dataPanel(int objNum) {
        JPanel outerPan = new FramedPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        int col = 0;
        col = addDataFields(objNum, outerPan, gbc, col);
        JPanel jp;
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col]);
        jp.add(bDelete);
        outerPan.add(jp, gbc);
        return outerPan;
    }

     int addDataFields(int objNum, JPanel outerPan, GridBagConstraints gbc, int col) {
        JPanel jp;
        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(new JLabel("" + objNum));
        outerPan.add(jp, gbc);
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(tlLinkType); //  cbLinkType);
        outerPan.add(jp, gbc);
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(tlItem1);
        outerPan.add(jp, gbc);
        gbc.gridx++;

        jp = new JPanel();
        jp.setPreferredSize(allDim[col++]);
        jp.add(tlItem2);
        outerPan.add(jp, gbc);
        gbc.gridx++;

//        jp = new JPanel();
//        jp.setPreferredSize(allDim[col++]);
//        jp.add(ntFreeLength);
//        outerPan.add(jp, gbc);
//        gbc.gridx++;
//
//        jp = new JPanel();
//        jp.setPreferredSize(allDim[col++]);
//        jp.add(ntkCompression);
//        outerPan.add(jp, gbc);
//        gbc.gridx++;
//
//        jp = new JPanel();
//        jp.setPreferredSize(allDim[col++]);
//        jp.add(ntkExpansion);
//        outerPan.add(jp, gbc);
        return col;
    }

    void enableRequired() {
        ntFreeLength.setEnabled(false);
        ntkExpansion.setEnabled(false);
        ntkCompression.setEnabled(false);
        switch(Influence.Type.getEnum(tlLinkType.getText())) { //(Influence.Type)cbLinkType.getSelectedItem()) {
            case SPRING:
                ntFreeLength.setEnabled(true);
                ntkExpansion.setEnabled(true);
                ntkCompression.setEnabled(true);
                break;
            case GRAVITY:
                break;
            case ROD:
                ntFreeLength.setEnabled(true);
                ntkCompression.setEnabled(true);
                break;
            case ROPE:
                ntFreeLength.setEnabled(true);
                ntkExpansion.setEnabled(true);
                break;
        }
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("item1", item1.toString()));
        xmlStr.append(XMLmv.putTag("item2", item2.toString())).
                append(XMLmv.putTag("inf", infInXML().toString()));

        return xmlStr;
    }

    StringBuilder infInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("type", inf.getType().toString()));
        xmlStr.append(XMLmv.putTag("params", inf.dataInXML().toString()));
        return xmlStr;
    }

    public boolean takeFromXML(String xmlStr) throws NumberFormatException {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "item1", 0);
        item1 = space.getItem(vp.val);
        if (item1 != null) {
            vp = XMLmv.getTag(xmlStr, "item2", 0);
            item2 = space.getItem(vp.val);
            if (item2 != null) {
                vp = XMLmv.getTag(xmlStr, "type", 0);
                String typeStr = vp.val;
                Influence.Type type = Influence.Type.getEnum(typeStr);
                if (type != null) {
                    switch (type) {
                        case GRAVITY:
                            inf = new Gravity(item1, item2);
                            break;
                        case ROD:
                            inf = new Rod(item1, item2, 0, 0, true);
                            break;
//                        case ROPE:
//                            inf = new Rope(item1, item2, 0, 0, true);
//                            break;
                        case SPRING:
                            inf = new Spring(item1, item2, 0, 0, true);
                            break;
                    }
                    if (inf != null) {
                        try {
                            vp = XMLmv.getTag(xmlStr, "params", 0);
                            inf.set(vp.val);
                        } catch (NumberFormatException e) {
                            throw new NumberFormatException("Influence type " + typeStr + ": " + e.getMessage());
                        }
                        retVal = true;
                    }
                }
            }
        }
        return retVal;
    }

    public String toString() {
        return "" + inf + "between " + item1 + "and " + item2;
    }


    class DeleteButtListener implements ActionListener {
        ItemSpace space;
        ItemLink link;
        DeleteButtListener(ItemLink link, ItemSpace space) {
            this.space = space;
            this.link = link;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            space.deleteThisLink(link);
        }
    }
}
