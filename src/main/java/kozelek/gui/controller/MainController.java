package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.*;
import kozelek.objekty.Car;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class MainController {
    private MainWindow view;
    private Model model;
    private DefaultListModel<Car> listModel;
    public MainController(MainWindow mView, Model model) {
        this.view = mView;
        this.model = model;

        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveState();
            }
        });

        view.getGetAllButton().addActionListener(e -> getAll());
        view.getFindIdButton().addActionListener(e -> findById());
        view.getFindSpzButton().addActionListener(e -> findBySPZ());

        view.getEditButton().addActionListener(e -> editData());
        view.getGenerateDataButton().addActionListener(e -> generateData());
        view.getDeleteButton().addActionListener(e -> deleteSelected());

        view.getAddButton().addActionListener(e -> addNew());
        view.getAddServisButton().addActionListener(e -> addServis());

        view.getPrintButton().addActionListener(e -> print());

        view.setVisible(true);

        this.listModel = new DefaultListModel<Car>();
    }

    private void print() {
        PrintWindow printWindow = new PrintWindow();
        PrintController printController = new PrintController(printWindow, model);
        printWindow.setVisible(true);
    }

    private void saveState() {
        try {
            model.saveStates();
            model.close();
            System.out.println("Subory ulozene a zatvorene spravne.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                    "Error pri ukladani: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addServis() {
        Car car = getSelectedItem();
        if (car != null) {
            AddServisWindow addServisWindow = new AddServisWindow();
            AddServisController addServisController = new AddServisController(addServisWindow, model, this, car);
            addServisWindow.setVisible(true);
        }
    }

    private Car getSelectedItem() {
        int index = this.view.getList1().getSelectedIndex();
        if (index >= 0)
            return this.listModel.get(index);
        else
            JOptionPane.showMessageDialog(this.view,
                    "Non selected.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        return null;
    }

    private void addNew() {
        AddWindow addWindow = new AddWindow();
        new AddController(addWindow, model);
        addWindow.setVisible(true);
    }

    private void deleteSelected() {
        int index = this.view.getList1().getSelectedIndex();
        Car car = this.getSelectedItem();
        if (index >= 0 && car != null) {
            this.delete(index, car);
        }
    }

    private void delete(int index, Car item) {
        if (item == null) {
            return;
        }
        this.listModel.remove(index);
        if (item.getId() != -1)
            this.model.deleteCarById(item.getId());
        else
            this.model.deleteCarBySPZ(item.getSPZ());

        this.updateList();
    }

    private void generateData() {
        GenerateDataWindow generateDataWindow = new GenerateDataWindow();
        new GenerateController(generateDataWindow, model);
        generateDataWindow.setVisible(true);
    }

    private void editData() {
        Car car = getSelectedItem();
        if (car != null) {
            EditWindow editWindow = new EditWindow(car);
            EditController editController = new EditController(editWindow, model, this);
            editWindow.setVisible(true);
        }
    }

    private void findBySPZ() {
        if (view.getInputField().getText().isEmpty()) {
            view.showError("Empty fields.");
            return;
        }

        String spz = view.getInputField().getText();

        Car car = this.model.findCarBySPZ(spz);
        if (car != null) {
            this.listModel.clear();
            this.listModel.addElement(car);
            this.updateList();
        } else {
            view.showError("Car doesnt exist");
        }

    }

    private void findById() {
        if (view.getInputField().getText().isEmpty()) {
            view.showError("Empty fields.");
            return;
        }
        int id = Integer.parseInt(view.getInputField().getText());

        Car car = this.model.findCarById(id);
        if (car != null) {
            this.listModel.clear();
            this.listModel.addElement(car);
            this.updateList();
        } else {
            view.showError("Car doesnt exist");
        }
    }

    private void getAll() {
        ArrayList<Car> cars = this.model.getAllCars();
        this.listModel.clear();
        this.listModel.addAll(cars);
        this.updateList();
    }

    public void replaceInModel(Car old, Car newItem) {
        int index = this.listModel.indexOf(old);
        if (index >= 0) {
            this.listModel.set(index, newItem);
            this.updateList();
        }
    }

    private void addToModel(ArrayList<Car> objekty) {
        if (objekty != null && !objekty.isEmpty()) {
            listModel.clear();
            listModel.addAll(objekty);
        } else {
            JOptionPane.showMessageDialog(this.view,
                    "Not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateList(ArrayList<Car> objekty) {
        this.addToModel(objekty);
        this.updateList();
    }

    private void updateList() {
        if (!listModel.isEmpty()) {
            this.view.getList1().setModel(listModel);
        }
    }
}
