package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.MainWindow;
import kozelek.gui.view.StartWindow;

public class StartController {
    private StartWindow view;
    private Model model;

    public StartController(StartWindow view) {
        this.view = view;
        this.view.setVisible(true);

        view.getStartButton().addActionListener(e -> this.start());
    }

    private void start() {
        String heap = view.getClusterHeapField().getText();
        String carId = view.getClusterCarIdField().getText();
        String carSpz = view.getClusterCarSpzField().getText();

        int clusterHeap = 5000; // default
        int clusterCarId = 50;
        int clusterCarSpz = 90;

        if (!heap.isBlank() && !carId.isBlank() && !carSpz.isBlank()) {
            clusterHeap = Integer.parseInt(heap);
            clusterCarId = Integer.parseInt(carId);
            clusterCarSpz = Integer.parseInt(carSpz);
        }

        String filename = view.getFilenameField().getText() + ".bin";
        this.model = new Model(filename, clusterHeap, clusterCarId, clusterCarSpz);
        MainWindow mView = new MainWindow();
        new MainController(mView, model);
        this.view.dispose();
    }
}
