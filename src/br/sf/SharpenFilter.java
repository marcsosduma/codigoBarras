package br.sf;

import java.awt.image.BufferedImage;

public class SharpenFilter {
    
    private static final float[][] SHARPEN_KERNEL = {
        { 0, -1,  0 },
        { -1,  5, -1 },
        { 0, -1,  0 }
    };

    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = applyKernel(image, x, y);
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    private static int applyKernel(BufferedImage image, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();

        float sumRed = 0, sumGreen = 0, sumBlue = 0;

        for (int ky = -1; ky <= 1; ky++) {
            for (int kx = -1; kx <= 1; kx++) {
                int pixelX = Math.min(Math.max(x + kx, 0), width - 1);
                int pixelY = Math.min(Math.max(y + ky, 0), height - 1);

                int rgb = image.getRGB(pixelX, pixelY);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                sumRed += red * SHARPEN_KERNEL[ky + 1][kx + 1];
                sumGreen += green * SHARPEN_KERNEL[ky + 1][kx + 1];
                sumBlue += blue * SHARPEN_KERNEL[ky + 1][kx + 1];
            }
        }

        int newRed = Math.min(Math.max((int)sumRed, 0), 255);
        int newGreen = Math.min(Math.max((int)sumGreen, 0), 255);
        int newBlue = Math.min(Math.max((int)sumBlue, 0), 255);

        return (newRed << 16) | (newGreen << 8) | newBlue;
    }
}
