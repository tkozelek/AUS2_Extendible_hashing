package kozelek.gui.view;

import javax.swing.*;
import java.awt.*;

public class GenerateDataWindow extends JFrame {
    private JButton goBackButton;
    private JTextField countField;
    private JButton generateButton;
    private JPanel panel1;
    private JTextField startsAtField;
    private JTextField countServisField;
    private JTextField countPopisField;

    public GenerateDataWindow() {
        setTitle("Generate data Window");
        setSize(1200, 600);
        setMinimumSize(new Dimension(600, 400));
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(panel1);
    }

    public JButton getGoBackButton() {
        return goBackButton;
    }

    public JTextField getCountField() {
        return countField;
    }

    public JButton getGenerateButton() {
        return generateButton;
    }

    public JTextField getStartsAtField() {
        return startsAtField;
    }

    public JTextField getCountServisField() {
        return countServisField;
    }

    public JTextField getCountPopisField() {
        return countPopisField;
    }
}
