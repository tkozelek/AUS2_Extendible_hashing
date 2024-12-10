package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.GenerateDataWindow;

import javax.swing.*;

public class GenerateController {
    private Model model;
    private GenerateDataWindow view;
    private int pocet;
    private int startsAt;
    private int pocetServis, pocetPopis;
    public GenerateController(GenerateDataWindow generateDataWindow, Model model) {
        this.model = model;
        this.view = generateDataWindow;

        view.getGoBackButton().addActionListener(e -> goBack());
        view.getGenerateButton().addActionListener(e -> generate());
    }


    private void goBack() {
        view.dispose();
    }

    private void generate() {
        if (!view.getCountField().getText().isBlank()) {
            this.pocet = Integer.parseInt(view.getCountField().getText());

            if (view.getStartsAtField().getText().isEmpty() || view.getCountServisField().getText().isEmpty() || view.getCountPopisField().getText().isEmpty()) {
                this.model.generujAVloz(pocet);
            } else {
                startsAt = Integer.parseInt(view.getStartsAtField().getText());
                pocetServis = Integer.parseInt(view.getCountServisField().getText());
                pocetPopis = Integer.parseInt(view.getCountPopisField().getText());

                this.model.generujAVloz(startsAt, pocet, pocetServis, pocetPopis);
            }
            JOptionPane.showMessageDialog(this.view,
                    "Success");
            return;
        }
        JOptionPane.showMessageDialog(this.view,
                "Empty fields.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
