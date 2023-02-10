package modrinth;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.regex.Matcher;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListModData {
    private String url;
    private String sha512;
    private String file_url;
    private String mod_name;
    private String jar_mod_id;
    private String version_id;
    private String version_number;

    public void updateData(ModrinthVersion modrinthVersion) {
        if(version_id == null) version_id = modrinthVersion.getId();
        if(mod_name == null) mod_name = modrinthVersion.getName();
        if(version_number == null) version_number = modrinthVersion.getVersion_number();
        if(modrinthVersion.getFiles().size() == 0) return;
        ModrinthFile modrinthFile = modrinthVersion.getFiles().get(0);
        if(file_url == null) file_url = modrinthFile.getUrl();
        if(sha512 == null) sha512 = modrinthFile.getHashes().get("sha512");
    }

    public String getVersion_number() {
        return version_number;
    }

    public void setVersion_number(String version_number) {
        this.version_number = version_number;
    }

    public String getVersion_id() {
        return version_id;
    }

    public void setVersion_id(String version_id) {
        this.version_id = version_id;
    }

    @JsonGetter("url")
    public String getUrl() {
        return url;
    }

    @JsonSetter("url")
    public void setUrl(String url) {
        if (url != null) {
            Matcher matcher = ModrithUtils.getUrlData(url);
            if (matcher.find()) {
                mod_name = matcher.group("mod");
                version_number = matcher.group("version");
            }
        }
        this.url = url;
    }

    public String getSha512() {
        return sha512;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getMod_name() {
        return mod_name;
    }

    public void setMod_name(String mod_name) {
        this.mod_name = mod_name;
    }

    public String getJar_mod_id() {
        return jar_mod_id;
    }

    public void setJar_mod_id(String jar_mod_id) {
        this.jar_mod_id = jar_mod_id;
    }
}
