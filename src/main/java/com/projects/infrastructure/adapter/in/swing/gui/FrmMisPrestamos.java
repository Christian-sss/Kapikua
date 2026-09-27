package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.ConsultarCronogramaCommand;
import com.projects.application.dto.command.ConsultarPrestamosCommand;
import com.projects.application.dto.command.GenerarComprobanteCommand;
import com.projects.application.dto.command.PagarCuotaCommand;
import com.projects.application.dto.response.PrestamoResumenResponse;
import com.projects.application.port.in.ConsultarCronogramaUseCase;
import com.projects.application.port.in.ConsultarPrestamosUseCase;
import com.projects.application.port.in.GenerarComprobanteUseCase;
import com.projects.application.port.in.PagarCuotaUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FrmMisPrestamos - Préstamos del cliente, el cronograma del préstamo seleccionado (RF-39)
 * y el pago de su próxima cuota con el saldo de la billetera (RF-40, RF-44).
 */
public class FrmMisPrestamos extends javax.swing.JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FrmBilletera parentBilletera;
    private final FrmCreditos parentCreditos;
    private final ConsultarPrestamosUseCase consultarPrestamosUseCase;
    private final ConsultarCronogramaUseCase consultarCronogramaUseCase;
    private final PagarCuotaUseCase pagarCuotaUseCase;
    private final GenerarComprobanteUseCase generarComprobanteUseCase;
    private final List<PrestamoResumenResponse> prestamosPorFila = new ArrayList<>();
    private DefaultTableModel modeloPrestamos;
    private DefaultTableModel modeloCuotas;

    public FrmMisPrestamos(FrmBilletera parentBilletera, FrmCreditos parentCreditos) {
        this(parentBilletera, parentCreditos,
                CompositionRoot.crearConsultarPrestamosUseCase(),
                CompositionRoot.crearConsultarCronogramaUseCase(),
                CompositionRoot.crearPagarCuotaUseCase(),
                CompositionRoot.crearGenerarComprobanteUseCase());
    }

    public FrmMisPrestamos(FrmBilletera parentBilletera, FrmCreditos parentCreditos,
                           ConsultarPrestamosUseCase consultarPrestamosUseCase,
                           ConsultarCronogramaUseCase consultarCronogramaUseCase,
                           PagarCuotaUseCase pagarCuotaUseCase,
                           GenerarComprobanteUseCase generarComprobanteUseCase) {
        this.parentBilletera = parentBilletera;
        this.parentCreditos = parentCreditos;
        this.consultarPrestamosUseCase = consultarPrestamosUseCase;
        this.consultarCronogramaUseCase = consultarCronogramaUseCase;
        this.pagarCuotaUseCase = pagarCuotaUseCase;
        this.generarComprobanteUseCase = generarComprobanteUseCase;
        initComponents();
        configurarEstilos();
        inicializarTablas();
        cargarPrestamos(null);
        UITheme.setupWindow(this, "KAPIKUA - Mis préstamos");
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
                int lineY = y + height - 3;
                g2.setColor(UITheme.AMARILLO_KAPIKUA);
                g2.fillRect(x, lineY, lineW / 2, 3);
                g2.setColor(UITheme.VERDE_KAPIKUA);
                g2.fillRect(x + lineW / 2, lineY, lineW / 2, 3);
                g2.dispose();
            }
        });

        lblCronograma.setFont(UITheme.FONT_LABEL);
        lblCronograma.setForeground(UITheme.TEXT_PRIMARY);

        UITheme.stylePrimaryButtonGreen(btnPagar);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void inicializarTablas() {
        modeloPrestamos = new DefaultTableModel(
                new String[]{"N°", "Producto", "Monto", "Saldo capital", "Cuotas", "Próx. venc.", "Próx. cuota", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPrestamos.setModel(modeloPrestamos);
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(tblPrestamos);

        var centro = UITheme.createCenterRenderer();
        var derecha = UITheme.createRightRenderer();
        tblPrestamos.getColumnModel().getColumn(0).setCellRenderer(centro);
        tblPrestamos.getColumnModel().getColumn(2).setCellRenderer(derecha);
        tblPrestamos.getColumnModel().getColumn(3).setCellRenderer(derecha);
        tblPrestamos.getColumnModel().getColumn(4).setCellRenderer(centro);
        tblPrestamos.getColumnModel().getColumn(5).setCellRenderer(centro);
        tblPrestamos.getColumnModel().getColumn(6).setCellRenderer(derecha);
        tblPrestamos.getColumnModel().getColumn(7).setCellRenderer(centro);
        int[] anchos = {30, 180, 95, 105, 55, 95, 90, 75};
        for (int i = 0; i < anchos.length; i++) {
            tblPrestamos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        tblPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarCronograma();
            }
        });

        modeloCuotas = new DefaultTableModel(
                new String[]{"N°", "Vencimiento", "Capital", "Interés", "Mora", "Total", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCuotas.setModel(modeloCuotas);
        UITheme.styleTable(tblCuotas);
        tblCuotas.getColumnModel().getColumn(0).setCellRenderer(centro);
        tblCuotas.getColumnModel().getColumn(1).setCellRenderer(centro);
        for (int i = 2; i <= 5; i++) {
            tblCuotas.getColumnModel().getColumn(i).setCellRenderer(derecha);
        }
        tblCuotas.getColumnModel().getColumn(6).setCellRenderer(centro);
    }

    private Long clienteId() {
        return (parentBilletera != null) ? parentBilletera.getClienteId() : null;
    }

    /**
     * @param prestamoASeleccionar el préstamo que queda seleccionado tras recargar (null = el primero).
     */
    private void cargarPrestamos(Long prestamoASeleccionar) {
        modeloPrestamos.setRowCount(0);
        prestamosPorFila.clear();

        if (clienteId() == null) {
            return;
        }

        var resultado = consultarPrestamosUseCase.ejecutar(new ConsultarPrestamosCommand(clienteId()));

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudieron cargar tus préstamos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (var prestamo : resultado.getValue().get()) {
            modeloPrestamos.addRow(new Object[]{
                    prestamo.prestamoId(),
                    prestamo.productoNombre(),
                    formatearMonto(prestamo.montoDesembolsado()),
                    formatearMonto(prestamo.saldoCapital()),
                    prestamo.cuotasPagadas() + " / " + prestamo.cuotasTotales(),
                    prestamo.proximoVencimiento() != null ? prestamo.proximoVencimiento().format(FORMATO_FECHA) : "—",
                    prestamo.proximaCuota() != null ? formatearMonto(prestamo.proximaCuota()) : "—",
                    prestamo.estado()
            });
            prestamosPorFila.add(prestamo);
        }

        if (prestamosPorFila.isEmpty()) {
            lblCronograma.setText("Aún no tienes préstamos. Solicita uno desde el catálogo de créditos.");
            return;
        }

        int fila = 0;
        for (int i = 0; i < prestamosPorFila.size(); i++) {
            if (prestamosPorFila.get(i).prestamoId().equals(prestamoASeleccionar)) {
                fila = i;
            }
        }
        tblPrestamos.setRowSelectionInterval(fila, fila);
    }

    private void cargarCronograma() {
        modeloCuotas.setRowCount(0);

        int fila = tblPrestamos.getSelectedRow();
        if (fila < 0 || fila >= prestamosPorFila.size()) {
            return;
        }

        Long prestamoId = prestamosPorFila.get(fila).prestamoId();
        var resultado = consultarCronogramaUseCase.ejecutar(new ConsultarCronogramaCommand(clienteId(), prestamoId));

        if (resultado.isFailure()) {
            lblCronograma.setText(resultado.getError().get().message());
            return;
        }

        lblCronograma.setText("Cronograma del préstamo N° " + prestamoId);
        for (var cuota : resultado.getValue().get()) {
            modeloCuotas.addRow(new Object[]{
                    cuota.numero(),
                    cuota.fechaVencimiento().format(FORMATO_FECHA),
                    formatearMonto(cuota.capital()),
                    formatearMonto(cuota.interes()),
                    formatearMonto(cuota.mora()),
                    formatearMonto(cuota.total()),
                    cuota.estado()
            });
        }
    }

    private void btnPagarActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = tblPrestamos.getSelectedRow();
        if (fila < 0 || fila >= prestamosPorFila.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona el préstamo cuya cuota deseas pagar.",
                    "Seleccionar préstamo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        var prestamo = prestamosPorFila.get(fila);
        if (prestamo.proximaCuota() == null) {
            JOptionPane.showMessageDialog(this, "Este préstamo ya no tiene cuotas por pagar.",
                    "Préstamo " + prestamo.estado(), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Validación de frontend con el saldo que muestra la billetera; PagarCuotaService vuelve a
        // validarlo con billetera.withDraw() dentro de la transacción.
        if (parentBilletera != null && prestamo.proximaCuota().compareTo(parentBilletera.getSaldo()) > 0) {
            JOptionPane.showMessageDialog(this,
                    String.format(Locale.US, "Saldo insuficiente. La cuota es de S/ %,.2f y tu saldo es S/ %,.2f.",
                            prestamo.proximaCuota(), parentBilletera.getSaldo()),
                    "Saldo insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numeroCuota = prestamo.cuotasPagadas() + 1;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format(Locale.US, "¿Deseas pagar la cuota %d de %d del préstamo N° %d?%n%nMonto: S/ %,.2f%nSe debitará de tu billetera.",
                        numeroCuota, prestamo.cuotasTotales(), prestamo.prestamoId(), prestamo.proximaCuota()),
                "Confirmar pago",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        var resultado = pagarCuotaUseCase.ejecutar(new PagarCuotaCommand(clienteId(), prestamo.prestamoId()));

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudo pagar la cuota", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var pago = resultado.getValue().get();
        String mensaje = String.format(Locale.US, "¡Pago exitoso de la cuota %d por S/ %,.2f!%nTu nuevo saldo: S/ %,.2f",
                pago.numeroCuota(), pago.montoPagado(), pago.saldoResultante());
        if (pago.prestamoPagado()) {
            mensaje += "\n\n¡Felicitaciones! Terminaste de pagar el préstamo N° " + pago.prestamoId() + ".";
        }

        Object[] opciones = {"Aceptar", "Descargar comprobante"};
        int opcion = JOptionPane.showOptionDialog(
                this,
                mensaje,
                "Operación completada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (opcion == 1) {
            descargarComprobante(pago.transaccionId());
        }

        if (parentBilletera != null) {
            parentBilletera.cargarBilletera();
        }
        cargarPrestamos(pago.prestamoId());
    }

    private void descargarComprobante(Long transaccionId) {
        var resultado = generarComprobanteUseCase.ejecutar(new GenerarComprobanteCommand(transaccionId, clienteId()));

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudo generar el comprobante", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var selector = new JFileChooser();
        selector.setSelectedFile(new File("comprobante-" + transaccionId + ".pdf"));

        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (var out = new FileOutputStream(selector.getSelectedFile())) {
                out.write(resultado.getValue().get());
                JOptionPane.showMessageDialog(this, "Comprobante guardado correctamente.",
                        "Descarga completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        if (parentCreditos != null) {
            parentCreditos.setVisible(true);
        } else if (parentBilletera != null) {
            parentBilletera.setVisible(true);
        }
        this.dispose();
    }

    private static String formatearMonto(BigDecimal monto) {
        return String.format(Locale.US, "S/ %,.2f", monto);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPrestamos = new javax.swing.JTable();
        lblCronograma = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblCuotas = new javax.swing.JTable();
        btnPagar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Mis préstamos");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(820, 660));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Mis préstamos");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Consulta el estado de tus créditos y su cronograma de pagos");

        tblPrestamos.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tblPrestamos.setGridColor(new java.awt.Color(228, 235, 231));
        tblPrestamos.setRowHeight(28);
        tblPrestamos.setSelectionBackground(new java.awt.Color(255, 249, 222));
        tblPrestamos.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblPrestamos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "N°", "Producto", "Monto", "Saldo capital", "Cuotas", "Próx. venc.", "Próx. cuota", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tblPrestamos);

        lblCronograma.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCronograma.setForeground(new java.awt.Color(32, 49, 45));
        lblCronograma.setText("Cronograma del préstamo seleccionado");

        tblCuotas.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tblCuotas.setGridColor(new java.awt.Color(228, 235, 231));
        tblCuotas.setRowHeight(26);
        tblCuotas.setSelectionBackground(new java.awt.Color(255, 249, 222));
        tblCuotas.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblCuotas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "N°", "Vencimiento", "Capital", "Interés", "Mora", "Total", "Estado"
            }
        ));
        jScrollPane2.setViewportView(tblCuotas);

        btnPagar.setBackground(new java.awt.Color(2, 123, 113));
        btnPagar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnPagar.setForeground(new java.awt.Color(255, 255, 255));
        btnPagar.setText("PAGAR PRÓXIMA CUOTA");
        btnPagar.setBorderPainted(false);
        btnPagar.setFocusPainted(false);
        btnPagar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPagarActionPerformed(evt);
            }
        });

        btnVolver.setBackground(new java.awt.Color(255, 255, 255));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(32, 49, 45));
        btnVolver.setText("Volver al catálogo");
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
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(lblCronograma, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(btnPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblCronograma)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPagar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCronograma;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTable tblCuotas;
    private javax.swing.JTable tblPrestamos;
    // End of variables declaration//GEN-END:variables
}
