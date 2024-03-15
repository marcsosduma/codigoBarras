package br.sf;

import java.awt.image.BufferedImage;

public class AdaptiveBinarization {
    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binarizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        int threshold = calculateThreshold(image);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int grayLevel = (pixel >> 16) & 0xFF;
                int binarizedPixel = (grayLevel < threshold) ? 0xFF000000 : 0xFFFFFFFF;
                binarizedImage.setRGB(x, y, binarizedPixel);
            }
        }

        return binarizedImage;
    }

    private static int calculateThreshold(BufferedImage image) {
        // Calcula o limiar global como a média dos níveis de cinza da imagem
        int width = image.getWidth();
        int height = image.getHeight();
        int totalGrayLevel = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int grayLevel = (pixel >> 16) & 0xFF; // Apenas o canal vermelho para simplificação
                totalGrayLevel += grayLevel;
            }
        }
        return totalGrayLevel / (width * height);
    }
}
