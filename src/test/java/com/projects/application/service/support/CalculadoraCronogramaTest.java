package com.projects.application.service.support;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraCronogramaTest {

    private final CalculadoraCronograma calculadora = new CalculadoraCronograma();

    // Caso de referencia de docs/PLAN_KAPIKUA.md (sección 5.6): P = 1 000, TEA = 40 %, n = 6.
    @Test
    void generaElCronogramaDeReferencia() {
        var cronograma = calculadora.generar(new BigDecimal("1000"), new BigDecimal("40"), 6, LocalDate.of(2026, 1, 15));

        assertEquals(new BigDecimal("0.028436"), cronograma.tem().setScale(6, java.math.RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("183.64"), cronograma.cuotaFija());
        assertEquals(new BigDecimal("101.86"), cronograma.totalIntereses());
        assertEquals(new BigDecimal("1101.86"), cronograma.totalAPagar());

        String[][] esperado = {
                // cuota, interés, capital, saldo
                {"183.64", "28.44", "155.20", "844.80"},
                {"183.64", "24.02", "159.62", "685.18"},
                {"183.64", "19.48", "164.16", "521.02"},
                {"183.64", "14.82", "168.82", "352.20"},
                {"183.64", "10.02", "173.62", "178.58"},
                {"183.66", "5.08", "178.58", "0.00"},
        };

        assertEquals(6, cronograma.cuotas().size());
        for (int i = 0; i < esperado.length; i++) {
            var cuota = cronograma.cuotas().get(i);
            assertEquals(i + 1, cuota.numero());
            assertEquals(new BigDecimal(esperado[i][0]), cuota.cuota(), "cuota " + (i + 1));
            assertEquals(new BigDecimal(esperado[i][1]), cuota.interes(), "interés " + (i + 1));
            assertEquals(new BigDecimal(esperado[i][2]), cuota.capital(), "capital " + (i + 1));
            assertEquals(new BigDecimal(esperado[i][3]), cuota.saldo(), "saldo " + (i + 1));
        }
    }

    @Test
    void venceUnaCuotaPorMesDesdeElDesembolso() {
        var cronograma = calculadora.generar(new BigDecimal("1000"), new BigDecimal("40"), 3, LocalDate.of(2026, 1, 31));

        assertEquals(LocalDate.of(2026, 2, 28), cronograma.cuotas().get(0).fechaVencimiento());
        assertEquals(LocalDate.of(2026, 3, 31), cronograma.cuotas().get(1).fechaVencimiento());
        assertEquals(LocalDate.of(2026, 4, 30), cronograma.cuotas().get(2).fechaVencimiento());
    }

    @Test
    void laSumaDeCapitalesEsElMontoPrestado() {
        var cronograma = calculadora.generar(new BigDecimal("2750.50"), new BigDecimal("60"), 12, LocalDate.of(2026, 9, 26));

        var sumaCapital = cronograma.cuotas().stream()
                .map(CuotaProyectada::capital)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("2750.50"), sumaCapital);
        assertEquals(new BigDecimal("0.00"), cronograma.cuotas().get(11).saldo());
    }
}
