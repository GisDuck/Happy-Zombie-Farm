package game.Happy_Zombie_Farm.idempotency;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(is);
    }

    public String getCachedBodyAsString() throws UnsupportedEncodingException {
        return new String(cachedBody, getCharacterEncodingOrUtf8());
    }

    private String getCharacterEncodingOrUtf8() {
        return getCharacterEncoding() != null ? getCharacterEncoding() : StandardCharsets.UTF_8.name();
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override public int read() { return byteStream.read(); }
            @Override public boolean isFinished() { return byteStream.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener readListener) {}
        };
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncodingOrUtf8()));
    }
}
