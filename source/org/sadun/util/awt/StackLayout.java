package org.sadun.util.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A simple layout manager to stack components vertically or horizontally,
 * optionally having the last component fill the remaining space.
 * 
 *
 * @author Cristiano Sadun
 */
public class StackLayout implements LayoutManager {
    
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    
    protected List components = new ArrayList();
    private int side;
    private int axis;
    private boolean fillLast;
    private int spacing; 
    
    /**
     * 
     * @param side
     * @param axis
     */
    public StackLayout(int side, int axis) {
        this(side, axis,false);
        
    }
    
    public StackLayout(int side, int axis, boolean fillLast) {
        if (axis != X_AXIS && axis != Y_AXIS) throw new IllegalArgumentException("Invalid axis constant, use StackLayout.X_AXIS or Y_AXIS");
        this.side=side;
        this.axis=axis;
        this.fillLast=fillLast;
        this.spacing=0;
    }

    public void addLayoutComponent(String name, Component comp) {
        components.add(comp);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int pos = axis == Y_AXIS ? insets.top : insets.left;
        Iterator i = components.iterator();
        while(i.hasNext()) {
            Component comp = (Component)i.next();
            if (fillLast && !i.hasNext()) {
                if (axis==Y_AXIS) {
                    comp.setSize(parent.getWidth()-insets.left-insets.right, parent.getHeight()-pos);
                    comp.setLocation(insets.left, pos);
                } else {
                    comp.setSize(parent.getWidth()-pos, parent.getHeight()-insets.top-insets.bottom);
                    comp.setLocation(pos, insets.top);
                }
            } else {
                if (axis==Y_AXIS) {
                    comp.setSize(parent.getWidth()-insets.left-insets.right, side);
                    comp.setLocation(insets.left, pos);
                } else {
                    comp.setSize(side, parent.getHeight()-insets.top-insets.bottom);
                    comp.setLocation(pos, insets.top);
                }
            }
            pos+=side+spacing;
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        if (axis==Y_AXIS)
            return new Dimension(parent.getWidth(), side*components.size());
        else
            return new Dimension(side*components.size(), parent.getHeight());
    }

    public Dimension preferredLayoutSize(Container parent) {
        return minimumLayoutSize(parent);
    }

    public void removeLayoutComponent(Component comp) {
        components.remove(comp);
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame jf = new JFrame();
        
        JPanel jp = new JPanel();
        
        jp.setLayout(new StackLayout(50, Y_AXIS, true));
        for(int i=0;i<5;i++) {
            jp.add("", new JButton("Button "+i));
        }
        
        jf.setContentPane(new JScrollPane(jp));
        jf.setSize(500,500);
        jf.setVisible(true);

    }

    public void setSpacing(int i) {
        this.spacing = 2;
    }

    public int getSpacing() {
        return spacing;
    }
    
}
