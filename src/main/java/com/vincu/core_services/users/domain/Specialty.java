package com.vincu.core_services.users.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Specialty {
    GASFITERO("Gasfitero"),
    ELECTRICISTA("Electricista"),
    PINTURA("Pintura"),
    MAESTRO_DE_OBRAS("Maestro de Obras"),
    TECNICO_ELECTRODOMESTICOS("Técnico de Electrodomésticos"),
    LIMPIEZA("Limpieza");

    private final String value;

    Specialty(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Specialty fromString(String text) {
        for (Specialty specialty : Specialty.values()) {
            if (specialty.value.equalsIgnoreCase(text) || specialty.name().equalsIgnoreCase(text)) {
                return specialty;
            }
        }
        throw new IllegalArgumentException("Especialidad no válida: " + text);
    }
}
