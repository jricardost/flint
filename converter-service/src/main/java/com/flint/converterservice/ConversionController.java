package com.flint.converterservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.flint.core.*;

@RestController
@RequestMapping("/api")
public class ConversionController {

    private final ConversionService conversionService;
    private static final Logger logger = LoggerFactory.getLogger(ConversionController.class);

    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "converter-service OK!";
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("to") String toFormat) {

        String contentType = file.getContentType();
        logger.info("Nova requisição de conversão: de [{}] para [{}]", contentType, toFormat);

        var fromConverterOpt = conversionService.getConverterByContentType(contentType);
        if (fromConverterOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Formato de arquivo de origem nao suportado: " + contentType);
        }

        var toConverterOpt = conversionService.getConverterByFormatName(toFormat);
        if (toConverterOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Formato de destino nao suportado: " + toFormat);
        }

        try {
            JsonNode standardData = fromConverterOpt.get().toStandard(file);

            logger.info("Formato Padrao Intermediario gerado: {}", standardData.toPrettyString());

            ConversionResult result = toConverterOpt.get().fromStandard(standardData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, result.getContentType())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted." + toFormat + "\"")
                    .body(result.getData());

        } catch (Exception e) {
            logger.error("Erro durante a conversao", e); // Logamos o erro completo também
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocorreu um erro durante a conversao: " + e.getMessage());
        }
    }
}
