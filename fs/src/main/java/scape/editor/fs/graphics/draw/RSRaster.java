package scape.editor.fs.graphics.draw;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class RSRaster {

    public static int maxRight;
    public static int height;
    public static int[] raster;
    public static int width;
    private static int centreX;
    private static int centreY;
    private static int clipBottom;
    private static int clipLeft;
    private static int clipRight;
    private static int clipTop;

    public static void drawHorizontal(int x, int y, int length, int colour) {
        if (y < clipBottom || y >= clipTop) {
            return;
        }

        if (x < clipLeft) {
            length -= clipLeft - x;
            x = clipLeft;
        }

        if (x + length > clipRight) {
            length = clipRight - x;
        }

        int offset = x + y * width;
        for (int index = 0; index < length; index++) {
            raster[offset + index] = colour;
        }
    }

    public static void drawHorizontal(int x, int y, int length, int colour, int alpha) {
        if (y < clipBottom || y >= clipTop) {
            return;
        }

        if (x < clipLeft) {
            length -= clipLeft - x;
            x = clipLeft;
        }

        if (x + length > clipRight) {
            length = clipRight - x;
        }

        int invertedAlpha = 256 - alpha;
        int r = (colour >> 16 & 0xff) * alpha;
        int g = (colour >> 8 & 0xff) * alpha;
        int b = (colour & 0xff) * alpha;
        int index = x + y * width;

        for (int i = 0; i < length; i++) {
            int r2 = (raster[index] >> 16 & 0xff) * invertedAlpha;
            int g2 = (raster[index] >> 8 & 0xff) * invertedAlpha;
            int b2 = (raster[index] & 0xff) * invertedAlpha;
            raster[index++] = (r + r2 >> 8 << 16) + (g + g2 >> 8 << 8) + (b + b2 >> 8);
        }
    }

    public static void drawRectangle(int x, int y, int width, int height, int colour) {
        drawHorizontal(x, y, width, colour);
        drawHorizontal(x, y + height - 1, width, colour);
        drawVertical(x, y, height, colour);
        drawVertical(x + width - 1, y, height, colour);
    }

    public static void drawRectangle(int x, int y, int width, int height, int colour, int alpha) {
        drawHorizontal(x, y, width, colour, alpha);
        drawHorizontal(x, y + height - 1, width, colour, alpha);
        if (height >= 3) {
            drawVertical(x, y + 1, height - 2, colour, alpha);
            drawVertical(x + width - 1, y + 1, height - 2, colour, alpha);
        }
    }

    public static void drawVertical(int x, int y, int length, int colour) {
        if (x < clipLeft || x >= clipRight) {
            return;
        }

        if (y < clipBottom) {
            length -= clipBottom - y;
            y = clipBottom;
        }

        if (y + length > clipTop) {
            length = clipTop - y;
        }

        int offset = x + y * width;
        for (int index = 0; index < length; index++) {
            raster[offset + index * width] = colour;
        }
    }

    public static void drawVertical(int x, int y, int length, int colour, int alpha) {
        if (x < clipLeft || x >= clipRight) {
            return;
        }

        if (y < clipBottom) {
            length -= clipBottom - y;
            y = clipBottom;
        }

        if (y + length > clipTop) {
            length = clipTop - y;
        }

        int invertedAlpha = 256 - alpha;
        int r = (colour >> 16 & 0xff) * alpha;
        int g = (colour >> 8 & 0xff) * alpha;
        int b = (colour & 0xff) * alpha;
        int index = x + y * width;

        for (int i = 0; i < length; i++) {
            int r2 = (raster[index] >> 16 & 0xff) * invertedAlpha;
            int g2 = (raster[index] >> 8 & 0xff) * invertedAlpha;
            int b2 = (raster[index] & 0xff) * invertedAlpha;
            raster[index] = (r + r2 >> 8 << 16) + (g + g2 >> 8 << 8) + (b + b2 >> 8);
            index += width;
        }
    }

    public static void fillRectangle(int x, int y, int width, int height, int colour) {
        if (x < clipLeft) {
            width -= clipLeft - x;
            x = clipLeft;
        }

        if (y < clipBottom) {
            height -= clipBottom - y;
            y = clipBottom;
        }

        if (x + width > clipRight) {
            width = clipRight - x;
        }

        if (y + height > clipTop) {
            height = clipTop - y;
        }

        int dx = RSRaster.width - width;
        int pixel = x + y * RSRaster.width;

        for (int i2 = -height; i2 < 0; i2++) {
            for (int j2 = -width; j2 < 0; j2++) {
                raster[pixel++] = colour;
            }

            pixel += dx;
        }
    }

    public static void fillRectangle(int drawX, int drawY, int width, int height, int colour, int alpha) {
        if (drawX < clipLeft) {
            width -= clipLeft - drawX;
            drawX = clipLeft;
        }

        if (drawY < clipBottom) {
            height -= clipBottom - drawY;
            drawY = clipBottom;
        }

        if (drawX + width > clipRight) {
            width = clipRight - drawX;
        }

        if (drawY + height > clipTop) {
            height = clipTop - drawY;
        }

        int inverseAlpha = 256 - alpha;
        int r = (colour >> 16 & 0xff) * alpha;
        int g = (colour >> 8 & 0xff) * alpha;
        int b = (colour & 0xff) * alpha;
        int dx = RSRaster.width - width;
        int pixel = drawX + drawY * RSRaster.width;

        for (int x = 0; x < height; x++) {
            for (int y = -width; y < 0; y++) {
                int r2 = (raster[pixel] >> 16 & 0xff) * inverseAlpha;
                int g2 = (raster[pixel] >> 8 & 0xff) * inverseAlpha;
                int b2 = (raster[pixel] & 0xff) * inverseAlpha;
                raster[pixel++] = (r + r2 >> 8 << 16) + (g + g2 >> 8 << 8) + (b + b2 >> 8);
            }

            pixel += dx;
        }
    }

    public static int getCentreX() {
        return centreX;
    }

    public static int getCentreY() {
        return centreY;
    }

    public static int getClipBottom() {
        return clipBottom;
    }

    public static int getClipLeft() {
        return clipLeft;
    }

    public static int getClipRight() {
        return clipRight;
    }

    public static int getClipTop() {
        return clipTop;
    }

    public static void init(int height, int width, int[] pixels) {
        RSRaster.raster = pixels;
        RSRaster.width = width;
        RSRaster.height = height;
        setBounds(height, 0, width, 0);
    }

    public static void reset() {
        int count = width * height;
        for (int index = 0; index < count; index++) {
            raster[index] = 0;
        }
    }

    public static void setBounds(int clipTop, int clipLeft, int clipRight, int clipBottom) {
        if (clipLeft < 0) {
            clipLeft = 0;
        }

        if (clipBottom < 0) {
            clipBottom = 0;
        }

        if (clipRight > RSRaster.width) {
            clipRight = RSRaster.width;
        }

        if (clipTop > RSRaster.height) {
            clipTop = RSRaster.height;
        }

        RSRaster.clipLeft = clipLeft;
        RSRaster.clipBottom = clipBottom;
        RSRaster.clipRight = clipRight;
        RSRaster.clipTop = clipTop;

        maxRight = RSRaster.clipRight - 1;
        centreX = RSRaster.clipRight / 2;
        centreY = RSRaster.clipTop / 2;
    }

    public static void setCentreX(int centreX) {
        RSRaster.centreX = centreX;
    }

    public static void setCentreY(int centreY) {
        RSRaster.centreY = centreY;
    }

    public static void setClipBottom(int clipBottom) {
        RSRaster.clipBottom = clipBottom;
    }

    public static void setClipLeft(int clipLeft) {
        RSRaster.clipLeft = clipLeft;
    }

    public static void setClipRight(int clipRight) {
        RSRaster.clipRight = clipRight;
    }

    public static void setClipTop(int clipTop) {
        RSRaster.clipTop = clipTop;
    }

    public static void setDefaultBounds() {
        clipLeft = 0;
        clipBottom = 0;
        clipRight = width;
        clipTop = height;
        maxRight = clipRight - 1;
        centreX = clipRight / 2;
    }

    public static BufferedImage toBufferedImage() {
        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] data = ((DataBufferInt)(bimage.getData().getDataBuffer())).getData();
        System.arraycopy(raster, 0, data, 0, raster.length);
        return bimage;
    }

    public static BufferedImage toBufferedImage(int width, int height, int[] raster) {
        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] data = ((DataBufferInt)(bimage.getData().getDataBuffer())).getData();
        System.arraycopy(raster, 0, data, 0, raster.length);
        return bimage;
    }

}