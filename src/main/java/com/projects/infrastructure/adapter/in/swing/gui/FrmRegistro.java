package com.projects.infrastructure.adapter.in.swing.gui;

import com.projects.application.dto.command.RegistrarClienteCommand;
import com.projects.application.dto.response.ClienteRegistradoResponse;
import com.projects.application.port.in.RegistrarClienteUseCase;
import com.projects.domain.result.Result;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * FrmRegistro - Formulario de registro de nuevos clientes en KAPIKUA.
 * Implementado según la Sección 6.2 del Plan de Implementación.
 * Totalmente compatible con el GUI Builder de NetBeans (FrmRegistro.form y código generado).
 */
public class FrmRegistro extends javax.swing.JFrame {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final RegistrarClienteUseCase registrarClienteUseCase;

    /**
     * Crea un nuevo formulario FrmRegistro
     */
    public FrmRegistro() {
        this(CompositionRoot.crearRegistrarClienteUseCase());
    }

    public FrmRegistro(RegistrarClienteUseCase registrarClienteUseCase) {
        this.registrarClienteUseCase = registrarClienteUseCase;
        initComponents();
        configurarEstilos();
        UITheme.setupWindow(this, "KAPIKUA - Crear cuenta");
    }

    /**
     * Aplica la identidad visual corporativa KAPIKUA definida en UITheme
     */
    private void configurarEstilos() {
        getContentPane().setBackground(UITheme.BG_WHITE);
        pnlFondo.setBackground(UITheme.BG_WHITE);
        UITheme.styleCardPanel(pnlTarjeta);

        UITheme.styleTextField(txtNombres);
        UITheme.styleTextField(txtApellidos);
        UITheme.styleTextField(txtDni);
        UITheme.styleTextField(txtCelular);
        UITheme.styleTextField(txtCorreo);
        UITheme.styleTextField(txtFechaNacimiento);
        UITheme.stylePasswordField(txtContrasena);
        UITheme.stylePasswordField(txtConfirmarContrasena);

        UITheme.stylePrimaryButtonGreen(btnRegistrar);
        UITheme.styleNeutralButton(btnVolver);

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

        lblNombres.setFont(UITheme.FONT_LABEL);
        lblNombres.setForeground(UITheme.TEXT_PRIMARY);
        lblApellidos.setFont(UITheme.FONT_LABEL);
        lblApellidos.setForeground(UITheme.TEXT_PRIMARY);
        lblDni.setFont(UITheme.FONT_LABEL);
        lblDni.setForeground(UITheme.TEXT_PRIMARY);
        lblCelular.setFont(UITheme.FONT_LABEL);
        lblCelular.setForeground(UITheme.TEXT_PRIMARY);
        lblCorreo.setFont(UITheme.FONT_LABEL);
        lblCorreo.setForeground(UITheme.TEXT_PRIMARY);
        lblFechaNacimiento.setFont(UITheme.FONT_LABEL);
        lblFechaNacimiento.setForeground(UITheme.TEXT_PRIMARY);
        lblContrasena.setFont(UITheme.FONT_LABEL);
        lblContrasena.setForeground(UITheme.TEXT_PRIMARY);
        lblConfirmarContrasena.setFont(UITheme.FONT_LABEL);
        lblConfirmarContrasena.setForeground(UITheme.TEXT_PRIMARY);

        lblMensaje.setFont(UITheme.FONT_SMALL);
        lblMensaje.setForeground(UITheme.DANGER);
        lblMensaje.setText(" ");
    }

    /**
     * Valida las reglas de frontend descritas en la Sección 6.2
     */
    public boolean validarCampos() {
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String dni = txtDni.getText().trim();
        String celular = txtCelular.getText().trim();
        String correo = txtCorreo.getText().trim();
        String fechaNacimiento = txtFechaNacimiento.getText().trim();
        String pass = new String(txtContrasena.getPassword());
        String confirmPass = new String(txtConfirmarContrasena.getPassword());

        // 1. Campos obligatorios
        if (nombres.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || celular.isEmpty()
                || correo.isEmpty() || fechaNacimiento.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.", true);
            return false;
        }

        // Solo el formato de la fecha: la mayoría de edad la valida el dominio (Cliente.validarDatos)
        if (leerFechaNacimiento() == null) {
            mostrarMensaje("La fecha de nacimiento debe tener el formato dd/mm/aaaa.", true);
            txtFechaNacimiento.requestFocus();
            return false;
        }

        // 2. DNI: numérico y de 8 dígitos
        if (!dni.matches("^\\d{8}$")) {
            mostrarMensaje("El DNI debe contener exactamente 8 dígitos numéricos.", true);
            txtDni.requestFocus();
            return false;
        }

        // 3. Celular: numérico y de 9 dígitos (inicia típicamente con 9 en Perú)
        if (!celular.matches("^9\\d{8}$") && !celular.matches("^\\d{9}$")) {
            mostrarMensaje("El celular debe contener 9 dígitos numéricos.", true);
            txtCelular.requestFocus();
            return false;
        }

        // 4. Correo electrónico válido
        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            mostrarMensaje("Ingresa un correo electrónico con formato válido.", true);
            txtCorreo.requestFocus();
            return false;
        }

        // 5. Longitud de contraseña
        if (pass.length() < 6) {
            mostrarMensaje("La contraseña debe tener al menos 6 caracteres.", true);
            txtContrasena.requestFocus();
            return false;
        }

        // 6. Contraseñas coinciden
        if (!pass.equals(confirmPass)) {
            mostrarMensaje("Las contraseñas ingresadas no coinciden.", true);
            txtConfirmarContrasena.requestFocus();
            return false;
        }

        limpiarMensaje();
        return true;
    }

    public void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setForeground(esError ? UITheme.DANGER : UITheme.ACCENT_SUCCESS);
        lblMensaje.setText(mensaje);
    }

    public void limpiarMensaje() {
        lblMensaje.setText(" ");
    }

    private LocalDate leerFechaNacimiento() {
        try {
            return LocalDate.parse(txtFechaNacimiento.getText().trim(), FORMATO_FECHA);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public void limpiarCampos() {
        txtNombres.setText("");
        txtApellidos.setText("");
        txtDni.setText("");
        txtCelular.setText("");
        txtCorreo.setText("");
        txtFechaNacimiento.setText("");
        txtContrasena.setText("");
        txtConfirmarContrasena.setText("");
        limpiarMensaje();
    }

    public void setCampos(String nombres, String apellidos, String dni, String celular, String correo, String pass, String confirmPass) {
        txtNombres.setText(nombres != null ? nombres : "");
        txtApellidos.setText(apellidos != null ? apellidos : "");
        txtDni.setText(dni != null ? dni : "");
        txtCelular.setText(celular != null ? celular : "");
        txtCorreo.setText(correo != null ? correo : "");
        txtContrasena.setText(pass != null ? pass : "");
        txtConfirmarContrasena.setText(confirmPass != null ? confirmPass : "");
    }

    /**
     * Acción del botón Registrar
     */
    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {
        if (!validarCampos()) {
            return;
        }

        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        var command = new RegistrarClienteCommand(
                txtCorreo.getText().trim(),
                new String(txtContrasena.getPassword()),
                nombres,
                apellidos,
                txtDni.getText().trim(),
                txtCelular.getText().trim(),
                leerFechaNacimiento()
        );

        btnRegistrar.setEnabled(false);
        mostrarMensaje("Registrando...", false);

        new SwingWorker<Result<ClienteRegistradoResponse>, Void>() {
            @Override
            protected Result<ClienteRegistradoResponse> doInBackground() {
                return registrarClienteUseCase.ejecutar(command);
            }

            @Override
            protected void done() {
                btnRegistrar.setEnabled(true);
                try {
                    var resultado = get();
                    if (resultado.isSuccess()) {
                        mostrarRegistroExitoso(nombres, apellidos, resultado.getValue().orElseThrow());
                    } else {
                        mostrarMensaje(resultado.getError().orElseThrow().message(), true);
                    }
                } catch (Exception ex) {
                    mostrarMensaje("No se pudo conectar con el servidor. Intenta nuevamente.", true);
                }
            }
        }.execute();
    }

    private void mostrarRegistroExitoso(String nombres, String apellidos, ClienteRegistradoResponse registro) {
        JOptionPane.showMessageDialog(
                this,
                String.format("¡Registro completado con éxito para %s %s!\n\n"
                                + "• Correo registrado: %s\n\n"
                                + "Tu billetera KAPIKUA ha sido creada con saldo S/ 0.00.\n"
                                + "Ya puedes iniciar sesión con tu correo.",
                        nombres, apellidos, registro.email()),
                "Registro exitoso - KAPIKUA",
                JOptionPane.INFORMATION_MESSAGE
        );

        FrmLogin login = new FrmLogin();
        login.setCredenciales(registro.email(), "");
        login.setVisible(true);
        this.dispose();
    }

    /**
     * Acción del botón Volver (Navegación Registro -> Login)
     */
    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {
        FrmLogin login = new FrmLogin();
        login.setVisible(true);
        this.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFondo = new javax.swing.JPanel();
        pnlTarjeta = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        lblNombres = new javax.swing.JLabel();
        txtNombres = new javax.swing.JTextField();
        lblApellidos = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        lblDni = new javax.swing.JLabel();
        txtDni = new javax.swing.JTextField();
        lblCelular = new javax.swing.JLabel();
        txtCelular = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        lblFechaNacimiento = new javax.swing.JLabel();
        txtFechaNacimiento = new javax.swing.JTextField();
        lblContrasena = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        lblConfirmarContrasena = new javax.swing.JLabel();
        txtConfirmarContrasena = new javax.swing.JPasswordField();
        lblMensaje = new javax.swing.JLabel();
        btnRegistrar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("KAPIKUA - Crear cuenta");
        setResizable(false);

        pnlFondo.setBackground(new java.awt.Color(255, 255, 255));
        pnlFondo.setPreferredSize(new java.awt.Dimension(520, 720));

        pnlTarjeta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTarjeta.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(2, 123, 113));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/LogoKapikua_small.png"))); // NOI18N
        lblTitulo.setText("Crear Cuenta en KAPIKUA");
        lblTitulo.setIconTextGap(8);

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 115, 109));
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitulo.setText("Ingresa tus datos personales para comenzar");

        lblNombres.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNombres.setForeground(new java.awt.Color(32, 49, 45));
        lblNombres.setText("Nombres:");

        txtNombres.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtNombres.setForeground(new java.awt.Color(32, 49, 45));
        txtNombres.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblApellidos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblApellidos.setForeground(new java.awt.Color(32, 49, 45));
        lblApellidos.setText("Apellidos:");

        txtApellidos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtApellidos.setForeground(new java.awt.Color(32, 49, 45));
        txtApellidos.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblDni.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDni.setForeground(new java.awt.Color(32, 49, 45));
        lblDni.setText("DNI (8 dígitos):");

        txtDni.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtDni.setForeground(new java.awt.Color(32, 49, 45));
        txtDni.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblCelular.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCelular.setForeground(new java.awt.Color(32, 49, 45));
        lblCelular.setText("Celular (9 dígitos):");

        txtCelular.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCelular.setForeground(new java.awt.Color(32, 49, 45));
        txtCelular.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCorreo.setForeground(new java.awt.Color(32, 49, 45));
        lblCorreo.setText("Correo electrónico:");

        txtCorreo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtCorreo.setForeground(new java.awt.Color(32, 49, 45));
        txtCorreo.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblFechaNacimiento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblFechaNacimiento.setForeground(new java.awt.Color(32, 49, 45));
        lblFechaNacimiento.setText("Fecha de nacimiento:");

        txtFechaNacimiento.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtFechaNacimiento.setForeground(new java.awt.Color(32, 49, 45));
        txtFechaNacimiento.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblContrasena.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblContrasena.setForeground(new java.awt.Color(32, 49, 45));
        lblContrasena.setText("Contraseña:");

        txtContrasena.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtContrasena.setForeground(new java.awt.Color(32, 49, 45));
        txtContrasena.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblConfirmarContrasena.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblConfirmarContrasena.setForeground(new java.awt.Color(32, 49, 45));
        lblConfirmarContrasena.setText("Confirmar contraseña:");

        txtConfirmarContrasena.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtConfirmarContrasena.setForeground(new java.awt.Color(32, 49, 45));
        txtConfirmarContrasena.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        lblMensaje.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblMensaje.setForeground(new java.awt.Color(200, 60, 60));
        lblMensaje.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMensaje.setText(" ");

        btnRegistrar.setBackground(new java.awt.Color(2, 123, 113));
        btnRegistrar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRegistrar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrar.setText("REGISTRARME");
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
            }
        });

        btnVolver.setBackground(new java.awt.Color(255, 255, 255));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(32, 49, 45));
        btnVolver.setText("Volver al inicio de sesión");
        btnVolver.setBorder(javax.swing.BorderFactory.createCompoundBorder());
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
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblNombres, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(txtNombres))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblApellidos, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(txtApellidos)))
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblDni, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(txtDni))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCelular, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(txtCelular)))
                    .addComponent(lblCorreo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtCorreo)
                    .addComponent(lblFechaNacimiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtFechaNacimiento)
                    .addGroup(pnlTarjetaLayout.createSequentialGroup()
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblContrasena, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(txtContrasena))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblConfirmarContrasena, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(txtConfirmarContrasena)))
                    .addComponent(lblMensaje, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRegistrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnVolver, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
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
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombres)
                    .addComponent(lblApellidos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombres, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtApellidos, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDni)
                    .addComponent(lblCelular))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCelular, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblCorreo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFechaNacimiento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFechaNacimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblContrasena)
                    .addComponent(lblConfirmarContrasena))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtContrasena, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConfirmarContrasena, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblMensaje)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFondoLayout = new javax.swing.GroupLayout(pnlFondo);
        pnlFondo.setLayout(pnlFondoLayout);
        pnlFondoLayout.setHorizontalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        pnlFondoLayout.setVerticalGroup(
            pnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFondoLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(pnlTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addComponent(pnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Punto de entrada principal para pruebas individuales del formulario
     */
    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Se mantiene el Look and Feel por defecto
        }

        java.awt.EventQueue.invokeLater(() -> {
            new FrmRegistro().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel lblApellidos;
    private javax.swing.JLabel lblCelular;
    private javax.swing.JLabel lblConfirmarContrasena;
    private javax.swing.JLabel lblContrasena;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDni;
    private javax.swing.JLabel lblFechaNacimiento;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblNombres;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlFondo;
    private javax.swing.JPanel pnlTarjeta;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtCelular;
    private javax.swing.JPasswordField txtConfirmarContrasena;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDni;
    private javax.swing.JTextField txtFechaNacimiento;
    private javax.swing.JTextField txtNombres;
    // End of variables declaration//GEN-END:variables
}
