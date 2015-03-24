package GeneralElements;

import Applications.ItemMovementsApp;
import Applications.SpaceEvaluator;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.ItemTable;
import GeneralElements.Display.LinkTable;
import GeneralElements.globalActions.AllGlobalActions;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.ItemLink;
import mvUtils.display.InputControl;
import mvUtils.math.DoubleMaxMin;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.media.j3d.Group;
import javax.media.j3d.RenderingAttributes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Vector;

//import Applications.SpaceEvaluator;

/**
 * Created by M Viswanathan on 23 May 2014
 */
public class ItemSpace {
    LinkedList<Item> allItems;
    LinkedList<ItemLink> allItemLinks;
    Vector<GlobalAction> activeGlobalActions;
    ItemMovementsApp mainApp;
    AllGlobalActions allGlobalActions;

    public ItemSpace(ItemMovementsApp mainApp) {
        this.mainApp = mainApp;
        clearSpace();
    }

    public void clearSpace() {
        allItems = new LinkedList<Item>();
        allItemLinks = new LinkedList<ItemLink>();
        allGlobalActions = new AllGlobalActions();
        trace("clearing ItemSpace");
    }

    public Vector<GlobalAction> getActiveGlobalActions() {
        return activeGlobalActions;
    }

    public LinkedList<Item> getAlItems() {
        return allItems;
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

    public void setGlobalLinksAndActions() {
        ItemLink link;
        for (int il = 0; il < allItemLinks.size();il++ ) {
            link = allItemLinks.get(il);
            if (link.isInterItem()) {
                allItemLinks.remove(il);
                il--;
            }
        }
        if (bItemGravityOn || anyElasticItem()) {
            Item item;
            int iLen = allItems.size();
            ItemLink oneLink;
            for (int i = 0; i < iLen; i++) {
                item = allItems.get(i);
                for (int n = i + 1; n < iLen; n++) {
                    oneLink = new ItemLink(item, allItems.get(n), bItemGravityOn, this);
                    if (oneLink.isValid())
                        addItemLink(oneLink);
                }
            }
        }
        activeGlobalActions = allGlobalActions.activeActions();

        for (ItemLink l: allItemLinks)
            l.setGravityLinks(bItemGravityOn);
    }

    boolean anyElasticItem() {
        boolean retVal = false;
        for (DarkMatter item: allItems)
            if (retVal = item.isElastic())
                break;
        return retVal;
    }

    public void noteItemData() {
        for (Item i: allItems)
            i.noteInput();
        initLinks();
    }

    void initLinks() {
        for (ItemLink l: allItemLinks)
            l.initStartForce();
    }

    JButton buttAddItem;
    JButton buttAddLink;
    ButtonListener bl;
    ItemTable itemTable;
    LinkTable linkTable;

    public JComponent dataEntryPanel() {
        JPanel outerP = new JPanel(new BorderLayout());
        outerP.add(itemListPanel(), BorderLayout.WEST);
        outerP.add(influenceListPanel(), BorderLayout.EAST);
        outerP.add(allGlobalActions.globalActionPanel(mainApp), BorderLayout.SOUTH);
        return outerP;
    }

    JComponent itemListPanel() {
        buttAddItem = new JButton("Add a new Item");
        if (bl == null)
            bl = new ButtonListener();
        buttAddItem.addActionListener(bl);
        JPanel outerP = new JPanel(new BorderLayout());
        JScrollPane sP = new JScrollPane();
        sP.setPreferredSize(new Dimension(800, 550));
        itemTable  = new ItemTable(mainApp, this, allItems);
        sP.setViewportView(itemTable.getTable());
        outerP.add(sP, BorderLayout.CENTER);
        JPanel buttPan = new JPanel(new GridLayout(1, 2));
        buttPan.add(buttAddItem);
        outerP.add(buttPan, BorderLayout.SOUTH);
        return outerP;
    }

    public void updateItemTable() {
        if (itemTable != null)
            itemTable.updateUI();
    }

    void addItem(Component c) {
//        Item newItem = new Item(this, "## Enter Item Name ##", 1, 1, Color.RED,  mainApp.parent());
        Item newItem = Item.getNewItem(this, "## Enter Item Name ##",mainApp.parent());
        if (newItem.editItem(mainApp) == Item.EditResponse.CHANGED) {
            newItem.setSpace(this);
            itemTable.addOneRow(newItem);
        }
    }

    LinkedList <ItemLink> tempInfList;
    JRadioButton rbItemGravity;
    boolean listEdited = false;

    /**
     * This is requires to filter out inter item gravity which is gloabal and not item specific
     */
    void fillTempInfList() {
        tempInfList = new LinkedList<ItemLink>();
        for (ItemLink il: allItemLinks)
            if (!il.isInterItem())
                tempInfList.add(il); //new ItemLink(allItems, il, this));
        listEdited = false;
    }

    JPanel infListPan;
    GridBagConstraints gbcInfList;
    boolean bItemGravityOn = false;

    public void enableItemGravity(boolean ena) {
        bItemGravityOn = ena;
        rbItemGravity.setSelected(ena);
    }


    public void enableButtons(boolean ena) {
//        buttSaveInf.setEnabled(ena);
//        buttAddInf.setEnabled(ena);
//        rbItemGravity.setEnabled(ena);
    }

    public void removeLinksOf(DarkMatter item) {
        linkTable.deleteLinksOfItem(item);
    }

    JComponent influenceListPanel() {
        fillTempInfList();
        buttAddLink = new JButton("Add New Link");
        rbItemGravity = new JRadioButton("Inter-Item Gravity Enabled");
        rbItemGravity.setSelected(bItemGravityOn);
        if (bl == null)
            bl = new ButtonListener();
        buttAddLink.addActionListener(bl);
        rbItemGravity.addActionListener(bl);
        JPanel outerP = new JPanel(new BorderLayout());
        JScrollPane sP = new JScrollPane();
        sP.setPreferredSize(new Dimension(400, 550));
        linkTable  = new LinkTable(mainApp, tempInfList);
        sP.setViewportView(linkTable.getTable());
        outerP.add(sP, BorderLayout.CENTER);
        JPanel buttPan = new JPanel(new GridLayout(1, 2));
        buttPan.add(rbItemGravity);
        buttPan.add(buttAddLink);
        outerP.add(buttPan, BorderLayout.SOUTH);
        return outerP;
    }


    public void linkEdited(ItemLink link) {
        listEdited = true;
    }

    void clearItemLinks() {
        allItemLinks.clear();
        for (Item it:allItems)
            it.clearInfluence();
    }

    public void saveInfluenceList() {
        clearItemLinks();
        ItemLink ilNow;
        for (ItemLink iL : tempInfList) {
            if ((ilNow = iL) != null)
                allItemLinks.add(ilNow);
        }
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
        noteItemData();
        if (allItems.size() > 1) {
            ItemLink link = new ItemLink(this);
            Item.EditResponse resp = link.editLink(getInputControl());
            if (resp == Item.EditResponse.CHANGED) {
                linkTable.addOneRow(link);
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
            Component src = (Component)e.getSource();
            if (src == buttAddLink)
                addInfluence();
            else if (src == buttAddItem) {
                addItem(src);
            }
            else if (src == rbItemGravity) {
                bItemGravityOn = rbItemGravity.isSelected();
            }
        }
    }

      public void addObjectAndOrbit(Vector<ItemGraphic> itemGraphics, Group grp, RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception {
        for (Item it: allItems) {
            if (!it.boundaryItem)
                itemGraphics.add(it.createItemGraphic(grp, orbitAttrib));
        }
        for (ItemLink inf: allItemLinks) {
            inf.addLinksDisplay(grp, linkAttrib);
//            inf.prepareEvaluator();
        }
    }

    public void addObjectAndOrbit(Vector<ItemGraphic> itemGraphics, Group grp,RenderingAttributes itemAttrib,
                                  RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception {
        for (Item it: allItems) {
            if (!it.boundaryItem) {
                ItemGraphic itemG = it.createItemGraphic(grp, orbitAttrib);
                itemG.setItemDisplayAttribute(itemAttrib);
                itemGraphics.add(itemG);
            }
        }
        for (ItemLink inf: allItemLinks) {
            inf.addLinksDisplay(grp, linkAttrib);
//            inf.prepareEvaluator();
        }
    }

    public void initForces() {
        for (Item i: allItems)
            i.setLocalForces();
        for (ItemLink link:allItemLinks)
            link.setLocalForces();
    }

    void setItemStartConditions() {
        for (Item i: allItems)
            i.setStartConditions();
        for (ItemLink link:allItemLinks)
            link.setStartConditions();
    }

    void updatePosAndVel(double deltaT, double nowT, boolean bFinal) throws Exception {
        for (Item i: allItems)
            i.updatePosAndVel(deltaT, nowT, bFinal);
        for (ItemLink link:allItemLinks)
            link.updatePosAndVel(deltaT, nowT, bFinal);
    }

    boolean evalInfluence(double deltaT, double nowT) throws Exception  {
        boolean ok = true;
        setItemStartConditions();
        for (int t = 0; t < 5; t++) {
            initForces();
            for (ItemLink inf : allItemLinks)
                if (!inf.evalForce()) {
                    showError("in evalInfluence: evalForce is false for Link " + inf);
                    ok = false;
                    break;
                }
            if (!ok)
                break;
            updatePosAndVel(deltaT, nowT, false); // not the final calculation
        }
        // now finalise it
        if (ok) {
            initForces();
            for (ItemLink inf : allItemLinks)
                if (!inf.evalForce()) {
                    showError("In evalInfluence: evalForce-final is false for Link " + inf);
                    ok = false;
                    break;
                }
            if (ok)
                updatePosAndVel(deltaT, nowT, true); // the final calculation
        }
        return ok;
    }

    boolean evalInfluence(SpaceEvaluator evaluator , double deltaT, double nowT) throws Exception  {
        initForces();

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
        return false;
    }

    public void updateLinkDisplay() {
        for (ItemLink lk: allItemLinks)
            lk.updateDisplay();
    }

    public DoubleMaxMin xMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
            maxMin.takeMaxValue(i.getPositionX());
        return maxMin;
    }

    public DoubleMaxMin yMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
            maxMin.takeMaxValue(i.getPositionY());
        return maxMin;
    }

    public DoubleMaxMin zMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
            maxMin.takeMaxValue(i.getPositionZ());
        return maxMin;
    }

    public int nItems() {
        return allItems.size();
    }

//    public void prepareLinkEvaluator() {
//        for (ItemLink lk: allItemLinks)
//            lk.prepareEvaluator();
//    }

    public boolean doCalculation(double deltaT, double nowT) throws Exception{
        return evalInfluence(deltaT, nowT);
    }

    public boolean  doCalculation( SpaceEvaluator evaluator, double deltaT, double nowT) throws Exception{
        return evalInfluence(evaluator, deltaT, nowT);
    }

    StringBuilder allGlobalActionsInXML() {
        return allGlobalActions.dataInXML();
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
        xmlStr.append(XMLmv.putTag("allGlobalActions", allGlobalActionsInXML().toString()));
        return xmlStr;
    }

    boolean takeItemsFromXML(String xmlStr) {
        boolean retVal = true;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "nItems", 0);
        try {
            int nItems = Integer.valueOf(vp.val);
            Item oneItem;
            for (int i = 0; i < nItems; i++) {
                vp = XMLmv.getTag(xmlStr, "it#" + ("" + i).trim(), vp.endPos);
                oneItem = Item.getItemFromXML(vp.val, mainApp.parent());
                addItem(oneItem);
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
        boolean retVal;
        clearSpace();
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "allItems", 0);
        retVal = takeItemsFromXML(vp.val);
        if (retVal) {
            vp = XMLmv.getTag(xmlStr, "allItemLinks", vp.endPos);
            retVal = takeLinksFromXML(vp.val);
            if (retVal) {
                vp = XMLmv.getTag(xmlStr, "allGlobalActions", vp.endPos);
                if (vp.val.length() > 10)
                    retVal = allGlobalActions.setAllValues(vp.val);
            }
        }
        return retVal;
    }

    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(mainApp.parent(), msg, "FOR INFORMATION", JOptionPane.INFORMATION_MESSAGE);
        mainApp.parent().toFront();
    }

    public void showError(String msg) {
        error("ItemSpace:" + msg);
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

