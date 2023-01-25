package modrinth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModrinthFile {
    private HashMap<String,String> hashes;
    private String url;
    private String filename;
    private int size;

    public HashMap<String, String> getHashes() {
        return hashes;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "modrinth.ModrinthFile{" +
                "hashes=" + hashes +
                ", url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                '}';
    }
}
