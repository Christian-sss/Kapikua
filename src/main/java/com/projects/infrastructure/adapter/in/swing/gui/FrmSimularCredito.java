package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.GenerarComprobanteCommand;
import com.projects.application.dto.command.SimularCreditoCommand;
import com.projects.application.dto.command.SolicitarCreditoCommand;
import com.projects.application.dto.response.ProductoCrediticioResponse;
import com.projects.application.dto.response.SimulacionCreditoResponse;
import com.projects.application.port.in.GenerarComprobanteUseCase;
import com.projects.application.port.in.SimularCreditoUseCase;
import com.projects.application.port.in.SolicitarCreditoUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * FrmSimularCredito - Simula un crédito (cuota, intereses, TCEA y cronograma) y lo solicita (RF-34, RF-35, RF-38).
 * Se abre desde FrmCreditos con el producto elegido en el catálogo.
 */
public class FrmSimularCredito extends javax.swing.JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FrmBilletera parentBilletera;
    private final FrmCreditos parentCreditos;
    private final ProductoCrediticioResponse producto;
    private final SimularCreditoUseCase simularCreditoUseCase;
    private final SolicitarCreditoUseCase solicitarCreditoUseCase;
    private final GenerarComprobanteUseCase generarComprobanteUseCase;

    // Solo se puede solicitar exactamente lo último que se simuló; si el usuario cambia monto o plazo,
    // debe volver a simular antes de solicitar.
    private SimulacionCreditoResponse ultimaSimulacion;
    private DefaultTableModel tableModel;

    public FrmSimularCredito(FrmBilletera parentBilletera, FrmCreditos parentCreditos, ProductoCrediticioResponse producto) {
        this(parentBilletera, parentCreditos, producto,
                CompositionRoot.crearSimularCreditoUseCase(),
                CompositionRoot.crearSolicitarCreditoUseCase(),
                CompositionRoot.crearGenerarComprobanteUseCase());
    }

    public FrmSimularCredito(FrmBilletera parentBilletera, FrmCreditos parentCreditos, ProductoCrediticioResponse producto,
                             SimularCreditoUseCase simularCreditoUseCase,
                             SolicitarCreditoUseCase solicitarCreditoUseCase,
                             GenerarComprobanteUseCase generarComprobanteUseCase) {
        this.parentBilletera = parentBilletera;
        this.parentCreditos = parentCreditos;
        this.producto = producto;
        this.simularCreditoUseCase = simularCreditoUseCase;
        this.solicitarCreditoUseCase = solicitarCreditoUseCase;
        this.generarComprobanteUseCase = generarComprobanteUseCase;
        initComponents();
        configurarEstilos();
        configurarProducto();
        inicializarTabla();
        UITheme.setupWindow(this, "KAPIKUA - Simular crédito");
    }

    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);
        UITheme.styleHighlightCardPanel(pnlResumen, UITheme.AMARILLO_KAPIKUA);

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

        lblRangos.setFont(UITheme.FONT_REGULAR);
        lblRangos.setForeground(UITheme.VERDE_PROFUNDO);

        lblMonto.setFont(UITheme.FONT_LABEL);
        lblMonto.setForeground(UITheme.TEXT_PRIMARY);
        lblPlazo.setFont(UITheme.FONT_LABEL);
        lblPlazo.setForeground(UITheme.TEXT_PRIMARY);
        UITheme.styleTextField(txtMonto);

        for (var titulo : new JLabel[]{lblCuotaTitulo, lblTasasTitulo, lblInteresesTitulo, lblTotalTitulo}) {
            titulo.setFont(UITheme.FONT_SMALL);
            titulo.setForeground(UITheme.TEXT_SECONDARY);
        }
        for (var valor : new JLabel[]{lblCuotaValor, lblTasasValor, lblInteresesValor, lblTotalValor}) {
            valor.setFont(UITheme.FONT_TITLE);
            valor.setForeground(UITheme.TEXT_PRIMARY);
        }
        lblCuotaValor.setForeground(UITheme.VERDE_PROFUNDO);

        lblMensaje.setFont(UITheme.FONT_SMALL);
        lblMensaje.setText(" ");

        UITheme.styleSecondaryButtonYellow(btnSimular);
        UITheme.stylePrimaryButtonGreen(btnSolicitar);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void configurarProducto() {
        lblSubtitulo.setText(producto.nombre() + " · " + FrmCreditos.nombreTipo(producto.tipo()));
        lblRangos.setText(String.format(Locale.US,
                "Monto: S/ %,.2f – S/ %,.2f   ·   Plazo: %d – %d meses   ·   TEA: %s%%",
                producto.montoMinimo(), producto.montoMaximo(),
                producto.plazoMinimoMeses(), producto.plazoMaximoMeses(),
                producto.tasaInteresAnual().toPlainString()));

        spnPlazo.setModel(new SpinnerNumberModel(
                producto.plazoMinimoMeses().intValue(),
                producto.plazoMinimoMeses().intValue(),
                producto.plazoMaximoMeses().intValue(),
                1));
        spnPlazo.setFont(UITheme.FONT_REGULAR);

        txtMonto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                invalidarSimulacion();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                invalidarSimulacion();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                invalidarSimulacion();
            }
        });
        spnPlazo.addChangeListener(e -> invalidarSimulacion());

        limpiarResumen();
    }

    private void inicializarTabla() {
        String[] columnas = {"N°", "Vencimiento", "Cuota", "Interés", "Capital", "Saldo"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCronograma.setModel(tableModel);
        UITheme.styleTable(tblCronograma);

        var centerRenderer = UITheme.createCenterRenderer();
        var rightRenderer = UITheme.createRightRenderer();
        tblCronograma.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblCronograma.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        for (int i = 2; i <= 5; i++) {
            tblCronograma.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
        tblCronograma.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblCronograma.getColumnModel().getColumn(1).setPreferredWidth(120);
    }

    private void invalidarSimulacion() {
        if (ultimaSimulacion != null) {
            ultimaSimulacion = null;
            tableModel.setRowCount(0);
            limpiarResumen();
            mostrarMensaje("Cambiaste el monto o el plazo: vuelve a simular antes de solicitar.", false);
        }
    }

    private void limpiarResumen() {
        btnSolicitar.setEnabled(false);
        lblCuotaValor.setText("—");
        lblTasasValor.setText(producto.tasaInteresAnual().toPlainString() + "%");
        lblInteresesValor.setText("—");
        lblTotalValor.setText("—");
    }

    public void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setForeground(esError ? UITheme.DANGER : UITheme.TEXT_SECONDARY);
        lblMensaje.setText(mensaje);
    }

    private BigDecimal leerMonto() {
        String texto = txtMonto.getText().trim();
        if (texto.isEmpty()) {
            mostrarMensaje("Ingresa el monto que deseas solicitar.", true);
            txtMonto.requestFocus();
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            mostrarMensaje("El monto ingresado no es válido.", true);
            txtMonto.requestFocus();
            return null;
        }
    }

    private void btnSimularActionPerformed(java.awt.event.ActionEvent evt) {
        var monto = leerMonto();
        if (monto == null) {
            return;
        }

        int plazo = (Integer) spnPlazo.getValue();
        var resultado = simularCreditoUseCase.ejecutar(new SimularCreditoCommand(producto.id(), monto, plazo));

        if (resultado.isFailure()) {
            mostrarMensaje(resultado.getError().get().message(), true);
            return;
        }

        var simulacion = resultado.getValue().get();
        var cronograma = simulacion.cronograma();

        tableModel.setRowCount(0);
        for (var cuota : cronograma.cuotas()) {
            tableModel.addRow(new Object[]{
                    cuota.numero(),
                    cuota.fechaVencimiento().format(FORMATO_FECHA),
                    formatearMonto(cuota.cuota()),
                    formatearMonto(cuota.interes()),
                    formatearMonto(cuota.capital()),
                    formatearMonto(cuota.saldo())
            });
        }

        var temPorcentaje = cronograma.tem().multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_EVEN);
        lblCuotaValor.setText(formatearMonto(cronograma.cuotaFija()));
        lblTasasValor.setText(cronograma.tea().toPlainString() + "% / " + cronograma.tcea().toPlainString() + "%");
        lblTasasValor.setToolTipText("TEM: " + temPorcentaje.toPlainString() + "%");
        lblInteresesValor.setText(formatearMonto(cronograma.totalIntereses()));
        lblTotalValor.setText(formatearMonto(cronograma.totalAPagar()));

        ultimaSimulacion = simulacion;
        btnSolicitar.setEnabled(true);
        mostrarMensaje("TEM: " + temPorcentaje.toPlainString() + "%. La última cuota ajusta el redondeo. "
                + "Sin comisiones ni seguros, la TCEA es igual a la TEA.", false);
    }

    private void btnSolicitarActionPerformed(java.awt.event.ActionEvent evt) {
        if (ultimaSimulacion == null) {
            mostrarMensaje("Primero simula tu crédito.", true);
            return;
        }

        Long clienteId = (parentBilletera != null) ? parentBilletera.getClienteId() : null;
        if (clienteId == null) {
            mostrarMensaje("No se pudo identificar tu cuenta. Vuelve a iniciar sesión.", true);
            return;
        }

        var simulacion = ultimaSimulacion;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format(Locale.US,
                        "¿Confirmas la solicitud de %s?%n%nMonto: S/ %,.2f%nPlazo: %d meses%nCuota mensual: S/ %,.2f%nTotal a pagar: S/ %,.2f",
                        producto.nombre(), simulacion.monto(), simulacion.plazoMeses(),
                        simulacion.cronograma().cuotaFija(), simulacion.cronograma().totalAPagar()),
                "Confirmar solicitud",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        var resultado = solicitarCreditoUseCase.ejecutar(new SolicitarCreditoCommand(
                clienteId, producto.id(), simulacion.monto(), simulacion.plazoMeses()));

        if (resultado.isFailure()) {
            mostrarMensaje(resultado.getError().get().message(), true);
            return;
        }

        var solicitud = resultado.getValue().get();

        if (!solicitud.aprobada()) {
            JOptionPane.showMessageDialog(this,
                    "Tu solicitud fue rechazada.\n\n" + solicitud.mensaje(),
                    "Solicitud rechazada", JOptionPane.WARNING_MESSAGE);
            volverAlCatalogo();
            return;
        }

        Object[] opciones = {"Aceptar", "Descargar comprobante"};
        int opcion = JOptionPane.showOptionDialog(
                this,
                String.format(Locale.US, "%s%nPréstamo N° %d · Cuota mensual: S/ %,.2f%nTu nuevo saldo: S/ %,.2f",
                        solicitud.mensaje(), solicitud.prestamoId(), solicitud.cuotaMensual(), solicitud.saldoResultante()),
                "Crédito aprobado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (opcion == 1) {
            descargarComprobante(solicitud.transaccionDesembolsoId(), clienteId);
        }

        if (parentBilletera != null) {
            parentBilletera.cargarBilletera();
        }

        volverAlCatalogo();
    }

    private void descargarComprobante(Long transaccionId, Long clienteId) {
        var resultado = generarComprobanteUseCase.ejecutar(new GenerarComprobanteCommand(transaccionId, clienteId));

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
        volverAlCatalogo();
    }

    private void volverAlCatalogo() {
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
        lblRangos = new javax.swing.JLabel();
        lblMonto = new javax.swing.JLabel();
        txtMonto = new javax.swing.JTextField();
        lblPlazo = new javax.swing.JLabel();
        spnPlazo = new javax.swing.JSpinner();
        btnSimular = new javax.swing.JButton();
        lblMensaje = new javax.swing.JLabel();
        pnlResumen = new javax.swing.JPanel();
        lblCuotaTitulo = new javax.swing.JLabel();
        lblTasasTitulo = new javax.swing.JLabel();
        lblInteresesTitulo = new javax.swing.JLabel();
        lblTotalTitulo = new javax.swing.JLabel();
        lblCuotaValor = new javax.swing.JLabel();
        lblTasasValor = new javax.swing.JLabel();
        lblInteresesValor = new javax.swing.JLabel();
        lblTotalValor = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCronograma = new javax.swing.JTable();
        btnSolicitar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Simular crédito");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(760, 740));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Simula y solicita tu crédito");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Producto seleccionado");

        lblRangos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblRangos.setForeground(new java.awt.Color(2, 123, 113));
        lblRangos.setText("Monto · Plazo · TEA");

        lblMonto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMonto.setForeground(new java.awt.Color(32, 49, 45));
        lblMonto.setText("Monto a solicitar (S/):");

        txtMonto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMonto.setForeground(new java.awt.Color(32, 49, 45));
        txtMonto.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true), javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        lblPlazo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPlazo.setForeground(new java.awt.Color(32, 49, 45));
        lblPlazo.setText("Plazo (meses):");

        spnPlazo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        btnSimular.setBackground(new java.awt.Color(255, 224, 20));
        btnSimular.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSimular.setForeground(new java.awt.Color(32, 49, 45));
        btnSimular.setText("Simular");
        btnSimular.setBorderPainted(false);
        btnSimular.setFocusPainted(false);
        btnSimular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimularActionPerformed(evt);
            }
        });

        lblMensaje.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblMensaje.setForeground(new java.awt.Color(100, 115, 109));
        lblMensaje.setText(" ");

        pnlResumen.setBackground(new java.awt.Color(255, 255, 255));
        pnlResumen.setLayout(new java.awt.GridLayout(2, 4, 12, 2));

        lblCuotaTitulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblCuotaTitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblCuotaTitulo.setText("Cuota mensual");
        pnlResumen.add(lblCuotaTitulo);

        lblTasasTitulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblTasasTitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblTasasTitulo.setText("TEA / TCEA");
        pnlResumen.add(lblTasasTitulo);

        lblInteresesTitulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblInteresesTitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblInteresesTitulo.setText("Total de intereses");
        pnlResumen.add(lblInteresesTitulo);

        lblTotalTitulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblTotalTitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblTotalTitulo.setText("Total a pagar");
        pnlResumen.add(lblTotalTitulo);

        lblCuotaValor.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblCuotaValor.setForeground(new java.awt.Color(2, 123, 113));
        lblCuotaValor.setText("—");
        pnlResumen.add(lblCuotaValor);

        lblTasasValor.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTasasValor.setForeground(new java.awt.Color(32, 49, 45));
        lblTasasValor.setText("—");
        pnlResumen.add(lblTasasValor);

        lblInteresesValor.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblInteresesValor.setForeground(new java.awt.Color(32, 49, 45));
        lblInteresesValor.setText("—");
        pnlResumen.add(lblInteresesValor);

        lblTotalValor.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        lblTotalValor.setForeground(new java.awt.Color(32, 49, 45));
        lblTotalValor.setText("—");
        pnlResumen.add(lblTotalValor);

        tblCronograma.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        tblCronograma.setGridColor(new java.awt.Color(228, 235, 231));
        tblCronograma.setRowHeight(26);
        tblCronograma.setSelectionBackground(new java.awt.Color(255, 249, 222));
        tblCronograma.setSelectionForeground(new java.awt.Color(32, 49, 45));
        tblCronograma.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "N°", "Vencimiento", "Cuota", "Interés", "Capital", "Saldo"
            }
        ));
        jScrollPane1.setViewportView(tblCronograma);

        btnSolicitar.setBackground(new java.awt.Color(2, 123, 113));
        btnSolicitar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSolicitar.setForeground(new java.awt.Color(255, 255, 255));
        btnSolicitar.setText("SOLICITAR CRÉDITO");
        btnSolicitar.setBorderPainted(false);
        btnSolicitar.setEnabled(false);
        btnSolicitar.setFocusPainted(false);
        btnSolicitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSolicitarActionPerformed(evt);
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
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                    .addComponent(lblRangos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMonto)
                            .addComponent(txtMonto, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPlazo)
                            .addComponent(spnPlazo, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnSimular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblMensaje, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(btnSolicitar, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRangos)
                .addGap(16, 16, 16)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMonto)
                    .addComponent(lblPlazo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMonto, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnPlazo, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSimular, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMensaje)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResumen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSolicitar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JButton btnSimular;
    private javax.swing.JButton btnSolicitar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCuotaTitulo;
    private javax.swing.JLabel lblCuotaValor;
    private javax.swing.JLabel lblInteresesTitulo;
    private javax.swing.JLabel lblInteresesValor;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblMonto;
    private javax.swing.JLabel lblPlazo;
    private javax.swing.JLabel lblRangos;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTasasTitulo;
    private javax.swing.JLabel lblTasasValor;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JLabel lblTotalValor;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JSpinner spnPlazo;
    private javax.swing.JTable tblCronograma;
    private javax.swing.JTextField txtMonto;
    // End of variables declaration//GEN-END:variables
}
