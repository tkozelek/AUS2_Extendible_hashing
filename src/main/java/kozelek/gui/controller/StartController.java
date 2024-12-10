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
        String s = view.getRecordCountField().getText();
        int maxBlockCount = 0;
        if (!s.isBlank())
            maxBlockCount = Integer.parseInt(s);
        String filename = view.getFilenameField().getText() + ".bin";

        this.model = new Model(filename, maxBlockCount);
        MainWindow mView = new MainWindow();
        new MainController(mView, model);
        this.view.dispose();
    }
}
