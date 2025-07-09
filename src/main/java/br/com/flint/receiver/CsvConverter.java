package src.main.java.br.com.flint.receiver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class CsvConverter implements Converter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getFormatName() {
        return "csv";
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return List.of("text/csv");
    }

    @Override
    public JsonNode toStandard(MultipartFile file) throws IOException, CsvException {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        try (Reader reader = new InputStreamReader(file.getInputStream());
                CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> allRows = csvReader.readAll();
            if (allRows.size() < 2) {
                return arrayNode;
            }

            String[] headers = allRows.get(0);
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                ObjectNode objectNode = objectMapper.createObjectNode();
                for (int j = 0; j < headers.length; j++) {
                    objectNode.put(headers[j], row[j]);
                }
                arrayNode.add(objectNode);
            }
        }
        return arrayNode;
    }

    @Override
    public ConversionResult fromStandard(JsonNode standardData) throws IOException {
        ArrayNode arrayNode = null;

        if (standardData.isArray()) {
            arrayNode = (ArrayNode) standardData;
        }
        else if (standardData.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = standardData.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isArray()) {
                    arrayNode = (ArrayNode) field.getValue();
                    break;
                }
            }
        }

        StringWriter stringWriter = new StringWriter();
        try (CSVWriter writer = new CSVWriter(stringWriter)) {
            if (arrayNode == null || arrayNode.isEmpty()) {
                return new ConversionResult("".getBytes(), "text/csv");
            }

            JsonNode firstNode = arrayNode.get(0);
            List<String> headers = new ArrayList<>();
            firstNode.fieldNames().forEachRemaining(headers::add);
            writer.writeNext(headers.toArray(new String[0]));

            for (JsonNode node : arrayNode) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    values.add(node.get(header).asText());
                }
                writer.writeNext(values.toArray(new String[0]));
            }
        }
        return new ConversionResult(stringWriter.toString().getBytes(), "text/csv");
    }
}
