package kozelek.gui.controller;

import kozelek.gui.model.Model;
import kozelek.gui.view.PrintWindow;

import javax.swing.*;
import java.util.ArrayList;

public class PrintController {
    private PrintWindow view;
    private Model model;
    private DefaultListModel<String> heapModel;
    private DefaultListModel<String> cidModel;
    private DefaultListModel<String> cspzModel;

    public PrintController(PrintWindow printWindow, Model model) {
        this.view = printWindow;
        this.model = model;

        this.heapModel = new DefaultListModel<>();
        this.cidModel = new DefaultListModel<>();
        this.cspzModel = new DefaultListModel<>();

        this.view.getRefreshButton().addActionListener(e -> refresh());
        this.view.getGoBackButton().addActionListener(e -> goBack());

        this.refresh();
    }

    private void goBack() {
        this.view.dispose();
    }

    private void refresh() {
        this.refreshLists();
        this.refreshHeaders();
    }

    private void refreshHeaders() {
        view.getHeapHeadLabel().setText(model.heapToString());
        view.getCidHeadLabel().setText(model.hfCarIdToString());
        view.getCspzHeadLabel().setText(model.hfCarSpzToString());
    }

    private void refreshLists() {
        ArrayList<String> heap = this.model.allHeapBlocksToString();
        ArrayList<String> cid = this.model.hbIdToString();
        ArrayList<String> cspz = this.model.hbSpzToString();

        this.clearModels();

        this.heapModel.addAll(heap);
        this.cidModel.addAll(cid);
        this.cspzModel.addAll(cspz);

        if (!heapModel.isEmpty())
            view.getHeapList().setModel(heapModel);
        if (!cidModel.isEmpty())
            view.getCidList().setModel(cidModel);
        if (!cspzModel.isEmpty())
            view.getCspzList().setModel(cspzModel);
    }

    private void clearModels() {
        this.heapModel.clear();
        this.cidModel.clear();
        this.cspzModel.clear();
    }


}
