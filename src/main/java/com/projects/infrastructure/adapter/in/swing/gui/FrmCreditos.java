package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.response.ProductoCrediticioResponse;
import com.projects.application.port.in.ListarProductosUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FrmCreditos - Catálogo de créditos y microcréditos para el cliente (RF-33).
 * Desde aquí se abre la simulación/solicitud (FrmSimularCredito) y "Mis préstamos" (FrmMisPrestamos).
 */
public class FrmCreditos extends javax.swing.JFrame {

    private final FrmBilletera parentBilletera;
    private final ListarProductosUseCase listarProductosUseCase;
    private final List<ProductoCrediticioResponse> productosPorFila = new ArrayList<>();
    private DefaultTableModel tableModel;

    public FrmCreditos() {
        this(null);
    }

    public FrmCreditos(FrmBilletera parentBilletera) {
        this(parentBilletera, CompositionRoot.crearListarProductosUseCase());
    }

    public FrmCreditos(FrmBilletera parentBilletera, ListarProductosUseCase listarProductosUseCase) {
        this.parentBilletera = parentBilletera;
        this.listarProductosUseCase = listarProductosUseCase;
        initComponents();
        configurarEstilos();
        inicializarTabla();
        UITheme.setupWindow(this, "KAPIKUA - Catálogo de Créditos");
    }

    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);

        lblTitulo.setIcon(new ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
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

        UITheme.stylePrimaryButtonGreen(btnSolicitar);
        UITheme.styleSecondaryButtonYellow(btnVerDetalle);
        UITheme.styleNeutralButton(btnMisPrestamos);
        btnMisPrestamos.setForeground(UITheme.VERDE_PROFUNDO);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void inicializarTabla() {
        String[] columnas = {"Producto", "Monto (S/)", "Plazo (meses)", "TEA", "Tipo"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCreditos.setModel(tableModel);
        UITheme.styleTable(tblCreditos);

        var centerRenderer = UITheme.createCenterRenderer();
        var rightRenderer = UITheme.createRightRenderer();
        tblCreditos.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        tblCreditos.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblCreditos.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblCreditos.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        tblCreditos.getColumnModel().getColumn(0).setPreferredWidth(200);
        tblCreditos.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblCreditos.getColumnModel().getColumn(2).setPreferredWidth(105);
        tblCreditos.getColumnModel().getColumn(3).setPreferredWidth(75);
        tblCreditos.getColumnModel().getColumn(4).setPreferredWidth(140);

        cargarProductos();
    }

    private void cargarProductos() {
        tableModel.setRowCount(0);
        productosPorFila.clear();

        var resultado = listarProductosUseCase.ejecutar();

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudo cargar el catálogo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (var producto : resultado.getValue().get()) {
            tableModel.addRow(new Object[]{
                    producto.nombre(),
                    String.format(Locale.US, "S/ %,.0f – %,.0f", producto.montoMinimo(), producto.montoMaximo()),
                    producto.plazoMinimoMeses() + " – " + producto.plazoMaximoMeses(),
                    producto.tasaInteresAnual().toPlainString() + "%",
                    nombreTipo(producto.tipo())
            });
            productosPorFila.add(producto);
        }
    }

    static String nombreTipo(String tipo) {
        return "MICROCREDITO".equals(tipo) ? "Microcrédito" : "Crédito personal";
    }

    private ProductoCrediticioResponse productoSeleccionado(String mensajeSiNoHay) {
        int fila = tblCreditos.getSelectedRow();
        if (fila < 0 || fila >= productosPorFila.size()) {
            JOptionPane.showMessageDialog(this, mensajeSiNoHay, "Seleccionar producto", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return productosPorFila.get(fila);
    }

    private void btnVerDetalleActionPerformed(java.awt.event.ActionEvent evt) {
        var producto = productoSeleccionado("Por favor, selecciona un producto del catálogo para ver su detalle.");
        if (producto == null) {
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                String.format(Locale.US,
                        "Detalle del Producto:%n%n• Nombre: %s%n• Tipo: %s%n• Monto: S/ %,.2f – S/ %,.2f%n• Plazo: %d – %d meses%n• TEA: %s%%%n• TCEA: %s%% (sin comisiones ni seguros)",
                        producto.nombre(), nombreTipo(producto.tipo()),
                        producto.montoMinimo(), producto.montoMaximo(),
                        producto.plazoMinimoMeses(), producto.plazoMaximoMeses(),
                        producto.tasaInteresAnual().toPlainString(), producto.tasaInteresAnual().toPlainString()),
                "Información de Crédito",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void btnSolicitarActionPerformed(java.awt.event.ActionEvent evt) {
        var producto = productoSeleccionado("Por favor, selecciona el crédito que deseas solicitar.");
        if (producto == null) {
            return;
        }

        new FrmSimularCredito(parentBilletera, this, producto).setVisible(true);
        this.setVisible(false);
    }

    private void btnMisPrestamosActionPerformed(java.awt.event.ActionEvent evt) {
        new FrmMisPrestamos(parentBilletera, this).setVisible(true);
        this.setVisible(false);
    }

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        if (parentBilletera != null) {
            parentBilletera.setVisible(true);
        }
        this.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCreditos = new javax.swing.JTable();
        btnSolicitar = new javax.swing.JButton();
        btnVerDetalle = new javax.swing.JButton();
        btnMisPrestamos = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Catálogo de Créditos");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(780, 560));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Catálogo de Créditos y Microcréditos");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Impulsa tus proyectos con nuestras soluciones de financiamiento al instante");

        tblCreditos.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tblCreditos.setGridColor(new java.awt.Color(228, 235, 231));
        tblCreditos.setRowHeight(28);
        tblCreditos.setSelectionBackground(new java.awt.Color(255, 249, 222));
        tblCreditos.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblCreditos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Monto (S/)", "Plazo (meses)", "TEA", "Tipo"
            }
        ));
        jScrollPane1.setViewportView(tblCreditos);

        btnSolicitar.setBackground(new java.awt.Color(2, 123, 113));
        btnSolicitar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSolicitar.setForeground(new java.awt.Color(255, 255, 255));
        btnSolicitar.setText("SOLICITAR CRÉDITO");
        btnSolicitar.setBorderPainted(false);
        btnSolicitar.setFocusPainted(false);
        btnSolicitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSolicitarActionPerformed(evt);
            }
        });

        btnVerDetalle.setBackground(new java.awt.Color(255, 224, 20));
        btnVerDetalle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVerDetalle.setForeground(new java.awt.Color(32, 49, 45));
        btnVerDetalle.setText("Ver detalle");
        btnVerDetalle.setBorderPainted(false);
        btnVerDetalle.setFocusPainted(false);
        btnVerDetalle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerDetalleActionPerformed(evt);
            }
        });

        btnMisPrestamos.setBackground(new java.awt.Color(255, 255, 255));
        btnMisPrestamos.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnMisPrestamos.setForeground(new java.awt.Color(2, 123, 113));
        btnMisPrestamos.setText("Mis préstamos");
        btnMisPrestamos.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnMisPrestamos.setFocusPainted(false);
        btnMisPrestamos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMisPrestamosActionPerformed(evt);
            }
        });

        btnVolver.setBackground(new java.awt.Color(255, 255, 255));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(32, 49, 45));
        btnVolver.setText("Volver a mi billetera");
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
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(btnSolicitar, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnVerDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnMisPrestamos, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnVolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlTarjetaLayout.setVerticalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitulo)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSolicitar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVerDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMisPrestamos, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            new FrmCreditos().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMisPrestamos;
    private javax.swing.JButton btnSolicitar;
    private javax.swing.JButton btnVerDetalle;
    private javax.swing.JButton btnVolver;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTable tblCreditos;
    // End of variables declaration//GEN-END:variables
}
