package br.sf;

import java.awt.image.BufferedImage;

public class ContrastEnhancement {
    public static BufferedImage apply(BufferedImage image) {
        // Este exemplo de aprimoramento de contraste é muito simples
        // Você pode implementar algoritmos mais avançados, como a equalização de histograma
        // Aqui, apenas aumentamos o contraste multiplicando os valores dos pixels

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage enhancedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                // Ajuste dos componentes de cor para aumentar o contraste
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                red = Math.min(255, Math.max(0, red * 2));
                green = Math.min(255, Math.max(0, green * 2));
                blue = Math.min(255, Math.max(0, blue * 2));

                int enhancedPixel = (red << 16) | (green << 8) | blue;
                enhancedImage.setRGB(x, y, enhancedPixel);
            }
        }

        return enhancedImage;
    }
}
