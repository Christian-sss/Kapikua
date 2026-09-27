package com.projects.infrastructure.adapter.in.swing;

import com.formdev.flatlaf.FlatDarkLaf;
import com.projects.application.port.in.CerrarSesionUseCase;
import com.projects.infrastructure.config.CompositionRoot;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    public static final String DASHBOARD = "dashboard";
    public static final String CREDITOS = "creditos";
    public static final String ADMIN = "admin";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);
    private final CerrarSesionUseCase cerrarSesionUseCase;

    public VentanaPrincipal() {
        this(CompositionRoot.crearCerrarSesionUseCase());
    }

    public VentanaPrincipal(CerrarSesionUseCase cerrarSesionUseCase) {
        super("Kapikua");
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        contenedor.add(new DashboardPanel(
                () -> new TransferenciaDialog(this).setVisible(true),
                () -> new RetiroDialog(this).setVisible(true),
                () -> mostrar(CREDITOS),
                this::cerrarSesion), DASHBOARD);
        contenedor.add(new CreditosPanel(() -> mostrar(DASHBOARD)), CREDITOS);
        contenedor.add(new AdminPanel(this::cerrarSesion), ADMIN);

        setContentPane(contenedor);
        setJMenuBar(crearMenuVistaPrevia());
        setMinimumSize(new Dimension(1000, 680));
        setSize(1100, 760);
        setLocationRelativeTo(null);
    }

    public void mostrar(String pantalla) {
        cardLayout.show(contenedor, pantalla);
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Deseas cerrar sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        cerrarSesionUseCase.ejecutar();
        dispose();
        new Login().setVisible(true);
    }

    private JMenuBar crearMenuVistaPrevia() {
        var menu = new JMenu("Vista previa");
        menu.add(itemMenu("Dashboard (cliente)", () -> mostrar(DASHBOARD)));
        menu.add(itemMenu("Créditos", () -> mostrar(CREDITOS)));
        menu.add(itemMenu("Panel de administración", () -> mostrar(ADMIN)));
        menu.addSeparator();
        menu.add(itemMenu("Login", () -> abrirSinCerrarApp(new Login())));
        menu.add(itemMenu("Registro", () -> abrirSinCerrarApp(new RegistroClienteForm())));

        var barra = new JMenuBar();
        barra.add(menu);
        return barra;
    }

    private static JMenuItem itemMenu(String texto, Runnable accion) {
        var item = new JMenuItem(texto);
        item.addActionListener(e -> accion.run());
        return item;
    }

    private static void abrirSinCerrarApp(JFrame ventana) {
        ventana.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        ventana.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
        } catch (Exception ex) {
            System.err.println("No se pudo inicializar el tema visual moderno.");
        }
        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
