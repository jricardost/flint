package src.main.java.com.flint.converterservice;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversionService {

    private final Map<String, Converter> formatNameMap;
    private final Map<String, Converter> contentTypeMap;

    public ConversionService(List<Converter> converters) {
        this.formatNameMap = converters.stream()
                .collect(Collectors.toMap(c -> c.getFormatName().toLowerCase(), Function.identity()));

        this.contentTypeMap = new HashMap<>();
        for (Converter converter : converters) {
            for (String contentType : converter.getSupportedContentTypes()) {
                this.contentTypeMap.put(contentType.toLowerCase(), converter);
            }
        }
    }

    public Optional<Converter> getConverterByFormatName(String format) {
        return Optional.ofNullable(formatNameMap.get(format.toLowerCase()));
    }

    public Optional<Converter> getConverterByContentType(String contentType) {
        return Optional.ofNullable(contentTypeMap.get(contentType.toLowerCase()));
    }
}
