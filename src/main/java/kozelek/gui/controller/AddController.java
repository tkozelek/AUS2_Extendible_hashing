package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.AddWindow;

import javax.swing.*;

public class AddController {
    private Model model;
    private AddWindow view;
    public AddController(AddWindow addWindow, Model model) {
        this.model = model;
        this.view = addWindow;

        view.getSubmitAddButton().addActionListener(e -> handleSave());
        view.getBackButton().addActionListener(e -> goBack());
    }

    private void goBack() {
        view.dispose();
    }

    private void handleSave() {
        try {
            int id = Integer.parseInt(view.getIdField().getText());
            String spz = view.getSpzField().getText();

            String name = view.getNameField().getText();
            String lastname = view.getLastnameField().getText();

            this.model.insert(name, lastname, id, spz);

            JOptionPane.showMessageDialog(this.view,
                    "Success.");
        } catch (NumberFormatException ex) {
            view.showError("Cisla ID, zbytok string");
        }
    }
}
