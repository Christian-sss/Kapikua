package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.ConsultarEstadisticasCommand;
import com.projects.application.dto.command.PeriodoEstadistica;
import com.projects.application.dto.response.EstadisticasResponse;
import com.projects.application.port.in.ConsultarEstadisticasUseCase;
import com.projects.application.port.in.GenerarReporteEstadisticasUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

/**
 * FrmGraficosAdmin - Panel de métricas y gráficos estadísticos de operaciones (solo ADMIN).
 * Alterna entre 'Por Día' (últimos 7 días) y 'Por Mes' (últimos 6 meses) y exporta el reporte en PDF.
 */
public class FrmGraficosAdmin extends javax.swing.JFrame {

    private final FrmAdministrador parentAdmin;
    private final ConsultarEstadisticasUseCase consultarEstadisticasUseCase;
    private final GenerarReporteEstadisticasUseCase generarReporteEstadisticasUseCase;
    private EstadisticasResponse estadisticasActuales;

    public FrmGraficosAdmin() {
        this(null);
    }

    public FrmGraficosAdmin(FrmAdministrador parentAdmin) {
        this(parentAdmin,
                CompositionRoot.crearConsultarEstadisticasUseCase(),
                CompositionRoot.crearGenerarReporteEstadisticasUseCase());
    }

    public FrmGraficosAdmin(FrmAdministrador parentAdmin, ConsultarEstadisticasUseCase consultarEstadisticasUseCase,
                            GenerarReporteEstadisticasUseCase generarReporteEstadisticasUseCase) {
        this.parentAdmin = parentAdmin;
        this.consultarEstadisticasUseCase = consultarEstadisticasUseCase;
        this.generarReporteEstadisticasUseCase = generarReporteEstadisticasUseCase;
        initComponents();
        configurarEstilos();
        refrescarDatosPeriodo();
        UITheme.setupWindow(this, "KAPIKUA - Gráficos Estadísticos");
    }

    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);
        UITheme.styleHighlightCardPanel(pnlKpi1, UITheme.VERDE_KAPIKUA);
        UITheme.styleHighlightCardPanel(pnlKpi2, UITheme.AMARILLO_KAPIKUA);
        UITheme.styleHighlightCardPanel(pnlKpi3, UITheme.VERDE_PROFUNDO);

        java.net.URL logoUrl = getClass().getResource("/images/LogoKapikua_small.png");
        if (logoUrl != null) {
            lblTitulo.setIcon(new javax.swing.ImageIcon(logoUrl));
        } else {
            lblTitulo.setIcon(UITheme.createLogoIcon(28, 28));
        }
        lblTitulo.setIconTextGap(8);
        lblTitulo.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitulo.setForeground(UITheme.VERDE_PROFUNDO);

        lblSubtitulo.setText("Volumen operado en Soles (S/) — transacciones exitosas");
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

        lblPeriodo.setFont(UITheme.FONT_LABEL);
        lblPeriodo.setForeground(UITheme.TEXT_PRIMARY);

        lblKpi1Titulo.setFont(UITheme.FONT_SMALL);
        lblKpi1Titulo.setForeground(UITheme.TEXT_SECONDARY);
        lblKpi1Valor.setFont(UITheme.FONT_TITLE);
        lblKpi1Valor.setForeground(UITheme.TEXT_PRIMARY);

        lblKpi2Titulo.setFont(UITheme.FONT_SMALL);
        lblKpi2Titulo.setForeground(UITheme.TEXT_SECONDARY);
        lblKpi2Valor.setFont(UITheme.FONT_TITLE);
        lblKpi2Valor.setForeground(UITheme.VERDE_PROFUNDO);

        lblKpi3Titulo.setFont(UITheme.FONT_SMALL);
        lblKpi3Titulo.setForeground(UITheme.TEXT_SECONDARY);
        lblKpi3Valor.setFont(UITheme.FONT_TITLE);
        lblKpi3Valor.setForeground(UITheme.TEXT_PRIMARY);

        UITheme.stylePrimaryButtonGreen(btnActualizar);
        UITheme.styleSecondaryButtonYellow(btnReporte);
        UITheme.styleNeutralButton(btnVolver);
    }

    private void actualizarGrafico() {
        pnlGrafico.repaint();
    }

    public void cambiarPeriodo(String periodo) {
        if (cmbPeriodo != null && periodo != null) {
            cmbPeriodo.setSelectedItem(periodo);
        }
        refrescarDatosPeriodo();
    }

    private PeriodoEstadistica periodoSeleccionado() {
        return "Por Mes".equals(cmbPeriodo.getSelectedItem()) ? PeriodoEstadistica.MES : PeriodoEstadistica.DIA;
    }

    private boolean refrescarDatosPeriodo() {
        var resultado = consultarEstadisticasUseCase.ejecutar(new ConsultarEstadisticasCommand(periodoSeleccionado()));

        if (resultado.isFailure()) {
            estadisticasActuales = null;
            lblKpi1Valor.setText("—");
            lblKpi2Valor.setText("—");
            lblKpi3Valor.setText("—");
            actualizarGrafico();
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudieron cargar las estadísticas", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        estadisticasActuales = resultado.getValue().get();
        lblKpi1Valor.setText(String.format(Locale.US, "%,d", estadisticasActuales.totalTransacciones()));
        lblKpi2Valor.setText(String.format(Locale.US, "S/ %,.2f", estadisticasActuales.volumenOperado()));
        lblKpi3Valor.setText(estadisticasActuales.porcentajeExito() != null
                ? estadisticasActuales.porcentajeExito().toPlainString() + "%"
                : "—");

        actualizarGrafico();
        return true;
    }

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {
        if (refrescarDatosPeriodo()) {
            JOptionPane.showMessageDialog(this, "Gráfico y métricas actualizados para el período: " + cmbPeriodo.getSelectedItem(), "Actualización", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void btnReporteActionPerformed(java.awt.event.ActionEvent evt) {
        var resultado = generarReporteEstadisticasUseCase.ejecutar(new ConsultarEstadisticasCommand(periodoSeleccionado()));

        if (resultado.isFailure()) {
            JOptionPane.showMessageDialog(this, resultado.getError().get().message(),
                    "No se pudo generar el reporte", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var selector = new JFileChooser();
        selector.setSelectedFile(new File("reporte-kapikua-" + LocalDate.now() + ".pdf"));

        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (var out = new FileOutputStream(selector.getSelectedFile())) {
                out.write(resultado.getValue().get());
                JOptionPane.showMessageDialog(this, "Reporte guardado correctamente.",
                        "Descarga completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        if (parentAdmin != null) {
            parentAdmin.setVisible(true);
        } else {
            new FrmAdministrador().setVisible(true);
        }
        this.dispose();
    }

    private void cmbPeriodoActionPerformed(java.awt.event.ActionEvent evt) {
        refrescarDatosPeriodo();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblPeriodo = new javax.swing.JLabel();
        cmbPeriodo = new javax.swing.JComboBox<>();
        btnActualizar = new javax.swing.JButton();
        pnlKpis = new javax.swing.JPanel();
        pnlKpi1 = new javax.swing.JPanel();
        lblKpi1Titulo = new javax.swing.JLabel();
        lblKpi1Valor = new javax.swing.JLabel();
        pnlKpi2 = new javax.swing.JPanel();
        lblKpi2Titulo = new javax.swing.JLabel();
        lblKpi2Valor = new javax.swing.JLabel();
        pnlKpi3 = new javax.swing.JPanel();
        lblKpi3Titulo = new javax.swing.JLabel();
        lblKpi3Valor = new javax.swing.JLabel();
        pnlGrafico = new PanelGraficoBarras();
        btnReporte = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Gráficos Estadísticos");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(860, 680));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Métricas y Gráficos Estadísticos");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setText("Volumen operado en Soles (S/) — transacciones exitosas");

        lblPeriodo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPeriodo.setForeground(new java.awt.Color(32, 49, 45));
        lblPeriodo.setText("Período:");

        cmbPeriodo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        cmbPeriodo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Por Día", "Por Mes" }));
        cmbPeriodo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPeriodoActionPerformed(evt);
            }
        });

        btnActualizar.setBackground(new java.awt.Color(2, 123, 113));
        btnActualizar.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnActualizar.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizar.setText("Actualizar datos");
        btnActualizar.setBorderPainted(false);
        btnActualizar.setFocusPainted(false);
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        pnlKpis.setBackground(new java.awt.Color(255, 255, 255));
        pnlKpis.setLayout(new java.awt.GridLayout(1, 3, 15, 0));

        pnlKpi1.setBackground(new java.awt.Color(255, 255, 255));
        pnlKpi1.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, new java.awt.Color(2, 123, 113)), javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16))));

        lblKpi1Titulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblKpi1Titulo.setForeground(new java.awt.Color(100, 115, 109));
        lblKpi1Titulo.setText("Total Transacciones");

        lblKpi1Valor.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblKpi1Valor.setForeground(new java.awt.Color(32, 49, 45));
        lblKpi1Valor.setText("185");

        javax.swing.GroupLayout pnlKpi1Layout = new javax.swing.GroupLayout(pnlKpi1);
        pnlKpi1.setLayout(pnlKpi1Layout);
        pnlKpi1Layout.setHorizontalGroup(
            pnlKpi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpi1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlKpi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpi1Titulo)
                    .addComponent(lblKpi1Valor))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        pnlKpi1Layout.setVerticalGroup(
            pnlKpi1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpi1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblKpi1Titulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpi1Valor)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpi1);

        pnlKpi2.setBackground(new java.awt.Color(255, 255, 255));
        pnlKpi2.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, new java.awt.Color(255, 224, 20)), javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16))));

        lblKpi2Titulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblKpi2Titulo.setForeground(new java.awt.Color(100, 115, 109));
        lblKpi2Titulo.setText("Volumen Operado");

        lblKpi2Valor.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblKpi2Valor.setForeground(new java.awt.Color(2, 123, 113));
        lblKpi2Valor.setText("S/ 14,820.00");

        javax.swing.GroupLayout pnlKpi2Layout = new javax.swing.GroupLayout(pnlKpi2);
        pnlKpi2.setLayout(pnlKpi2Layout);
        pnlKpi2Layout.setHorizontalGroup(
            pnlKpi2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpi2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlKpi2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpi2Titulo)
                    .addComponent(lblKpi2Valor))
                .addContainerGap(131, Short.MAX_VALUE))
        );
        pnlKpi2Layout.setVerticalGroup(
            pnlKpi2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpi2Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblKpi2Titulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpi2Valor)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpi2);

        pnlKpi3.setBackground(new java.awt.Color(255, 255, 255));
        pnlKpi3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, new java.awt.Color(2, 123, 113)), javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true), javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16))));

        lblKpi3Titulo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblKpi3Titulo.setForeground(new java.awt.Color(100, 115, 109));
        lblKpi3Titulo.setText("Efectividad / Éxito");

        lblKpi3Valor.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblKpi3Valor.setForeground(new java.awt.Color(32, 49, 45));
        lblKpi3Valor.setText("97.2%");

        javax.swing.GroupLayout pnlKpi3Layout = new javax.swing.GroupLayout(pnlKpi3);
        pnlKpi3.setLayout(pnlKpi3Layout);
        pnlKpi3Layout.setHorizontalGroup(
            pnlKpi3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpi3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlKpi3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKpi3Titulo)
                    .addComponent(lblKpi3Valor))
                .addContainerGap(139, Short.MAX_VALUE))
        );
        pnlKpi3Layout.setVerticalGroup(
            pnlKpi3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKpi3Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblKpi3Titulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblKpi3Valor)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlKpis.add(pnlKpi3);

        pnlGrafico.setBackground(new java.awt.Color(255, 255, 255));
        pnlGrafico.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true));

        javax.swing.GroupLayout pnlGraficoLayout = new javax.swing.GroupLayout(pnlGrafico);
        pnlGrafico.setLayout(pnlGraficoLayout);
        pnlGraficoLayout.setHorizontalGroup(
            pnlGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlGraficoLayout.setVerticalGroup(
            pnlGraficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 280, Short.MAX_VALUE)
        );

        btnReporte.setBackground(new java.awt.Color(255, 224, 20));
        btnReporte.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnReporte.setForeground(new java.awt.Color(32, 49, 45));
        btnReporte.setText("Descargar reporte PDF");
        btnReporte.setBorderPainted(false);
        btnReporte.setFocusPainted(false);
        btnReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReporteActionPerformed(evt);
            }
        });

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
                            .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblPeriodo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnActualizar))
                    .addComponent(pnlKpis, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                    .addComponent(pnlGrafico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addComponent(btnReporte, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnVolver, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                    .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPeriodo)
                        .addComponent(cmbPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(pnlKpis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlGrafico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReporte, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGap(20, 20, 20)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, 850, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Panel personalizado que renderiza un gráfico de barras nítido con Graphics2D.
     * Muestra el monto total transferido en Soles (S/) por día y por mes según la Sección 4.10 del Plan.
     */
    private class PanelGraficoBarras extends javax.swing.JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Fondo del gráfico: blanco puro
            g2.setColor(UITheme.BG_WHITE);
            g2.fillRect(0, 0, width, height);

            String periodo = (cmbPeriodo != null && cmbPeriodo.getSelectedItem() != null)
                    ? (String) cmbPeriodo.getSelectedItem()
                    : "Por Día";

            if (estadisticasActuales == null) {
                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString("No hay datos para mostrar.", 70, height / 2);
                g2.dispose();
                return;
            }

            var serie = estadisticasActuales.serie();
            String[] labels = new String[serie.size()];
            double[] valoresSoles = new double[serie.size()];
            for (int i = 0; i < serie.size(); i++) {
                labels[i] = serie.get(i).etiqueta();
                valoresSoles[i] = serie.get(i).monto().doubleValue();
            }

            Color barColor;
            String serieNombre;

            if ("Por Mes".equals(periodo)) {
                barColor = UITheme.AMARILLO_KAPIKUA; // Amarillo de la marca (#FFE014)
                serieNombre = "Monto mensual (S/)";
            } else {
                barColor = UITheme.VERDE_KAPIKUA; // Verde de la marca (#027B71)
                serieNombre = "Monto diario (S/)";
            }

            double maxVal = 0;
            for (double v : valoresSoles) {
                if (v > maxVal) maxVal = v;
            }
            if (maxVal <= 0) maxVal = 1000.0;
            double escalaMax = Math.ceil(maxVal / 1000.0) * 1000.0;

            int marginX = 70;
            int marginTop = 45;
            int marginBottom = 45;
            int plotHeight = height - marginTop - marginBottom;
            int plotWidth = width - (marginX * 2);

            // Líneas horizontales de guía y etiquetas en Soles
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fmA = g2.getFontMetrics();
            for (int i = 0; i <= 4; i++) {
                double valGuia = escalaMax * (4 - i) / 4.0;
                int y = marginTop + (int) (plotHeight * (i / 4.0));

                g2.setColor(UITheme.BORDER_SUBTLE);
                g2.drawLine(marginX, y, width - marginX, y);

                g2.setColor(UITheme.TEXT_SECONDARY);
                String yLabel = String.format(java.util.Locale.US, "S/ %,.0f", valGuia);
                g2.drawString(yLabel, marginX - fmA.stringWidth(yLabel) - 8, y + 4);
            }

            int n = valoresSoles.length;
            int barWidth = Math.min(48, plotWidth / (n * 2));
            int gap = plotWidth / n;

            for (int i = 0; i < n; i++) {
                int barHeight = (int) ((valoresSoles[i] / escalaMax) * plotHeight);
                int x = marginX + (i * gap) + (gap - barWidth) / 2;
                int y = height - marginBottom - barHeight;

                // Barra sólida con borde nítido
                g2.setColor(barColor);
                g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);
                if ("Por Mes".equals(periodo)) {
                    g2.setColor(new Color(0xD4, 0xB8, 0x00));
                    g2.drawRoundRect(x, y, barWidth, barHeight, 6, 6);
                } else {
                    g2.setColor(new Color(0x01, 0x5D, 0x55));
                    g2.drawRoundRect(x, y, barWidth, barHeight, 6, 6);
                }

                // Monto encima de la barra
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String valStr = String.format(java.util.Locale.US, "S/ %,.0f", valoresSoles[i]);
                int valWidth = g2.getFontMetrics().stringWidth(valStr);
                g2.drawString(valStr, x + (barWidth - valWidth) / 2, y - 6);

                // Etiqueta abajo
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                int lblWidth = g2.getFontMetrics().stringWidth(labels[i]);
                g2.drawString(labels[i], x + (barWidth - lblWidth) / 2, height - marginBottom + 20);
            }

            // Título de la serie con aviso explícito de datos de prueba
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("Monto total operado en Soles (" + periodo.toLowerCase() + ")", marginX, 24);

            // Leyenda en la esquina superior derecha
            int legendX = width - marginX - 170;
            int legendY = 14;
            g2.setColor(barColor);
            g2.fillRect(legendX, legendY, 12, 12);
            if ("Por Mes".equals(periodo)) {
                g2.setColor(new Color(0xD4, 0xB8, 0x00));
                g2.drawRect(legendX, legendY, 12, 12);
            } else {
                g2.setColor(new Color(0x01, 0x5D, 0x55));
                g2.drawRect(legendX, legendY, 12, 12);
            }

            g2.setColor(UITheme.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(serieNombre, legendX + 18, legendY + 10);

            // Línea base
            g2.setColor(UITheme.BORDER_SUBTLE);
            g2.drawLine(marginX, height - marginBottom, width - marginX, height - marginBottom);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmGraficosAdmin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnReporte;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> cmbPeriodo;
    private javax.swing.JLabel lblKpi1Titulo;
    private javax.swing.JLabel lblKpi1Valor;
    private javax.swing.JLabel lblKpi2Titulo;
    private javax.swing.JLabel lblKpi2Valor;
    private javax.swing.JLabel lblKpi3Titulo;
    private javax.swing.JLabel lblKpi3Valor;
    private javax.swing.JLabel lblPeriodo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlGrafico;
    private javax.swing.JPanel pnlKpi1;
    private javax.swing.JPanel pnlKpi2;
    private javax.swing.JPanel pnlKpi3;
    private javax.swing.JPanel pnlKpis;
    private javax.swing.JPanel pnlTarjeta;
    // End of variables declaration//GEN-END:variables
}
