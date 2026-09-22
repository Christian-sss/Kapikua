package com.projects.domain.model.billetera;
import com.projects.domain.model.NaturalezaTransaccion;

public class TipoTransaccion {
    private Long id;
    private String codigo;
    private NaturalezaTransaccion naturaleza;


    public TipoTransaccion(Long id, NaturalezaTransaccion naturaleza, String codigo) {
        this.id = id;
        this.naturaleza = naturaleza;
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public NaturalezaTransaccion getNaturaleza() {
        return naturaleza;
    }

    public void setNaturaleza(NaturalezaTransaccion naturaleza) {
        this.naturaleza = naturaleza;
    }
}

