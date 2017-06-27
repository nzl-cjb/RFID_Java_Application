/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import javax.swing.JFrame;

/**
 *
 * @author Callum
 */
public class MainGUI {
    private JFrame frame = new JFrame();

    /**
     * Creates new form MainGUI
     */
    public MainGUI() {
        HomeForm form = new HomeForm(frame);
        frame.add(form);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        MainGUI gui = new MainGUI();
    }
}
