package src.main.java.br.com.flint.receiver;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface Converter {
    String getFormatName();
    List<String> getSupportedContentTypes();
    JsonNode toStandard(MultipartFile file) throws Exception;
    ConversionResult fromStandard(JsonNode standardData) throws Exception;
}
