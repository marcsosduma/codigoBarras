package br.sf;

import java.awt.image.BufferedImage;

public class NoiseReduction {

    public static BufferedImage applyGaussianBlur(BufferedImage image, int radius) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double[][] kernel = generateGaussianKernel(radius);

        // Aplicar o filtro Gaussiano à imagem
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgb = applyKernel(image, kernel, x, y);
                result.setRGB(x, y, rgbToInt(rgb));
            }
        }

        return result;
    }

    private static double[][] generateGaussianKernel(int radius) {
        int size = radius * 2 + 1;
        double[][] kernel = new double[size][size];
        double sigma = radius / 3.0;

        double sum = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                kernel[x + radius][y + radius] = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
                sum += kernel[x + radius][y + radius];
            }
        }

        // Normalizar o kernel para garantir que a soma de todos os elementos seja 1
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        return kernel;
    }

    private static int[] applyKernel(BufferedImage image, double[][] kernel, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();
        int radius = kernel.length / 2;
        double[] sum = {0, 0, 0};

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int pixelX = clamp(x + i, 0, width - 1);
                int pixelY = clamp(y + j, 0, height - 1);
                int[] rgb = getRGB(image.getRGB(pixelX, pixelY));
                for (int k = 0; k < 3; k++) {
                    sum[k] += rgb[k] * kernel[i + radius][j + radius];
                }
            }
        }

        return new int[]{(int) sum[0], (int) sum[1], (int) sum[2]};
    }

    private static int[] getRGB(int rgb) {
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
    }

    private static int rgbToInt(int[] rgb) {
        return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
