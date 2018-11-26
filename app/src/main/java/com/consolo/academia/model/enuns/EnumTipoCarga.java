package com.consolo.academia.model.enuns;

public enum EnumTipoCarga {

    PESO(1, "Peso"),
    TEMPO(2, "Tempo");

    public Integer value;
    public String name;

    EnumTipoCarga(Integer value, String name) {
        this.value = value;
        this.name = name;
    }
}
