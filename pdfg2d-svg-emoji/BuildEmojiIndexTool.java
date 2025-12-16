import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Build tool that generates emoji.zip from the Google Noto Emoji repository.
 * 
 * <p>
 * This tool downloads SVG emoji files from the official Noto Emoji GitHub
 * repository,
 * repackages them into a single ZIP file, and generates an INDEX file for fast
 * lookup.
 * 
 * <p>
 * Environment variables:
 * <ul>
 * <li>{@code FORCE_DOWNLOAD} - Set to "true" to force re-download even if files
 * exist</li>
 * </ul>
 * 
 * @since Java 21
 */
public class BuildEmojiIndexTool {

    /** URL to the Noto Emoji repository ZIP archive. */
    private static final String NOTO_EMOJI_URL = "https://github.com/googlefonts/noto-emoji/archive/refs/heads/main.zip";

    /** Resource output directory path. */
    private static final Path RESOURCE_DIR = Path.of("src/main/resources/net/zamasoft/pdfg2d/font/emoji");

    /** Temporary download directory. */
    private static final Path BUILD_DIR = Path.of("build");

    /** Connection and read timeout for HTTP requests. */
    private static final Duration HTTP_TIMEOUT = Duration.ofMinutes(5);

    /**
     * Entry point for the emoji index builder.
     *
     * @param args command-line arguments (not used)
     * @throws IOException          if an I/O error occurs during processing
     * @throws InterruptedException if the HTTP request is interrupted
     */
    public static void main(final String[] args) throws IOException, InterruptedException {
        final var tool = new BuildEmojiIndexTool();
        tool.run();
    }

    /**
     * Executes the main build process.
     *
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the HTTP request is interrupted
     */
    private void run() throws IOException, InterruptedException {
        Files.createDirectories(RESOURCE_DIR);
        Files.createDirectories(BUILD_DIR);

        final var emojiZipPath = RESOURCE_DIR.resolve("emoji.zip");
        final var downloadPath = BUILD_DIR.resolve("noto-emoji.zip");

        // Skip if emoji.zip already exists and FORCE_DOWNLOAD is not set
        if (shouldSkipGeneration(emojiZipPath)) {
            System.out.println("emoji.zip already exists. Skipping generation.");
            return;
        }

        // Download the source archive if needed
        if (shouldDownload(downloadPath)) {
            downloadNotoEmoji(downloadPath);
        }

        // Generate the emoji.zip with all SVG files and INDEX
        generateEmojiZip(downloadPath, emojiZipPath);

        // Clean up any loose files from previous builds
        cleanupLooseFiles();

        System.out.println("emoji.zip generation complete.");
    }

    /**
     * Checks if generation should be skipped based on existing file and
     * environment.
     *
     * @param emojiZipPath path to the emoji.zip file
     * @return true if generation should be skipped
     */
    private boolean shouldSkipGeneration(final Path emojiZipPath) throws IOException {
        return Files.exists(emojiZipPath)
                && Files.size(emojiZipPath) > 0
                && !isForceDownloadEnabled();
    }

    /**
     * Checks if download is required based on existing file and environment.
     *
     * @param downloadPath path to the download file
     * @return true if download is needed
     */
    private boolean shouldDownload(final Path downloadPath) {
        return !Files.exists(downloadPath) || isForceDownloadEnabled();
    }

    /**
     * Checks if FORCE_DOWNLOAD environment variable is enabled.
     *
     * @return true if forced download is requested
     */
    private boolean isForceDownloadEnabled() {
        return "true".equals(System.getenv("FORCE_DOWNLOAD"));
    }

    /**
     * Downloads the Noto Emoji archive from GitHub using the modern HttpClient API.
     *
     * @param downloadPath destination path for the downloaded file
     * @throws IOException          if download fails
     * @throws InterruptedException if the request is interrupted
     */
    private void downloadNotoEmoji(final Path downloadPath) throws IOException, InterruptedException {
        System.out.println("Downloading from " + NOTO_EMOJI_URL);

        // Use HttpClient with automatic redirect following (Java 11+)
        try (final var client = HttpClient.newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .connectTimeout(HTTP_TIMEOUT)
                .build()) {

            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(NOTO_EMOJI_URL))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            // Stream response directly to file
            final var response = client.send(request, HttpResponse.BodyHandlers.ofFile(downloadPath));

            if (response.statusCode() != 200) {
                throw new IOException("Download failed with status: " + response.statusCode());
            }
        }
    }

    /**
     * Generates emoji.zip by extracting SVG files from the source archive and
     * creating an INDEX.
     *
     * @param sourcePath path to the downloaded Noto Emoji archive
     * @param targetPath path for the output emoji.zip
     * @throws IOException if processing fails
     */
    private void generateEmojiZip(final Path sourcePath, final Path targetPath) throws IOException {
        System.out.println("Generating emoji.zip...");

        final var emojiFiles = new ArrayList<String>();
        final var addedEntries = new HashSet<String>();

        try (final var zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(sourcePath)));
                final var zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(targetPath)))) {

            processSourceArchive(zis, zos, emojiFiles, addedEntries);
            writeIndex(zos, emojiFiles);
        }
    }

    /**
     * Processes entries from the source archive, extracting LICENSE and SVG files.
     *
     * @param zis          source ZIP input stream
     * @param zos          target ZIP output stream
     * @param emojiFiles   list to collect emoji file names
     * @param addedEntries set to track already-added entries (prevents duplicates)
     * @throws IOException if I/O error occurs
     */
    private void processSourceArchive(final ZipInputStream zis, final ZipOutputStream zos,
            final List<String> emojiFiles, final Set<String> addedEntries) throws IOException {
        final var buffer = new byte[8192];
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }

            final var name = entry.getName();

            // Handle LICENSE file
            if (name.endsWith("LICENSE") && name.contains("noto-emoji-main")) {
                copyEntryIfNew(zis, zos, "LICENSE", buffer, addedEntries);
            }
            // Handle SVG emoji files
            else if (name.endsWith(".svg") && name.contains("/svg/")) {
                final var fileName = extractFileName(name);
                if (fileName.startsWith("emoji_u")) {
                    if (copyEntryIfNew(zis, zos, fileName, buffer, addedEntries)) {
                        emojiFiles.add(fileName);
                    }
                }
            }
        }
    }

    /**
     * Extracts the file name from a full path.
     *
     * @param path full path string
     * @return file name portion only
     */
    private String extractFileName(final String path) {
        final int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    /**
     * Copies a ZIP entry to the output stream if not already added.
     *
     * @param zis          source input stream positioned at entry data
     * @param zos          target output stream
     * @param entryName    name for the new entry
     * @param buffer       reusable byte buffer
     * @param addedEntries set tracking added entries
     * @return true if entry was added, false if duplicate
     * @throws IOException if I/O error occurs
     */
    private boolean copyEntryIfNew(final ZipInputStream zis, final ZipOutputStream zos,
            final String entryName, final byte[] buffer, final Set<String> addedEntries) throws IOException {
        if (addedEntries.contains(entryName)) {
            return false;
        }

        zos.putNextEntry(new ZipEntry(entryName));
        int len;
        while ((len = zis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        zos.closeEntry();
        addedEntries.add(entryName);
        return true;
    }

    /**
     * Writes the INDEX entry containing all emoji code sequences.
     * 
     * <p>
     * The INDEX contains code sequences derived from emoji file names.
     * Each emoji_u{code}.svg file contributes its code and all prefix subsequences.
     * For example, "emoji_u1f468_200d_1f469.svg" generates entries for:
     * "1f468_200d_1f469", "1f468_200d", and "1f468".
     *
     * @param zos        target ZIP output stream
     * @param emojiFiles list of emoji file names
     * @throws IOException if I/O error occurs
     */
    private void writeIndex(final ZipOutputStream zos, final List<String> emojiFiles) throws IOException {
        System.out.println("Generating INDEX...");

        final var codes = new HashSet<String>();
        final var sw = new StringWriter();

        try (final var out = new PrintWriter(sw)) {
            // Add all emoji codes and their prefix sequences
            for (final var fileName : emojiFiles) {
                // Extract code from "emoji_u{code}.svg" format
                final var code = fileName.substring(7, fileName.length() - 4);

                // Add progressive prefix codes (e.g., "a_b_c" -> "a_b_c", "a_b", "a")
                var current = code;
                while (current != null) {
                    if (codes.add(current)) {
                        out.println(current);
                    }
                    final int underscorePos = current.lastIndexOf('_');
                    current = underscorePos > 0 ? current.substring(0, underscorePos) : null;
                }
            }

            // Add Zero Width Joiner code (used in combined emoji sequences)
            out.println("200d");

            // Ensure all individual code components are registered
            for (final var fileName : emojiFiles) {
                final var code = fileName.substring(7, fileName.length() - 4);
                for (final var component : code.split("_")) {
                    codes.add(component);
                }
            }
        }

        // Write INDEX to ZIP
        zos.putNextEntry(new ZipEntry("INDEX"));
        zos.write(sw.toString().getBytes(StandardCharsets.ISO_8859_1));
        zos.closeEntry();
    }

    /**
     * Removes any loose SVG or INDEX files from previous builds.
     * Only files in the resource directory are cleaned; emoji.zip is preserved.
     *
     * @throws IOException if cleanup fails
     */
    private void cleanupLooseFiles() throws IOException {
        try (final var stream = Files.list(RESOURCE_DIR)) {
            stream.filter(this::isLooseFile)
                    .forEach(this::deleteQuietly);
        }
    }

    /**
     * Checks if a file should be cleaned up (loose SVG or INDEX, not emoji.zip).
     *
     * @param path file to check
     * @return true if file should be deleted
     */
    private boolean isLooseFile(final Path path) {
        final var fileName = path.getFileName().toString();
        return (fileName.endsWith(".svg") || fileName.equals("INDEX"))
                && !fileName.equals("emoji.zip");
    }

    /**
     * Deletes a file without throwing exceptions.
     *
     * @param path file to delete
     */
    private void deleteQuietly(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (final IOException e) {
            System.err.println("Warning: Could not delete " + path + ": " + e.getMessage());
        }
    }
}
