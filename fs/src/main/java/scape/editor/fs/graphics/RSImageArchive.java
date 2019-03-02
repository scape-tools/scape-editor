package scape.editor.fs.graphics;

import scape.editor.fs.RSArchive;
import scape.editor.util.HashUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class RSImageArchive {

    private int hash;
    private final List<RSSprite> sprites = new ArrayList<>();

    public RSImageArchive(int hash) {
        this.hash = hash;
    }

    public static RSImageArchive decode(RSArchive archive, int hash) {
        RSImageArchive imageArchive = new RSImageArchive(hash);

        for (int i = 0; ; i++) {
            try {
                RSSprite decoded = RSSprite.decode(archive, hash, i);

                if (decoded == null) {
                    break;
                }

                imageArchive.sprites.add(decoded);
            } catch (IOException e) {
                break;
            }
        }

        return imageArchive;
    }

    public static RSImageArchive decode(RSArchive archive, String name) {
        return decode(archive, HashUtils.hashName(name));
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public void setName(String name) {
        this.hash = HashUtils.hashName(name);
    }

    public List<RSSprite> getSprites() {
        return sprites;
    }

}
