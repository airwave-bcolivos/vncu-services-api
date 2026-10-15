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

    // Le dice a Spring que al devolver JSON, envíe este texto amigable
    @JsonValue
    public String getValue() {
        return value;
    }

    // Permite que Spring acepte el texto exacto desde el request del frontend
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
