package GeneralElements;

import Applications.ItemMovementsApp;
import Applications.SpaceEvaluator;
import GeneralElements.Display.ItemGraphic;
import GeneralElements.Display.ItemTable;
import GeneralElements.Display.LinkTable;
import GeneralElements.globalActions.AllGlobalActions;
import GeneralElements.globalActions.GlobalAction;
import GeneralElements.link.Influence;
import GeneralElements.link.ItemLink;
import mvUtils.display.InputControl;
import mvUtils.display.MultiPairColPanel;
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
//    Vector<GlobalAction> globalActions;
    Vector<GlobalAction> activeGlobalActions;
    ItemMovementsApp mainApp;
    AllGlobalActions allGlobalActions;

    public ItemSpace(ItemMovementsApp mainApp) {
        this.mainApp = mainApp;
        clearSpace();
    }

    void clearSpace() {
        allItems = new LinkedList<Item>();
        allItemLinks = new LinkedList<ItemLink>();
        allGlobalActions = new AllGlobalActions();
//        globalActions = new Vector<GlobalAction>();
//        for (GlobalAction.Type type:GlobalAction.Type.values())
//            globalActions.add(GlobalAction.getGlobalAction(type));
        trace("clearing ItemSpace");
    }

    public Vector<GlobalAction> getActiveGlobalActions() {
        return activeGlobalActions;
    }

    public LinkedList<Item> getAlItems() {
        return allItems;
    }

    public LinkedList<ItemLink> getAllItemLinks() {
        return allItemLinks;
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
//            if (link.isGravity() || link.isContact()) {
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
        Item newItem = new Item(this, "## Enter Item Name ##", 1, 1, Color.RED,  mainApp.parent());
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
//            if (!il.isGravity())
            if (!il.isInterItem())
                tempInfList.add(il); //new ItemLink(allItems, il, this));
        listEdited = false;
    }

    JPanel infListPan;
    GridBagConstraints gbcInfList;
    boolean bItemGravityOn = false;

    public void enableItemGravity(boolean ena) {
        bItemGravityOn = ena;
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

    public void saveInfluenceList() {
        clearItemLinks();
        ItemLink ilNow;
        for (ItemLink iL : tempInfList) {
//            if ((ilNow = iL.getInfFromUI()) != null)
//                allItemLinks.add(ilNow);
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
//            else if (src == buttSaveInf) {
//                saveInfluenceList();
//                showMessage("Saving Links");
//            }
            else if (src == buttAddItem) {
                addItem(src);
            }
            else if (src == rbItemGravity) {
                bItemGravityOn = rbItemGravity.isSelected();
            }
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
            setTitle("New Link between Items");
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

    public void addObjectAndOrbit(Vector<ItemGraphic> itemGraphics, Group grp, RenderingAttributes orbitAttrib, RenderingAttributes linkAttrib) throws Exception {
        int count = 0;
        for (Item it: allItems) {
            count++;
            if (!it.boundaryItem)
                itemGraphics.add(it.createItemGraphic(grp, orbitAttrib));
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
        return false;
    }

    public void updateLinkDisplay() {
        for (ItemLink lk: allItemLinks)
            lk.updateDisplay();
    }

    public DoubleMaxMin xMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
//            maxMin.takeMaxValue(i.status.pos.getX());
            maxMin.takeMaxValue(i.getPositionX());
        return maxMin;
    }

    public DoubleMaxMin yMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
//            maxMin.takeMaxValue(i.status.pos.getY());
            maxMin.takeMaxValue(i.getPositionY());
        return maxMin;
    }

    public DoubleMaxMin zMaxMin() {
        DoubleMaxMin maxMin = new DoubleMaxMin(0, 0);
        for (Item i: allItems)
//            maxMin.takeMaxValue(i.status.pos.getZ());
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
//
    public boolean doCalculation(double deltaT, double nowT) throws Exception{
        return evalInfluence(deltaT, nowT);
    }

    public boolean  doCalculation( SpaceEvaluator evaluator, double deltaT, double nowT) throws Exception{
        return evalInfluence(evaluator, deltaT, nowT);
    }

    StringBuilder allGlobalActionsInXML() {
        return allGlobalActions.dataInXML();
//        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("nGActions", globalActions.size()));
//        int i = 0;
//        for (GlobalAction gA: globalActions)
//            xmlStr.append(XMLmv.putTag("gA#" + ("" + i++).trim(), gA.dataInXML().toString()));
//        return xmlStr;
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
//                oneItem = new Item(vp.val, mainApp.parent());
                oneItem = Item.getItemFromXML(vp.val, mainApp.parent());
                addItem(oneItem);
//                allItems.add(new Item(vp.val, mainApp.parent()));
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

