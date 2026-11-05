package com.vincu.core_services.utils;

import com.vincu.core_services.users.domain.Specialty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSpecialtyConverter implements Converter<String, Specialty> {

    @Override
    public Specialty convert(String source) {
        return Specialty.fromString(source);
    }
}
