import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BuildEmojiIndexTool {

    private static final String NOTO_EMOJI_URL = "https://github.com/googlefonts/noto-emoji/archive/refs/heads/main.zip";

    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/net/zamasoft/pdfg2d/font/emoji");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Check if SVGs exist, if not download
        if (dir.list((d, name) -> name.endsWith(".svg")).length == 0) {
            System.out.println("No SVG files found. Downloading noto-emoji...");
            downloadAndExtract(dir);
        } else {
            System.out.println("SVG files already exist. Skipping download.");
        }

        File indexFile = new File(dir, "INDEX");
        System.out.println("Generating INDEX file at: " + indexFile.getAbsolutePath());

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(indexFile), "ISO8859-1"))) {
            Set<String> codes = new HashSet<String>();
            String[] list = dir.list();
            if (list == null) {
                System.err.println("Error: Directory list is null. Path: " + dir.getAbsolutePath());
                return;
            }

            for (String code : list) {
                if (!code.endsWith(".svg")) {
                    continue;
                }
                code = code.substring(7, code.length() - 4);
                for (;;) {
                    if (!codes.contains(code)) {
                        out.println(code);
                        codes.add(code);
                    }
                    int ub = code.lastIndexOf('_');
                    if (ub == -1) {
                        break;
                    }
                    code = code.substring(0, ub);
                }
            }
            out.println("200d");

            for (String code : list) {
                if (!code.endsWith(".svg")) {
                    continue;
                }
                code = code.substring(7, code.length() - 4);
                for (String c : code.split("_")) {
                    if (!codes.contains(c)) {
                        codes.add(c);
                    }
                }
            }
        }
        System.out.println("INDEX generation complete.");
    }

    private static void downloadAndExtract(File destDir) throws IOException {
        File zipFile = new File("build/noto-emoji.zip");
        File buildDir = zipFile.getParentFile();
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }

        if (!zipFile.exists() || "true".equals(System.getenv("FORCE_DOWNLOAD"))) {
            System.out.println("Downloading from " + NOTO_EMOJI_URL);
            URL url = new URL(NOTO_EMOJI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                    OutputStream out = new FileOutputStream(zipFile)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }
        }

        System.out.println("Extracting SVG files...");
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile.toPath())))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    if (name.endsWith("LICENSE") && name.contains("noto-emoji-main")) {
                        File targetFile = new File(destDir, "LICENSE");
                        Files.copy(zis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else if (name.endsWith(".svg") && name.contains("/svg/")) {
                        String fileName = name.substring(name.lastIndexOf('/') + 1);
                        if (fileName.startsWith("emoji_u")) {
                            File targetFile = new File(destDir, fileName);
                            Files.copy(zis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
        System.out.println("Extraction complete.");
    }
}
