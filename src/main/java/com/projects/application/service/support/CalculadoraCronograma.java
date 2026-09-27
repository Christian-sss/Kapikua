package com.projects.application.service.support;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Método francés (cuota fija). La usan SimularCreditoService y SolicitarCreditoService,
 * así el cronograma simulado y el que se guarda en la BD son idénticos.
 */
public class CalculadoraCronograma {

    private static final BigDecimal CIEN = new BigDecimal("100");

    public Cronograma generar(BigDecimal monto, BigDecimal teaPorcentaje, int plazoMeses, LocalDate fechaDesembolso) {

        var tea = teaPorcentaje.divide(CIEN, MathContext.DECIMAL64);

        // BigDecimal no tiene raíz n-ésima: la TEM se calcula en double y no se redondea.
        var tem = new BigDecimal(Math.pow(1 + tea.doubleValue(), 1.0 / 12) - 1, MathContext.DECIMAL64);

        // C = P × TEM × (1 + TEM)^n / ((1 + TEM)^n − 1)
        var factor = BigDecimal.ONE.add(tem).pow(plazoMeses, MathContext.DECIMAL64);
        var cuotaFija = monto.multiply(tem)
                .multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), MathContext.DECIMAL64)
                .setScale(2, RoundingMode.HALF_EVEN);

        var saldo = monto.setScale(2, RoundingMode.HALF_EVEN);
        var totalIntereses = BigDecimal.ZERO.setScale(2);
        List<CuotaProyectada> cuotas = new ArrayList<>(plazoMeses);

        for (int numero = 1; numero <= plazoMeses; numero++) {
            var interes = saldo.multiply(tem).setScale(2, RoundingMode.HALF_EVEN);

            // La última cuota absorbe la diferencia de redondeo y deja el saldo exactamente en cero.
            var capital = (numero == plazoMeses) ? saldo : cuotaFija.subtract(interes);

            saldo = saldo.subtract(capital);
            totalIntereses = totalIntereses.add(interes);

            cuotas.add(new CuotaProyectada(
                    numero,
                    fechaDesembolso.plusMonths(numero),
                    capital.add(interes),
                    interes,
                    capital,
                    saldo
            ));
        }

        return new Cronograma(
                teaPorcentaje,
                tem,
                cuotaFija,
                totalIntereses,
                monto.setScale(2, RoundingMode.HALF_EVEN).add(totalIntereses),
                List.copyOf(cuotas)
        );
    }
}
