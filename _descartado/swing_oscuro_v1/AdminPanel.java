package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class AdminPanel extends JPanel {

    private static final String[] COLUMNAS_TOP = {"Cliente", "Celular", "Monto", "Operaciones"};
    private static final Object[][] TOP_ENVIADO_EJEMPLO = {
            {"María López", "*****4322", "S/ 3,420.00", 28},
            {"Carlos Rodríguez", "*****4323", "S/ 2,915.50", 19},
            {"Luis García", "*****4325", "S/ 2,110.00", 22},
            {"Ana Torres", "*****4324", "S/ 1,780.30", 11},
            {"Pablo Quispe", "*****4321", "S/ 1,204.90", 9},
    };
    private static final Object[][] TOP_RECIBIDO_EJEMPLO = {
            {"Luis García", "*****4325", "S/ 3,880.00", 31},
            {"Pablo Quispe", "*****4321", "S/ 2,640.00", 17},
            {"María López", "*****4322", "S/ 2,305.75", 20},
            {"Christian Apaza", "*****2353", "S/ 1,512.00", 8},
            {"Carlos Rodríguez", "*****4323", "S/ 998.40", 6},
    };

    private static final String[] COLUMNAS_TRANSACCIONES = {"Fecha", "Tipo", "Monto", "Origen", "Destino", "Estado"};
    private static final Object[][] TRANSACCIONES_EJEMPLO = {
            {"25/09/2026 18:42", "Transferencia", "S/ 367.08", "Billetera #2", "Billetera #1", "EXITOSA"},
            {"25/09/2026 17:10", "Retiro", "S/ 150.00", "Billetera #5", "BCP ****8841", "EXITOSA"},
            {"25/09/2026 15:33", "Pago de cuota", "S/ 152.58", "Billetera #1", "Préstamo #1", "EXITOSA"},
            {"25/09/2026 11:05", "Transferencia", "S/ 45.00", "Billetera #3", "Billetera #2", "FALLIDA"},
            {"24/09/2026 20:48", "Desembolso de crédito", "S/ 2,500.00", "Préstamo #7", "Billetera #4", "EXITOSA"},
            {"24/09/2026 09:12", "Transferencia", "S/ 80.00", "Billetera #5", "Billetera #3", "EXITOSA"},
    };

    private static final String[] COLUMNAS_CONCILIACION = {"Billetera", "Cliente", "Saldo", "Saldo del último movimiento", "Diferencia"};

    private static final String[] DIAS_EJEMPLO = {"16/09", "17/09", "18/09", "19/09", "20/09", "21/09", "22/09", "23/09", "24/09", "25/09"};
    private static final double[] VOLUMEN_DIARIO_EJEMPLO = {1820, 2410, 1990, 3120, 2750, 1340, 980, 2640, 3010, 2815};

    private static final String[] TIPOS_EJEMPLO = {"Transferencias", "Retiros", "Desembolsos", "Pagos de cuota"};
    private static final double[] VOLUMEN_POR_TIPO_EJEMPLO = {21874.50, 9120.00, 12500.00, 4310.40};

    public AdminPanel(Runnable onCerrarSesion) {
        super(new BorderLayout());

        var contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(Estilos.margen(24));
        agregarBloque(contenido, crearEncabezado(onCerrarSesion));
        agregarBloque(contenido, crearIndicadores());
        agregarBloque(contenido, crearGraficos());
        agregarBloque(contenido, crearTablas());

        var scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private static void agregarBloque(JPanel contenido, JComponent bloque) {
        bloque.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloque.setMaximumSize(new Dimension(Integer.MAX_VALUE, bloque.getPreferredSize().height));
        if (contenido.getComponentCount() > 0) {
            contenido.add(Box.createVerticalStrut(20));
        }
        contenido.add(bloque);
    }

    private JPanel crearEncabezado(Runnable onCerrarSesion) {
        var aplicar = Estilos.botonPrimario("Aplicar");
        var exportarPdf = new JButton("Exportar PDF");
        exportarPdf.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "La exportación a PDF (iText) aún no está implementada.",
                "Pendiente", JOptionPane.INFORMATION_MESSAGE));

        var filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Desde:"));
        filtros.add(new JTextField("2026-08-26", 9));
        filtros.add(new JLabel("Hasta:"));
        filtros.add(new JTextField("2026-09-25", 9));
        filtros.add(aplicar);
        filtros.add(exportarPdf);

        var cerrarSesion = new JButton("Cerrar sesión");
        cerrarSesion.addActionListener(e -> onCerrarSesion.run());

        var titulo = new JPanel(new BorderLayout());
        titulo.add(Estilos.titulo("Panel de administración", 22f), BorderLayout.WEST);
        titulo.add(cerrarSesion, BorderLayout.EAST);

        var encabezado = new JPanel(new BorderLayout(0, 14));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(filtros, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearIndicadores() {
        var indicadores = new JPanel(new GridLayout(2, 3, 12, 12));
        indicadores.add(Estilos.tarjeta("Clientes registrados", "128", "+14 en el rango"));
        indicadores.add(Estilos.tarjeta("Billeteras", "121 activas", "7 bloqueadas"));
        indicadores.add(Estilos.tarjeta("Dinero en el sistema", "S/ 84,560.30", "Suma de todos los saldos"));
        indicadores.add(Estilos.tarjeta("Transferencias", "342", "Volumen: S/ 21,874.50"));
        indicadores.add(Estilos.tarjeta("Monto promedio", "S/ 63.96", "Por transferencia"));
        indicadores.add(Estilos.tarjeta("Retiros", "57", "Volumen: S/ 9,120.00"));
        return indicadores;
    }

    private JPanel crearGraficos() {
        var graficos = new JPanel(new GridLayout(1, 2, 12, 0));
        graficos.add(new GraficoBarras("Volumen diario (S/)", DIAS_EJEMPLO, VOLUMEN_DIARIO_EJEMPLO, false));
        graficos.add(new GraficoBarras("Distribución por tipo (S/)", TIPOS_EJEMPLO, VOLUMEN_POR_TIPO_EJEMPLO, true));
        return graficos;
    }

    private JTabbedPane crearTablas() {
        var top = new JPanel(new GridLayout(1, 2, 12, 0));
        top.setBorder(Estilos.margen(12));
        top.add(Estilos.seccion("Por monto enviado",
                tablaConScroll(COLUMNAS_TOP, TOP_ENVIADO_EJEMPLO, new int[]{2}, new int[]{1, 3})));
        top.add(Estilos.seccion("Por monto recibido",
                tablaConScroll(COLUMNAS_TOP, TOP_RECIBIDO_EJEMPLO, new int[]{2}, new int[]{1, 3})));

        var filtroTipo = new JComboBox<>(new String[]{"Todos", "Transferencia", "Retiro", "Desembolso de crédito", "Pago de cuota"});
        var barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barraFiltro.add(new JLabel("Tipo:"));
        barraFiltro.add(filtroTipo);
        var ultimas = new JPanel(new BorderLayout(0, 8));
        ultimas.setBorder(Estilos.margen(12));
        ultimas.add(barraFiltro, BorderLayout.NORTH);
        ultimas.add(tablaConScroll(COLUMNAS_TRANSACCIONES, TRANSACCIONES_EJEMPLO, new int[]{2}, new int[]{5}),
                BorderLayout.CENTER);

        var sinDiferencias = new JLabel("✔ Sin diferencias: el saldo de cada billetera coincide con su último movimiento.");
        sinDiferencias.setForeground(Estilos.EXITO);
        var conciliacion = new JPanel(new BorderLayout(0, 8));
        conciliacion.setBorder(Estilos.margen(12));
        conciliacion.add(sinDiferencias, BorderLayout.NORTH);
        conciliacion.add(tablaConScroll(COLUMNAS_CONCILIACION, new Object[0][0], new int[]{2, 3, 4}, new int[0]),
                BorderLayout.CENTER);

        var pestanias = new JTabbedPane();
        pestanias.addTab("Top clientes", top);
        pestanias.addTab("Últimas transacciones", ultimas);
        pestanias.addTab("Conciliación", conciliacion);
        pestanias.setPreferredSize(new Dimension(800, 320));
        return pestanias;
    }

    private static JScrollPane tablaConScroll(String[] columnas, Object[][] filas, int[] columnasDerecha, int[] columnasCentro) {
        var tabla = Estilos.tabla(columnas, filas);
        Estilos.alinear(tabla, Estilos.rendererDerecha(), columnasDerecha);
        Estilos.alinear(tabla, Estilos.rendererCentro(), columnasCentro);
        return new JScrollPane(tabla);
    }

    private static class GraficoBarras extends JComponent {

        private final String titulo;
        private final String[] etiquetas;
        private final double[] valores;
        private final boolean horizontal;

        GraficoBarras(String titulo, String[] etiquetas, double[] valores, boolean horizontal) {
            this.titulo = titulo;
            this.etiquetas = etiquetas;
            this.valores = valores;
            this.horizontal = horizontal;
            setBorder(Estilos.bordeTarjeta());
            setPreferredSize(new Dimension(400, 260));
        }

        @Override
        protected void paintComponent(Graphics g) {
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            var colorTexto = UIManager.getColor("Label.foreground");
            var insets = getInsets();
            int izquierda = insets.left;
            int derecha = getWidth() - insets.right;
            int arriba = insets.top;
            int abajo = getHeight() - insets.bottom;

            g2.setColor(colorTexto);
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            g2.drawString(titulo, izquierda, arriba + g2.getFontMetrics().getAscent());

            g2.setFont(getFont().deriveFont(11f));
            var metricas = g2.getFontMetrics();
            double maximo = Arrays.stream(valores).max().orElse(1);
            int inicioBarras = arriba + 28;

            if (horizontal) {
                int anchoEtiqueta = Arrays.stream(etiquetas).mapToInt(metricas::stringWidth).max().orElse(0) + 10;
                int anchoValor = metricas.stringWidth(Estilos.soles(maximo)) + 10;
                int alturaFila = (abajo - inicioBarras) / valores.length;
                for (int i = 0; i < valores.length; i++) {
                    int y = inicioBarras + i * alturaFila;
                    int centroTexto = y + alturaFila / 2 + metricas.getAscent() / 2 - 1;
                    int largo = (int) ((derecha - izquierda - anchoEtiqueta - anchoValor) * valores[i] / maximo);

                    g2.setColor(colorTexto);
                    g2.drawString(etiquetas[i], izquierda, centroTexto);
                    g2.setColor(Estilos.ACENTO);
                    g2.fillRoundRect(izquierda + anchoEtiqueta, y + 6, largo, alturaFila - 12, 6, 6);
                    g2.setColor(colorTexto);
                    g2.drawString(Estilos.soles(valores[i]), izquierda + anchoEtiqueta + largo + 6, centroTexto);
                }
            } else {
                int baseBarras = abajo - metricas.getHeight() - 4;
                int anchoColumna = (derecha - izquierda) / valores.length;
                for (int i = 0; i < valores.length; i++) {
                    int alto = (int) ((baseBarras - inicioBarras) * valores[i] / maximo);
                    int x = izquierda + i * anchoColumna;

                    g2.setColor(Estilos.ACENTO);
                    g2.fillRoundRect(x + anchoColumna / 5, baseBarras - alto, anchoColumna * 3 / 5, alto, 6, 6);
                    g2.setColor(colorTexto);
                    int anchoTexto = metricas.stringWidth(etiquetas[i]);
                    g2.drawString(etiquetas[i], x + (anchoColumna - anchoTexto) / 2, abajo - 2);
                }
            }
            g2.dispose();
        }
    }
}
