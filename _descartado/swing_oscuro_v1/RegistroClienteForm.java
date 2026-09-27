package com.projects.infrastructure.adapter.in.swing;

import com.projects.application.dto.command.RegistrarClienteCommand;
import com.projects.application.port.in.RegistrarClienteUseCase;
import com.projects.domain.result.Result;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class RegistroClienteForm extends JFrame {

    private final RegistrarClienteUseCase registrarClienteUseCase;

    private JTextField campoNombres;
    private JTextField campoApellidos;
    private JTextField campoDni;
    private JTextField campoCelular;
    private JTextField campoEmail;
    private JTextField campoFechaNacimiento;
    private JPasswordField campoPassword;
    private JPasswordField campoConfirmarPassword;
    private JButton botonRegistrar;
    private JLabel etiquetaEstado;

    public RegistroClienteForm() {
        this(CompositionRoot.crearRegistrarClienteUseCase());
    }

    public RegistroClienteForm(RegistrarClienteUseCase registrarClienteUseCase) {
        this.registrarClienteUseCase = registrarClienteUseCase;
        initComponents();
    }

    private void initComponents() {
        setTitle("Registro de cliente - Kapikua");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        var panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campoNombres = new JTextField(20);
        campoApellidos = new JTextField(20);
        campoDni = new JTextField(20);
        campoCelular = new JTextField(20);
        campoEmail = new JTextField(20);
        campoFechaNacimiento = new JTextField(20);
        campoPassword = new JPasswordField(20);
        campoConfirmarPassword = new JPasswordField(20);

        agregarFila(panel, gbc, 0, "Nombres:", campoNombres);
        agregarFila(panel, gbc, 1, "Apellidos:", campoApellidos);
        agregarFila(panel, gbc, 2, "DNI:", campoDni);
        agregarFila(panel, gbc, 3, "Celular:", campoCelular);
        agregarFila(panel, gbc, 4, "Email:", campoEmail);
        agregarFila(panel, gbc, 5, "Fecha de nacimiento (aaaa-mm-dd):", campoFechaNacimiento);
        agregarFila(panel, gbc, 6, "Password:", campoPassword);
        agregarFila(panel, gbc, 7, "Confirmar password:", campoConfirmarPassword);

        botonRegistrar = new JButton("Registrar");
        botonRegistrar.addActionListener(e -> onRegistrar());

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(botonRegistrar, gbc);

        etiquetaEstado = new JLabel(" ");
        etiquetaEstado.setForeground(Color.RED);
        gbc.gridy = 9;
        panel.add(etiquetaEstado, gbc);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(campo, gbc);
    }

    private void onRegistrar() {
        etiquetaEstado.setText(" ");

        char[] password = campoPassword.getPassword();
        char[] confirmarPassword = campoConfirmarPassword.getPassword();

        if (!Arrays.equals(password, confirmarPassword)) {
            etiquetaEstado.setText("Las contraseñas no coinciden.");
            return;
        }

        LocalDate fechaNacimiento;
        try {
            fechaNacimiento = LocalDate.parse(campoFechaNacimiento.getText().trim());
        } catch (DateTimeParseException ex) {
            etiquetaEstado.setText("Fecha de nacimiento inválida, usa el formato aaaa-mm-dd.");
            return;
        }

        var command = new RegistrarClienteCommand(
                campoEmail.getText(),
                new String(password),
                campoNombres.getText(),
                campoApellidos.getText(),
                campoDni.getText(),
                campoCelular.getText(),
                fechaNacimiento
        );

        Arrays.fill(password, '\0');
        Arrays.fill(confirmarPassword, '\0');

        botonRegistrar.setEnabled(false);
        etiquetaEstado.setForeground(Color.GRAY);
        etiquetaEstado.setText("Registrando...");

        new SwingWorker<Result<?>, Void>() {
            @Override
            protected Result<?> doInBackground() {
                return registrarClienteUseCase.ejecutar(command);
            }

            @Override
            protected void done() {
                botonRegistrar.setEnabled(true);
                try {
                    var resultado = get();
                    mostrarResultado(resultado);
                } catch (Exception ex) {
                    etiquetaEstado.setForeground(Color.RED);
                    etiquetaEstado.setText("Error inesperado: " + ex.getCause());
                }
            }
        }.execute();
    }

    private void mostrarResultado(Result<?> resultado) {
        if (resultado.isSuccess()) {
            etiquetaEstado.setForeground(new Color(0, 128, 0));
            etiquetaEstado.setText("Cliente registrado correctamente.");
            JOptionPane.showMessageDialog(this, "Cliente registrado correctamente.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
        } else {
            var error = resultado.getError().orElseThrow();
            etiquetaEstado.setForeground(Color.RED);
            etiquetaEstado.setText("[" + error.code() + "] " + error.message());
        }
    }

    private void limpiarFormulario() {
        campoNombres.setText("");
        campoApellidos.setText("");
        campoDni.setText("");
        campoCelular.setText("");
        campoEmail.setText("");
        campoFechaNacimiento.setText("");
        campoPassword.setText("");
        campoConfirmarPassword.setText("");
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        } catch (Exception ex) {
            System.err.println("No se pudo inicializar el tema visual moderno.");
        }

        EventQueue.invokeLater(() -> new RegistroClienteForm().setVisible(true));
    }
}
