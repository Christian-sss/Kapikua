package com.projects.infrastructure.adapter.in.swing.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UITheme - Identidad visual oficial de KAPIKUA (Fondo Blanco Puro).
 * Implementa la paleta cromática, tipografías y componentes acordados en
 * el Plan de Diseño Visual de KAPIKUA (Plan_Diseno_Visual_KAPIKUA_Fondo_Blanco.md).
 */
public final class UITheme {

    private UITheme() {
        // Clase utilitaria y de constantes, no instanciable
    }

    // ==========================================
    // Paleta Oficial KAPIKUA (Fondo Blanco)
    // ==========================================
    /** Fondo obligatorio: Blanco puro #FFFFFF */
    public static final Color BG_WHITE = Color.WHITE;

    /** Principal A: Amarillo KAPIKUA #FFE014 */
    public static final Color AMARILLO_KAPIKUA = new Color(0xFF, 0xE0, 0x14);

    /** Principal B: Verde KAPIKUA #15CC77 */
    public static final Color VERDE_KAPIKUA = new Color(0x15, 0xCC, 0x77);

    /** Complementario A: Dorado del logo #FDB203 */
    public static final Color DORADO_LOGO = new Color(0xFD, 0xB2, 0x03);

    /** Complementario B: Verde profundo del logo #027B71 */
    public static final Color VERDE_PROFUNDO = new Color(0x02, 0x7B, 0x71);

    /** Texto grafito oscuro #20312D */
    public static final Color TEXT_PRIMARY = new Color(0x20, 0x31, 0x2D);

    /** Texto secundario / gris medio #64736D */
    public static final Color TEXT_SECONDARY = new Color(0x64, 0x73, 0x6D);

    /** Borde sutil gris tenue #E4EBE7 */
    public static final Color BORDER_SUBTLE = new Color(0xE4, 0xEB, 0xE7);

    /** Borde visible y nítido para campos de entrada #B8C7C0 */
    public static final Color BORDER_INPUT = new Color(0xB8, 0xC7, 0xC0);

    /** Fondo de acento amarillo pálido #FFF9DE */
    public static final Color BG_ACCENT_AMARILLO = new Color(0xFF, 0xF9, 0xDE);

    /** Fondo de acento verde pálido #E8F9F0 */
    public static final Color BG_ACCENT_VERDE = new Color(0xE8, 0xF9, 0xF0);

    /** Error / rojo discreto #C83C3C */
    public static final Color DANGER = new Color(0xC8, 0x3C, 0x3C);

    // ==========================================
    // Alias de Retrocompatibilidad
    // ==========================================
    public static final Color BG_WINDOW = BG_WHITE;
    public static final Color CARD_BG = BG_WHITE;
    public static final Color CARD_BORDER = BORDER_SUBTLE;
    public static final Color PRIMARY = VERDE_PROFUNDO;
    public static final Color PRIMARY_HOVER = new Color(0x02, 0x63, 0x5B);
    public static final Color PRIMARY_DARK = new Color(0x01, 0x4D, 0x47);
    public static final Color ACCENT_SUCCESS = VERDE_KAPIKUA;
    public static final Color WARNING = DORADO_LOGO;
    public static final Color BORDER_FOCUS = VERDE_PROFUNDO;
    public static final Color TEXT_MUTED = new Color(160, 175, 170);

    // ==========================================
    // Tipografías Estándar (Segoe UI)
    // ==========================================
    private static final String FONT_NAME = "Segoe UI";

    public static final Font FONT_BRAND = new Font(FONT_NAME, Font.BOLD, 24);
    public static final Font FONT_TITLE_LARGE = new Font(FONT_NAME, Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_LABEL = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font(FONT_NAME, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 12);

    // ==========================================
    // Configuración base de Ventanas JFrame
    // ==========================================
    public static void setupWindow(JFrame frame, String title) {
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().setBackground(BG_WHITE);
        frame.setLocationRelativeTo(null);
    }

    // ==========================================
    // Elementos de Marca Oficiales
    // ==========================================

    /**
     * Crea el separador bicolor oficial de KAPIKUA:
     * 50% Amarillo (#FFE014) y 50% Verde (#15CC77).
     */
    public static JComponent createBicolorSeparator() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int half = w / 2;
                g2.setColor(AMARILLO_KAPIKUA);
                g2.fillRect(0, 0, half, h);
                g2.setColor(VERDE_KAPIKUA);
                g2.fillRect(half, 0, w - half, h);
                g2.dispose();
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(100, 3));
        sep.setMinimumSize(new Dimension(20, 3));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        return sep;
    }

    /**
     * Carga el Logotipo oficial original de KAPIKUA (LogoKapikua) escalado a las dimensiones deseadas.
     */
    public static Icon createLogoIcon(int width, int height) {
        try {
            java.net.URL url = UITheme.class.getResource("/images/LogoKapikua_original.png");
            if (url == null) {
                url = UITheme.class.getResource("/images/LogoKapikua.png");
            }
            if (url != null) {
                ImageIcon orig = new ImageIcon(url);
                Image scaled = orig.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ==========================================
    // UI Moderna de Botones Redondeados
    // ==========================================
    public static class ModernRoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color textFg;
        private final Color borderColor;
        private final int cornerRadius;

        public ModernRoundedButtonUI(Color normalBg, Color hoverBg, Color pressedBg, Color textFg, Color borderColor, int cornerRadius) {
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            this.textFg = textFg;
            this.borderColor = borderColor;
            this.cornerRadius = cornerRadius;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setOpaque(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setForeground(textFg);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = b.getWidth();
            int h = b.getHeight();

            Color bg = normalBg;
            ButtonModel model = b.getModel();
            if (model.isPressed()) {
                bg = pressedBg;
            } else if (model.isRollover()) {
                bg = hoverBg;
            }

            // Fondo redondeado moderno
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

            // Borde si existe
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
            }

            g2.dispose();
            super.paint(g, c);
        }
    }

    // ==========================================
    // Estilos de Botones
    // ==========================================

    /**
     * Acción Principal: Verde Profundo (#027B71) con texto blanco y esquinas redondeadas
     */
    public static void stylePrimaryButtonGreen(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setUI(new ModernRoundedButtonUI(
                VERDE_PROFUNDO,
                PRIMARY_HOVER,
                PRIMARY_DARK,
                Color.WHITE,
                null,
                10
        ));
    }

    /**
     * Acción Principal Alternativa: Verde Brillante (#15CC77) con texto grafito oscuro
     */
    public static void stylePrimaryButtonBrightGreen(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setUI(new ModernRoundedButtonUI(
                VERDE_KAPIKUA,
                new Color(0x12, 0xB5, 0x69),
                new Color(0x0E, 0x9A, 0x57),
                TEXT_PRIMARY,
                null,
                10
        ));
    }

    /**
     * Acción Complementaria: Amarillo KAPIKUA (#FFE014) con texto grafito oscuro (#20312D)
     */
    public static void styleSecondaryButtonYellow(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setUI(new ModernRoundedButtonUI(
                AMARILLO_KAPIKUA,
                new Color(0xF5, 0xD6, 0x00),
                new Color(0xE5, 0xC7, 0x00),
                TEXT_PRIMARY,
                null,
                10
        ));
    }

    /**
     * Acción de Cancelación / Retorno / Neutra:
     * Botón blanco con borde tenue (#E4EBE7) y texto grafito oscuro (#20312D).
     */
    public static void styleNeutralButton(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setUI(new ModernRoundedButtonUI(
                Color.WHITE,
                new Color(0xF4, 0xF7, 0xF5),
                new Color(0xE8, 0xEC, 0xE9),
                TEXT_PRIMARY,
                BORDER_SUBTLE,
                10
        ));
    }

    /**
     * Compatibilidad: asigna el botón verde principal por defecto
     */
    public static void stylePrimaryButton(JButton button) {
        stylePrimaryButtonGreen(button);
    }

    /**
     * Compatibilidad: asigna el botón neutro/blanco por defecto
     */
    public static void styleSecondaryButton(JButton button) {
        styleNeutralButton(button);
    }

    public static void styleLinkButton(JButton button) {
        button.setFont(FONT_SUBTITLE);
        button.setForeground(VERDE_PROFUNDO);
        button.setBackground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(VERDE_PROFUNDO);
            }
        });
    }

    // ==========================================
    // Estilos de Campos de Entrada
    // ==========================================
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_REGULAR);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(Color.WHITE);
        textField.setCaretColor(VERDE_PROFUNDO);

        Border standardBorder = new CompoundBorder(
                new LineBorder(BORDER_INPUT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        );
        Border focusBorder = new CompoundBorder(
                new LineBorder(VERDE_PROFUNDO, 2, true),
                new EmptyBorder(7, 11, 7, 11)
        );

        textField.setBorder(standardBorder);

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(focusBorder);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(standardBorder);
            }
        });
    }

    public static void stylePasswordField(JPasswordField passwordField) {
        styleTextField(passwordField);
    }

    // ==========================================
    // Tarjetas y Paneles
    // ==========================================

    /**
     * Tarjeta normal: blanca, borde tenue (#E4EBE7) y padding 20-24px.
     */
    public static void styleCardPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(22, 26, 22, 26)
        ));
    }

    /**
     * Tarjeta destacada: blanca, borde tenue y acento lateral (amarillo o verde).
     */
    public static void styleHighlightCardPanel(JPanel panel, Color accentColor) {
        panel.setBackground(Color.WHITE);
        Color accent = (accentColor != null) ? accentColor : AMARILLO_KAPIKUA;
        panel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                new CompoundBorder(
                        new LineBorder(BORDER_SUBTLE, 1),
                        new EmptyBorder(20, 24, 20, 24)
                )
        ));
    }

    // ==========================================
    // Tablas
    // ==========================================
    public static void styleTableHeader(JTableHeader header) {
        if (header != null) {
            header.setFont(FONT_LABEL);
            header.setBackground(VERDE_PROFUNDO);
            header.setForeground(Color.WHITE);
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(header.getWidth(), 32));
        }
    }

    public static void styleTable(JTable table) {
        if (table == null) return;
        table.setFont(FONT_REGULAR);
        table.setRowHeight(28);
        table.setGridColor(BORDER_SUBTLE);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(BG_ACCENT_VERDE);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        styleTableHeader(table.getTableHeader());
        if (table.getParent() instanceof JViewport viewport) {
            viewport.setBackground(Color.WHITE);
        }
        table.addHierarchyListener(e -> {
            if (table.getParent() instanceof JViewport viewport) {
                viewport.setBackground(Color.WHITE);
            }
        });
    }

    public static javax.swing.table.DefaultTableCellRenderer createCenterRenderer() {
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    public static javax.swing.table.DefaultTableCellRenderer createRightRenderer() {
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    // ==========================================
    // Botones de Tarjeta de Menú con Iconos Vectoriales
    // ==========================================
    public static void styleMenuCardButton(JButton button, Icon icon, Color leftAccent) {
        button.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setIcon(icon);
        button.setIconTextGap(16);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color accent = (leftAccent != null) ? leftAccent : BORDER_SUBTLE;
        button.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new CompoundBorder(
                        new LineBorder(BORDER_SUBTLE, 1),
                        new EmptyBorder(10, 18, 10, 18)
                )
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(0xF8, 0xFA, 0xF9));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
    }

    public static Icon createClientIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                double scale = (double) size / 24.0;
                g2.scale(scale, scale);

                g2.setColor(VERDE_KAPIKUA);
                g2.fillOval(7, 2, 10, 10);
                g2.fillRoundRect(3, 14, 18, 9, 6, 6);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createTransactionIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                double scale = (double) size / 24.0;
                g2.scale(scale, scale);

                g2.setColor(AMARILLO_KAPIKUA);
                g2.fillRoundRect(2, 6, 14, 4, 2, 2);
                Polygon p1 = new Polygon(new int[]{14, 20, 14}, new int[]{2, 8, 14}, 3);
                g2.fillPolygon(p1);

                g2.setColor(DORADO_LOGO);
                g2.fillRoundRect(8, 14, 14, 4, 2, 2);
                Polygon p2 = new Polygon(new int[]{10, 4, 10}, new int[]{10, 16, 22}, 3);
                g2.fillPolygon(p2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }

    public static Icon createChartIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                double scale = (double) size / 24.0;
                g2.scale(scale, scale);

                g2.setColor(VERDE_KAPIKUA);
                g2.fillRoundRect(3, 11, 5, 11, 2, 2);

                g2.setColor(AMARILLO_KAPIKUA);
                g2.fillRoundRect(10, 5, 5, 17, 2, 2);

                g2.setColor(VERDE_PROFUNDO);
                g2.fillRoundRect(17, 2, 5, 20, 2, 2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }
            @Override
            public int getIconHeight() { return size; }
        };
    }
}
