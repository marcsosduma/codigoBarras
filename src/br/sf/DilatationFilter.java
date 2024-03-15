package br.sf;

import java.awt.image.BufferedImage;

public class DilatationFilter {
    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage dilatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Define o tamanho do elemento estruturante para a dilatação
        int kernelSize = 3;
        int[][] kernel = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };

        // Aplica a dilatação para cada pixel da imagem
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // Inicializa o máximo local como o valor do pixel atual
                int maxIntensity = (image.getRGB(x, y) & 0xFF);

                // Percorre a vizinhança do pixel atual usando o elemento estruturante
                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        if (kernel[ky][kx] == 1) {
                            int neighborIntensity = (image.getRGB(x + kx - 1, y + ky - 1) & 0xFF);
                            maxIntensity = Math.max(maxIntensity, neighborIntensity);
                        }
                    }
                }

                // Define o valor do pixel dilatado como o máximo local encontrado
                int dilatedPixel = (255 << 24) | (maxIntensity << 16) | (maxIntensity << 8) | maxIntensity;
                dilatedImage.setRGB(x, y, dilatedPixel);
            }
        }

        return dilatedImage;
    }
}
