package br.sf;

import java.awt.image.BufferedImage;

public class MedianFilter {

    public static BufferedImage apply(BufferedImage image, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int radius = kernelSize / 2;
        int[] pixels = new int[kernelSize * kernelSize];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = 0;
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int px = Math.min(Math.max(x + kx, 0), width - 1);
                        int py = Math.min(Math.max(y + ky, 0), height - 1);
                        pixels[index++] = image.getRGB(px, py) & 0xFF;
                    }
                }
                // Ordena os valores dos pixels
                java.util.Arrays.sort(pixels);
                // Obtém o valor mediano
                int median = pixels[pixels.length / 2];
                filteredImage.setRGB(x, y, (median << 16) | (median << 8) | median);
            }
        }
        return filteredImage;
    }
}
