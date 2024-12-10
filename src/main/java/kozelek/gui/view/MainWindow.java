package kozelek.gui.view;

import javax.swing.*;

public class MainWindow extends JFrame {
    private JButton findIdButton;
    private JButton findSpzButton;
    private JList list1;
    private JButton editButton;
    private JButton generateDataButton;
    private JButton addButton;
    private JTextField inputField;
    private JButton deleteButton;
    private JPanel panel1;
    private JButton getAllButton;
    private JButton addServisButton;

    public MainWindow() {
        add(panel1);
        setTitle("Hlavne okno");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public JButton getFindIdButton() {
        return findIdButton;
    }

    public JButton getFindSpzButton() {
        return findSpzButton;
    }

    public JList getList1() {
        return list1;
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getGenerateDataButton() {
        return generateDataButton;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getAddServisButton() {
        return addServisButton;
    }

    public JButton getGetAllButton() {
        return getAllButton;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
