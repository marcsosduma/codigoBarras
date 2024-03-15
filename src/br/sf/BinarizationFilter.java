package br.sf;

import java.awt.image.BufferedImage;

public class BinarizationFilter {
    
    // Método para aplicar binarização em uma imagem
    public static BufferedImage apply(BufferedImage image) {
        // Converte a imagem para tons de cinza
        BufferedImage grayscaleImage = convertToGrayscale(image);
        
        // Aplica a binarização
        BufferedImage binaryImage = binarize(grayscaleImage);
        
        return binaryImage;
    }
    
    // Converte a imagem para tons de cinza
    private static BufferedImage convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b); // Conversão para tom de cinza
                int grayValue = (gray << 16) + (gray << 8) + gray; // Cria o valor do pixel em tons de cinza
                grayscaleImage.setRGB(x, y, grayValue);
            }
        }
        
        return grayscaleImage;
    }
    
    // Aplica a binarização na imagem em tons de cinza
    private static BufferedImage binarize(BufferedImage grayscaleImage) {
        int width = grayscaleImage.getWidth();
        int height = grayscaleImage.getHeight();
        
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        
        int threshold = calculateThreshold(grayscaleImage); // Calcula o limiar de binarização
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grayscaleImage.getRGB(x, y) & 0xFF; // Obtém o valor do pixel em tons de cinza
                int binaryValue = (gray < threshold) ? 0xFF000000 : 0xFFFFFFFF; // Aplica a binarização
                binaryImage.setRGB(x, y, binaryValue);
            }
        }
        
        return binaryImage;
    }
    
    // Calcula o limiar de binarização usando o método de Otsu
    private static int calculateThreshold(BufferedImage grayscaleImage) {
        int[] histogram = new int[256];
        int totalPixels = grayscaleImage.getWidth() * grayscaleImage.getHeight();
        
        // Calcula o histograma da imagem em tons de cinza
        for (int y = 0; y < grayscaleImage.getHeight(); y++) {
            for (int x = 0; x < grayscaleImage.getWidth(); x++) {
                int gray = grayscaleImage.getRGB(x, y) & 0xFF;
                histogram[gray]++;
            }
        }
        
        // Calcula a média ponderada dos histogramas
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }
        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float varMax = 0;
        int threshold = 0;
        
        for (int i = 0; i < 256; i++) {
            wB += histogram[i]; // Weight Background
            if (wB == 0) continue;
            wF = totalPixels - wB; // Weight Foreground
            if (wF == 0) break;
            
            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB; // Mean Background
            float mF = (sum - sumB) / wF; // Mean Foreground
            
            // Calculate Between Class Variance
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
            
            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
        
        return threshold;
    }
}
