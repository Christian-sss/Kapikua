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

import java.io.ByteArrayOutputStream;
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

            agregarSeccion(documento, estadisticas.periodo() == PeriodoEstadistica.DIA ? "Volumen por día" : "Volumen por mes");
            var serie = tabla(new float[]{3, 2}, "Período", "Monto");
            for (var punto : estadisticas.serie()) {
                agregarCeldas(serie, punto.etiqueta() + " (" + punto.inicio().format(FORMATO_DIA) + ")",
                        formatearMonto(punto.monto()));
            }
            documento.add(serie);

            agregarSeccion(documento, "Volumen por tipo de operación");
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

        } catch (DocumentException ex) {
            throw new RuntimeException("Error al generar el reporte de estadísticas en PDF", ex);
        }
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
