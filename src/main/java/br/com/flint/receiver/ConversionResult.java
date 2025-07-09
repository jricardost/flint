package src.main.java.br.com.flint.receiver;

public class ConversionResult {
    private final byte[] data;
    private final String contentType;

    public ConversionResult(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}
