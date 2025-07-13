package src.main.java.br.com.flint.receiver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Component
public class JsonConverter implements Converter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getFormatName() {
        return "json";
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return List.of("application/json");
    }

    @Override
    public JsonNode toStandard(MultipartFile file) throws Exception {
        return objectMapper.readTree(file.getInputStream());
    }

    @Override
    public ConversionResult fromStandard(JsonNode standardData) throws Exception {
        byte[] jsonData = objectMapper.writeValueAsBytes(standardData);
        return new ConversionResult(jsonData, "application/json");
    }
}
