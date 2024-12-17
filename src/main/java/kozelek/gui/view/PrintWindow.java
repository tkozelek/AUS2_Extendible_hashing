package kozelek.gui.view;

import javax.swing.*;
import java.awt.*;

public class PrintWindow extends JFrame {
    private JPanel panel1;
    private JList heapList;
    private JList cidList;
    private JList cspzList;
    private JLabel heapHeadLabel;
    private JLabel cidHeadLabel;
    private JLabel cspzHeadLabel;
    private JButton refreshButton;
    private JButton goBackButton;

    public PrintWindow() {
        setTitle("Print info window");
        setSize(1800, 800);
        setMinimumSize(new Dimension(1300, 700));
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(panel1);
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getGoBackButton() {
        return goBackButton;
    }

    public JList getHeapList() {
        return heapList;
    }

    public JList getCidList() {
        return cidList;
    }

    public JList getCspzList() {
        return cspzList;
    }

    public JLabel getHeapHeadLabel() {
        return heapHeadLabel;
    }

    public JLabel getCidHeadLabel() {
        return cidHeadLabel;
    }

    public JLabel getCspzHeadLabel() {
        return cspzHeadLabel;
    }
}
