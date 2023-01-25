package modrinth;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModrithUtils {
    private static final Pattern pattern = Pattern.compile(".*/mod/(?<mod>.+)/version/(?<version>.+)");

    public static Matcher getUrlData(String url) {
        return pattern.matcher(url);
    }
}
