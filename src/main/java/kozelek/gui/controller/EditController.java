package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.AddWindow;
import kozelek.gui.view.EditWindow;
import kozelek.objekty.Car;
import kozelek.objekty.Servis;

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
        view.getDeleteServisButton().addActionListener(e -> deleteServis());
    }

    private void deleteServis() {
        Servis servis = view.getSelectedServis();
        if (servis != null) {
            Car car = view.getData();
            if (view.getData().getId() != -1)
                this.model.deleteServiceVisitById(view.getData().getId(), servis);
            else
                this.model.deleteServiceVisitBySPZ(view.getData().getSPZ(), servis);

            car.removeServis(servis);
            this.mainController.replaceInModel(car, car);
            view.setData(car);
        }
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
            newCar.addAllServisy(oldCar.getServisy());

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
