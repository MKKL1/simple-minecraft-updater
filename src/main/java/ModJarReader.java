import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModJarReader {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JarInputStream jarInputStream;

    private ModJarReader(InputStream inputStream) throws IOException {
        jarInputStream = new JarInputStream(inputStream);
    }

    public static ModJarReader create(byte[] body) throws IOException {
        return new ModJarReader(new ByteArrayInputStream(body));
    }

    public static ModJarReader create(InputStream inputStream) throws IOException {
        return new ModJarReader(inputStream);
    }

    public FabricModJson getFabricModJson() throws IOException {
        ZipEntry zipEntry = jarInputStream.getNextEntry();
        while(true) {
            if(zipEntry == null) return null;
            if (zipEntry.getName().equals("fabric.mod.json")) break;
            zipEntry = jarInputStream.getNextEntry();
        }
        byte[] bytes = jarInputStream.readAllBytes();
        return objectMapper.readValue(bytes, FabricModJson.class);
    }
}
