package com.projects;

import com.projects.infrastructure.adapter.in.swing.gui.FrmLogin;

public class Main {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmLogin().setVisible(true);
        });
    }
}
