package net.zamasoft.font.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BufferedRandomAccessFileTest {

    private File tempFile;
    private byte[] data;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("braf_test", ".bin");
        data = new byte[10 * 1024]; // 10KB
        new Random(12345).nextBytes(data);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(data);
        }
    }

    @AfterEach
    public void tearDown() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    @Test
    public void testReadByte() throws IOException {
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(tempFile.getAbsolutePath(), "r", 1024)) {
            for (int i = 0; i < data.length; i++) {
                int expected = data[i] & 0xFF;
                int actual = braf.read();
                if (actual != expected) {
                    fail("Mismatch at " + i + ": expected " + expected + ", got " + actual);
                }
            }
            assertEquals(-1, braf.read());
        }
    }

    @Test
    public void testReadBytes() throws IOException {
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(tempFile.getAbsolutePath(), "r", 128)) {
            byte[] buf = new byte[256];
            int read = braf.read(buf);
            assertEquals(256, read);

            byte[] expected = new byte[256];
            System.arraycopy(data, 0, expected, 0, 256);
            assertArrayEquals(expected, buf);
        }
    }

    @Test
    public void testSeek() throws IOException {
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(tempFile.getAbsolutePath(), "r", 100)) {
            braf.seek(5000);
            assertEquals(5000, braf.getFilePointer());
            int val = braf.read();
            assertEquals(data[5000] & 0xFF, val);

            // Backward seek within buffer
            braf.seek(4950);
            assertEquals(4950, braf.getFilePointer());
            val = braf.read();
            assertEquals(data[4950] & 0xFF, val);

            // Forward seek outside buffer
            braf.seek(8000);
            assertEquals(8000, braf.getFilePointer());
            val = braf.read();
            assertEquals(data[8000] & 0xFF, val);
        }
    }

    @Test
    public void testReadPrimitives() throws IOException {
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(tempFile.getAbsolutePath(), "r", 512);
                RandomAccessFile raf = new RandomAccessFile(tempFile, "r")) {

            for (int i = 0; i < 100; i++) {
                int p = new Random().nextInt(data.length - 8);
                braf.seek(p);
                raf.seek(p);

                assertEquals(raf.readInt(), braf.readInt(), "readInt at " + p);

                braf.seek(p);
                raf.seek(p);
                assertEquals(raf.readShort(), braf.readShort(), "readShort at " + p);

                braf.seek(p);
                raf.seek(p);
                assertEquals(raf.readLong(), braf.readLong(), "readLong at " + p);
            }
        }
    }
}
