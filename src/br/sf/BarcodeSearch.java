package br.sf;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class BarcodeSearch {

    public static String stringCode(BufferedImage image) {
        String code = "Not Found....";
        try {
            // Escalona a imagem para o dobro do tamanho original
            BufferedImage scaledImage = scaleImage(image, 2);

            // Convertendo a imagem para tons de cinza
            BufferedImage grayscaleImage = convertToGrayscale(scaledImage);

            // Remoção de ruído
            BufferedImage denoisedImage = removeNoise(grayscaleImage);

            // Aprimoramento de contraste adaptativo
            BufferedImage enhancedImage = enhanceContrast(denoisedImage);

            // Detecção de bordas
            BufferedImage edgeDetectedImage = detectEdges(enhancedImage);

            // Detecção de códigos de barras
            code = detectBarcode(edgeDetectedImage);
        } catch (ReaderException e) {
            System.out.println("No barcode found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static BufferedImage scaleImage(BufferedImage image, double scale) {
        int scaledWidth = (int) (image.getWidth() * scale);
        int scaledHeight = (int) (image.getHeight() * scale);
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return scaledImage;
    }

    public static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return grayImage;
    }

    public static BufferedImage removeNoise(BufferedImage image) {
        // Aplicando um filtro de mediana adaptativo para remoção de ruído
        int width = image.getWidth();
        int height = image.getHeight();
        int kernelSize = 3; // Tamanho do kernel
        int radius = kernelSize / 2;
        BufferedImage denoisedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Itera sobre cada pixel da imagem
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Calcula a mediana dos pixels na vizinhança do pixel atual
                int median = calculateAdaptiveMedian(image, x, y, kernelSize);
                // Define o valor do pixel na imagem denoisedImage
                denoisedImage.setRGB(x, y, median << 16 | median << 8 | median);
            }
        }

        return denoisedImage;
    }

    public static int calculateAdaptiveMedian(BufferedImage image, int x, int y, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] values = new int[kernelSize * kernelSize];
        int index = 0;

        // Itera sobre a janela do kernel
        for (int ky = -kernelSize / 2; ky <= kernelSize / 2; ky++) {
            for (int kx = -kernelSize / 2; kx <= kernelSize / 2; kx++) {
                int nx = x + kx;
                int ny = y + ky;

                // Verifica os limites da imagem
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    // Obtém o valor do pixel na posição (nx, ny)
                    values[index++] = image.getRGB(nx, ny) & 0xFF;
                }
            }
        }

        // Ordena os valores dos pixels
        Arrays.sort(values);

        // Calcula a mediana dos valores dos pixels
        int median;
        if (index % 2 == 0) {
            median = (values[index / 2] + values[index / 2 - 1]) / 2;
        } else {
            median = values[index / 2];
        }

        return median;
    }

    public static BufferedImage enhanceContrast(BufferedImage image) {
        // Realiza o esticamento de contraste
        BufferedImage enhancedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        int minPixel = 255;
        int maxPixel = 0;

        // Encontra os valores mínimo e máximo dos pixels na imagem
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                if (pixel < minPixel) {
                    minPixel = pixel;
                }
                if (pixel > maxPixel) {
                    maxPixel = pixel;
                }
            }
        }

        // Realiza o esticamento de contraste
        double contrastFactor = 255.0 / (maxPixel - minPixel);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                int newPixel = (int) ((pixel - minPixel) * contrastFactor);
                enhancedImage.setRGB(x, y, newPixel << 16 | newPixel << 8 | newPixel);
            }
        }

        return enhancedImage;
    }

    public static BufferedImage detectEdges(BufferedImage image) {
        // Implemente aqui o algoritmo de detecção de bordas
        // Por exemplo, operador Sobel ou Canny
        // Veja: https://en.wikipedia.org/wiki/Edge_detection
        return image; // Retorno temporário, você deve implementar a detecção de bordas
    }

    private static String detectBarcode(BufferedImage image) throws Exception {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(bitmap);
        return result.getText();
    }

    public static void main(String[] args) {
        try {
            File file = new File("C:\\docs\\codigo_barra_exemplo.png");
            BufferedImage image = ImageIO.read(file);
            String result = stringCode(image);
            System.out.println("Barcode text is " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
