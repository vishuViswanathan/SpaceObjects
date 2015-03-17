package GeneralElements.globalActions;

import mvUtils.display.InputControl;
import mvUtils.mvXML.ValAndPos;
import mvUtils.mvXML.XMLmv;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Created by M Viswanathan on 01 Feb 2015
 */
public class AllGlobalActions {
    LinkedHashMap<GlobalAction.Type, GlobalAction> allActions;

    public AllGlobalActions() {
        allActions = new LinkedHashMap<GlobalAction.Type, GlobalAction>();
        for (GlobalAction.Type type:GlobalAction.Type.values())
            allActions.put(type, GlobalAction.getGlobalAction(type));
    }

    public Vector<GlobalAction> activeActions() {
        Vector<GlobalAction> activeActions = new Vector<GlobalAction>();
        for (GlobalAction gA: allActions.values())
            if (gA.bUsed)
                activeActions.add(gA);
        return activeActions;
    }

    public Collection<GlobalAction> getActions() {
        return allActions.values();
    }

    public JComponent globalActionPanel(InputControl ipC) {
        JPanel outerP = new JPanel(new GridLayout(1, allActions.size()));
        for (GlobalAction g:allActions.values())
            outerP.add(g.editPanel(ipC));
        return outerP;
    }

    boolean setValues(String xmlStr) {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "type", 0);
        try {
            GlobalAction.Type type = GlobalAction.Type.getEnum(vp.val);
            if (type != null) {
                allActions.get(type).setValues(xmlStr);
                retVal = true;
            }
        } catch (Exception e) {
            retVal = false;
        }
        return retVal;
    }

    public boolean setAllValues(String xmlStr) {
        boolean retVal = false;
        ValAndPos vp;
        vp = XMLmv.getTag(xmlStr, "nGActions", 0);
        try {
            int nGActions = Integer.valueOf(vp.val);
            for (int a = 0; a < nGActions; a++) {
                String tag = "gA#" + ("" + a).trim();
                vp = XMLmv.getTag(xmlStr, tag, vp.endPos);
                retVal = setValues(vp.val);
                if (!retVal)
                    break;
            }
        } catch (Exception e) {
            retVal = false;
        }
        return retVal;
    }

    public StringBuilder dataInXML() {
        StringBuilder xmlStr = new StringBuilder(XMLmv.putTag("nGActions", allActions.size()));
        int i = 0;
        for (GlobalAction gA: allActions.values())
            xmlStr.append(XMLmv.putTag("gA#" + ("" + i++).trim(), gA.dataInXML().toString()));
        return xmlStr;
    }
}
