package src.main.java.br.com.flint.receiver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Component
public class XmlConverter implements Converter {

  private final XmlMapper xmlMapper = new XmlMapper();
  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public String getFormatName() {
    return "xml";
  }

  @Override
  public List<String> getSupportedContentTypes() {
    return List.of("application/xml", "text/xml");
  }

  @Override
  public JsonNode toStandard(MultipartFile file) throws Exception {
    return xmlMapper.readTree(file.getInputStream());
  }

  @Override
  public ConversionResult fromStandard(JsonNode standardData) throws Exception {
    byte[] xmlData;

    if (standardData.isArray()) {
      ObjectNode wrapper = jsonMapper.createObjectNode();

      wrapper.set("item", standardData);

      xmlData = xmlMapper.writer().withRootName("data").writeValueAsBytes(wrapper);

    } else {
      xmlData = xmlMapper.writeValueAsBytes(standardData);
    }

    return new ConversionResult(xmlData, "application/xml");
  }
}