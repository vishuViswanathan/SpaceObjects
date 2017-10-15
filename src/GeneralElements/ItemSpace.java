package GeneralElements;

import Applications.ItemMovementsApp;
import Applications.SpaceEvaluator;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.ItemTable;
import GeneralElements.Display.LinkTable;
import GeneralElements.globalActions.AllGlobalActions;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.Gravity;
import GeneralElements.link.ItemLink;
import GeneralElements.utils.ThreeDSize;
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
    LinkedList<ItemInterface> allItems;
    LinkedList<ItemLink> allItemLinks;
    Vector<GlobalAction> activeGlobalActions;
    ItemMovementsApp mainApp;
    AllGlobalActions allGlobalActions;
    double pastHistoryTime = 36000; // time in s to keep position history
    public boolean bConsiderTimeDilation = false; // gravity effect on local clock
    public boolean bConsiderGravityVelocity = false; // propagation time for Gravity

    public ItemSpace(ItemMovementsApp mainApp) {
        this.mainApp = mainApp;
        clearSpace();
    }

    public void clearSpace() {
        allItems = new LinkedList<ItemInterface>();
        allItemLinks = new LinkedList<ItemLink>();
        allGlobalActions = new AllGlobalActions(this);
        bConsiderTimeDilation = false;
        bConsiderGravityVelocity = false;
        trace("clearing ItemSpace");
    }

    public Vector<GlobalAction> getActiveGlobalActions() {
        return activeGlobalActions;
    }

    public LinkedList<ItemInterface> getAlItems() {
        return allItems;
    }

    public ItemInterface getOneItem(int i) {
        return allItems.get(i);
    }

    public ItemLink getOneLink(int i) {
        return allItemLinks.get(i);
    }

    public int nItemLinks() {
        return allItemLinks.size();
    }

    LinkedList<ItemInterface> getAllItems() {
        return allItems;
    }

    public ItemInterface[] getOtherItems(ItemInterface excludeThis) {
        ItemInterface[] others = new ItemInterface[nItems() - 1];
        int c = 0;
        for (ItemInterface i: allItems)
            if (i != excludeThis)
                others[c++] = i;
        return others;
    }

    public int addItem(ItemInterface oneItem)  {
        allItems.add(oneItem);
        ((DarkMatter)oneItem).setSpace(this);
        return allItems.size();
    }

    public ThreeDSize getSize() {
        return new ThreeDSize(xMaxMin(), yMaxMin(), zMaxMin());
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
        if (anyElasticItem()) {
            ItemInterface item;
            int iLen = allItems.size();
            ItemLink oneLink;
            for (int i = 0; i < iLen; i++) {
                item = allItems.get(i);
                for (int n = i + 1; n < iLen; n++) {
                    oneLink = new ItemLink((DarkMatter)item, (DarkMatter)allItems.get(n), bItemGravityOn, this);
                    if (oneLink.isValid())
                        addItemLink(oneLink);
                }
            }
        }
        if (bItemGravityOn) {
            ItemInterface item;
            int iLen = allItems.size();
            Gravity oneGravity;
            double totalGm = 0;
            for (int i = 0; i < iLen; i++) {
                item = allItems.get(i);
                totalGm += item.getGM();
                for (int n = 0; n < iLen; n++) {
                    if (n != i) {
                        oneGravity = new Gravity((DarkMatter) item, (DarkMatter) allItems.get(n), this);
                        if (oneGravity.isValid())
                            ((DarkMatter) item).addGravityLink(oneGravity);
                    }
                }
            }
            for (ItemInterface i:allItems)
                i.noteTotalGM(totalGm);

        }
        activeGlobalActions = allGlobalActions.activeActions();
//        for (ItemLink l: allItemLinks)
//            l.setGravityLinks(bItemGravityOn);
    }

    boolean anyElasticItem() {
        boolean retVal = false;
        for (ItemInterface item: allItems) {
            if (retVal = ((DarkMatter)item).isElastic())
                break;
        }
        return retVal;
    }

    public boolean noteItemData() {
        if (allItems.size() > 0) {
            for (ItemInterface i : allItems)
                i.noteInput();
            initLinks();
            return true;
        }
        else
            return false;
    }

    void initLinks() {
        for (ItemLink l: allItemLinks)
            l.initStartForce();
    }

    JButton buttAddItem;
    JToggleButton cloneItem;
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
        sP.setPreferredSize(new Dimension(800, 350));
        itemTable = new ItemTable(mainApp, this, allItems);
        sP.setViewportView(itemTable.getTable());
        outerP.add(sP, BorderLayout.CENTER);
//        JPanel buttPan = new JPanel(new GridLayout(1, 2));
//        buttPan.add(buttAddItem);
//        outerP.add(buttPan, BorderLayout.SOUTH);
        cloneItem = new JToggleButton("Clone Item");
        outerP.add(itemListBottomPanel(), BorderLayout.SOUTH);
        return outerP;
    }

    JPanel itemListBottomPanel() {
        JPanel buttPan = new JPanel(new GridLayout(1, 2));
        buttPan.add(buttAddItem);
        buttPan.add(cloneItem);
        return buttPan;
    }
    public void updateItemTable() {
        if (itemTable != null)
            itemTable.updateUI();
    }

    void addItem(Component c) {
        Window parent = mainApp.parent();
        Item newItem = Item.getNewItem(this, "## Enter Item Name ##", parent);
        if (newItem != null) {
            newItem.setSpace(this);
            if (newItem.editItem(mainApp, parent) == Item.EditResponse.CHANGED) {
                itemTable.addOneRow(newItem);
            }
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
    public boolean bItemGravityOn = false;

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
        sP.setPreferredSize(new Dimension(400, 350));
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
        for (ItemInterface it:allItems)
            ((DarkMatter)it).clearGravityLinks();
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

    public ItemInterface getItem(String name) {
        ItemInterface item = null;
        for (ItemInterface it: allItems)
            if (it.getName().equals(name)) {
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

//      public void addObjectAndOrbit(Vector<ItemGraphic> itemGraphics, Group grp, RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception {
//        for (Item it: allItems) {
//            if (!it.boundaryItem)
//                itemGraphics.add(it.createItemGraphic(grp, orbitAttrib));
//        }
//        for (ItemLink inf: allItemLinks) {
//            inf.addLinksDisplay(grp, linkAttrib);
////            inf.prepareEvaluator();
//        }
//    }

    public void addObjectAndOrbit(Vector<ItemGraphic> itemGraphics, Group grp,RenderingAttributes itemAttrib,
                                  RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception {
        for (ItemInterface it: allItems) {
            if (!((DarkMatter)it).boundaryItem) {
                ItemGraphic itemG = it.createItemGraphic(grp, orbitAttrib);
                if (itemG != null) {
                    itemG.setItemDisplayAttribute(itemAttrib);
                    itemGraphics.add(itemG);
                }
            }
        }
        for (ItemLink inf: allItemLinks) {
            inf.addLinksDisplay(grp, linkAttrib);
//            inf.prepareEvaluator();
        }
    }

    public void initForces() {
        for (ItemInterface i: allItems)
            i.setLocalForces();
        for (ItemLink link:allItemLinks)
            link.setLocalForces();
    }

    void setItemStartConditions(double duration, double nowT) {
        for (ItemInterface i: allItems)
            i.setStartConditions(duration, nowT);
        for (ItemLink link:allItemLinks)
            link.setStartConditions(duration, nowT);
    }

    void updatePosAndVel(double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception {
        for (ItemInterface i: allItems)
            i.updatePosAndVel(deltaT, nowT, updateStep);
        for (ItemLink link:allItemLinks)
            link.updatePosAndVel(deltaT, nowT, updateStep);
    }

    boolean evalInfluence(double deltaT, double nowT) throws Exception  {
        boolean ok = true;
        setItemStartConditions(deltaT, nowT);
        for (int t = 0; t < mainApp.repeats; t++) {
            initForces();
            for (ItemLink inf : allItemLinks)
                if (!inf.evalForce(deltaT, false)) {
                    showError("in evalInfluence: evalForce is false for Link " + inf);
                    ok = false;
                    break;
                }
            if (!ok)
                break;
            updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.INTERMEDIATE); // not the final calculation
        }
        // now finalise it
        if (ok) {
            initForces();
            for (ItemLink inf : allItemLinks)
                if (!inf.evalForce(deltaT, true)) {
                    showError("In evalInfluence: evalForce-final is false for Link " + inf);
                    ok = false;
                    break;
                }
            if (ok) {
//                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.FINAL); // the final calculation
                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.K1);
                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.K2);
                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.K3);
                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.K4);
                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.RK4);
//                updatePosAndVel(deltaT, nowT, ItemInterface.UpdateStep.EuFwd);
            }
        }
        return ok;
    }

    boolean evalInfluence(SpaceEvaluator evaluator , double deltaT, double nowT) throws Exception  {
        boolean ok = true;
        setItemStartConditions(deltaT, nowT);
        for (int t = 0; t < mainApp.repeats; t++) {
            initForces();
            evaluator.awaitStartLinkCalculations(); // this should start the netForce calculations
            evaluator.awaitForceComplete(); // now all netForce calculations are ready
            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.INTERMEDIATE);

        }
        // now finalise it
        if (ok) {
            initForces();
            evaluator.awaitStartLinkCalculations(); // this should start the netForce calculations
            evaluator.awaitForceComplete(); // now all netForce calculations are ready
//            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.FINAL);
            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.K1);
            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.K2);
            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.K3);
            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.K4);
            updatePosAndVel(evaluator, deltaT, nowT, ItemInterface.UpdateStep.RK4);
        }
        return ok;
    }

    void updatePosAndVel(SpaceEvaluator evaluator , double deltaT, double nowT, ItemInterface.UpdateStep updateStep) throws Exception  {
        evaluator.setTimes(deltaT, nowT, updateStep);
        evaluator.awaitStartUpdatePositions(); // this should start the upatePosVelocity calculations
        evaluator.awaitPositionsReady(); // now all Positions are ready
    }

    public void updateLinkDisplay() {
        for (ItemLink lk: allItemLinks)
            lk.updateDisplay();
    }

    public DoubleMaxMin xMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin();
        double objectRadius;
        double pos;
        for (ItemInterface i: allItems) {
            DarkMatter dm = (DarkMatter)i;
            objectRadius = dm.dia / 2;
            if (objectRadius > 0) {
                pos = dm.getPositionX();
                maxMin.takeNewValue(pos + objectRadius);
                maxMin.takeNewValue(pos - objectRadius);
            }
        }
        return maxMin;
    }

    public DoubleMaxMin yMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin();
        double objectRadius;
        double pos;
        for (ItemInterface i: allItems) {
            DarkMatter dm = (DarkMatter)i;
            objectRadius = dm.dia / 2;
            if (objectRadius > 0) {
                pos = dm.getPositionY();
                maxMin.takeNewValue(pos + objectRadius);
                maxMin.takeNewValue(pos - objectRadius);
            }
        }
        return maxMin;
    }

    public DoubleMaxMin zMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin();
        double objectRadius;
        double pos;
        for (ItemInterface i: allItems) {
            DarkMatter dm = (DarkMatter)i;
            objectRadius = dm.dia / 2;
            if (objectRadius > 0) {
                pos = dm.getPositionZ();
                maxMin.takeNewValue(pos + objectRadius);
                maxMin.takeNewValue(pos - objectRadius);
            }
        }
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
        for (ItemInterface it: allItems)
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

