package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.IniciarSesionCommand;
import com.projects.application.dto.response.SesionIniciadaResponse;
import com.projects.application.port.in.IniciarSesionUseCase;
import com.projects.domain.result.Result;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.util.Arrays;

/**
 * FrmLogin - Formulario de inicio de sesión de KAPIKUA.
 * Implementado según el Plan de Diseño Visual KAPIKUA (Fondo Blanco).
 * Compatible con el GUI Builder de NetBeans 31 (FrmLogin.form).
 */
public class FrmLogin extends javax.swing.JFrame {

    private final IniciarSesionUseCase iniciarSesionUseCase;

    /**
     * Crea un nuevo formulario FrmLogin
     */
    public FrmLogin() {
        this(CompositionRoot.crearIniciarSesionUseCase());
    }

    public FrmLogin(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
        initComponents();
        configurarEstilos();
        UITheme.setupWindow(this, "KAPIKUA - Iniciar sesión");
    }

    /**
     * Aplica la identidad visual corporativa KAPIKUA definida en UITheme
     */
    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);
        UITheme.styleTextField(txtCorreoDni);
        UITheme.stylePasswordField(txtContrasena);
        UITheme.stylePrimaryButtonGreen(btnIniciarSesion);
        UITheme.styleSecondaryButtonYellow(btnCrearCuenta);
    }

    /**
     * Valida que los campos no se encuentren vacíos antes de procesar el inicio de sesión.
     */
    public boolean validarCampos() {
        String correoDni = txtCorreoDni.getText().trim();
        char[] passwordChars = txtContrasena.getPassword();

        if (correoDni.isEmpty()) {
            mostrarMensaje("Por favor, ingresa tu correo electrónico.", true);
            txtCorreoDni.requestFocus();
            return false;
        }

        if (passwordChars.length == 0) {
            mostrarMensaje("Por favor, ingresa tu contraseña.", true);
            txtContrasena.requestFocus();
            return false;
        }

        limpiarMensaje();
        return true;
    }

    /**
     * Muestra mensajes de estado o error en la interfaz
     */
    public void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setForeground(esError ? UITheme.DANGER : UITheme.ACCENT_SUCCESS);
        lblMensaje.setText(mensaje);
    }

    /**
     * Limpia el mensaje visible
     */
    public void limpiarMensaje() {
        lblMensaje.setText(" ");
    }

    /**
     * Limpia los campos del formulario
     */
    public void limpiarCampos() {
        txtCorreoDni.setText("");
        txtContrasena.setText("");
        limpiarMensaje();
    }

    public void setCredenciales(String user, String pass) {
        txtCorreoDni.setText(user != null ? user : "");
        txtContrasena.setText(pass != null ? pass : "");
    }

    /**
     * Acción del botón Iniciar Sesión
     */
    private void btnIniciarSesionActionPerformed(java.awt.event.ActionEvent evt) {
        if (!validarCampos()) {
            return;
        }

        char[] password = txtContrasena.getPassword();
        var command = new IniciarSesionCommand(txtCorreoDni.getText().trim(), new String(password));
        Arrays.fill(password, '\0');

        btnIniciarSesion.setEnabled(false);
        mostrarMensaje("Verificando credenciales...", false);

        new SwingWorker<Result<SesionIniciadaResponse>, Void>() {
            @Override
            protected Result<SesionIniciadaResponse> doInBackground() {
                return iniciarSesionUseCase.ejecutar(command);
            }

            @Override
            protected void done() {
                btnIniciarSesion.setEnabled(true);
                try {
                    var resultado = get();
                    if (resultado.isSuccess()) {
                        abrirPantallaSegunRol(resultado.getValue().orElseThrow());
                    } else {
                        var error = resultado.getError().orElseThrow();
                        mostrarMensaje(error.message(), true);
                    }
                } catch (Exception ex) {
                    mostrarMensaje("No se pudo conectar con el servidor. Intenta nuevamente.", true);
                }
            }
        }.execute();
    }

    private void abrirPantallaSegunRol(SesionIniciadaResponse sesion) {
        if (sesion.esAdmin()) {
            new FrmAdministrador().setVisible(true);
        } else {
            new FrmBilletera(sesion).setVisible(true);
        }
        this.dispose();
    }

    /**
     * Acción del botón Crear Cuenta (Navegación Login -> Registro)
     */
    private void btnCrearCuentaActionPerformed(java.awt.event.ActionEvent evt) {
        FrmRegistro registro = new FrmRegistro();
        registro.setVisible(true);
        this.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblSeparador = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblCorreoDni = new javax.swing.JLabel();
        txtCorreoDni = new javax.swing.JTextField();
        lblContrasena = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        lblMensaje = new javax.swing.JLabel();
        btnIniciarSesion = new javax.swing.JButton();
        btnCrearCuenta = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Iniciar sesión");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(450, 600));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(228, 235, 231), 1),
            javax.swing.BorderFactory.createEmptyBorder(20, 28, 24, 28)
        ));

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua.png")));
        lblLogo.setPreferredSize(new java.awt.Dimension(80, 80));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 26));
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("KAPIKUA");

        lblSeparador.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSeparador.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/kapikua_separator.png")));

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Tu billetera digital • Iniciar sesión");

        lblCorreoDni.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblCorreoDni.setForeground(new java.awt.Color(32, 49, 45));
        lblCorreoDni.setText("Correo electrónico:");

        txtCorreoDni.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtCorreoDni.setForeground(new java.awt.Color(32, 49, 45));
        txtCorreoDni.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        lblContrasena.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblContrasena.setForeground(new java.awt.Color(32, 49, 45));
        lblContrasena.setText("Contraseña:");

        txtContrasena.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtContrasena.setForeground(new java.awt.Color(32, 49, 45));
        txtContrasena.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new java.awt.Color(184, 199, 192), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        lblMensaje.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblMensaje.setForeground(new java.awt.Color(200, 60, 60));
        lblMensaje.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMensaje.setText(" ");

        btnIniciarSesion.setBackground(new java.awt.Color(2, 123, 113));
        btnIniciarSesion.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnIniciarSesion.setForeground(new java.awt.Color(255, 255, 255));
        btnIniciarSesion.setText("INICIAR SESIÓN");
        btnIniciarSesion.setBorderPainted(false);
        btnIniciarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnIniciarSesion.setFocusPainted(false);
        btnIniciarSesion.setPreferredSize(new java.awt.Dimension(330, 44));
        btnIniciarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarSesionActionPerformed(evt);
            }
        });

        btnCrearCuenta.setBackground(new java.awt.Color(255, 224, 20));
        btnCrearCuenta.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnCrearCuenta.setForeground(new java.awt.Color(32, 49, 45));
        btnCrearCuenta.setText("CREAR CUENTA");
        btnCrearCuenta.setBorderPainted(false);
        btnCrearCuenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCrearCuenta.setFocusPainted(false);
        btnCrearCuenta.setPreferredSize(new java.awt.Dimension(330, 44));
        btnCrearCuenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearCuentaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTarjetaLayout = new javax.swing.GroupLayout(pnlTarjeta);
        pnlTarjeta.setLayout(pnlTarjetaLayout);
        pnlTarjetaLayout.setHorizontalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLogo, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(lblSeparador, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .addComponent(lblCorreoDni, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtCorreoDni)
                    .addComponent(lblContrasena, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtContrasena)
                    .addComponent(lblMensaje, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnIniciarSesion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCrearCuenta, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlTarjetaLayout.setVerticalGroup(
            pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarjetaLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblTitulo)
                .addGap(6, 6, 6)
                .addComponent(lblSeparador, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblSubtitulo)
                .addGap(20, 20, 20)
                .addComponent(lblCorreoDni)
                .addGap(6, 6, 6)
                .addComponent(txtCorreoDni, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblContrasena)
                .addGap(6, 6, 6)
                .addComponent(txtContrasena, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnIniciarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnCrearCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFondoLayout.createSequentialGroup()
                .addContainerGap(35, Short.MAX_VALUE)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFondoLayout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
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

    /**
     * Punto de entrada principal para pruebas individuales del formulario
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmLogin().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCrearCuenta;
    private javax.swing.JButton btnIniciarSesion;
    private javax.swing.JLabel lblContrasena;
    private javax.swing.JLabel lblCorreoDni;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblSeparador;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtCorreoDni;
    // End of variables declaration//GEN-END:variables
}
