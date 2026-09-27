package com.projects.infrastructure.adapter.in.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

final class Estilos {

    static final Color ACENTO = new Color(33, 150, 243);
    static final Color EXITO = new Color(76, 175, 80);
    static final Color PELIGRO = new Color(229, 57, 53);
    static final Color TEXTO_SECUNDARIO = new Color(160, 160, 160);

    private static final DecimalFormat FORMATO_SOLES =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private Estilos() {
    }

    static String soles(BigDecimal monto) {
        return "S/ " + FORMATO_SOLES.format(monto);
    }

    static String soles(double monto) {
        return soles(BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP));
    }

    static JLabel titulo(String texto, float tamanio) {
        var label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(Font.BOLD, tamanio));
        return label;
    }

    static JLabel secundario(String texto) {
        var label = new JLabel(texto);
        label.setForeground(TEXTO_SECUNDARIO);
        return label;
    }

    static JButton botonPrimario(String texto) {
        var boton = new JButton(texto);
        boton.setBackground(ACENTO);
        boton.setForeground(Color.WHITE);
        return boton;
    }

    static EmptyBorder margen(int px) {
        return new EmptyBorder(px, px, px, px);
    }

    static Border bordeTarjeta() {
        var color = UIManager.getColor("Component.borderColor");
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color != null ? color : Color.GRAY),
                margen(12));
    }

    static JPanel tarjeta(String titulo, String valor, String detalle) {
        return tarjeta(titulo, titulo(valor, 20f), detalle);
    }

    static JPanel tarjeta(String titulo, JLabel valor, String detalle) {
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(bordeTarjeta());
        panel.add(secundario(titulo));
        panel.add(Box.createVerticalStrut(6));
        panel.add(valor);
        if (detalle != null) {
            panel.add(Box.createVerticalStrut(4));
            panel.add(secundario(detalle));
        }
        return panel;
    }

    static JPanel seccion(String titulo, JComponent contenido) {
        var panel = new JPanel(new BorderLayout(0, 8));
        panel.add(titulo(titulo, 15f), BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);
        return panel;
    }

    static JTable tabla(String[] columnas, Object[][] filas) {
        return tabla(new DefaultTableModel(filas, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        });
    }

    static JTable tabla(DefaultTableModel modelo) {
        var tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    static DefaultTableModel modeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    static DefaultTableCellRenderer rendererDerecha() {
        var renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    static DefaultTableCellRenderer rendererCentro() {
        var renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    static void alinear(JTable tabla, DefaultTableCellRenderer renderer, int... columnas) {
        for (int columna : columnas) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(renderer);
        }
    }

    static DefaultTableCellRenderer rendererMontoConSigno() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                var componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(RIGHT);
                if (!isSelected) {
                    var texto = String.valueOf(value);
                    setForeground(texto.startsWith("+") ? EXITO
                            : texto.startsWith("-") ? PELIGRO
                            : table.getForeground());
                }
                return componente;
            }
        };
    }

    static String enmascararCuenta(String cuenta) {
        return cuenta.length() > 4 ? "****" + cuenta.substring(cuenta.length() - 4) : cuenta;
    }
}
