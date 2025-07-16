package com.flint.converterservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.flint.core.*;

@RestController
@RequestMapping("/api")
public class ConversionController {

    private final ConversionService conversionService;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ConversionController.class);

    public ConversionController(ConversionService conversionService, RestTemplate restTemplate) {
        this.conversionService = conversionService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/ping")
    public String ping() {
        String response = restTemplate.getForObject("http://converter-csv/ping", String.class);
        return "converter-service OK! - " + response;
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("to") String toFormat) {

        var fileExt = file.getOriginalFilename().split("\\.")[1].toLowerCase();
        logger.info("form [{}] to [{}]", fileExt, toFormat);

        if (restTemplate.getForObject("http://converter-" + fileExt + "/alive", Boolean.class)) {
            System.out.println(fileExt + " service is alive");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Converter for format '" + fileExt + "' not found.");
        }

        if (restTemplate.getForObject("http://converter-" + toFormat + "/alive", Boolean.class)) {
            System.out.println(toFormat + " service is alive");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Converter for format '" + toFormat + "' not found.");
        }

        JsonNode standardData;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            body.add("file", fileAsResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String conversionUrl = "http://converter-" + fileExt + "/std";
            standardData = restTemplate.postForObject(conversionUrl, requestEntity, JsonNode.class);

            if (standardData == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to convert file to standard format.");
            }

            System.out.println("Converted to standard format: " + standardData.toString());

            // return ResponseEntity.ok()
            // .contentType(MediaType.APPLICATION_JSON)
            // .body(standardData);

            String conversionResultUrl = "http://converter-" + toFormat + "/out";
            ConversionResult conversionResult = restTemplate.postForObject(conversionResultUrl, standardData,
                    ConversionResult.class);

            if (conversionResult == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to convert standard data to format '" + toFormat + "'.");
            }

            String qlqrCoisa = new String(conversionResult.getData(),"UTF-8");
            System.out.println("qlqrCoisa: " + qlqrCoisa);

            System.out.println("Converted to " + toFormat + " format: " +
                    conversionResult.getData().toString());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(conversionResult.getContentType()));
            responseHeaders.setContentDispositionFormData("attachment", "converted." + toFormat);

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(new ByteArrayResource(conversionResult.getData().toString().getBytes()));

        } catch (IOException e) {
            logger.error("Error reading file content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the uploaded file.");
        }
    }
}
