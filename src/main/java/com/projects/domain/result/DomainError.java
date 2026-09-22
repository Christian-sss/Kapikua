package com.projects.domain.result;


public record DomainError(String code, String message) {

    public static DomainError of(String message) {
        return new DomainError("ERROR_GENERAL", message != null ? message : "");
    }

    public static DomainError of(String code, String message) {
        return new DomainError(code != null ? code : "ERROR", message != null ? message : "");
    }

    public static DomainError saldoInsuficiente(String detalle) {
        return new DomainError(BilleteraError.SALDO_INSUFICIENTE.name(), detalle);
    }

    public static DomainError billeteraInactiva() {
        return new DomainError(BilleteraError.BILLETERA_INACTIVA.name(), "La billetera se encuentra bloqueada o inactiva.");
    }

    public static DomainError limiteExcedido(String detalle) {
        return new DomainError(BilleteraError.LIMITE_EXCEDIDO.name(), detalle);
    }
}