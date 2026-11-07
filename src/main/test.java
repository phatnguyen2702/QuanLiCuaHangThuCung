package main;

import UI.DangNhapUI;
import javax.swing.*;


public class test {



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DangNhapUI().setVisible(true);
        });
    }
}
