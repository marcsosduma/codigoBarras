package br.sf;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class AdvancedSharpenFilter {

    public static BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage sharpenedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Definir a máscara gaussiana para a suavização
        float[] smoothKernel = {
                1.0f / 16, 2.0f / 16, 1.0f / 16,
                2.0f / 16, 4.0f / 16, 2.0f / 16,
                1.0f / 16, 2.0f / 16, 1.0f / 16
        };

        // Aplicar a suavização para reduzir o ruído
        BufferedImage smoothedImg = applyFilter(image, smoothKernel);

        // Calcular a máscara de nitidez (diferença entre a imagem original e a suavizada)
        float[] sharpMask = {
                -1.0f, -1.0f, -1.0f,
                -1.0f,  9.0f, -1.0f,
                -1.0f, -1.0f, -1.0f
        };

        // Aplicar a máscara de nitidez
        BufferedImage sharpMaskedImg = applyFilter(smoothedImg, sharpMask);

        return sharpMaskedImg;
    }

    private static BufferedImage applyFilter(BufferedImage image, float[] kernel) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Kernel convolutionKernel = new Kernel(3, 3, kernel);
        ConvolveOp convolveOp = new ConvolveOp(convolutionKernel, ConvolveOp.EDGE_NO_OP, null);
        convolveOp.filter(image, filteredImage);

        return filteredImage;
    }
}
