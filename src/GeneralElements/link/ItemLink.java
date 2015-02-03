package GeneralElements.link;

import GeneralElements.DarkMatter;
import GeneralElements.Item;
import GeneralElements.ItemSpace;
import evaluations.EvalOnce;
import mvUtils.SmartFormatter;
import mvUtils.display.*;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

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
        valid = true;
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
        valid = false;
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
            valid = true;
        }
    }

    public ItemLink(String xmlStr, ItemSpace space) throws NumberFormatException {
        this(space);
        valid = takeFromXML(xmlStr);
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

    public boolean isGravity() {
        return (inf.getType() == Influence.Type.GRAVITY);
    }


    public boolean isValid(LinkedList<Item> allItems) {
        boolean retVal = (allItems.contains(item1) && allItems.contains(item2));
        retVal &= (inf != null);
        return retVal;
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
            new Dimension(50, cellHeight)};

    public static String[] getColHeader() {
        ColType[] values = ColType.values();
        String[] colHeader = new String[values.length];
        for (int i = 0; i < colHeader.length; i++)
            colHeader[i] = "" + values[i];
        return colHeader;
    }

    static public enum ColType {
        SLNO("SlNo."),
        LINKTYPE("Link Type"),
        ITEM1("Item 1"),
        ITEM2("Item 2");

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


    public static int[] getColumnWidths() {
        ColType[] values = ColType.values();
        int[] colWidths = new int[values.length];
        for (int i = 0; i < colWidths.length; i++)
            colWidths[i] = oneColWidth(values[i]);
        return colWidths;
    }

    static int oneColWidth(ColType colType) {
        switch(colType) {
            case SLNO:
                return 30;
            case LINKTYPE:
                return 200;
            case ITEM1:
                return 100;
            case ITEM2:
                return 100;
        }
        return 0;
    }

    public Object[] getRowData(int slNo) {
        ColType[] values = ColType.values();
        Object[] rowData = new Object[values.length];
        rowData[0] = "" + slNo;
        for (int i = 1; i < rowData.length; i++)
            rowData[i] = getOneColData(values[i]);
        return rowData;
    }

    Object getOneColData(ColType colType) {
        SmartFormatter fmt = new SmartFormatter(6);
        switch(colType) {
            case LINKTYPE:
                return "" + inf.type;
            case ITEM1:
                return "" + item1;
            case ITEM2:
                return "" + item2;
        }
        return "";
    }

    public Item.EditResponse editLink(InputControl inpC, Component caller) {
        Item.EditResponse retVal = Item.EditResponse.CANCEL;
        LinkBasic dlg = new LinkBasic(inpC);
        if (caller == null)
            dlg.setLocationRelativeTo(caller);
        else
            dlg.setLocation(600, 400);
        dlg.setVisible(true);
        Item.EditResponse response = dlg.getResponse();
        switch (response) {
            case OK:
                Influence inf = getInfluence();
                if (inf.hasDetails) {
                    LinkDetails detDlg = new LinkDetails(inf);
                    if (caller == null)
                        detDlg.setLocationRelativeTo(caller);
                    else
                        detDlg.setLocation(600, 400);
                    detDlg.setVisible(true);
                }
                retVal = Item.EditResponse.CHANGED;
                break;
            case DELETE:
                retVal = Item.EditResponse.DELETE;
                break;
            default:
                break;
        }
        return retVal;
    }

    public Item.EditResponse editLink(InputControl inpC) {
        return editLink(inpC, null);
    }

    class LinkBasic extends JDialog {
        Item[] allI;
        JComboBox<Item> cbItem1;
        JComboBox<Item> cbItem2;
        Influence.Type type;
        JButton delete = new JButton("DELETE");
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JComboBox<Influence.Type> cbType;
        Item.EditResponse response = Item.EditResponse.CANCEL;

        InputControl inpC;

        LinkBasic(InputControl inpC) {
            setModal(true);
            setTitle("New Link between Items");
            this.inpC = inpC;
            dbInit();
        }

        void dbInit() {
            allI = space.getAlItems().toArray(new Item[0]);
            cbItem1 = new JComboBox<Item>(allI);
            if (item1 != null)
                cbItem1.setSelectedItem(item1);
            cbItem2 = new JComboBox<Item>(allI);
            if (item2 != null)
                cbItem2.setSelectedItem(item2);
            cbType = new JComboBox<Influence.Type>(Influence.Type.getValidTypes());
            if (inf != null) {
                type = inf.getType();
                cbType.setSelectedItem(type);
            }
            JPanel outerPan = new JPanel(new BorderLayout());
            MultiPairColPanel jp = new MultiPairColPanel("Items to Link");
            jp.addItemPair("Item 1 ", cbItem1);
            jp.addItemPair("Item 2 ", cbItem2);
            jp.addItemPair("Link type ", cbType);
            jp.addBlank();
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeValuesFromUI()) {
                            response = Item.EditResponse.OK;
                             closeThisWindow();
                        }
                    } else if (src == delete) {
                        response = Item.EditResponse.DELETE;
                        closeThisWindow();
                    } else {
                        closeThisWindow();
                    }
                 }
            };
            delete.addActionListener(li);
            ok.addActionListener(li);
            cancel.addActionListener(li);
            JPanel buttPanel = new JPanel(new BorderLayout());
            buttPanel.add(delete, BorderLayout.WEST);
            buttPanel.add(cancel, BorderLayout.CENTER);
            buttPanel.add(ok, BorderLayout.EAST);
            outerPan.add(buttPanel, BorderLayout.SOUTH);
            outerPan.add(jp, BorderLayout.CENTER);
            add(outerPan);
            pack();
        }

        Item.EditResponse getResponse() {
            return response;
        }

        boolean takeValuesFromUI() {
            boolean retVal = true;
            Item nowItem1 = (Item) cbItem1.getSelectedItem();
            Item nowItem2 = (Item) cbItem2.getSelectedItem();
            Influence.Type nowType = (Influence.Type) (cbType.getSelectedItem());
            if (nowItem1 != item1 || nowItem2 != item2 || nowType != type) {
                item1 = (Item) cbItem1.getSelectedItem();
                item2 = (Item) cbItem2.getSelectedItem();
                type = (Influence.Type) (cbType.getSelectedItem());
                if (item1 != null && item2 != null && item1 != item2)
                    inf = Influence.createInfluence(item1, item2, type);
                else {
                    space.showMessage("Cannot link a Item to itself!");
                    retVal = false;
                }
            }
            return retVal;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

    class LinkDetails extends JDialog {
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        Influence inf;
        LinkDetails(Influence inf) {
            setModal(true);
            this.inf = inf;
            dbInit();
        }

        void dbInit() {
            JPanel outerP = new JPanel(new BorderLayout());
            MultiPairColPanel jpBasic = new MultiPairColPanel("Link Details");
            jpBasic.addItemPair("Item 1 ", "" + inf.item1, false);
            jpBasic.addItemPair("Item 2 ",  "" + inf.item2, false);
            jpBasic.addItemPair("Link type ", "" + inf.getType(), false);
            outerP.add(jpBasic, BorderLayout.NORTH);
            if (inf.hasDetails) {
                outerP.add(inf.detailsPanel(), BorderLayout.CENTER);
            }
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (inf.takeDataFromUI())
                            closeThisWindow();
                    } else
                        closeThisWindow();
                }
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
            MultiPairColPanel buttonP = new MultiPairColPanel("");
            buttonP.addItemPair(cancel, ok);
            outerP.add(buttonP, BorderLayout.SOUTH);
            add(outerP);
            pack();
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

    public boolean linksItem(DarkMatter item) {
        return (item == item1 || item == item2);
    }
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
        return col;
    }

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
                        case ROPE:
                            inf = new Rope(item1, item2);
                            break;
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
        return "" + inf + "between " + item1 + " and " + item2;
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
