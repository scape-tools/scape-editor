package scape.editor.fs.graphics;

import scape.editor.fs.RSArchive;
import scape.editor.fs.graphics.draw.RSRaster;
import scape.editor.util.ByteBufferUtils;
import scape.editor.util.HashUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class RSSprite {

    private int width;
    private int height;
    private int offsetX;
    private int offsetY;
    private int resizeWidth;
    private int resizeHeight;
    private int[] pixels;
    private int format;

    public RSSprite() {

    }

    public RSSprite(BufferedImage bimage) {
        this.pixels = ((DataBufferInt)bimage.getRaster().getDataBuffer()).getData();
        this.width = bimage.getWidth();
        this.height = bimage.getHeight();
        this.resizeWidth = bimage.getWidth();
        this.resizeHeight = bimage.getHeight();
    }

    public RSSprite(int width, int height) {
        this.pixels = new int[width * height];
        this.width = this.resizeWidth = width;
        this.height = this.resizeHeight = height;
    }

    public RSSprite(int resizeWidth, int resizeHeight, int horizontalOffset, int verticalOffset, int width, int height, int format,
                    int[] pixels) {
        this.resizeWidth = resizeWidth;
        this.resizeHeight = resizeHeight;
        this.offsetX = horizontalOffset;
        this.offsetY = verticalOffset;
        this.width = width;
        this.height = height;
        this.format = format;
        this.pixels = pixels;
    }

    public static RSSprite decode(RSArchive archive, int hash, int id) throws IOException {
        ByteBuffer dataBuf = archive.readFile(hash);
        ByteBuffer metaBuf = archive.readFile("index.dat");

        RSSprite sprite = new RSSprite();

        // position of the current image archive within the archive
        metaBuf.position(dataBuf.getShort() & 0xFFFF);

        // the maximum width the images in this archive can scale to
        sprite.setResizeWidth(metaBuf.getShort() & 0xFFFF);

        // the maximum height the images in this archive can scale to
        sprite.setResizeHeight(metaBuf.getShort() & 0xFFFF);

        // the number of colors that are used in this image archive (limit is 256 if one of the rgb values is 0 else its 255)
        int colours = metaBuf.get() & 0xFF;

        // the array of colors that can only be used in this archive
        int[] palette = new int[colours];

        for (int index = 0; index < colours - 1; index++) {
            int colour = ByteBufferUtils.readU24Int(metaBuf);
            // + 1 because index = 0 is for transparency, = 1 is a flag for opacity. (BufferedImage#OPAQUE)
            palette[index + 1] = colour == 0 ? 1 : colour;
        }

        for (int i = 0; i < id; i++) {

            if (metaBuf.position() + 7 >= metaBuf.capacity()) {
                return null;
            }

            // skip the current offsetX and offsetY
            metaBuf.position(metaBuf.position() + 2);

            // skip the current array of pixels
            dataBuf.position(dataBuf.position() + ((metaBuf.getShort() & 0xFFFF) * (metaBuf.getShort() & 0xFFFF)));

            // skip the current format
            metaBuf.position(metaBuf.position() + 1);
        }

        // offsets are used to reposition the sprite on an interface.
        sprite.setOffsetX(metaBuf.get() & 0xFF);
        sprite.setOffsetY(metaBuf.get() & 0xFF);

        // actual width of this sprite
        sprite.setWidth(metaBuf.getShort() & 0xFFFF);

        // actual height of this sprite
        sprite.setHeight(metaBuf.getShort() & 0xFFFF);

        // there are 2 ways the pixels can be written (0 or 1, 0 means the position is read horizontally, 1 means vertically)
        sprite.setFormat(metaBuf.get() & 0xFF);

        if (sprite.getFormat() != 0 && sprite.getFormat() != 1) {
            throw new IOException(String.format("Detected end of archive=%d id=%d or wrong format=%d", hash, id, sprite.getFormat()));
        }

        if (sprite.getWidth() > 765 || sprite.getHeight() > 765 || sprite.getWidth() <= 0 || sprite.getHeight() <= 0) {
            throw new IOException(String.format("Detected end of archive=%d id=%d", hash, id));
        }

        int[] raster = new int[sprite.getWidth() * sprite.getHeight()];

        if (sprite.getFormat() == 0) { // read horizontally
            for (int index = 0; index < raster.length; index++) {
                raster[index] = palette[dataBuf.get() & 0xFF];
            }
        } else if (sprite.getFormat() == 1) { // read vertically
            for (int x = 0; x < sprite.getWidth(); x++) {
                for (int y = 0; y < sprite.getHeight(); y++) {
                    raster[x + y * sprite.getWidth()] = palette[dataBuf.get() & 0xFF];
                }
            }
        }
        sprite.setPixels(raster);
        return sprite;
    }

    public static RSSprite decode(RSArchive archive, String name, int id) throws IOException {
        return decode(archive, HashUtils.hashName(name.contains(".dat") ? name : name + ".dat"), id);
    }

    public void drawSprite(int x, int y) {
        x += offsetX;
        y += offsetY;
        int rasterClip = x + y * RSRaster.width;
        int imageClip = 0;
        int height = this.height;
        int width = this.width;
        int rasterOffset = RSRaster.width - width;
        int imageOffset = 0;

        if (y < RSRaster.getClipBottom()) {
            int dy = RSRaster.getClipBottom() - y;
            height -= dy;
            y = RSRaster.getClipBottom();
            imageClip += dy * width;
            rasterClip += dy * RSRaster.width;
        }

        if (y + height > RSRaster.getClipTop()) {
            height -= y + height - RSRaster.getClipTop();
        }

        if (x < RSRaster.getClipLeft()) {
            int dx = RSRaster.getClipLeft() - x;
            width -= dx;
            x = RSRaster.getClipLeft();
            imageClip += dx;
            rasterClip += dx;
            imageOffset += dx;
            rasterOffset += dx;
        }

        if (x + width > RSRaster.getClipRight()) {
            int dx = x + width - RSRaster.getClipRight();
            width -= dx;
            imageOffset += dx;
            rasterOffset += dx;
        }

        if (width > 0 && height > 0) {
            draw(RSRaster.raster, pixels, 0, imageClip, rasterClip, width, height, rasterOffset, imageOffset);
        }
    }

    private void draw(int raster[], int[] image, int colour, int sourceIndex, int destIndex, int width, int height, int destStep,
                      int sourceStep) {
        int minX = -(width >> 2);
        width = -(width & 3);

        for (int y = -height; y < 0; y++) {
            for (int x = minX; x < 0; x++) {
                colour = image[sourceIndex++];
                if (colour != 0) {
                    raster[destIndex++] = colour;
                } else {
                    destIndex++;
                }
                colour = image[sourceIndex++];

                if (colour != 0) {
                    raster[destIndex++] = colour;
                } else {
                    destIndex++;
                }
                colour = image[sourceIndex++];

                if (colour != 0) {
                    raster[destIndex++] = colour;
                } else {
                    destIndex++;
                }
                colour = image[sourceIndex++];

                if (colour != 0) {
                    raster[destIndex++] = colour;
                } else {
                    destIndex++;
                }
            }

            for (int k2 = width; k2 < 0; k2++) {
                colour = image[sourceIndex++];
                if (colour != 0) {
                    raster[destIndex++] = colour;
                } else {
                    destIndex++;
                }
            }

            destIndex += destStep;
            sourceIndex += sourceStep;
        }
    }

    public BufferedImage toBufferedImage() {

        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);

        final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        System.arraycopy(this.pixels, 0, pixels, 0, this.pixels.length);

        return image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getResizeWidth() {
        return resizeWidth;
    }

    public void setResizeWidth(int resizeWidth) {
        this.resizeWidth = resizeWidth;
    }

    public int getResizeHeight() {
        return resizeHeight;
    }

    public void setResizeHeight(int resizeHeight) {
        this.resizeHeight = resizeHeight;
    }

    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

}
