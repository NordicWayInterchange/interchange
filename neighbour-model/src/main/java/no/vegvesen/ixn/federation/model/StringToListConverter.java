package no.vegvesen.ixn.federation.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringToListConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        if(strings == null) return "";
        return String.join(",", strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String s) {
        if(s == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(s.split(",")));
    }
}
