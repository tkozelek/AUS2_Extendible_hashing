package kozelek.gui.view;

import javax.swing.*;
import java.awt.*;

public class StartWindow extends JFrame {
    private JTextField filenameField;
    private JPanel panel1;
    private JButton startButton;
    private JTextField clusterHeapField;
    private JTextField clusterCarIdField;
    private JTextField clusterCarSpzField;

    public StartWindow() {
        setTitle("Start Window");
        setSize(1200, 600);
        setMinimumSize(new Dimension(600, 400));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(panel1);
    }

    public JTextField getFilenameField() {
        return filenameField;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JTextField getClusterHeapField() {
        return clusterHeapField;
    }

    public JTextField getClusterCarIdField() {
        return clusterCarIdField;
    }

    public JTextField getClusterCarSpzField() {
        return clusterCarSpzField;
    }
}
