package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.ConsultarBilleteraCommand;
import com.projects.application.dto.response.SesionIniciadaResponse;
import com.projects.application.port.in.CerrarSesionUseCase;
import com.projects.application.port.in.ConsultarBilleteraUseCase;
import com.projects.application.port.out.ClienteRepository;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * FrmBilletera - Pantalla principal del cliente en KAPIKUA.
 * Implementado según la Sección 6.3 del Plan de Implementación.
 * Muestra el saludo al cliente, saldo disponible, estado de billetera y accesos a operaciones.
 * Compatible con NetBeans GUI Builder (FrmBilletera.form).
 */
public class FrmBilletera extends javax.swing.JFrame {

    private final SesionIniciadaResponse sesion;
    private final CerrarSesionUseCase cerrarSesionUseCase;
    private final ConsultarBilleteraUseCase consultarBilleteraUseCase;

    // Puente provisional usuarioId -> clienteId (ver CompositionRoot.crearClienteRepository).
    private final Long clienteId;

    private BigDecimal saldo = BigDecimal.ZERO;
    private String estadoBilletera = "ACTIVA";

    public FrmBilletera(SesionIniciadaResponse sesion) {
        this(sesion,
                CompositionRoot.crearCerrarSesionUseCase(),
                CompositionRoot.crearConsultarBilleteraUseCase(),
                CompositionRoot.crearClienteRepository());
    }

    public FrmBilletera(SesionIniciadaResponse sesion, CerrarSesionUseCase cerrarSesionUseCase,
                         ConsultarBilleteraUseCase consultarBilleteraUseCase, ClienteRepository clienteRepository) {
        this.sesion = sesion;
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        this.consultarBilleteraUseCase = consultarBilleteraUseCase;
        this.clienteId = clienteRepository.findByUsuarioId(sesion.usuarioId())
                .map(cliente -> cliente.getId())
                .orElse(null);
        initComponents();
        configurarEstilos();
        cargarBilletera();
        UITheme.setupWindow(this, "KAPIKUA - Mi Billetera");
    }

    /**
     * Aplica los estilos de UITheme a los componentes
     */
    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        pnlEncabezado.setBackground(UITheme.BG_WHITE);

        // Borde inferior con línea de marca bicolor debajo del encabezado
        pnlEncabezado.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 0) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                int lineH = 3;
                int lineY = y + height - lineH;
                int half = width / 2;
                g2.setColor(UITheme.AMARILLO_KAPIKUA);
                g2.fillRect(x, lineY, half, lineH);
                g2.setColor(UITheme.VERDE_KAPIKUA);
                g2.fillRect(x + half, lineY, width - half, lineH);
                g2.dispose();
            }
        });

        // Tarjeta principal de saldo con acento amarillo lateral y fondo blanco
        UITheme.styleHighlightCardPanel(pnlTarjetaSaldo, UITheme.AMARILLO_KAPIKUA);
        UITheme.styleCardPanel(pnlAcciones);

        lblMarca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblMarca.setIconTextGap(8);
        lblMarca.setFont(UITheme.FONT_BRAND);
        lblMarca.setForeground(UITheme.VERDE_PROFUNDO);

        lblMiCuenta.setFont(UITheme.FONT_SUBTITLE);
        lblMiCuenta.setForeground(UITheme.TEXT_SECONDARY);

        lblNombreCliente.setFont(UITheme.FONT_TITLE_LARGE);
        lblNombreCliente.setForeground(UITheme.TEXT_PRIMARY);

        lblTituloSaldo.setFont(UITheme.FONT_SUBTITLE);
        lblTituloSaldo.setForeground(UITheme.TEXT_SECONDARY);

        lblSaldo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblSaldo.setForeground(UITheme.TEXT_PRIMARY);

        lblEstadoBilletera.setFont(UITheme.FONT_SMALL);
        lblEstadoBilletera.setForeground(UITheme.VERDE_PROFUNDO);

        // Acciones: Transferir (Verde principal), Retirar (Neutro), Historial (Amarillo secundario), Créditos (Neutro con borde), Cerrar sesión
        UITheme.stylePrimaryButtonGreen(btnTransferir);
        UITheme.styleNeutralButton(btnRetirar);
        UITheme.styleSecondaryButtonYellow(btnHistorial);
        UITheme.styleNeutralButton(btnCreditos);
        UITheme.styleNeutralButton(btnCerrarSesion);
        btnCerrarSesion.setForeground(UITheme.DANGER);
    }

    /**
     * Consulta el saldo real de la billetera (ConsultarBilleteraUseCase) y refresca la pantalla.
     * Público para que otras pantallas (ej. FrmTransferencia) puedan pedir un refresco al volver.
     */
    public void cargarBilletera() {
        if (clienteId == null) {
            estadoBilletera = "DESCONOCIDO";
            actualizarDatosVisuales();
            return;
        }

        var resultado = consultarBilleteraUseCase.ejecutar(new ConsultarBilleteraCommand(clienteId));

        if (resultado.isSuccess()) {
            var billetera = resultado.getValue().get();
            saldo = billetera.saldo();
            estadoBilletera = billetera.estado();
        }

        actualizarDatosVisuales();
    }

    /**
     * Carga y refresca los datos visibles en pantalla
     */
    public void actualizarDatosVisuales() {
        lblNombreCliente.setText("¡Hola, " + getNombreCliente() + "!");
        lblSaldo.setText(String.format(java.util.Locale.US, "S/ %,.2f", saldo));
        lblEstadoBilletera.setText("● Estado: Billetera " + estadoBilletera);
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public SesionIniciadaResponse getSesion() {
        return sesion;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNombreCliente() {
        var email = sesion.email();
        return email.substring(0, email.indexOf('@'));
    }

    /**
     * Acción Transferir Dinero
     */
    private void btnTransferirActionPerformed(java.awt.event.ActionEvent evt) {
        FrmTransferencia transferencia = new FrmTransferencia(this);
        transferencia.setVisible(true);
        this.setVisible(false);
    }

    /**
     * Acción Retirar Saldo
     */
    private void btnRetirarActionPerformed(java.awt.event.ActionEvent evt) {
        FrmRetiro retiro = new FrmRetiro(this);
        retiro.setVisible(true);
        this.setVisible(false);
    }

    /**
     * Acción Ver Historial
     */
    private void btnHistorialActionPerformed(java.awt.event.ActionEvent evt) {
        FrmHistorial historial = new FrmHistorial(this);
        historial.setVisible(true);
        this.setVisible(false);
    }

    /**
     * Acción Catálogo de Créditos
     */
    private void btnCreditosActionPerformed(java.awt.event.ActionEvent evt) {
        FrmCreditos creditos = new FrmCreditos(this);
        creditos.setVisible(true);
        this.setVisible(false);
    }

    /**
     * Acción Cerrar Sesión
     */
    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas cerrar tu sesión de KAPIKUA?",
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
        pnlEncabezado = new javax.swing.JPanel();
        lblMarca = new javax.swing.JLabel();
        lblMiCuenta = new javax.swing.JLabel();
        lblNombreCliente = new javax.swing.JLabel();
        pnlTarjetaSaldo = new javax.swing.JPanel();
        lblTituloSaldo = new javax.swing.JLabel();
        lblSaldo = new javax.swing.JLabel();
        lblEstadoBilletera = new javax.swing.JLabel();
        pnlAcciones = new javax.swing.JPanel();
        btnTransferir = new javax.swing.JButton();
        btnRetirar = new javax.swing.JButton();
        btnHistorial = new javax.swing.JButton();
        btnCreditos = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Mi Billetera");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(500, 660));

        pnlEncabezado.setBackground(new java.awt.Color(255, 255, 255));

        lblMarca.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblMarca.setForeground(new java.awt.Color(2, 123, 113));
        lblMarca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png")));
        lblMarca.setText("KAPIKUA");
        lblMarca.setIconTextGap(8);

        lblMiCuenta.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblMiCuenta.setForeground(new java.awt.Color(100, 115, 109));
        lblMiCuenta.setText("Mi cuenta personal");

        lblNombreCliente.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblNombreCliente.setForeground(new java.awt.Color(32, 49, 45));
        lblNombreCliente.setText("¡Hola, Cliente!");

        javax.swing.GroupLayout pnlEncabezadoLayout = new javax.swing.GroupLayout(pnlEncabezado);
        pnlEncabezado.setLayout(pnlEncabezadoLayout);
        pnlEncabezadoLayout.setHorizontalGroup(
            pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNombreCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                        .addComponent(lblMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblMiCuenta)))
                .addGap(30, 30, 30))
        );
        pnlEncabezadoLayout.setVerticalGroup(
            pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMarca)
                    .addComponent(lblMiCuenta))
                .addGap(18, 18, 18)
                .addComponent(lblNombreCliente)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pnlTarjetaSaldo.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjetaSaldo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 5, 0, 0, new java.awt.Color(255, 224, 20)),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(228, 235, 231), 1),
                javax.swing.BorderFactory.createEmptyBorder(18, 22, 18, 22)
            )
        ));

        lblTituloSaldo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblTituloSaldo.setForeground(new java.awt.Color(100, 115, 109));
        lblTituloSaldo.setText("Saldo disponible");

        lblSaldo.setFont(new java.awt.Font("Segoe UI", 1, 32));
        lblSaldo.setForeground(new java.awt.Color(32, 49, 45));
        lblSaldo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSaldo.setText("S/ 0.00");

        lblEstadoBilletera.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblEstadoBilletera.setForeground(new java.awt.Color(2, 123, 113));
        lblEstadoBilletera.setText("● Billetera ACTIVA");

        javax.swing.GroupLayout pnlTarjetaSaldoLayout = new javax.swing.GroupLayout(pnlTarjetaSaldo);
        pnlTarjetaSaldo.setLayout(pnlTarjetaSaldoLayout);
        pnlTarjetaSaldoLayout.setHorizontalGroup(
            pnlTarjetaSaldoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaSaldoLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlTarjetaSaldoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloSaldo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSaldo, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(lblEstadoBilletera, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        pnlTarjetaSaldoLayout.setVerticalGroup(
            pnlTarjetaSaldoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaSaldoLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTituloSaldo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEstadoBilletera)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pnlAcciones.setBackground(new java.awt.Color(255, 255, 255));
        pnlAcciones.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));

        btnTransferir.setBackground(new java.awt.Color(2, 123, 113));
        btnTransferir.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnTransferir.setForeground(new java.awt.Color(255, 255, 255));
        btnTransferir.setText("TRANSFERIR DINERO");
        btnTransferir.setBorderPainted(false);
        btnTransferir.setFocusPainted(false);
        btnTransferir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferirActionPerformed(evt);
            }
        });

        btnRetirar.setBackground(new java.awt.Color(255, 255, 255));
        btnRetirar.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnRetirar.setForeground(new java.awt.Color(32, 49, 45));
        btnRetirar.setText("Retirar saldo");
        btnRetirar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnRetirar.setFocusPainted(false);
        btnRetirar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetirarActionPerformed(evt);
            }
        });

        btnHistorial.setBackground(new java.awt.Color(255, 224, 20));
        btnHistorial.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnHistorial.setForeground(new java.awt.Color(32, 49, 45));
        btnHistorial.setText("Ver historial");
        btnHistorial.setBorderPainted(false);
        btnHistorial.setFocusPainted(false);
        btnHistorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistorialActionPerformed(evt);
            }
        });

        btnCreditos.setBackground(new java.awt.Color(255, 255, 255));
        btnCreditos.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnCreditos.setForeground(new java.awt.Color(32, 49, 45));
        btnCreditos.setText("Catálogo de créditos");
        btnCreditos.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnCreditos.setFocusPainted(false);
        btnCreditos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreditosActionPerformed(evt);
            }
        });

        btnCerrarSesion.setBackground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnCerrarSesion.setForeground(new java.awt.Color(200, 60, 60));
        btnCerrarSesion.setText("Cerrar sesión");
        btnCerrarSesion.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(228, 235, 231), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAccionesLayout = new javax.swing.GroupLayout(pnlAcciones);
        pnlAcciones.setLayout(pnlAccionesLayout);
        pnlAccionesLayout.setHorizontalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTransferir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRetirar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlAccionesLayout.createSequentialGroup()
                        .addComponent(btnHistorial, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCreditos, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                    .addComponent(btnCerrarSesion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        pnlAccionesLayout.setVerticalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(btnTransferir, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnRetirar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnHistorial, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreditos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(btnCerrarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlTarjetaSaldo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlAcciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addComponent(pnlEncabezado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlTarjetaSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlAcciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Punto de entrada principal para pruebas del formulario
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmBilletera(new SesionIniciadaResponse(1L, "pabloquispe@upt.pe", 1L, "CLIENTE")).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnCreditos;
    private javax.swing.JButton btnHistorial;
    private javax.swing.JButton btnRetirar;
    private javax.swing.JButton btnTransferir;
    private javax.swing.JLabel lblEstadoBilletera;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblMiCuenta;
    private javax.swing.JLabel lblNombreCliente;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JLabel lblTituloSaldo;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjetaSaldo;
    // End of variables declaration//GEN-END:variables
}
