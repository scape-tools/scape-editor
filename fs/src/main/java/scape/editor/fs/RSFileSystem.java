package scape.editor.fs;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RSFileSystem implements Closeable {

    private Path root;

    private final RSFileStore[] fileStores = new RSFileStore[255];

    private boolean loaded;

    public boolean load() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectory(root);
            }

            final Path dataPath = root.resolve("main_file_cache.dat");

            if (!Files.exists(dataPath)) {
                return false;
            }

            for (int i = 0; i < 255; i++) {
                Path indexPath = root.resolve("main_file_cache.idx" + i);
                if (Files.exists(indexPath)) {
                    fileStores[i] = new RSFileStore(i, new RandomAccessFile(dataPath.toFile(), "rw").getChannel(), new RandomAccessFile(indexPath.toFile(), "rw").getChannel());
                }
            }
            loaded = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createStore(int storeId) throws IOException {
        if (storeId < 0 || storeId >= fileStores.length) {
            return false;
        }

        if (fileStores[storeId] != null) {
            fileStores[storeId].close();
        }

        final Path dataPath = root.resolve("main_file_cache.dat");

        if (!Files.exists(dataPath)) {
            Files.createFile(dataPath);
        }

        final Path path = root.resolve("main_file_cache.idx" + storeId);

        if (!Files.exists(path)) {
            Files.createFile(path);
        } else {
            Files.deleteIfExists(path);
        }
        fileStores[storeId] = new RSFileStore(storeId + 1, new RandomAccessFile(dataPath.toFile(), "rw").getChannel(), new RandomAccessFile(path.toFile(), "rw").getChannel());
        return true;
    }

    public boolean removeStore(int storeId) {
        if (storeId < 0 || storeId >= fileStores.length) {
            return false;
        }

        reset();

        try {
            Files.deleteIfExists(root.resolve("main_file_cache.idx" + storeId));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean defragment() {
        try {
            if (!isLoaded()) {
                return false;
            }

            File[] files = root.toFile().listFiles();

            if (files == null) {
                return false;
            }

            final int storeCount = getStoreCount();

            final Map<Integer, List<ByteBuffer>> map = new LinkedHashMap<>();
            final Map<Integer, Integer> counts = new HashMap<>();

            for (int store = 0; store < storeCount; store++) {

                RSFileStore fileStore = getStore(store);

                if (fileStore == null) {
                    continue;
                }

                map.put(store, new ArrayList<>());

                for (int file = 0; file < fileStore.getFileCount(); file++) {
                    ByteBuffer buffer = fileStore.readFile(file);

                    if (buffer == null) {
                        buffer = ByteBuffer.wrap(new byte[0]);
                    }

                    if (buffer.capacity() > 0) {
                        counts.put(store, file);
                    }

                    List<ByteBuffer> data = map.get(store);
                    data.add(buffer);
                }

            }

            reset();

            Files.deleteIfExists(root.resolve("main_file_cache.dat"));

            for (int i = 0; i < fileStores.length; i++) {
               Files.deleteIfExists(root.resolve("main_file_cache.idx" + i));
            }

            Files.createFile(root.resolve("main_file_cache.dat"));

            for (int i = 0; i < storeCount; i++) {
                Files.createFile(root.resolve("main_file_cache.idx" + i));
            }

            load();

            for (Map.Entry<Integer, List<ByteBuffer>> entry : map.entrySet()) {

                final int fileStoreId = entry.getKey();

                RSFileStore fileStore = getStore(fileStoreId);

                if (fileStore == null) {
                    continue;
                }

                final int lastEntry = counts.getOrDefault(fileStoreId, 0);

                for (int file = 0; file < entry.getValue().size(); file++) {

                    if (file > lastEntry) {
                        break;
                    }

                    ByteBuffer data = entry.getValue().get(file);

                    fileStore.writeFile(file, data == null ? new byte[0] : data.array());
                }

            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public RSFileStore getStore(int storeId) {
        if (storeId < 0 || storeId >= fileStores.length) {
            return null;
        }
        return fileStores[storeId];
    }

    public ByteBuffer readFile(int storeId, int fileId) {
        RSFileStore store = getStore(storeId);

        if (store == null) {
            return null;
        }

        return store.readFile(fileId);
    }

    public RSArchive getArchive(int fileId) throws IOException {
        RSFileStore store = getStore(RSFileStore.ARCHIVE_FILE_STORE);

        if (store == null) {
            return null;
        }

        return RSArchive.decode(store.readFile(fileId));
    }

    public RSArchive getArchive(int storeId, int fileId) throws IOException {
        RSFileStore store = getStore(storeId);

        if (store == null) {
            return null;
        }

        return RSArchive.decode(store.readFile(fileId));
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        if (isLoaded()) {
            reset();
        }
        this.root = root;
    }

    public int getStoreCount() {
        int count = 0;
        for (int i = 0; i < 255; i++) {
            Path indexPath = root.resolve("main_file_cache.idx" + i);
            if (Files.exists(indexPath)) {
                count++;
            }
        }

        return count;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void reset() {
        try {
            close();
            loaded = false;
            Arrays.fill(fileStores, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        for (final RSFileStore fileStore : fileStores) {
            if (fileStore == null) {
                continue;
            }

            fileStore.close();
        }
    }

}
