package br.sf;

import java.awt.image.BufferedImage;

public class ErosionFilter {
    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage erodedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Define o tamanho do elemento estruturante para a erosão
        int kernelSize = 3;
        int[][] kernel = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };

        // Aplica a erosão para cada pixel da imagem
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // Inicializa o mínimo local como o valor do pixel atual
                int minIntensity = (image.getRGB(x, y) & 0xFF);

                // Percorre a vizinhança do pixel atual usando o elemento estruturante
                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        if (kernel[ky][kx] == 1) {
                            int neighborIntensity = (image.getRGB(x + kx - 1, y + ky - 1) & 0xFF);
                            minIntensity = Math.min(minIntensity, neighborIntensity);
                        }
                    }
                }

                // Define o valor do pixel erodido como o mínimo local encontrado
                int erodedPixel = (255 << 24) | (minIntensity << 16) | (minIntensity << 8) | minIntensity;
                erodedImage.setRGB(x, y, erodedPixel);
            }
        }

        return erodedImage;
    }
}
