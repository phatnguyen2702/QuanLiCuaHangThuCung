package main;

import UI.DangNhap;
import javax.swing.*;

// Lớp khởi chạy ứng dụng và mở màn hình đăng nhập.
public class test {

    // Khởi chạy màn hình đăng nhập trên luồng giao diện Swing.
    // Tham số args: các đối số dòng lệnh.
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new DangNhap().setVisible(true);
        });
    }
}
