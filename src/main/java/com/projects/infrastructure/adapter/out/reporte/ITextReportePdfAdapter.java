package com.projects.infrastructure.adapter.out.reporte;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.projects.application.dto.command.PeriodoEstadistica;
import com.projects.application.dto.response.ComprobanteResponse;
import com.projects.application.dto.response.EstadisticasResponse;
import com.projects.application.port.out.ReportePdfPort;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ITextReportePdfAdapter implements ReportePdfPort {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BaseColor VERDE_KAPIKUA = new BaseColor(2, 123, 113);

    @Override
    public byte[] generarComprobante(ComprobanteResponse comprobante) {

        var salida = new ByteArrayOutputStream();
        var documento = new Document(PageSize.A5);

        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            var fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            var fuenteEtiqueta = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            var fuenteValor = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

            var titulo = new Paragraph("Comprobante KAPIKUA", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(16f);
            documento.add(titulo);

            agregarFila(documento, "N° de operación:", "OP-" + comprobante.transaccionId(), fuenteEtiqueta, fuenteValor);
            agregarFila(documento, "Fecha:", comprobante.fecha().atZoneSameInstant(ZoneId.systemDefault()).format(FORMATO_FECHA), fuenteEtiqueta, fuenteValor);
            agregarFila(documento, "Tipo:", comprobante.tipoCodigo(), fuenteEtiqueta, fuenteValor);
            agregarFila(documento, "Monto:", formatearMonto(comprobante.monto()), fuenteEtiqueta, fuenteValor);
            agregarFila(documento, "Origen:", comprobante.origenNombre(), fuenteEtiqueta, fuenteValor);
            agregarFila(documento, "Destino:", comprobante.destinoNombre(), fuenteEtiqueta, fuenteValor);
            agregarFila(documento, "Estado:", comprobante.estado(), fuenteEtiqueta, fuenteValor);

            if (comprobante.saldoResultanteSolicitante() != null) {
                agregarFila(documento, "Tu saldo resultante:",
                        formatearMonto(comprobante.saldoResultanteSolicitante()), fuenteEtiqueta, fuenteValor);
            }

            documento.close();
            return salida.toByteArray();

        } catch (DocumentException ex) {
            throw new RuntimeException("Error al generar el comprobante en PDF", ex);
        }
    }

    @Override
    public byte[] generarReporteEstadisticas(EstadisticasResponse estadisticas) {

        var salida = new ByteArrayOutputStream();
        var documento = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            var titulo = new Paragraph("Reporte de estadísticas KAPIKUA",
                    new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, VERDE_KAPIKUA));
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            var fuenteDetalle = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
            var periodo = estadisticas.periodo() == PeriodoEstadistica.DIA ? "Últimos 7 días" : "Últimos 6 meses";
            var detalle = new Paragraph(String.format("%s (desde el %s)  ·  Generado el %s",
                    periodo,
                    estadisticas.desde().format(FORMATO_DIA),
                    estadisticas.generadoEn().atZoneSameInstant(ZoneId.systemDefault()).format(FORMATO_FECHA)),
                    fuenteDetalle);
            detalle.setAlignment(Element.ALIGN_CENTER);
            detalle.setSpacingAfter(18f);
            documento.add(detalle);

            agregarSeccion(documento, "Resumen de operaciones del período");
            var resumen = tabla(new float[]{3, 2}, "Indicador", "Valor");
            agregarCeldas(resumen, "Total de transacciones", String.valueOf(estadisticas.totalTransacciones()));
            agregarCeldas(resumen, "Transacciones exitosas", String.valueOf(estadisticas.transaccionesExitosas()));
            agregarCeldas(resumen, "Tasa de éxito", formatearPorcentaje(estadisticas.porcentajeExito()));
            agregarCeldas(resumen, "Volumen operado (exitosas)", formatearMonto(estadisticas.volumenOperado()));
            documento.add(resumen);

            documento.add(bloqueConGrafico(
                    estadisticas.periodo() == PeriodoEstadistica.DIA ? "Volumen por día" : "Volumen por mes",
                    graficoBarras(estadisticas)));
            var serie = tabla(new float[]{3, 2}, "Período", "Monto");
            for (var punto : estadisticas.serie()) {
                agregarCeldas(serie, punto.etiqueta() + " (" + punto.inicio().format(FORMATO_DIA) + ")",
                        formatearMonto(punto.monto()));
            }
            documento.add(serie);

            if (estadisticas.volumenPorTipo().isEmpty()) {
                agregarSeccion(documento, "Volumen por tipo de operación");
            } else {
                documento.add(bloqueConGrafico("Volumen por tipo de operación", graficoTorta(estadisticas)));
            }
            var tipos = tabla(new float[]{3, 1, 2}, "Tipo", "Cantidad", "Monto");
            if (estadisticas.volumenPorTipo().isEmpty()) {
                agregarCeldas(tipos, "Sin operaciones en el período", "-", "-");
            }
            for (var tipo : estadisticas.volumenPorTipo()) {
                agregarCeldas(tipos, tipo.tipo(), String.valueOf(tipo.cantidad()), formatearMonto(tipo.monto()));
            }
            documento.add(tipos);

            var cartera = estadisticas.cartera();
            agregarSeccion(documento, "Cartera de préstamos y morosidad (a la fecha)");
            var tablaCartera = tabla(new float[]{3, 2}, "Indicador", "Valor");
            agregarCeldas(tablaCartera, "Préstamos vigentes (ACTIVO + EN_MORA)", String.valueOf(cartera.prestamosVigentes()));
            agregarCeldas(tablaCartera, "Préstamos en mora", String.valueOf(cartera.prestamosEnMora()));
            agregarCeldas(tablaCartera, "Cartera vigente (saldo de capital)", formatearMonto(cartera.carteraVigente()));
            agregarCeldas(tablaCartera, "Cartera en mora", formatearMonto(cartera.carteraEnMora()));
            agregarCeldas(tablaCartera, "Índice de morosidad", formatearPorcentaje(estadisticas.indiceMorosidad()));
            documento.add(tablaCartera);

            documento.close();
            return salida.toByteArray();

        } catch (DocumentException | IOException ex) {
            throw new RuntimeException("Error al generar el reporte de estadísticas en PDF", ex);
        }
    }

    // Título y gráfico en una sola tabla sin cortes: iText 5 aplaza las imágenes que no caben en la
    // página, y el gráfico terminaría separado de su título (o detrás de la tabla siguiente).
    private PdfPTable bloqueConGrafico(String titulo, com.itextpdf.text.Image grafico) {
        var bloque = new PdfPTable(1);
        bloque.setWidthPercentage(100);
        bloque.setKeepTogether(true);
        bloque.setSpacingBefore(12f);

        var celdaTitulo = new PdfPCell(new Paragraph(titulo, new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD)));
        celdaTitulo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        celdaTitulo.setPaddingLeft(0);
        celdaTitulo.setPaddingBottom(8f);
        bloque.addCell(celdaTitulo);

        var celdaGrafico = new PdfPCell(grafico, false);
        celdaGrafico.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        celdaGrafico.setPaddingBottom(8f);
        bloque.addCell(celdaGrafico);
        return bloque;
    }

    private com.itextpdf.text.Image graficoBarras(EstadisticasResponse estadisticas) throws IOException, DocumentException {
        var datos = new DefaultCategoryDataset();
        for (var punto : estadisticas.serie()) {
            datos.addValue(punto.monto(), "Monto (S/)", punto.etiqueta());
        }
        boolean porDia = estadisticas.periodo() == PeriodoEstadistica.DIA;
        JFreeChart grafico = ChartFactory.createBarChart(null, porDia ? "Día" : "Mes", "Monto (S/)",
                datos, PlotOrientation.VERTICAL, false, true, false);
        grafico.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = grafico.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        // Sin ventas el eje se calcularía sobre un rango de ~0 y mostraría etiquetas como 4E-9.
        boolean hayMontos = estadisticas.serie().stream().anyMatch(punto -> punto.monto().signum() > 0);
        var ejeMontos = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        ejeMontos.setLowerBound(0);
        if (!hayMontos) {
            ejeMontos.setUpperBound(100);
        }
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, new Color(2, 123, 113));
        ((BarRenderer) plot.getRenderer()).setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        return aImagen(grafico, 500, 190);
    }

    private com.itextpdf.text.Image graficoTorta(EstadisticasResponse estadisticas) throws IOException, DocumentException {
        var datos = new DefaultPieDataset();
        for (var tipo : estadisticas.volumenPorTipo()) {
            datos.setValue(tipo.tipo(), tipo.monto());
        }
        JFreeChart grafico = ChartFactory.createPieChart(null, datos, false, true, false);
        grafico.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) grafico.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        Color[] paleta = {new Color(0x2A, 0x78, 0xD6), new Color(0xEB, 0x68, 0x34), new Color(0x1B, 0xAF, 0x7A),
                new Color(0xED, 0xA1, 0x00), new Color(0xE8, 0x7B, 0xA4), new Color(0x00, 0x83, 0x00)};
        int i = 0;
        for (var tipo : estadisticas.volumenPorTipo()) {
            plot.setSectionPaint(tipo.tipo(), paleta[i++ % paleta.length]);
        }
        return aImagen(grafico, 500, 190);
    }

    private com.itextpdf.text.Image aImagen(JFreeChart grafico, int ancho, int alto) throws IOException, DocumentException {
        var png = new ByteArrayOutputStream();
        ImageIO.write(grafico.createBufferedImage(ancho, alto), "png", png);
        var imagen = com.itextpdf.text.Image.getInstance(png.toByteArray());
        imagen.setAlignment(Element.ALIGN_CENTER);
        imagen.scaleToFit(480, 190);
        imagen.setSpacingAfter(8f);
        return imagen;
    }

    private void agregarSeccion(Document documento, String texto) throws DocumentException {
        var seccion = new Paragraph(texto, new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD));
        seccion.setSpacingBefore(12f);
        seccion.setSpacingAfter(8f);
        documento.add(seccion);
    }

    private PdfPTable tabla(float[] anchos, String... encabezados) throws DocumentException {
        var tabla = new PdfPTable(anchos.length);
        tabla.setWidthPercentage(100);
        tabla.setWidths(anchos);
        var fuente = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        for (var encabezado : encabezados) {
            var celda = new PdfPCell(new Phrase(encabezado, fuente));
            celda.setBackgroundColor(VERDE_KAPIKUA);
            celda.setPadding(6f);
            tabla.addCell(celda);
        }
        return tabla;
    }

    private void agregarCeldas(PdfPTable tabla, String... valores) {
        var fuente = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        for (int i = 0; i < valores.length; i++) {
            var celda = new PdfPCell(new Phrase(valores[i], fuente));
            celda.setPadding(5f);
            celda.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            tabla.addCell(celda);
        }
    }

    private String formatearPorcentaje(java.math.BigDecimal valor) {
        return valor == null ? "-" : valor.toPlainString() + " %";
    }

    private void agregarFila(Document documento, String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor)
            throws DocumentException {
        var parrafo = new Paragraph();
        parrafo.add(new Chunk(etiqueta + " ", fuenteEtiqueta));
        parrafo.add(new Chunk(valor != null ? valor : "-", fuenteValor));
        parrafo.setSpacingAfter(6f);
        documento.add(parrafo);
    }

    private String formatearMonto(java.math.BigDecimal monto) {
        return String.format(Locale.US, "S/ %,.2f", monto);
    }
}
