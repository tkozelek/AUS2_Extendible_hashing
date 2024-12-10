package kozelek.gui.view;

import javax.swing.*;
import java.awt.*;

public class AddServisWindow extends JFrame {

    private JButton backButton;
    private JList list1;
    private JButton submitAddButton;
    private JPanel panel1;
    private JTextField priceField;
    private JTextField dayField;
    private JTextField monthField;
    private JTextField yearField;
    private JTextField popisField;
    private JButton addButton;
    private JLabel popisCountLabel;

    public AddServisWindow() {
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

    public JLabel getPopisCountLabel() {
        return popisCountLabel;
    }

    public JTextField getPriceField() {
        return priceField;
    }

    public JTextField getDayField() {
        return dayField;
    }

    public JTextField getMonthField() {
        return monthField;
    }

    public JTextField getYearField() {
        return yearField;
    }

    public JTextField getPopisField() {
        return popisField;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
