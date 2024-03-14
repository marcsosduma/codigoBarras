package br.sf;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class Principal {
    public static void main(String[] args) {
        try {
            File file = new File("C:\\docs\\codigo_barra_exemplo.png");
            BufferedImage image = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(bitmap);
            System.out.println("Barcode text is " + result.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
