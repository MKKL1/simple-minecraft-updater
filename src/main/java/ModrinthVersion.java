import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModrinthVersion {
    private String name;
    private String version_number;
    private String id;
    private String project_id;
    private List<ModrinthFile> files;

    public String getName() {
        return name;
    }

    public String getVersion_number() {
        return version_number;
    }

    public String getId() {
        return id;
    }

    public String getProject_id() {
        return project_id;
    }

    public List<ModrinthFile> getFiles() {
        return files;
    }
}

