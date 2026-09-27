package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.port.in.CerrarSesionUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;

/**
 * FrmAdministrador - Menú principal del módulo administrativo de KAPIKUA.
 * Implementado según la Sección 7.1 del Plan de Implementación.
 * Permite acceder a la gestión de clientes, auditoría de transacciones y gráficos estadísticos.
 */
public class FrmAdministrador extends javax.swing.JFrame {

    private final CerrarSesionUseCase cerrarSesionUseCase;

    public FrmAdministrador() {
        this(CompositionRoot.crearCerrarSesionUseCase());
    }

    public FrmAdministrador(CerrarSesionUseCase cerrarSesionUseCase) {
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        initComponents();
        configurarEstilos();
        UITheme.setupWindow(this, "KAPIKUA - Panel de Administración");
    }

    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);

        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblTitulo.setIconTextGap(8);
        lblTitulo.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitulo.setForeground(UITheme.VERDE_PROFUNDO);

        lblSubtitulo.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitulo.setForeground(UITheme.TEXT_SECONDARY);
        lblSubtitulo.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 0) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                int lineW = 60;
                int startX = x + (width - lineW) / 2;
                int lineY = y + height - 3;
                g2.setColor(UITheme.AMARILLO_KAPIKUA);
                g2.fillRect(startX, lineY, lineW / 2, 3);
                g2.setColor(UITheme.VERDE_KAPIKUA);
                g2.fillRect(startX + lineW / 2, lineY, lineW / 2, 3);
                g2.dispose();
            }
        });

        lblRol.setFont(UITheme.FONT_SMALL);
        lblRol.setForeground(UITheme.VERDE_PROFUNDO);

        // Tres tarjetas de acceso alineadas: icono verde para Clientes, amarillo para Transacciones, y verde para Gráficos
        UITheme.styleMenuCardButton(btnClientes, UITheme.createClientIcon(24), UITheme.VERDE_KAPIKUA);
        UITheme.styleMenuCardButton(btnTransacciones, UITheme.createTransactionIcon(24), UITheme.AMARILLO_KAPIKUA);
        UITheme.styleMenuCardButton(btnGraficos, UITheme.createChartIcon(24), UITheme.VERDE_PROFUNDO);
        UITheme.styleNeutralButton(btnCerrarSesion);
        btnCerrarSesion.setForeground(UITheme.DANGER);
    }

    private void btnClientesActionPerformed(java.awt.event.ActionEvent evt) {
        FrmClientesAdmin clientesAdmin = new FrmClientesAdmin(this);
        clientesAdmin.setVisible(true);
        this.setVisible(false);
    }

    private void btnTransaccionesActionPerformed(java.awt.event.ActionEvent evt) {
        FrmTransaccionesAdmin transaccionesAdmin = new FrmTransaccionesAdmin(this);
        transaccionesAdmin.setVisible(true);
        this.setVisible(false);
    }

    private void btnGraficosActionPerformed(java.awt.event.ActionEvent evt) {
        FrmGraficosAdmin graficosAdmin = new FrmGraficosAdmin(this);
        graficosAdmin.setVisible(true);
        this.setVisible(false);
    }

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas cerrar la sesión del panel de administración?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            cerrarSesionUseCase.ejecutar();
            FrmLogin login = new FrmLogin();
            login.setVisible(true);
            this.dispose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblRol = new javax.swing.JLabel();
        btnClientes = new javax.swing.JButton();
        btnTransacciones = new javax.swing.JButton();
        btnGraficos = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Panel de Administración");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(520, 560));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Panel de Administración KAPIKUA");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Supervisión, clientes, auditoría financiera y métricas");

        lblRol.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblRol.setForeground(new java.awt.Color(2, 123, 113));
        lblRol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRol.setText("● Sesión activa: Administrador del Sistema");

        btnClientes.setBackground(new java.awt.Color(255, 255, 255));
        btnClientes.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnClientes.setForeground(new java.awt.Color(32, 49, 45));
        btnClientes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnClientes.setText("Gestión de Clientes");
        btnClientes.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, new java.awt.Color(2, 123, 113)), javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 18))));
        btnClientes.setFocusPainted(false);
        btnClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClientesActionPerformed(evt);
            }
        });

        btnTransacciones.setBackground(new java.awt.Color(255, 255, 255));
        btnTransacciones.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransacciones.setForeground(new java.awt.Color(32, 49, 45));
        btnTransacciones.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTransacciones.setText("Auditoría de Transacciones");
        btnTransacciones.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, new java.awt.Color(255, 224, 20)), javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 18))));
        btnTransacciones.setFocusPainted(false);
        btnTransacciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransaccionesActionPerformed(evt);
            }
        });

        btnGraficos.setBackground(new java.awt.Color(255, 255, 255));
        btnGraficos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnGraficos.setForeground(new java.awt.Color(32, 49, 45));
        btnGraficos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnGraficos.setText("Métricas y Gráficos Estadísticos");
        btnGraficos.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, new java.awt.Color(2, 123, 113)), javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 18))));
        btnGraficos.setFocusPainted(false);
        btnGraficos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficosActionPerformed(evt);
            }
        });

        btnCerrarSesion.setBackground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCerrarSesion.setForeground(new java.awt.Color(224, 36, 36));
        btnCerrarSesion.setText("Cerrar sesión de administrador");
        btnCerrarSesion.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTarjetaLayout = new javax.swing.GroupLayout(pnlTarjeta);
        pnlTarjeta.setLayout(pnlTarjetaLayout);
        pnlTarjetaLayout.setHorizontalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(lblRol, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(btnClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnTransacciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGraficos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCerrarSesion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        pnlTarjetaLayout.setVerticalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblRol)
                .addGap(25, 25, 25)
                .addComponent(btnClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnTransacciones, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnGraficos, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(btnCerrarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmAdministrador().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnGraficos;
    private javax.swing.JButton btnTransacciones;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    // End of variables declaration//GEN-END:variables
}
