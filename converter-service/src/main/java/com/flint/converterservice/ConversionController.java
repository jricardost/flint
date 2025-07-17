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

    @GetMapping("/convert-ui")
    @ResponseBody
public String convertUi() {
    return """
        <!DOCTYPE html>
        <html lang="pt-br">
        <head>
            <meta charset="UTF-8">
            <title>Conversor de Arquivos</title>
            <style>
                body { font-family: sans-serif; margin: 20px; }
                form { padding: 20px; border: 1px solid #ccc; border-radius: 8px; max-width: 400px; }
                #result { margin-top: 20px; padding: 10px; border-radius: 5px; background-color: #f0f0f0; }
                .success { border: 1px solid green; color: green; }
                .error { border: 1px solid red; color: red; }
            </style>
        </head>
        <body>
            <h1>Conversor de Arquivos Flint</h1>
            <form id="upload-form">
                <div>
                    <label for="file">Selecione o arquivo para converter:</label><br>
                    <input type="file" id="file" name="file" required>
                </div>
                <br>
                <div>
                    <label for="to">Converter para o formato:</label><br>
                    <input type="text" id="to" name="to" placeholder="ex: csv" required>
                </div>
                <br><br>
                <button type="submit">Converter</button>
            </form>

            <div id="result"></div>

            <script>
                const form = document.getElementById('upload-form');
                const resultDiv = document.getElementById('result');

                form.addEventListener('submit', async (event) => {
                    // Previne o comportamento padrão do formulário
                    event.preventDefault();

                    resultDiv.textContent = 'Enviando e convertendo...';
                    resultDiv.className = '';

                    const formData = new FormData(form);

                    try {
                        // AJUSTE FEITO AQUI: A URL agora é "/api/convert"
                        const response = await fetch('/api/convert', {
                            method: 'POST',
                            body: formData
                        });

                        if (response.ok) {
                            // Pega o nome do arquivo do cabeçalho 'content-disposition'
                            const disposition = response.headers.get('content-disposition');
                            const filenameMatch = disposition ? disposition.match(/filename="(.+)"/) : null;
                            const filename = filenameMatch ? filenameMatch[1] : 'converted-file';

                            // Converte a resposta para um Blob (um objeto de arquivo)
                            const blob = await response.blob();
                            
                            // Cria uma URL temporária para o blob
                            const downloadUrl = window.URL.createObjectURL(blob);
                            
                            // Cria um link temporário para iniciar o download
                            const a = document.createElement('a');
                            a.style.display = 'none';
                            a.href = downloadUrl;
                            a.download = filename; // Define o nome do arquivo para download
                            document.body.appendChild(a);
                            
                            a.click(); // Simula o clique no link para iniciar o download
                            
                            // Limpeza
                            window.URL.revokeObjectURL(downloadUrl);
                            a.remove();
                            
                            resultDiv.textContent = `Download do arquivo '${filename}' iniciado com sucesso!`;
                            resultDiv.className = 'success';
                        } else {
                            // Se a resposta for um erro (ex: 404, 500), exibe a mensagem
                            const errorText = await response.text();
                            resultDiv.textContent = `Erro ${response.status}: ${errorText}`;
                            resultDiv.className = 'error';
                        }
                    } catch (error) {
                        // Se houver um erro de rede
                        console.error('Erro na requisição:', error);
                        resultDiv.textContent = 'Erro de rede. Verifique o console para mais detalhes.';
                        resultDiv.className = 'error';
                    }
                });
            </script>
        </body>
        </html>
    """;
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
            System.out.println("qlqrCoisa: \n" + qlqrCoisa + conversionResult.getContentType());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType(conversionResult.getContentType()));
            responseHeaders.setContentDispositionFormData("attachment", "converted." + toFormat);

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(new String(conversionResult.getData(),"UTF-8"));

        } catch (IOException e) {
            logger.error("Error reading file content", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the uploaded file.");
        }
    }
}
