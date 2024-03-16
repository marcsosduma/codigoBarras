package br.sf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;

public class BarcodeSearch {

	public static BufferedImage stringCode(BufferedImage image, StringBuilder code) {
	    BufferedImage newImage = null;
	    String valor = "";
	    code.append("");
	    for (int m = 0; m < 1; m++) {
	        try {
	            valor = "Not Found....";
	            code.setLength(0);
	            
	            newImage = image;
	            
	            if(m==0) {
		            newImage = MedianFilter.apply(newImage, 1);
		            newImage = convertToGrayscale(newImage);
		            newImage = ContrastAdjustmentFilter.apply(newImage, 1.2);
		            newImage = NoiseReduction.applyGaussianBlur(newImage, 1);
		            newImage = scaleImage(newImage, 3);
		            //newImage = convertToBlackAndWhite(newImage);
		            newImage = ContrastAdjustmentFilter.apply(newImage, 2.0);
		            newImage = PerspectiveCorrection.applyPerspectiveCorrectionIfNeeded(newImage);
	            }else if(m==1){
		            
		            /*
		            newImage = convertToGrayscale(newImage);
		            newImage = HistogramEqualizationFilter.apply(newImage);
		            newImage = DilatationFilter.apply(newImage);
		            newImage = ErosionFilter.apply(newImage);
		            newImage = SmoothingFilter.apply(newImage);
		            newImage = ContrastAdjustmentFilter.apply(newImage, 2.8);
		            */
		            // código atual
	                // Remoção de ruido e melhora da imagem, especialmente para codigo de barras
	                newImage = MedianFilter.apply(newImage, 1);
	                newImage = AdvancedSharpenFilter.apply(newImage);
		            newImage = scaleImage(newImage, 2);
	                newImage = convertToBlackAndWhite(newImage);
	                newImage = NoiseReduction.applyGaussianBlur(newImage, 1);
	                newImage = enhanceContrast(newImage);
	            }
                
                // Deteccao do codigo de barras
	            valor = detectBarcode(newImage);
	            code = code.append(valor);
	            break;
	        } catch (ReaderException e) {
	            code = code.append(valor);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return newImage;
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

    public static BufferedImage convertToBlackAndWhite(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        
        // Percorre cada pixel da imagem original
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Obtem o valor de intensidade do pixel na imagem original
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;
                
                // Calcula o valor médio da intensidade para decidir se sera branco ou preto
                int averageIntensity = (red + green + blue) / 3;

                // Define o pixel correspondente na imagem binaria como branco ou preto
                int binaryValue = (averageIntensity < 128) ? 0xFF000000 : 0xFFFFFFFF;
                binaryImage.setRGB(x, y, binaryValue);
            }
        }

        return binaryImage;
    }
    
    public static BufferedImage convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                int grayValue = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);
                int grayRGB = new Color(grayValue, grayValue, grayValue).getRGB();
                grayImage.setRGB(x, y, grayRGB);
            }
        }
        return grayImage;
    }

    public static BufferedImage enhanceContrast(BufferedImage image) {
        // Aplica um simples esticamento de contraste

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

    private static String detectBarcode(BufferedImage image) throws Exception {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        MultiFormatReader reader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap(DecodeHintType.class);
        
        List<BarcodeFormat> formats = new ArrayList();
        for (BarcodeFormat format : BarcodeFormat.values()) {
            formats.add(format);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        
        reader.setHints(hints);
        MultipleBarcodeReader decoder = new GenericMultipleBarcodeReader(reader);
        Result result[] = decoder.decodeMultiple(bitmap, hints);
        return result[0].getText();
    }

    public static void main(String[] args) {
        try {
            File file = new File("codigo_barra_exemplo.png");
            BufferedImage image = ImageIO.read(file);
            StringBuilder result = new StringBuilder("");
            BufferedImage newImage = stringCode(image, result);
            System.out.println("Barcode text is " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
