package br.sf;

import java.awt.image.BufferedImage;

public class EdgeDetection {
    public static BufferedImage apply(BufferedImage image) {
        // Este exemplo de detecção de bordas é muito simples
        // Você pode implementar algoritmos mais avançados, como o algoritmo de Canny

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edgeDetectedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Aqui, apenas detectamos bordas considerando a diferença de intensidade entre pixels vizinhos
        // e aplicando um limiar fixo

        int threshold = 50; // Limiar fixo

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixel = image.getRGB(x, y);
                int intensity = (pixel >> 16) & 0xFF; // Apenas o canal vermelho para simplificação

                int pixelTop = (image.getRGB(x, y - 1) >> 16) & 0xFF;
                int pixelBottom = (image.getRGB(x, y + 1) >> 16) & 0xFF;
                int pixelLeft = (image.getRGB(x - 1, y) >> 16) & 0xFF;
                int pixelRight = (image.getRGB(x + 1, y) >> 16) & 0xFF;

                int gradient = Math.abs(intensity - pixelTop) +
                        Math.abs(intensity - pixelBottom) +
                        Math.abs(intensity - pixelLeft) +
                        Math.abs(intensity - pixelRight);

                int edgePixel = (gradient > threshold) ? 0xFF000000 : 0xFFFFFFFF;
                edgeDetectedImage.setRGB(x, y, edgePixel);
            }
        }

        return edgeDetectedImage;
    }
}
