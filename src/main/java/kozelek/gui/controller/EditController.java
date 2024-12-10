package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.AddWindow;
import kozelek.gui.view.EditWindow;
import kozelek.objekty.Car;

import javax.swing.*;

public class EditController {
    private Model model;
    private EditWindow view;
    private MainController mainController;
    public EditController(EditWindow addWindow, Model model, MainController mainController) {
        this.model = model;
        this.view = addWindow;
        this.mainController = mainController;

        view.getSubmitEditButton().addActionListener(e -> handleSave());
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

            Car oldCar = view.getData();
            Car newCar = new Car(name, lastname, id, spz);

            this.model.editCar(oldCar, newCar);

            this.mainController.replaceInModel(oldCar, newCar);
            this.view.setData(newCar);
            JOptionPane.showMessageDialog(this.view,
                    "Success.");
        } catch (NumberFormatException ex) {
            view.showError("Cisla ID, zbytok string");
        }
    }
}
