package main;

import UI.DangNhap;

import javax.swing.*;

public class test {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new DangNhap().setVisible(true);
        });
    }
}