package kozelek.gui.view;

import kozelek.objekty.Car;
import kozelek.objekty.Servis;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class EditWindow extends JFrame {
    private JPanel panel1;
    private JButton backButton;
    private JTextField nameField;
    private JTextField lastnameField;
    private JTextField idField;
    private JTextField spzField;
    private JButton submitEditButton;
    private JList list1;
    private Car data;

    public EditWindow(Car data) {
        this.data = data;
        this.setValues();
        setTitle("Edit Window");
        setMinimumSize(new Dimension(800, 500));
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(panel1);
    }

    public Car getData() {
        return data;
    }

    public void setData(Car data) {
        this.data = data;
        this.setValues();
    }

    private void setValues() {
        if (data != null) {
            this.idField.setText(String.valueOf(data.getId()));
            this.spzField.setText(String.valueOf(data.getSPZ()));

            this.lastnameField.setText(data.getPriezvisko());
            this.nameField.setText(String.valueOf(data.getMeno()));
        }
        this.updateList();
    }

    private void updateList() {
        DefaultListModel<Servis> model = new DefaultListModel<>();

        ArrayList<Servis> objekty;

        objekty = data.getServisy();

        if (objekty != null) {
            model.addAll(objekty);
            this.list1.setModel(model);
        }
    }


    public JButton getBackButton() {
        return backButton;
    }

    public JButton getSubmitEditButton() {
        return submitEditButton;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getLastnameField() {
        return lastnameField;
    }

    public JTextField getIdField() {
        return idField;
    }

    public JTextField getSpzField() {
        return spzField;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
