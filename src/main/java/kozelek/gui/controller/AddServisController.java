package kozelek.gui.controller;

import kozelek.Helper;
import kozelek.gui.model.Model;
import kozelek.gui.view.AddServisWindow;
import kozelek.objekty.Car;
import kozelek.objekty.Servis;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class AddServisController {
    private MainController mainController;
    private Model model;
    private AddServisWindow view;
    private ArrayList<String> popises;
    private Car car;
    public AddServisController(AddServisWindow addWindow, Model model, MainController mainController, Car car) {
        this.model = model;
        this.view = addWindow;
        this.car = car;
        this.mainController = mainController;

        popises = new ArrayList<>();

        view.getSubmitAddButton().addActionListener(e -> handleSave());
        view.getBackButton().addActionListener(e -> goBack());
        view.getAddButton().addActionListener(e -> addPopis());
    }

    private void addPopis() {
        if (!view.getPopisField().getText().isEmpty() && popises.size() < Helper.MAX_POPIS_POCET) {
            String text = view.getPopisField().getText();

            popises.add(text);
        }
        view.getPopisField().setText("");
        view.getPopisCountLabel().setText("Pocet popisov: " + popises.size() + " / " + Helper.MAX_POPIS_POCET);
    }

    private void goBack() {
        view.dispose();
    }

    private void handleSave() {
        try {
            int id = Integer.parseInt(view.getIdField().getText());
            int den = Integer.parseInt(view.getDayField().getText());
            int mesiac = Integer.parseInt(view.getMonthField().getText());
            int rok = Integer.parseInt(view.getYearField().getText());
            double cena = Double.parseDouble(view.getPriceField().getText());

            LocalDate datum = LocalDate.of(rok, mesiac, den);

            Car newCar = this.model.addServiceVisitById(car.getId(), cena, datum, popises, id);

            this.mainController.replaceInModel(car, newCar);

            JOptionPane.showMessageDialog(this.view,
                    "Success.");
        } catch (NumberFormatException ex) {
            view.showError("Cisla ID, zbytok string");
        }
    }
}
