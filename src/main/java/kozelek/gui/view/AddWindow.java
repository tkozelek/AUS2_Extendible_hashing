package kozelek.gui.view;

import javax.swing.*;
import java.awt.*;

public class AddWindow extends JFrame {

    private JButton backButton;
    private JList list1;
    private JTextField dayField;
    private JTextField popisField;
    private JTextField priceField;
    private JTextField spzField;
    private JButton submitAddButton;
    private JPanel panel1;
    private JTextField monthField;
    private JTextField yearField;
    private JButton addButton;

    public AddWindow() {
        setTitle("Add Window");
        setSize(1200, 600);
        setMinimumSize(new Dimension(600, 400));
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(panel1);
    }

    public JButton getSubmitAddButton() {
        return submitAddButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JTextField getNameField() {
        return dayField;
    }

    public JTextField getLastnameField() {
        return popisField;
    }

    public JTextField getIdField() {
        return priceField;
    }

    public JTextField getSpzField() {
        return spzField;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
