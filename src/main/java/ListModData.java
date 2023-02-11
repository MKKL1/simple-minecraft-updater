import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListModData {
    private String sha512;
    private String file_url;
    private String mod_name;
    private String jar_mod_id;
    private String version_number;

    public String getVersion_number() {
        return version_number;
    }

    public String getSha512() {
        return sha512;
    }

    public String getFile_url() {
        return file_url;
    }

    public String getMod_name() {
        return mod_name;
    }

    public String getJar_mod_id() {
        return jar_mod_id;
    }

    public void setFabricModJson(FabricModJson fabricModJson) {
        this.jar_mod_id = fabricModJson.id;
        this.version_number = fabricModJson.version;
        this.mod_name = fabricModJson.name;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }
}
