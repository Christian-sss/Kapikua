package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;

public class CreditosPanel extends JPanel {

    private record ProductoEjemplo(String nombre, String tipo, BigDecimal montoMinimo, BigDecimal montoMaximo,
                                   int plazoMinimo, int plazoMaximo, BigDecimal tea) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private static final ProductoEjemplo[] PRODUCTOS_EJEMPLO = {
            new ProductoEjemplo("Crédito Personal Kapikua", "CREDITO_PERSONAL",
                    new BigDecimal("500"), new BigDecimal("20000"), 3, 36, new BigDecimal("45.00")),
            new ProductoEjemplo("Microcrédito Kapikua", "MICROCREDITO",
                    new BigDecimal("100"), new BigDecimal("3000"), 1, 12, new BigDecimal("60.00")),
            new ProductoEjemplo("Microcrédito Express", "MICROCREDITO",
                    new BigDecimal("100"), new BigDecimal("1500"), 1, 6, new BigDecimal("70.00")),
    };

    private static final String[] COLUMNAS_PRESTAMOS = {"Producto", "Monto", "Saldo capital", "Estado", "Próximo vencimiento"};
    private static final Object[][] PRESTAMOS_EJEMPLO = {
            {"Microcrédito Kapikua", "S/ 800.00", "S/ 553.94", "ACTIVO", "10/10/2026"},
            {"Microcrédito Express", "S/ 500.00", "S/ 0.00", "PAGADO", "—"},
    };

    private static final String[] COLUMNAS_CUOTAS = {"N°", "Vencimiento", "Cuota", "Capital", "Interés", "Estado"};
    private static final Object[][][] CRONOGRAMAS_EJEMPLO = {
            {
                    {1, "10/08/2026", "S/ 152.58", "S/ 120.62", "S/ 31.96", "PAGADA"},
                    {2, "10/09/2026", "S/ 152.58", "S/ 125.44", "S/ 27.14", "PAGADA"},
                    {3, "10/10/2026", "S/ 152.58", "S/ 130.45", "S/ 22.13", "PENDIENTE"},
                    {4, "10/11/2026", "S/ 152.58", "S/ 135.66", "S/ 16.92", "PENDIENTE"},
                    {5, "10/12/2026", "S/ 152.58", "S/ 141.08", "S/ 11.50", "PENDIENTE"},
                    {6, "10/01/2027", "S/ 152.58", "S/ 146.75", "S/ 5.83", "PENDIENTE"},
            },
            {
                    {1, "10/06/2026", "S/ 181.96", "S/ 159.35", "S/ 22.61", "PAGADA"},
                    {2, "10/07/2026", "S/ 181.96", "S/ 166.56", "S/ 15.40", "PAGADA"},
                    {3, "10/08/2026", "S/ 181.96", "S/ 174.09", "S/ 7.87", "PAGADA"},
            },
    };

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JComboBox<ProductoEjemplo> comboProducto = new JComboBox<>(PRODUCTOS_EJEMPLO);
    private final JLabel etiquetaDetalleProducto = Estilos.secundario(" ");
    private final JTextField campoMonto = new JTextField(12);
    private final JSpinner spinnerPlazo = new JSpinner();
    private final JButton botonSolicitar = Estilos.botonPrimario("Solicitar");
    private final JLabel etiquetaErrorSimulacion = new JLabel(" ");
    private final JLabel valorCuota = Estilos.titulo("—", 20f);
    private final JLabel valorTem = Estilos.titulo("—", 20f);
    private final JLabel valorIntereses = Estilos.titulo("—", 20f);
    private final JLabel valorTotal = Estilos.titulo("—", 20f);
    private final DefaultTableModel modeloSimulacion = Estilos.modeloNoEditable(
            new String[]{"N°", "Vencimiento", "Cuota", "Interés", "Capital", "Saldo"});

    private final JTable tablaPrestamos = Estilos.tabla(COLUMNAS_PRESTAMOS, PRESTAMOS_EJEMPLO);
    private final DefaultTableModel modeloCuotas = Estilos.modeloNoEditable(COLUMNAS_CUOTAS);
    private final JButton botonPagar = Estilos.botonPrimario("Pagar próxima cuota");

    public CreditosPanel(Runnable onVolver) {
        super(new BorderLayout(0, 16));
        setBorder(Estilos.margen(24));

        var volver = new JButton("← Volver");
        volver.addActionListener(e -> onVolver.run());
        var encabezado = new JPanel(new BorderLayout());
        encabezado.add(Estilos.titulo("Créditos", 22f), BorderLayout.WEST);
        encabezado.add(volver, BorderLayout.EAST);

        var pestanias = new JTabbedPane();
        pestanias.addTab("Solicitar crédito", crearPestaniaSolicitar());
        pestanias.addTab("Mis préstamos", crearPestaniaMisPrestamos());

        add(encabezado, BorderLayout.NORTH);
        add(pestanias, BorderLayout.CENTER);
    }

    private JPanel crearPestaniaSolicitar() {
        var formulario = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;

        comboProducto.addActionListener(e -> alSeleccionarProducto());
        etiquetaErrorSimulacion.setForeground(Estilos.PELIGRO);

        var simular = new JButton("Simular");
        simular.addActionListener(e -> simular());
        botonSolicitar.setEnabled(false);
        botonSolicitar.addActionListener(e -> solicitar());
        var botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.add(simular);
        botones.add(Box.createHorizontalStrut(8));
        botones.add(botonSolicitar);

        agregar(formulario, gbc, new JLabel("Producto:"), 0, 0, 1);
        agregar(formulario, gbc, comboProducto, 1, 0, 1);
        agregar(formulario, gbc, etiquetaDetalleProducto, 1, 1, 1);
        agregar(formulario, gbc, new JLabel("Monto (S/):"), 0, 2, 1);
        agregar(formulario, gbc, campoMonto, 1, 2, 1);
        agregar(formulario, gbc, new JLabel("Plazo (meses):"), 0, 3, 1);
        agregar(formulario, gbc, spinnerPlazo, 1, 3, 1);
        agregar(formulario, gbc, botones, 1, 4, 1);
        agregar(formulario, gbc, etiquetaErrorSimulacion, 1, 5, 1);

        var resumen = new JPanel(new GridLayout(1, 4, 12, 0));
        resumen.add(Estilos.tarjeta("Cuota mensual", valorCuota, null));
        resumen.add(Estilos.tarjeta("TEM", valorTem, null));
        resumen.add(Estilos.tarjeta("Total de intereses", valorIntereses, null));
        resumen.add(Estilos.tarjeta("Total a pagar", valorTotal, null));

        var tablaSimulacion = Estilos.tabla(modeloSimulacion);
        Estilos.alinear(tablaSimulacion, Estilos.rendererCentro(), 0, 1);
        Estilos.alinear(tablaSimulacion, Estilos.rendererDerecha(), 2, 3, 4, 5);

        var resultado = new JPanel(new BorderLayout(0, 16));
        resultado.add(resumen, BorderLayout.NORTH);
        resultado.add(Estilos.seccion("Cronograma de pagos", new JScrollPane(tablaSimulacion)), BorderLayout.CENTER);

        var pestania = new JPanel(new BorderLayout(0, 16));
        pestania.setBorder(Estilos.margen(16));
        var formularioAlineado = new JPanel(new BorderLayout());
        formularioAlineado.add(formulario, BorderLayout.WEST);
        pestania.add(formularioAlineado, BorderLayout.NORTH);
        pestania.add(resultado, BorderLayout.CENTER);

        alSeleccionarProducto();
        return pestania;
    }

    private JPanel crearPestaniaMisPrestamos() {
        var tablaCuotas = Estilos.tabla(modeloCuotas);
        Estilos.alinear(tablaCuotas, Estilos.rendererCentro(), 0, 1, 5);
        Estilos.alinear(tablaCuotas, Estilos.rendererDerecha(), 2, 3, 4);
        Estilos.alinear(tablaPrestamos, Estilos.rendererDerecha(), 1, 2);
        Estilos.alinear(tablaPrestamos, Estilos.rendererCentro(), 3, 4);

        tablaPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alSeleccionarPrestamo();
            }
        });

        botonPagar.addActionListener(e -> pagarProximaCuota());
        var barraPago = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barraPago.add(botonPagar);

        var cronograma = new JPanel(new BorderLayout(0, 12));
        cronograma.add(Estilos.seccion("Cronograma", new JScrollPane(tablaCuotas)), BorderLayout.CENTER);
        cronograma.add(barraPago, BorderLayout.SOUTH);

        var division = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                Estilos.seccion("Mis préstamos", new JScrollPane(tablaPrestamos)),
                cronograma);
        division.setResizeWeight(0.35);
        division.setBorder(null);

        var pestania = new JPanel(new BorderLayout());
        pestania.setBorder(Estilos.margen(16));
        pestania.add(division, BorderLayout.CENTER);

        tablaPrestamos.setRowSelectionInterval(0, 0);
        return pestania;
    }

    private void alSeleccionarProducto() {
        var producto = (ProductoEjemplo) comboProducto.getSelectedItem();
        etiquetaDetalleProducto.setText(String.format("%s · %s – %s · %d – %d meses · TEA %s%%",
                producto.tipo(),
                Estilos.soles(producto.montoMinimo()),
                Estilos.soles(producto.montoMaximo()),
                producto.plazoMinimo(),
                producto.plazoMaximo(),
                producto.tea().toPlainString()));
        spinnerPlazo.setModel(new SpinnerNumberModel(producto.plazoMinimo(), producto.plazoMinimo(), producto.plazoMaximo(), 1));
        limpiarSimulacion();
    }

    private void limpiarSimulacion() {
        modeloSimulacion.setRowCount(0);
        valorCuota.setText("—");
        valorTem.setText("—");
        valorIntereses.setText("—");
        valorTotal.setText("—");
        botonSolicitar.setEnabled(false);
        etiquetaErrorSimulacion.setText(" ");
    }

    // Cálculo solo para la vista previa: el real debe vivir en CalculadoraCronograma (dominio).
    private void simular() {
        limpiarSimulacion();
        var producto = (ProductoEjemplo) comboProducto.getSelectedItem();

        BigDecimal monto;
        try {
            monto = new BigDecimal(campoMonto.getText().trim());
        } catch (NumberFormatException ex) {
            etiquetaErrorSimulacion.setText("Ingresa un monto válido.");
            return;
        }
        if (monto.compareTo(producto.montoMinimo()) < 0 || monto.compareTo(producto.montoMaximo()) > 0) {
            etiquetaErrorSimulacion.setText("El monto debe estar entre " + Estilos.soles(producto.montoMinimo())
                    + " y " + Estilos.soles(producto.montoMaximo()) + ".");
            return;
        }

        int plazo = (Integer) spinnerPlazo.getValue();
        double tem = Math.pow(1 + producto.tea().doubleValue() / 100, 1.0 / 12) - 1;
        double capitalInicial = monto.doubleValue();
        double cuota = capitalInicial * tem / (1 - Math.pow(1 + tem, -plazo));

        double saldo = capitalInicial;
        double totalIntereses = 0;
        var hoy = LocalDate.now();
        for (int numero = 1; numero <= plazo; numero++) {
            double interes = saldo * tem;
            double capital = cuota - interes;
            saldo = numero == plazo ? 0 : saldo - capital;
            totalIntereses += interes;
            modeloSimulacion.addRow(new Object[]{
                    numero,
                    hoy.plusMonths(numero).format(FORMATO_FECHA),
                    Estilos.soles(cuota),
                    Estilos.soles(interes),
                    Estilos.soles(capital),
                    Estilos.soles(saldo)
            });
        }

        valorCuota.setText(Estilos.soles(cuota));
        valorTem.setText(String.format(Locale.US, "%.4f%%", tem * 100));
        valorIntereses.setText(Estilos.soles(totalIntereses));
        valorTotal.setText(Estilos.soles(capitalInicial + totalIntereses));
        botonSolicitar.setEnabled(true);
    }

    private void solicitar() {
        var producto = (ProductoEjemplo) comboProducto.getSelectedItem();
        var monto = new BigDecimal(campoMonto.getText().trim());
        int plazo = (Integer) spinnerPlazo.getValue();

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Solicitar " + producto.nombre() + " por " + Estilos.soles(monto) + " a " + plazo + " meses?",
                "Confirmar solicitud", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        var datos = new LinkedHashMap<String, String>();
        datos.put("Producto", producto.nombre());
        datos.put("Monto desembolsado", Estilos.soles(monto));
        datos.put("Plazo", plazo + " meses");
        datos.put("Cuota mensual", valorCuota.getText());
        ComprobanteDialog.mostrar(SwingUtilities.getWindowAncestor(this), "Desembolso de crédito", datos);
    }

    private void alSeleccionarPrestamo() {
        int fila = tablaPrestamos.getSelectedRow();
        modeloCuotas.setRowCount(0);
        if (fila < 0) {
            botonPagar.setEnabled(false);
            return;
        }
        for (var cuota : CRONOGRAMAS_EJEMPLO[fila]) {
            modeloCuotas.addRow(cuota);
        }
        botonPagar.setEnabled("ACTIVO".equals(tablaPrestamos.getValueAt(fila, 3)));
    }

    private void pagarProximaCuota() {
        int fila = tablaPrestamos.getSelectedRow();
        Object[] proxima = null;
        for (var cuota : CRONOGRAMAS_EJEMPLO[fila]) {
            if ("PENDIENTE".equals(cuota[5])) {
                proxima = cuota;
                break;
            }
        }
        if (proxima == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Pagar la cuota N° " + proxima[0] + " por " + proxima[2] + "?",
                "Confirmar pago", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        var datos = new LinkedHashMap<String, String>();
        datos.put("Préstamo", String.valueOf(tablaPrestamos.getValueAt(fila, 0)));
        datos.put("Cuota N°", String.valueOf(proxima[0]));
        datos.put("Monto pagado", String.valueOf(proxima[2]));
        datos.put("Capital", String.valueOf(proxima[3]));
        datos.put("Interés", String.valueOf(proxima[4]));
        ComprobanteDialog.mostrar(SwingUtilities.getWindowAncestor(this), "Pago de cuota", datos);
    }

    private static void agregar(JPanel panel, GridBagConstraints gbc, JComponent componente, int x, int y, int ancho) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = ancho;
        panel.add(componente, gbc);
    }
}
