package GeneralElements.Display;

import Applications.ItemMovementsApp;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by M Viswanathan on 04 Oct 2014
 */
public class LocalViewFrame  extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    ItemMovementsApp controller;
    JPanel localViewPanel;
    LocalViewFrame(String name, JPanel localViewPanel, ItemMovementsApp controller) {
        super(name);
        this.controller = controller;
        this.localViewPanel = localViewPanel;
        jbInit();
    }

    void jbInit() {
        this.setSize(1300, 700);
        add(localViewPanel);
        pack();

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }
}
