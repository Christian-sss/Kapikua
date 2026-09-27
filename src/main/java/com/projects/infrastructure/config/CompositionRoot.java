package com.projects.infrastructure.config;

import com.projects.application.port.in.BuscarDestinatarioUseCase;
import com.projects.application.port.in.CerrarSesionUseCase;
import com.projects.application.port.in.ConsultarBilleteraUseCase;
import com.projects.application.port.in.ConsultarCronogramaUseCase;
import com.projects.application.port.in.ConsultarEstadisticasUseCase;
import com.projects.application.port.in.GenerarReporteEstadisticasUseCase;
import com.projects.application.port.in.ConsultarMovimientosUseCase;
import com.projects.application.port.in.ConsultarPrestamosUseCase;
import com.projects.application.port.in.GenerarComprobanteUseCase;
import com.projects.application.port.in.IniciarSesionUseCase;
import com.projects.application.port.in.ListarProductosUseCase;
import com.projects.application.port.in.PagarCuotaUseCase;
import com.projects.application.port.in.RegistrarClienteUseCase;
import com.projects.application.port.in.RetirarSaldoUseCase;
import com.projects.application.port.in.SimularCreditoUseCase;
import com.projects.application.port.in.SolicitarCreditoUseCase;
import com.projects.application.port.in.TransferirMontoUseCase;
import com.projects.application.port.out.ClienteRepository;
import com.projects.application.port.out.SesionContexto;
import com.projects.application.service.BuscarDestinatarioService;
import com.projects.application.service.CerrarSesionService;
import com.projects.application.service.ConsultarBilleteraService;
import com.projects.application.service.ConsultarCronogramaService;
import com.projects.application.service.ConsultarEstadisticasService;
import com.projects.application.service.GenerarReporteEstadisticasService;
import com.projects.application.service.ConsultarMovimientosService;
import com.projects.application.service.ConsultarPrestamosService;
import com.projects.application.service.GenerarComprobanteService;
import com.projects.application.service.IniciarSesionService;
import com.projects.application.service.ListarProductosService;
import com.projects.application.service.PagarCuotaService;
import com.projects.application.service.RegistroClienteService;
import com.projects.application.service.RetirarSaldoService;
import com.projects.application.service.SimularCreditoService;
import com.projects.application.service.SolicitarCreditoService;
import com.projects.application.service.TransferirService;
import com.projects.application.service.support.CalculadoraCronograma;
import com.projects.infrastructure.adapter.out.persistence.PgBilleteraRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgClienteRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgComprobanteQueryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgCuotaRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgEstadisticasQueryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgHistorialCrediticioQueryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgMovimientoQueryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgMovimientoRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgPagoRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgPrestamoQueryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgPrestamoRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgProductoRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgRolRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgSolicitudCreditoRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgTipoTransaccionRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgTransaccionRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.PgUsuarioRepositoryAdapter;
import com.projects.infrastructure.adapter.out.persistence.TransactionManagerAdapter;
import com.projects.infrastructure.adapter.out.reporte.ITextReportePdfAdapter;
import com.projects.infrastructure.adapter.out.security.PasswordEncoderAdapter;
import com.projects.infrastructure.adapter.out.session.SesionContextoAdapter;

public final class CompositionRoot {

    // Una sola instancia para toda la app: el login y el logout (y, más adelante,
    // cualquier pantalla que necesite saber quién está logueado) deben compartir
    // la misma sesión activa.
    private static final SesionContexto SESION_CONTEXTO = new SesionContextoAdapter();

    private CompositionRoot() {
    }

    public static RegistrarClienteUseCase crearRegistrarClienteUseCase() {
        return new RegistroClienteService(
                new PgUsuarioRepositoryAdapter(),
                new PgClienteRepositoryAdapter(),
                new PasswordEncoderAdapter(),
                new PgBilleteraRepositoryAdapter(),
                new PgRolRepositoryAdapter(),
                new TransactionManagerAdapter()
        );
    }

    public static IniciarSesionUseCase crearIniciarSesionUseCase() {
        return new IniciarSesionService(
                new PgUsuarioRepositoryAdapter(),
                new PgRolRepositoryAdapter(),
                new PasswordEncoderAdapter(),
                SESION_CONTEXTO
        );
    }

    public static CerrarSesionUseCase crearCerrarSesionUseCase() {
        return new CerrarSesionService(SESION_CONTEXTO);
    }

    public static ConsultarBilleteraUseCase crearConsultarBilleteraUseCase() {
        return new ConsultarBilleteraService(new PgBilleteraRepositoryAdapter());
    }

    public static ConsultarMovimientosUseCase crearConsultarMovimientosUseCase() {
        return new ConsultarMovimientosService(new PgBilleteraRepositoryAdapter(), new PgMovimientoQueryAdapter());
    }

    public static BuscarDestinatarioUseCase crearBuscarDestinatarioUseCase() {
        return new BuscarDestinatarioService(new PgClienteRepositoryAdapter());
    }

    public static TransferirMontoUseCase crearTransferirMontoUseCase() {
        return new TransferirService(
                new PgBilleteraRepositoryAdapter(),
                new PgClienteRepositoryAdapter(),
                new PgTransaccionRepositoryAdapter(),
                new PgMovimientoRepositoryAdapter(),
                new PgTipoTransaccionRepositoryAdapter(),
                new TransactionManagerAdapter()
        );
    }

    public static GenerarComprobanteUseCase crearGenerarComprobanteUseCase() {
        return new GenerarComprobanteService(new PgComprobanteQueryAdapter(), new ITextReportePdfAdapter());
    }

    public static RetirarSaldoUseCase crearRetirarSaldoUseCase() {
        return new RetirarSaldoService(
                new PgBilleteraRepositoryAdapter(),
                new PgTransaccionRepositoryAdapter(),
                new PgMovimientoRepositoryAdapter(),
                new PgTipoTransaccionRepositoryAdapter(),
                new TransactionManagerAdapter()
        );
    }

    public static ListarProductosUseCase crearListarProductosUseCase() {
        return new ListarProductosService(new PgProductoRepositoryAdapter());
    }

    public static SimularCreditoUseCase crearSimularCreditoUseCase() {
        return new SimularCreditoService(new PgProductoRepositoryAdapter(), new CalculadoraCronograma());
    }

    public static SolicitarCreditoUseCase crearSolicitarCreditoUseCase() {
        return new SolicitarCreditoService(
                new PgProductoRepositoryAdapter(),
                new PgBilleteraRepositoryAdapter(),
                new PgHistorialCrediticioQueryAdapter(),
                new PgSolicitudCreditoRepositoryAdapter(),
                new PgPrestamoRepositoryAdapter(),
                new PgCuotaRepositoryAdapter(),
                new PgTransaccionRepositoryAdapter(),
                new PgMovimientoRepositoryAdapter(),
                new PgTipoTransaccionRepositoryAdapter(),
                new TransactionManagerAdapter(),
                new CalculadoraCronograma()
        );
    }

    public static ConsultarPrestamosUseCase crearConsultarPrestamosUseCase() {
        return new ConsultarPrestamosService(new PgPrestamoQueryAdapter());
    }

    public static ConsultarCronogramaUseCase crearConsultarCronogramaUseCase() {
        return new ConsultarCronogramaService(new PgPrestamoQueryAdapter());
    }

    public static PagarCuotaUseCase crearPagarCuotaUseCase() {
        return new PagarCuotaService(
                new PgPrestamoQueryAdapter(),
                new PgPrestamoRepositoryAdapter(),
                new PgCuotaRepositoryAdapter(),
                new PgPagoRepositoryAdapter(),
                new PgBilleteraRepositoryAdapter(),
                new PgTransaccionRepositoryAdapter(),
                new PgMovimientoRepositoryAdapter(),
                new PgTipoTransaccionRepositoryAdapter(),
                new TransactionManagerAdapter()
        );
    }

    public static ConsultarEstadisticasUseCase crearConsultarEstadisticasUseCase() {
        return new ConsultarEstadisticasService(new PgEstadisticasQueryAdapter(), SESION_CONTEXTO);
    }

    public static GenerarReporteEstadisticasUseCase crearGenerarReporteEstadisticasUseCase() {
        return new GenerarReporteEstadisticasService(crearConsultarEstadisticasUseCase(), new ITextReportePdfAdapter());
    }

    /**
     * Puente provisional: resuelve el clienteId a partir del usuarioId de la sesión.
     * Cuando la sesión guarde directamente el clienteId (rediseño pendiente), este método deja
     * de ser necesario y las pantallas dejan de depender de ClienteRepository directamente.
     */
    public static ClienteRepository crearClienteRepository() {
        return new PgClienteRepositoryAdapter();
    }
}
