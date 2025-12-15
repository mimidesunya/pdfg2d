package net.zamasoft.pdfg2d.demo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class DemoUtils {
    public static File getResourceFile(String name) throws IOException {
        // Try to load from classpath
        URL url = DemoUtils.class.getClassLoader().getResource(name);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            } catch (IllegalArgumentException e) {
                // URI is not distinct or hierarchical (e.g. inside a JAR)
                // We cannot return a File object for a resource inside a JAR.
                // However, for this demo in an IDE environment, it should be a file.
            }
        }

        // Fallback to local file system relative paths
        String[] paths = {
                "pdfg2d-demo/src/main/resources/" + name,
                "src/main/resources/" + name,
                "src/example/" + name
        };

        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }

        throw new IOException("Resource not found: " + name);
    }

    public static File getOutputDir() {
        File dir = new File("output");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
