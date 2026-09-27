package com.projects.infrastructure.adapter.in.swing.gui;

import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * FrmClientesAdmin - Vista administrativa de consulta y filtrado de clientes registrados.
 * Implementado según la Sección 7.2 del Plan de Implementación.
 * Contiene tabla de clientes (tblClientes), buscador por DNI/nombre y retorno al menú admin.
 */
public class FrmClientesAdmin extends javax.swing.JFrame {

    private FrmAdministrador parentAdmin;
    private DefaultTableModel tableModel;

    public FrmClientesAdmin() {
        this(null);
    }

    public FrmClientesAdmin(FrmAdministrador parentAdmin) {
        this.parentAdmin = parentAdmin;
        initComponents();
        configurarEstilos();
        inicializarTabla();
        UITheme.setupWindow(this, "KAPIKUA - Gestión de Clientes");
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
        lblSubtitulo.setBorder(new javax.swing.border.EmptyBorder(0, 0, 8, 0) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                int lineW = 60;
                int startX = x;
                int lineY = y + height - 3;
                g2.setColor(UITheme.AMARILLO_KAPIKUA);
                g2.fillRect(startX, lineY, lineW / 2, 3);
                g2.setColor(UITheme.VERDE_KAPIKUA);
                g2.fillRect(startX + lineW / 2, lineY, lineW / 2, 3);
                g2.dispose();
            }
        });

        lblBuscar.setFont(UITheme.FONT_LABEL);
        lblBuscar.setForeground(UITheme.TEXT_PRIMARY);
        UITheme.styleTextField(txtBuscarCliente);

        UITheme.stylePrimaryButtonGreen(btnBuscar);
        UITheme.styleSecondaryButtonYellow(btnActualizar);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void inicializarTabla() {
        String[] columnas = {"ID", "Nombres", "Apellidos", "DNI", "Celular", "Correo", "Fecha Registro", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblClientes.setModel(tableModel);
        UITheme.styleTable(tblClientes);

        var centerRenderer = UITheme.createCenterRenderer();
        tblClientes.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblClientes.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblClientes.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblClientes.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tblClientes.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        tblClientes.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblClientes.getColumnModel().getColumn(1).setPreferredWidth(105);
        tblClientes.getColumnModel().getColumn(2).setPreferredWidth(115);
        tblClientes.getColumnModel().getColumn(3).setPreferredWidth(85);
        tblClientes.getColumnModel().getColumn(4).setPreferredWidth(95);
        tblClientes.getColumnModel().getColumn(5).setPreferredWidth(160);
        tblClientes.getColumnModel().getColumn(6).setPreferredWidth(120);
        tblClientes.getColumnModel().getColumn(7).setPreferredWidth(80);

        cargarClientesDemostracion("");
    }

    // Pendiente ListarClientesUseCase (rol ADMIN): hasta entonces se muestran clientes de ejemplo.
    private static final Object[][] CLIENTES_EJEMPLO = {
            {"CLI-1", "Juan", "Pérez Gómez", "71234561", "987654321", "juan.perez@gmail.com", "2026-07-01", "ACTIVO"},
            {"CLI-2", "María", "López Sánchez", "71234562", "987654322", "maria.lopez@outlook.com", "2026-07-03", "ACTIVO"},
            {"CLI-3", "Carlos", "Rodríguez Díaz", "71234563", "987654323", "carlos.rodriguez@upt.pe", "2026-07-10", "ACTIVO"},
            {"CLI-4", "Ana", "Torres Flores", "71234564", "987654324", "ana.torres@gmail.com", "2026-08-02", "INACTIVO"},
            {"CLI-5", "Luis", "García Mendoza", "71234565", "987654325", "luis.garcia@virtual.upt.pe", "2026-08-15", "ACTIVO"},
    };

    private void cargarClientesDemostracion(String filtro) {
        tableModel.setRowCount(0);
        String f = (filtro == null) ? "" : filtro.trim().toLowerCase();

        for (Object[] cliente : CLIENTES_EJEMPLO) {
            String nombres = ((String) cliente[1]).toLowerCase();
            String apellidos = ((String) cliente[2]).toLowerCase();
            String dni = (String) cliente[3];
            String celular = (String) cliente[4];

            if (f.isEmpty() || nombres.contains(f) || apellidos.contains(f) || dni.contains(f) || celular.contains(f)) {
                tableModel.addRow(cliente);
            }
        }
    }

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {
        String texto = txtBuscarCliente.getText().trim();
        cargarClientesDemostracion(texto);
    }

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {
        txtBuscarCliente.setText("");
        cargarClientesDemostracion("");
    }

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        if (parentAdmin != null) {
            parentAdmin.setVisible(true);
        }
        this.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscarCliente = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Gestión de Clientes");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(900, 600));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Directorio de Clientes KAPIKUA");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Visualiza y busca usuarios registrados en la plataforma");

        lblBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblBuscar.setForeground(new java.awt.Color(32, 49, 45));
        lblBuscar.setText("Buscar cliente (Nombre, Apellido o DNI):");

        txtBuscarCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBuscarCliente.setForeground(new java.awt.Color(32, 49, 45));
        txtBuscarCliente.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        btnBuscar.setBackground(new java.awt.Color(2, 123, 113));
        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBuscar.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscar.setText("Buscar");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnActualizar.setBackground(new java.awt.Color(255, 224, 20));
        btnActualizar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnActualizar.setForeground(new java.awt.Color(32, 49, 45));
        btnActualizar.setText("Limpiar / Todos");
        btnActualizar.setBorderPainted(false);
        btnActualizar.setFocusPainted(false);
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        tblClientes.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tblClientes.setGridColor(new java.awt.Color(228, 235, 231));
        tblClientes.setRowHeight(28);
        tblClientes.setSelectionBackground(new java.awt.Color(232, 249, 240));
        tblClientes.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombres", "Apellidos", "DNI", "Celular", "Correo", "Fecha Registro", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tblClientes);

        btnVolver.setBackground(new java.awt.Color(255, 255, 255));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(32, 49, 45));
        btnVolver.setText("Volver al panel principal");
        btnVolver.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTarjetaLayout = new javax.swing.GroupLayout(pnlTarjeta);
        pnlTarjeta.setLayout(pnlTarjetaLayout);
        pnlTarjetaLayout.setHorizontalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBuscar)
                            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                                .addComponent(txtBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnBuscar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnActualizar))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 820, Short.MAX_VALUE)
                    .addComponent(btnVolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlTarjetaLayout.setVerticalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSubtitulo))
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(lblBuscar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
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
            new FrmClientesAdmin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtBuscarCliente;
    // End of variables declaration//GEN-END:variables
}
