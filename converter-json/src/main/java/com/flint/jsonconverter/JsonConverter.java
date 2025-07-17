package com.flint.jsonconverter;

import com.flint.core.*;
import java.util.List;
import java.io.InputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonConverter implements Converter {

    private final JsonConverterService jsonConverterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonConverter(JsonConverterService jsonConverterService) {
        this.jsonConverterService = jsonConverterService;
    }

    @GetMapping("/alive")
    public Boolean alive() {
        return true;
    }

    @Override
    public String getFormatName() {
        return "json";
    }

    @Override
    public List<String> getSupportedContentTypes() {
        return List.of("application/json");
    }

    @Override
    @PostMapping("/std")
    public JsonNode toStandard(MultipartFile file) throws Exception {
        System.out.println("Arquivo recebido: " + file.getOriginalFilename());

        JsonNode incomingData;
        try (InputStream inputStream = file.getInputStream()) {
            incomingData = objectMapper.readTree(inputStream);
        }
        System.out.println("Dados brutos lidos do arquivo: " + incomingData.toString());

        ObjectNode responseNode = objectMapper.createObjectNode();

        if (incomingData.isArray()) {
            responseNode.set("data", incomingData);
        } else {
            ArrayNode dataArray = objectMapper.createArrayNode();
            dataArray.add(incomingData);
            responseNode.set("data", dataArray);
        }

        System.out.println("Resposta formatada enviada: " + responseNode.toString());
        return responseNode;
    }

    @Override
    @PostMapping("/out")
    public ConversionResult fromStandard(@RequestBody JsonNode standardData) throws Exception {
        byte[] jsonData = objectMapper.writeValueAsBytes(standardData);
        return new ConversionResult(jsonData, "application/json");
    }
}
