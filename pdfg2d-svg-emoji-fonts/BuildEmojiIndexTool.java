import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BuildEmojiIndexTool {

    private static final String NOTO_EMOJI_URL = "https://github.com/googlefonts/noto-emoji/archive/refs/heads/main.zip";

    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/net/zamasoft/pdfg2d/font/emoji");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File zipFile = new File(dir, "emoji.zip");
        if (zipFile.exists() && zipFile.length() > 0 && !"true".equals(System.getenv("FORCE_DOWNLOAD"))) {
            System.out.println("emoji.zip already exists. Skipping generation.");
            return;
        }

        File downloadFile = new File("build/noto-emoji.zip");
        File buildDir = downloadFile.getParentFile();
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }

        if (!downloadFile.exists() || "true".equals(System.getenv("FORCE_DOWNLOAD"))) {
            System.out.println("Downloading from " + NOTO_EMOJI_URL);
            URL url = new URL(NOTO_EMOJI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                    OutputStream out = new FileOutputStream(downloadFile)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
            }
        }

        System.out.println("Generating emoji.zip...");
        List<String> emojiFiles = new ArrayList<>();
        Set<String> addedEntries = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(downloadFile.toPath())));
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {

            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    if (name.endsWith("LICENSE") && name.contains("noto-emoji-main")) {
                        if (addedEntries.contains("LICENSE"))
                            continue;
                        // Copy license as is
                        ZipEntry newEntry = new ZipEntry("LICENSE");
                        zos.putNextEntry(newEntry);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                        addedEntries.add("LICENSE");
                    } else if (name.endsWith(".svg") && name.contains("/svg/")) {
                        String fileName = name.substring(name.lastIndexOf('/') + 1);
                        if (fileName.startsWith("emoji_u")) {
                            if (addedEntries.contains(fileName))
                                continue;
                            emojiFiles.add(fileName);
                            ZipEntry newEntry = new ZipEntry(fileName);
                            zos.putNextEntry(newEntry);
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();
                            addedEntries.add(fileName);
                        }
                    }
                }
            }

            // Generate INDEX
            System.out.println("Generating INDEX...");
            Set<String> codes = new HashSet<String>();
            StringWriter sw = new StringWriter();
            PrintWriter out = new PrintWriter(sw);

            for (String code : emojiFiles) {
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

            for (String code : emojiFiles) {
                code = code.substring(7, code.length() - 4);
                for (String c : code.split("_")) {
                    if (!codes.contains(c)) {
                        codes.add(c);
                    }
                }
            }
            out.flush();

            ZipEntry indexEntry = new ZipEntry("INDEX");
            zos.putNextEntry(indexEntry);
            byte[] indexBytes = sw.toString().getBytes(StandardCharsets.ISO_8859_1);
            zos.write(indexBytes);
            zos.closeEntry();
        }
        System.out.println("emoji.zip generation complete.");

        // Cleanup loose files if they exist
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if ((f.getName().endsWith(".svg") || f.getName().equals("INDEX")) && !f.getName().equals("emoji.zip")) {
                    f.delete();
                }
            }
        }
    }
}
