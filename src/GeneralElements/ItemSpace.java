package GeneralElements;

import Applications.ItemMovementsApp;
//import Applications.SpaceEvaluator;
import Applications.SpaceEvaluator;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.link.Influence;
import GeneralElements.link.ItemLink;
import display.InputControl;
import display.MultiPairColPanel;
import mvXML.ValAndPos;
import mvXML.XMLmv;
import mvmath.DoubleMaxMin;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class ItemSpace {
    LinkedList<Item> allItems;
    LinkedList<ItemLink> allItemLinks;
    ItemMovementsApp mainApp;

    public ItemSpace(ItemMovementsApp mainApp) {
        this.mainApp = mainApp;
        clearSpace();
    }

    void clearSpace() {
        allItems = new LinkedList<Item>();
        allItemLinks = new LinkedList<ItemLink>();
        trace("clearing ItemSpace");
    }

    public Item getOneItem(int i) {
        return allItems.get(i);
    }

    public ItemLink getOneLink(int i) {
        return allItemLinks.get(i);
    }

    public int nItemLinks() {
        return allItemLinks.size();
    }

    LinkedList<Item> getAllItems() {
        return allItems;
    }

    public int addItem(Item oneItem)  {
        allItems.add(oneItem);
        oneItem.setSpace(this);
        return allItems.size();
    }

    public InputControl getInputControl() {
        return mainApp;
    }

    public void addItemLink(ItemLink l) {
        allItemLinks.add(l);
    }

    public void setGravityLinks() {
        if (globalGravityOn) {
//            if (!gravityAdded) {
                Item item;
                int iLen = allItems.size();
                for (int i = 0; i < iLen; i++) {
                    item = allItems.get(i);
                    for (int n = i + 1; n < iLen; n++)
                        addItemLink(new ItemLink(item, allItems.get(n), this));  // by default is GRAVITY
               }
//                gravityAdded = true;
//            }
        }
        else {
            ItemLink link;
            for (int il = 0; il < allItemLinks.size();il++ ) {
                link = allItemLinks.get(il);
                if (link.isGravity()) {
                    allItemLinks.remove(il);
                    il--;
                }
            }
//            for (ItemLink link: allItemLinks) {
//                if (link.isGravity())
//                    allItemLinks.remove(link);
//            }
        }
    }

    public void noteInput() {
        for (Item i: allItems)
            i.noteInput();
    }

    JPanel itemListPan;
    GridBagConstraints gbcItemList;
    JButton buttAddItem;
    ButtonListener bl;

    public JComponent itemListPanel() {
        buttAddItem = new JButton("Add a new Item");
        if (bl == null)
            bl = new ButtonListener();
        buttAddItem.addActionListener(bl);
        JPanel outerP = new JPanel(new BorderLayout());
        JScrollPane sP = new JScrollPane();
        sP.setPreferredSize(new Dimension(450, 600));
        itemListPan = new JPanel(new GridBagLayout());
        gbcItemList = new GridBagConstraints();
        prepareItemList();
        sP.setViewportView(itemListPan);
        outerP.add(sP, BorderLayout.CENTER);
        JPanel buttPan = new JPanel(new GridLayout(1, 2));
        buttPan.add(buttAddItem);
        outerP.add(buttPan, BorderLayout.SOUTH);
        return outerP;
    }

    void prepareItemList() {
        if (itemListPan != null)
            itemListPan.removeAll();
        int itN = 1;
        gbcItemList.gridx = 0;
        gbcItemList.gridy = 0;
        for (Item it: allItems) {
            itemListPan.add(it.dataPanel(itN++), gbcItemList);
            gbcItemList.gridy++;
        }
    }

    void addItem() {
        Item newItem = new Item("Item New #" + (allItems.size() + 1), 1, 1, Color.RED,  mainApp.parent());
        newItem.setSpace(this);
        allItems.add(newItem);
        prepareItemList();
        itemListPan.updateUI();
    }

     LinkedList <ItemLink> tempInfList;
    JButton buttSaveInf;
    JButton buttAddInf;
    JRadioButton rbItemGravity;
    boolean listEdited = false;

    void fillTempInfList() {
        tempInfList = new LinkedList<ItemLink>();
//        int slNo = 0;
        for (ItemLink il: allItemLinks)
            if (!il.isGravity())
            tempInfList.add(new ItemLink(allItems, il, this));
        listEdited = false;
    }

    JPanel infListPan;
    GridBagConstraints gbcInfList;
    boolean globalGravityOn = false;
//    boolean gravityAdded = false;

    public void enableGlobalGravity(boolean ena) {
        globalGravityOn = ena;
//        setGravityLinks();
    }

    public void enableButtons(boolean ena) {
        buttSaveInf.setEnabled(ena);
        buttAddInf.setEnabled(ena);
        rbItemGravity.setEnabled(ena);
    }

    public JComponent influenceListPanel() {
        fillTempInfList();
        buttSaveInf = new JButton("Save Links");
        buttAddInf = new JButton("Add a new Link");
        rbItemGravity = new JRadioButton("Inter-Item Gravity Enabled");
        rbItemGravity.setSelected(globalGravityOn);
        if (bl == null)
            bl = new ButtonListener();
        buttSaveInf.addActionListener(bl);
        buttAddInf.addActionListener(bl);
        rbItemGravity.addActionListener(bl);
        JPanel outerP = new JPanel(new BorderLayout());
        JPanel colHeader = ItemLink.colHeader();
        outerP.add(colHeader, BorderLayout.NORTH);
        JScrollPane sP = new JScrollPane();
        sP.setPreferredSize(new Dimension(colHeader.getPreferredSize().width + 20, 600));
        infListPan = new JPanel(new GridBagLayout());
        gbcInfList = new GridBagConstraints();
        prepareLinkList();
        sP.setViewportView(infListPan);
        outerP.add(sP, BorderLayout.CENTER);
        JPanel buttPan = new JPanel(new GridLayout(1, 2));
        buttPan.add(rbItemGravity);
        buttPan.add(buttAddInf);
        buttPan.add(buttSaveInf);
        outerP.add(buttPan, BorderLayout.SOUTH);
        return outerP;
    }

    void prepareLinkList() {
        if (infListPan != null)
            infListPan.removeAll();
        int infN = 1;
        gbcInfList.gridx = 0;
        gbcInfList.gridy = 0;
        for (ItemLink il: tempInfList) {
            infListPan.add(il.dataPanel(infN++), gbcInfList);
            gbcInfList.gridy++;
        }
    }

    public  void resetLinkList() {
        if (infListPan != null) {
            fillTempInfList();
            prepareLinkList();
            infListPan.updateUI();
        }
    }

    public void deleteThisLink(ItemLink link) {
        trace("deleting " + link);
        if (tempInfList.remove(link)) {
            prepareLinkList();
            infListPan.updateUI();
            listEdited = true;
        }
        else
            showError("The link is not in the list!");
    }

    public void linkEdited(ItemLink link) {
        listEdited = true;
    }

    public boolean anyUnsavedLink() {
        return listEdited;
    }

    void clearItemLinks() {
        allItemLinks.clear();
        for (Item it:allItems)
            it.clearInfluence();
    }

    void saveInfluenceList() {
        clearItemLinks();
//        gravityAdded = false;
        ItemLink ilNow;
        for (ItemLink iL:tempInfList)
            if ((ilNow = iL.getInfFromUI()) != null)
                allItemLinks.add(ilNow);
//       setGravityLinks();
        listEdited = false;
    }

    public Item getItem(String name) {
        Item item = null;
        for (Item it: allItems)
            if (it.name.equals(name)) {
                item = it;
                break;
            }
        return item;
    }

    void addInfluence() {
        if (allItems.size() > 1) {
            LinkBasic linkDlg = new LinkBasic(tempInfList.size() + 1, this);
            linkDlg.setLocation(600, 400);
            linkDlg.setVisible(true);
            if (linkDlg.selOk) {
                ItemLink link = linkDlg.getLink();
                tempInfList.add(link);
                gbcInfList.gridy = tempInfList.size();
                infListPan.add(link.dataPanel(gbcInfList.gridy), gbcInfList);
                infListPan.updateUI();
                linkEdited(link);
            }
        }
        else {
            showError("Create items before creating links");
        }
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == buttAddInf)
                addInfluence();
            else if (src == buttSaveInf) {
                saveInfluenceList();
                showMessage("Saving Links");
            }
            else if (src == buttAddItem) {
                addItem();
            }
            else if (src == rbItemGravity) {
                globalGravityOn = rbItemGravity.isSelected();
            }
        }
    }

    class LinkBasic extends JDialog {
        Item[] allI = (Item[])allItems.toArray(new Item[0]);
        JComboBox<Item> cbItem1 = new JComboBox<Item>(allI);
        JComboBox<Item> cbItem2 = new JComboBox<Item>(allI);
        Item item1;
        Item item2;
        Influence.Type type;
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JComboBox<Influence.Type> cbType = new JComboBox<Influence.Type>(Influence.Type.values());
        ItemSpace space;
        boolean selOk = false;
        int slNo;
        LinkBasic(int slNo, ItemSpace space) {
            setModal(true);
            this.slNo = slNo;
            this.space = space;
            dbInit();
        }

        void dbInit() {
            MultiPairColPanel jp = new MultiPairColPanel("Items to Link");
            jp.addItemPair("Item 1 ", cbItem1);
            jp.addItemPair("Item 2 ", cbItem2);
            jp.addItemPair("Link type ", cbType);
            jp.addBlank();
            jp.addItemPair(cancel, ok);
            ActionListener li = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object src = e.getSource();
                    if (src == ok) {
                        if (takeValuesFromUI())
                            closeThisWindow();
                    } else  {
                        selOk = false;
                        closeThisWindow();
                    }
                }
            };
            ok.addActionListener(li);
            cancel.addActionListener(li);
            jp.addItemPair(cancel, ok);
            add(jp);
            pack();
        }

        boolean takeValuesFromUI() {
            item1 = (Item)cbItem1.getSelectedItem();
            item2 = (Item)cbItem2.getSelectedItem();
            type = (Influence.Type)(cbType.getSelectedItem());
            if (item1 != null && item2 != null && item1 != item2) {
                selOk = true;
            }
            else {
                showMessage("Cannot link a Item to itself!");
                selOk = false;
            }
            return selOk;
        }

        ItemLink getLink() {
            if (selOk)
//                return new ItemLink(allItems, new ItemLink(item1, item2, space), space);
                return new ItemLink(item1, item2, type, space);
            else
                return null;
        }

        void closeThisWindow() {
            setVisible(false);
            dispose();
        }
    }

//    public void addObjectAndOrbitOLD(Group grp, RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception{
//        for (Item it: allItems)
//            it.addObjectAndOrbit(grp, orbitAttrib);
//        for (ItemLink inf: allItemLinks)
//            inf.addLinksDisplay(grp, linkAttrib);
//    }

    public void addObjectAndOrbit(Vector<ItemGraphic> itemGraphics, Group grp, RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception {
        for (Item it: allItems)
            itemGraphics.add(it.createItemGraphic(grp, orbitAttrib));
        for (ItemLink inf: allItemLinks) {
            inf.addLinksDisplay(grp, linkAttrib);
//            inf.prepareEvaluator();
        }
    }


    public void initForces() {
        for (Item i: allItems)
            i.setLocalForces();
    }

    void setItemStartConditions() {
        for (Item i: allItems)
            i.setStartConditions();
    }

    void updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {
        for (Item i: allItems)
            i.updatePosAndVel(deltaT, nowT, bFinal);
    }

    void evalInfluence(double deltaT, double nowT) throws Exception  {
        setItemStartConditions();
        for (int t = 0; t < 5; t++) {
            initForces();
            for (ItemLink inf : allItemLinks)
                inf.evalForce();
            updatePosAndVel(deltaT, nowT, false); // not the final calculation
        }
        // now finalise it
        initForces();
        for (ItemLink inf : allItemLinks)
            inf.evalForce();
        updatePosAndVel(deltaT, nowT, true); // the final calculation
    }

    void evalInfluence(SpaceEvaluator evaluator , double deltaT, double nowT) throws Exception  {
        initForces();
////        for (ItemLink inf: allItemLinks)
////            inf.evalForce();
//        evaluator.submitLinkTasks();
//        if (evaluator.isComplete())
//            updatePosAndVel(deltaT, nowT);
////        evaluator.submitItemTasks(deltaT, nowT);
////        evaluator.isComplete();

        evaluator.resetForceBarrier();
//        evaluator.startItemLinkGroups();
//        System.out.println("ItemSpace before awaitStartBarrier");
        evaluator.awaitStartBarrier(); // this should start the force calculations
        evaluator.resetStartBarrier();
//        System.out.println("ItemSpace before awaitForceComplete");
        evaluator.awaitForceComplete();
//        System.out.println("ItemSpace After awaitForceComplete");
//        evaluator.resetStartBarrier();
        updatePosAndVel(deltaT, nowT, true);
    }

    public void updateLinkDisplay() {
        for (ItemLink lk: allItemLinks)
            lk.updateDisplay();
    }

    public DoubleMaxMin xMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
            maxMin.takeMaxValue(i.status.pos.getX());
        return maxMin;
    }

    public DoubleMaxMin yMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
            maxMin.takeMaxValue(i.status.pos.getY());
        return maxMin;
    }

    public DoubleMaxMin zMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
            maxMin.takeMaxValue(i.status.pos.getZ());
        return maxMin;
    }

    public int nItems() {
        return allItems.size();
    }

//    public void prepareLinkEvaluator() {
//        for (ItemLink lk: allItemLinks)
//            lk.prepareEvaluator();
//    }
//
    public void doCalculation(double deltaT, double nowT) throws Exception{
        evalInfluence(deltaT, nowT);
    }

    public void doCalculation( SpaceEvaluator evaluator, double deltaT, double nowT) throws Exception{
        evalInfluence(evaluator, deltaT, nowT);
    }

    StringBuilder allItemsInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("nItems", nItems()));
        int i = 0;
        for (Item it: allItems)
            xmlStr.append(XMLmv.putTag("it#" + ("" + i++).trim(), it.dataInXML().toString()));
        return xmlStr;
    }

    StringBuilder allItemLinksInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("nLinks", allItemLinks.size()));
        int l = 0;
        for (ItemLink lk: allItemLinks)
            xmlStr.append(XMLmv.putTag("lk#" + ("" + l++).trim(), lk.dataInXML().toString()));
        return xmlStr;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("allItems", allItemsInXML().toString()));
        xmlStr.append(XMLmv.putTag("allItemLinks", allItemLinksInXML().toString()));
        return xmlStr;
    }

    boolean takeItemsFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "nItems", 0);
        try {
            int nItems = Integer.valueOf(vp.val);
            for (int i = 0; i < nItems; i++) {
                vp = XMLmv.getTag(xmlStr, "it#" + ("" + i).trim(), vp.endPos);
                allItems.add(new Item(vp.val, mainApp.parent()));
            }
        } catch (NumberFormatException e) {
            showError("Problem in XML data for Items:" + e.getMessage());
            retVal = false;
        }
        return retVal;
    }

    boolean takeLinksFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        try {
            vp = XMLmv.getTag(xmlStr, "nLinks", 0);
            int nLinks = Integer.valueOf(vp.val);
            for (int i = 0; i < nLinks; i++) {
                vp = XMLmv.getTag(xmlStr, "lk#" + ("" + i).trim(), vp.endPos);
                allItemLinks.add(new ItemLink(vp.val, this));
            }
        } catch (NumberFormatException e) {
            showError("Problem in XML data for Items:" + e.getMessage());
            retVal = false;
        }
        return retVal;
    }

    public boolean takeFromXML(String xmlStr) {
        boolean retVal = true;
        clearSpace();
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "allItems", 0);
        retVal = takeItemsFromXML(vp.val);
        if (retVal) {
            vp = XMLmv.getTag(xmlStr, "allItemLinks", vp.endPos);
            retVal = takeLinksFromXML(vp.val);
        }
        return retVal;
    }

    void showMessage(String msg) {
        JOptionPane.showMessageDialog(mainApp.parent(), msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
        mainApp.parent().toFront();
    }

    void showError(String msg) {
        error(msg);
        JOptionPane.showMessageDialog(mainApp.parent(), msg, "ERROR", JOptionPane.ERROR_MESSAGE);
        mainApp.parent().toFront();
    }

    void trace(String msg) {
        ItemMovementsApp.log.trace(msg);
    }

    void debug(String msg) {
        ItemMovementsApp.log.debug(msg);
    }

    void error(String msg) {
        ItemMovementsApp.log.error(msg);
    }


}

