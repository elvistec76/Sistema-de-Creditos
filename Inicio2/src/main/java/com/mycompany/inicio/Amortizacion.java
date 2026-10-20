package com.mycompany.inicio;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

/** Plan mensual en Bs: tasa nominal anual / 12; redondeo comercial a centavos. */
public final class Amortizacion {
    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal MAX = new BigDecimal("999999999999.99");
    public record CuotaPlan(int numero, LocalDate vence, BigDecimal capital, BigDecimal interes,
                            BigDecimal monto, BigDecimal saldoCapital) {}
    private Amortizacion() {}

    public static List<CuotaPlan> generar(BigDecimal principal, BigDecimal tasaAnual, int meses,
                                          String tipo, LocalDate desembolso) {
        if (principal == null || principal.signum() <= 0 || principal.compareTo(MAX) > 0
                || principal.stripTrailingZeros().scale() > 2 || tasaAnual == null || tasaAnual.signum() < 0
                || tasaAnual.compareTo(new BigDecimal("999.999")) > 0 || meses < 1 || meses > 1200
                || desembolso == null || !Set.of("FIJA", "GRADUAL").contains(tipo == null ? "" : tipo))
            throw new IllegalArgumentException("Condiciones del cronograma no validas (plazo de 1 a 1200 meses).");
        BigDecimal saldo = dinero(principal);
        BigDecimal i = tasaAnual.divide(new BigDecimal("1200"), MC);
        BigDecimal capitalBase = principal.divide(BigDecimal.valueOf(meses), 2, RoundingMode.DOWN);
        BigDecimal cuotaBase;
        if (i.signum() == 0) cuotaBase = dinero(principal.divide(BigDecimal.valueOf(meses), MC));
        else {
            BigDecimal factor = BigDecimal.ONE.add(i, MC).pow(meses, MC);
            cuotaBase = dinero(principal.multiply(i, MC).multiply(factor, MC)
                    .divide(factor.subtract(BigDecimal.ONE, MC), MC));
        }
        List<CuotaPlan> plan = new ArrayList<>();
        for (int n = 1; n <= meses; n++) {
            BigDecimal interes = dinero(saldo.multiply(i, MC));
            BigDecimal capital = "GRADUAL".equals(tipo) ? capitalBase : cuotaBase.subtract(interes);
            if (n == meses) capital = saldo;
            if (capital.signum() <= 0 || capital.compareTo(saldo) > 0)
                throw new IllegalArgumentException("El monto y plazo producen cuotas sin capital positivo; revisa las condiciones.");
            BigDecimal monto = capital.add(interes);
            if (monto.compareTo(MAX) > 0) throw new IllegalArgumentException("Una cuota supera el importe permitido.");
            saldo = saldo.subtract(capital);
            // Se calcula siempre desde la fecha original: 31 enero -> fin febrero -> 31 marzo.
            plan.add(new CuotaPlan(n, desembolso.plusMonths(n), capital, interes, monto, saldo));
        }
        if (saldo.signum() != 0) throw new IllegalStateException("El cronograma no cancela el capital.");
        return List.copyOf(plan);
    }

    private static BigDecimal dinero(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
}
