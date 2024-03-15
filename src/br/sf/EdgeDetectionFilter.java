package br.sf;

import java.awt.image.BufferedImage;

public class EdgeDetectionFilter {

    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumX = 0;
                int sumY = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixel = image.getRGB(x + i, y + j);
                        int gray = (int) (0.2989 * ((pixel >> 16) & 0xFF) +
                                0.5870 * ((pixel >> 8) & 0xFF) +
                                0.1140 * (pixel & 0xFF));
                        sumX += gray * sobelX[i + 1][j + 1];
                        sumY += gray * sobelY[i + 1][j + 1];
                    }
                }

                int magnitude = Math.min(255, (int) Math.sqrt(sumX * sumX + sumY * sumY));
                int edgePixel = (0xFF << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
                result.setRGB(x, y, edgePixel);
            }
        }

        return result;
    }

}

