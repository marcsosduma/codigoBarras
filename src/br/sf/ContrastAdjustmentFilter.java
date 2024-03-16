package br.sf;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ContrastAdjustmentFilter {
    public static BufferedImage apply(BufferedImage image, double factor) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage adjustedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Aplica o ajuste de contraste para cada pixel da imagem
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // Ajusta os componentes de cor de acordo com o fator de ajuste de contraste
                int adjustedRed = adjustComponent(red, factor);
                int adjustedGreen = adjustComponent(green, factor);
                int adjustedBlue = adjustComponent(blue, factor);

                // Define o valor do pixel ajustado na imagem de saída
                Color adjustedColor = new Color(adjustedRed, adjustedGreen, adjustedBlue);
                adjustedImage.setRGB(x, y, adjustedColor.getRGB());
            }
        }

        return adjustedImage;
    }

    private static int adjustComponent(int component, double factor) {
        // Ajusta o componente de cor usando o fator de ajuste de contraste
        double adjustedComponent = (component / 255.0 - 0.5) * factor + 0.5;
        adjustedComponent = Math.min(1.0, Math.max(0.0, adjustedComponent)); // Limita o intervalo de 0 a 1
        return (int) (adjustedComponent * 255);
    }
}
