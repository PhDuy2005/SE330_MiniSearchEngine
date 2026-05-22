package com.NgonNguLapTrinhJava.MiniSearchEngine.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.NgonNguLapTrinhJava.MiniSearchEngine.service.index.IndexStatistics;
import com.NgonNguLapTrinhJava.MiniSearchEngine.service.index.InvertedIndex;

/**
 * Persistence Layer - Serialize/Deserialize Inverted Index xuống ổ cứng.
 *
 * Dùng Java Serialization (file .bin) để:
 *   - Lưu toàn bộ InvertedIndex + IndexStatistics sau khi build xong
 *   - Load lên RAM khi Search khởi động (không cần re-index)
 *
 * Cải tiến:
 *   - Lưu version number để phát hiện file cũ không tương thích
 *   - Tạo thư mục tự động nếu chưa tồn tại
 *   - Log thời gian load/save để debug performance
 */
public class IndexPersistence {

    private static final String INDEX_FILE    = "inverted_index.bin";
    private static final String STATS_FILE    = "index_stats.bin";
    private static final int    VERSION       = 1;
    private static final String VERSION_FILE  = "index_version.txt";

    /**
     * Lưu InvertedIndex và IndexStatistics xuống thư mục chỉ định.
     *
     * @param dirPath thư mục lưu file (ví dụ: "output/index")
     * @param index   InvertedIndex đã build xong
     * @param stats   IndexStatistics đã tính xong
     */
    public static void saveIndex(String dirPath, InvertedIndex index, IndexStatistics stats) throws IOException {
        long startTime = System.currentTimeMillis();

        // Tạo thư mục nếu chưa có
        Path dir = Paths.get(dirPath);
        Files.createDirectories(dir);

        // Lưu InvertedIndex
        serialize(index, dir.resolve(INDEX_FILE).toString());

        // Lưu IndexStatistics
        serialize(stats, dir.resolve(STATS_FILE).toString());

        // Lưu version để kiểm tra tương thích
        Files.writeString(dir.resolve(VERSION_FILE), String.valueOf(VERSION));

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("[Persistence] Saved index to '%s' in %dms%n", dirPath, elapsed);
        System.out.printf("[Persistence] Index size: vocabulary=%d, documents=%d%n",
                index.getVocabularySize(), stats.getTotalDocuments());
    }

    /**
     * Load InvertedIndex từ thư mục chỉ định.
     *
     * @param dirPath thư mục chứa file index
     * @return InvertedIndex đã load
     */
    public static InvertedIndex loadIndex(String dirPath) throws IOException, ClassNotFoundException {
        long startTime = System.currentTimeMillis();

        checkVersion(dirPath);

        InvertedIndex index = (InvertedIndex) deserialize(
                Paths.get(dirPath, INDEX_FILE).toString());

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("[Persistence] Loaded InvertedIndex from '%s' in %dms%n", dirPath, elapsed);
        return index;
    }

    /**
     * Load IndexStatistics từ thư mục chỉ định.
     *
     * @param dirPath thư mục chứa file stats
     * @return IndexStatistics đã load
     */
    public static IndexStatistics loadStatistics(String dirPath) throws IOException, ClassNotFoundException {
        long startTime = System.currentTimeMillis();

        checkVersion(dirPath);

        IndexStatistics stats = (IndexStatistics) deserialize(
                Paths.get(dirPath, STATS_FILE).toString());

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("[Persistence] Loaded IndexStatistics from '%s' in %dms%n", dirPath, elapsed);
        return stats;
    }

    /**
     * Kiểm tra file index đã tồn tại chưa.
     */
    public static boolean indexExists(String dirPath) {
        return Files.exists(Paths.get(dirPath, INDEX_FILE))
                && Files.exists(Paths.get(dirPath, STATS_FILE));
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private static void serialize(Object obj, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(obj);
        }
    }

    private static Object deserialize(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filePath)))) {
            return ois.readObject();
        }
    }

    private static void checkVersion(String dirPath) throws IOException {
        Path versionFile = Paths.get(dirPath, VERSION_FILE);
        if (Files.exists(versionFile)) {
            int savedVersion = Integer.parseInt(Files.readString(versionFile).trim());
            if (savedVersion != VERSION) {
                throw new IOException(String.format(
                        "Index version mismatch: saved=%d, current=%d. Please re-index.",
                        savedVersion, VERSION));
            }
        }
    }
}
