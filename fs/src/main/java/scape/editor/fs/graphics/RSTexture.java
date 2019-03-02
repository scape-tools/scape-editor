package scape.editor.fs.graphics;

import scape.editor.fs.RSArchive;
import scape.editor.util.ByteBufferUtils;
import scape.editor.util.HashUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class RSTexture {

    public byte palettePixels[];

    public final int[] palette;

    public int width;
    public int height;
    public int drawOffsetX;
    public int drawOffsetY;
    public int resizeWidth;
    private int resizeHeight;

    private RSTexture(int[] palette) {
        this.palette = palette;
    }

    public static RSTexture decode(RSArchive archive, int hash, int id) throws IOException {
        ByteBuffer dataBuffer = archive.readFile(hash);
        ByteBuffer metaBuffer = archive.readFile("index.dat");

        metaBuffer.position(dataBuffer.getShort() & 0xffff);

        final int resizeWidth = metaBuffer.getShort() & 0xffff;
        final int resizeHeight = metaBuffer.getShort() & 0xffff;
        final int colorLength = metaBuffer.get() & 0xff;

        final int[] palette = new int[colorLength];

        final RSTexture indexedImage = new RSTexture(palette);

        indexedImage.resizeWidth = resizeWidth;
        indexedImage.resizeHeight = resizeHeight;

        for (int index = 0; index < colorLength - 1; index++) {
            indexedImage.palette[index + 1] = ByteBufferUtils.readU24Int(metaBuffer);
        }

        for (int i = 0; i < id; i++) {
            metaBuffer.position(metaBuffer.position() + 2);
            dataBuffer.position(dataBuffer.position() + metaBuffer.getShort() & 0xffff * metaBuffer.getShort() & 0xffff);
            metaBuffer.position(metaBuffer.position() + 1);
        }

        indexedImage.drawOffsetX = metaBuffer.get() & 0xff;
        indexedImage.drawOffsetY = metaBuffer.get() & 0xff;
        indexedImage.width = metaBuffer.getShort() & 0xffff;
        indexedImage.height = metaBuffer.getShort() & 0xffff;
        int type = metaBuffer.get() & 0xff;
        int pixels = indexedImage.width * indexedImage.height;
        indexedImage.palettePixels = new byte[pixels];

        if (type == 0) {
            for (int index = 0; index < pixels; index++) {
                indexedImage.palettePixels[index] = dataBuffer.get();
            }
        } else if (type == 1) {
            for (int x = 0; x < indexedImage.width; x++) {
                for (int y = 0; y < indexedImage.height; y++) {
                    indexedImage.palettePixels[x + y * indexedImage.width] = dataBuffer.get();
                }
            }
        }

        return indexedImage;
    }

    public static RSTexture decode(RSArchive archive, String name, int id) throws IOException {
        return decode(archive, HashUtils.hashName(name.contains(".dat") ? name : name + ".dat"), id);
    }

    public void downscale() {
        resizeWidth /= 2;
        resizeHeight /= 2;
        byte raster[] = new byte[resizeWidth * resizeHeight];
        int sourceIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster[(x + drawOffsetX >> 1) + (y + drawOffsetY >> 1) * resizeWidth] = raster[sourceIndex++];
            }
        }
        this.palettePixels = raster;
        width = resizeWidth;
        height = resizeHeight;
        drawOffsetX = 0;
        drawOffsetY = 0;
    }

    public void resize() {
        if (width == resizeWidth && height == resizeHeight) {
            return;
        }

        byte raster[] = new byte[resizeWidth * resizeHeight];

        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                raster[x + drawOffsetX + (y + drawOffsetY) * resizeWidth] = raster[i++];
            }
        }
        this.palettePixels = raster;
        width = resizeWidth;
        height = resizeHeight;
        drawOffsetX = 0;
        drawOffsetY = 0;
    }

    public void flipHorizontally() {
        byte raster[] = new byte[width * height];
        int pixel = 0;
        for (int y = 0; y < height; y++) {
            for (int x = width - 1; x >= 0; x--) {
                raster[pixel++] = raster[x + y * width];
            }
        }
        this.palettePixels = raster;
        drawOffsetX = resizeWidth - width - drawOffsetX;
    }

    public void flipVertically() {
        byte raster[] = new byte[width * height];
        int pixel = 0;
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                raster[pixel++] = raster[x + y * width];
            }
        }
        this.palettePixels = raster;
        drawOffsetY = resizeHeight - height - drawOffsetY;
    }

    public void offsetColor(int redOffset, int greenOffset, int blueOffset) {
        for (int index = 0; index < palette.length; index++) {
            int red = palette[index] >> 16 & 0xff;
            red += redOffset;

            if (red < 0) {
                red = 0;
            } else if (red > 255) {
                red = 255;
            }

            int green = palette[index] >> 8 & 0xff;

            green += greenOffset;
            if (green < 0) {
                green = 0;
            } else if (green > 255) {
                green = 255;
            }

            int blue = palette[index] & 0xff;

            blue += blueOffset;
            if (blue < 0) {
                blue = 0;
            } else if (blue > 255) {
                blue = 255;
            }
            palette[index] = (red << 16) + (green << 8) + blue;
        }
    }

    private void draw(int i, int raster[], byte image[], int destStep, int destIndex, int width, int sourceIndex, int ai1[], int sourceStep) {
        int minX = -(width >> 2);
        width = -(width & 3);
        for (int y = -i; y < 0; y++) {
            for (int x = minX; x < 0; x++) {

                byte pixel = image[sourceIndex++];

                if (pixel != 0) {
                    raster[destIndex++] = ai1[pixel & 0xff];
                } else {
                    destIndex++;
                }
                pixel = image[sourceIndex++];
                if (pixel != 0) {
                    raster[destIndex++] = ai1[pixel & 0xff];
                } else {
                    destIndex++;
                }
                pixel = image[sourceIndex++];
                if (pixel != 0) {
                    raster[destIndex++] = ai1[pixel & 0xff];
                } else {
                    destIndex++;
                }
                pixel = image[sourceIndex++];
                if (pixel != 0) {
                    raster[destIndex++] = ai1[pixel & 0xff];
                } else {
                    destIndex++;
                }
            }
            for (int x = width; x < 0; x++) {
                byte pixel = image[sourceIndex++];
                if (pixel != 0) {
                    raster[destIndex++] = ai1[pixel & 0xff];
                } else {
                    destIndex++;
                }
            }
            destIndex += destStep;
            sourceIndex += sourceStep;
        }
    }

    public BufferedImage toBufferedImage() {

        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_INDEXED);

        final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        System.arraycopy(this.palette, 0, pixels, 0, this.palette.length);

        return image;
    }

}
