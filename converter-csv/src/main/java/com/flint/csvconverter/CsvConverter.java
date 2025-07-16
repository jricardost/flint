package com.flint.csvconverter;

import com.flint.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
public class CsvConverter implements Converter {

    private final CsvConverterService csvConverterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CsvConverter(CsvConverterService csvConverterService) {
        this.csvConverterService = csvConverterService;
    }

    @GetMapping("/alive")
    public Boolean alive() {
        return true;
    }

    @Override
    public String getFormatName() {
        return "csv";
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return List.of("text/csv");
    }

    @Override
    @PostMapping("/std")
    public JsonNode toStandard(MultipartFile file) throws IOException, CsvException {
        System.out.println("Converting CSV to standard format...");
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

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("data", arrayNode);

        System.out.println("rootNode: " + rootNode.toString());

        return rootNode;
    }

    @Override
    @PostMapping("/out")
    public ConversionResult fromStandard(@RequestBody JsonNode standardData) throws IOException {
        // standardData = {"data":[{"a":"1","b":"2","c":"3"}]}
        System.out.println("Converting standard format to CSV...");
        List<String[]> csvData = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = standardData.get("data").fields();
        List<String> headers = new ArrayList<>();

        // Extract headers
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            headers.add(entry.getKey());
        }
        csvData.add(headers.toArray(new String[0]));

        // Extract rows
        for (JsonNode row : standardData.get("data")) {
            String[] csvRow = new String[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                csvRow[i] = row.get(headers.get(i)).asText();
            }
            csvData.add(csvRow);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            writer.writeAll(csvData);
        }

        return new ConversionResult(outputStream.toByteArray(), "text/csv");
    }
}
