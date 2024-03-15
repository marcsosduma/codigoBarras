package br.sf;

import java.awt.image.BufferedImage;

public class SmoothingFilter {
    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage smoothedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumRed = 0;
                int sumGreen = 0;
                int sumBlue = 0;

                // Aplica uma média ponderada dos pixels vizinhos para suavização
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int pixel = image.getRGB(x + dx, y + dy);
                        sumRed += (pixel >> 16) & 0xFF;
                        sumGreen += (pixel >> 8) & 0xFF;
                        sumBlue += pixel & 0xFF;
                    }
                }

                int avgRed = sumRed / 9;
                int avgGreen = sumGreen / 9;
                int avgBlue = sumBlue / 9;

                int smoothedPixel = (avgRed << 16) | (avgGreen << 8) | avgBlue;
                smoothedImage.setRGB(x, y, smoothedPixel);
            }
        }

        return smoothedImage;
    }
}
