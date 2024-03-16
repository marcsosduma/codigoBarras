package br.sf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HistogramEqualizationFilter {
    public static BufferedImage apply(BufferedImage image) {
        // Converter para tons de cinza
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int grayValue = (int) (0.2989 * ((rgb >> 16) & 0xFF) + 0.5870 * ((rgb >> 8) & 0xFF) + 0.1140 * (rgb & 0xFF));
                grayImage.setRGB(x, y, grayValue << 16 | grayValue << 8 | grayValue);
            }
        }

        // Calcular histograma
        int[] histogram = new int[256];
        for (int y = 0; y < grayImage.getHeight(); y++) {
            for (int x = 0; x < grayImage.getWidth(); x++) {
                int grayValue = grayImage.getRGB(x, y) & 0xFF;
                histogram[grayValue]++;
            }
        }

        // Calcular função de distribuição cumulativa
        int[] cdf = new int[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        // Equalizar a imagem
        double scaleFactor = 255.0 / (grayImage.getWidth() * grayImage.getHeight());
        for (int y = 0; y < grayImage.getHeight(); y++) {
            for (int x = 0; x < grayImage.getWidth(); x++) {
                int grayValue = grayImage.getRGB(x, y) & 0xFF;
                int newValue = (int) (cdf[grayValue] * scaleFactor);
                grayImage.setRGB(x, y, newValue << 16 | newValue << 8 | newValue);
            }
        }

        return grayImage;
    }
}
