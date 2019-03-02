package scape.editor.fs;

import scape.editor.util.ByteBufferUtils;
import scape.editor.util.CompressionUtils;
import scape.editor.util.HashUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RSArchive {

    public static final int TITLE_ARCHIVE = 1;
    public static final int CONFIG_ARCHIVE = 2;
    public static final int INTERFACE_ARCHIVE = 3;
    public static final int MEDIA_ARCHIVE = 4;
    public static final int VERSION_LIST_ARCHIVE = 5;
    public static final int TEXTURE_ARCHIVE = 6;
    public static final int WORDENC_ARCHIVE = 7;
    public static final int SOUND_ARCHIVE = 8;

    public static final class ArchiveEntry {

        private final int hash;
        private final int uncompressedSize;
        private final int compressedSize;
        private final byte[] data;

        public ArchiveEntry(int hash, int uncompressedSize, int compressedSize, byte[] data) {
            this.hash = hash;
            this.uncompressedSize = uncompressedSize;
            this.compressedSize = compressedSize;
            this.data = data;
        }

        public int getHash() {
            return hash;
        }

        public int getUncompressedSize() {
            return uncompressedSize;
        }

        public int getCompresseedSize() {
            return compressedSize;
        }

        public byte[] getData() {
            return data;
        }

    }

    private boolean extracted;

    private final Map<Integer, ArchiveEntry> entries = new LinkedHashMap<>();

    public RSArchive() {

    }

    public RSArchive(ArchiveEntry[] entries) {
        Arrays.asList(entries).forEach(it -> this.entries.put(it.getHash(), it));
    }

    public static RSArchive decode(ByteBuffer buffer) throws IOException {
        final int uncompressedLength = ByteBufferUtils.readU24Int(buffer);
        final int compressedLength = ByteBufferUtils.readU24Int(buffer);

        boolean extracted = false;

        if (uncompressedLength != compressedLength) {
            final byte[] compressed = new byte[compressedLength];
            final byte[] decompressed = new byte[uncompressedLength];
            buffer.get(compressed);
            CompressionUtils.debzip2(compressed, decompressed);
            buffer = ByteBuffer.wrap(decompressed);
            extracted = true;
        }

        final int entries = buffer.getShort() & 0xFFFF;

        final int[] hashes = new int[entries];
        final int[] uncompressedSizes = new int[entries];
        final int[] compressedSizes = new int[entries];

        final ArchiveEntry[] archiveEntries = new ArchiveEntry[entries];

        final ByteBuffer entryBuf = ByteBuffer.wrap(buffer.array());
        entryBuf.position(buffer.position() + entries * 10);

        for (int i = 0; i < entries; i++) {

            hashes[i] = buffer.getInt();
            uncompressedSizes[i] = ByteBufferUtils.readU24Int(buffer);
            compressedSizes[i] = ByteBufferUtils.readU24Int(buffer);

            final byte[] entryData = new byte[compressedSizes[i]];
            entryBuf.get(entryData);

            archiveEntries[i] = new ArchiveEntry(hashes[i], uncompressedSizes[i], compressedSizes[i], entryData);
        }

        final RSArchive archive = new RSArchive(archiveEntries);
        archive.extracted = extracted;

        return archive;
    }

    public synchronized byte[] encode() throws IOException {
        int size = 2 + entries.size() * 10;

        for (ArchiveEntry file : entries.values()) {
            size += file.getCompresseedSize();
        }

        ByteBuffer buffer;
        if (!extracted) {
            buffer = ByteBuffer.allocate(size + 6);
            ByteBufferUtils.write24Int(buffer, size);
            ByteBufferUtils.write24Int(buffer, size);
        } else {
            buffer = ByteBuffer.allocate(size);
        }

        buffer.putShort((short) entries.size());

        for (ArchiveEntry entry : entries.values()) {
            buffer.putInt(entry.getHash());
            ByteBufferUtils.write24Int(buffer, entry.getUncompressedSize());
            ByteBufferUtils.write24Int(buffer, entry.getCompresseedSize());
        }

        for (ArchiveEntry file : entries.values()) {
            buffer.put(file.getData());
        }

        byte[] data;
        if (!extracted) {
            data = buffer.array();
        } else {
            byte[] unzipped = buffer.array();
            byte[] zipped = CompressionUtils.bzip2(unzipped);
            if (unzipped.length == zipped.length) {
                throw new RuntimeException("error zipped size matches original");
            }
            buffer = ByteBuffer.allocate(zipped.length + 6);
            ByteBufferUtils.write24Int(buffer, unzipped.length);
            ByteBufferUtils.write24Int(buffer, zipped.length);
            buffer.put(zipped, 0, zipped.length);
            data = buffer.array();
        }

        return data;
    }

    public ByteBuffer readFile(String name) throws IOException {
        return readFile(HashUtils.hashName(name));
    }

    public ByteBuffer readFile(int hash) throws IOException {
        final ArchiveEntry entry = entries.get(hash);

        if (entry == null) {
            throw new FileNotFoundException(String.format("file=%d could not be found.", hash));
        }

        if (!extracted) {
            byte[] decompressed = new byte[entry.getUncompressedSize()];
            CompressionUtils.debzip2(entry.getData(), decompressed);
            return ByteBuffer.wrap(decompressed);
        } else {
            return ByteBuffer.wrap(entry.getData());
        }
    }

    public boolean replaceFile(int oldHash, String newName, byte[] data) throws IOException {
        return replaceFile(oldHash, HashUtils.hashName(newName), data);
    }

    public boolean replaceFile(int oldHash, int newHash, byte[] data) throws IOException {
        if (!entries.containsKey(oldHash)) {
            return false;
        }

        ArchiveEntry entry;
        if (!extracted) {
            byte[] compressed = CompressionUtils.bzip2(data);
            entry = new RSArchive.ArchiveEntry(newHash, data.length, compressed.length, compressed);
        } else {
            entry = new RSArchive.ArchiveEntry(newHash, data.length, data.length, data);
        }

        entries.replace(oldHash, entry);
        return true;
    }

    public boolean writeFile(String name, byte[] data) throws IOException {
        return writeFile(HashUtils.hashName(name), data);
    }

    public boolean writeFile(int hash, byte[] data) throws IOException {
        if (entries.containsKey(hash)) {
            replaceFile(hash, hash, data);
        }

        ArchiveEntry entry;
        if (!extracted) {
            byte[] compressed = CompressionUtils.bzip2(data);
            entry = new RSArchive.ArchiveEntry(hash, data.length, compressed.length, compressed);
        } else {
            entry = new RSArchive.ArchiveEntry(hash, data.length, data.length, data);
        }

        entries.put(hash, entry);
        return true;
    }

    public boolean rename(int oldHash, String newName) {
        return rename(oldHash, HashUtils.hashName(newName));
    }

    public boolean rename(int oldHash, int newHash) {
        final ArchiveEntry old = entries.get(oldHash);

        if (old == null) {
            return false;
        }

        entries.replace(oldHash, new ArchiveEntry(newHash, old.getUncompressedSize(), old.getCompresseedSize(), old.getData()));
        return true;
    }

    public ArchiveEntry getEntry(String name) throws FileNotFoundException {
        return getEntry(HashUtils.hashName(name));
    }

    public ArchiveEntry getEntry(int hash) throws FileNotFoundException {
        if (entries.containsKey(hash)) {
            return entries.get(hash);
        }

        throw new FileNotFoundException(String.format("Could not find entry: %d.", hash));
    }

    public ArchiveEntry getEntryAt(int index) throws IOException {
        if (index >= entries.size()) {
            throw new FileNotFoundException(String.format("File at index=%d could not be found.", index));
        }

        int pos = 0;
        for (ArchiveEntry entry : entries.values()) {
            if (pos == index) {
                return entry;
            }
            pos++;
        }

        throw new FileNotFoundException(String.format("File at index=%d could not be found.", index));
    }

    public int indexOf(String name) {
        return indexOf(HashUtils.hashName(name));
    }

    public int indexOf(int hash) {
        int index = 0;
        for (ArchiveEntry entry : entries.values()) {
            if (entry.getHash() == hash) {
                return index;
            }
            index++;
        }

        return -1;
    }

    public boolean contains(String name) {
        return contains(HashUtils.hashName(name));
    }

    public boolean contains(int hash) {
        return entries.containsKey(hash);
    }

    public boolean remove(String name) {
        return remove(HashUtils.hashName(name));
    }

    public boolean remove(int hash) {
        if (entries.containsKey(hash)) {
            entries.remove(hash);
            return true;
        }
        return false;
    }

    public int getEntryCount() {
        return entries.size();
    }

    public ArchiveEntry[] getEntries() {
        return entries.values().toArray(new ArchiveEntry[0]);
    }

    public boolean isExtracted() {
        return extracted;
    }

}
