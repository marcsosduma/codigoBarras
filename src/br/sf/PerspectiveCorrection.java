package br.sf;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class PerspectiveCorrection {
    private static final double ROTATION_THRESHOLD = Math.toRadians(5);

    private static BufferedImage applyPerspectiveCorrection(BufferedImage originalImage, Point2D.Double[] srcPoints, Point2D.Double[] dstPoints) {
        // Calcular a matriz de transformação de perspectiva
        AffineTransform transform = calculatePerspectiveTransform(srcPoints, dstPoints);

        // Criar uma nova imagem com o tamanho da imagem original
        BufferedImage correctedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        // Aplicar a transformação de perspectiva
        Graphics2D g2d = correctedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, transform, null); // Use o método drawImage que aceita uma AffineTransform
        g2d.dispose();

        return correctedImage;
    }

    private static AffineTransform calculatePerspectiveTransform(Point2D.Double[] srcPoints, Point2D.Double[] dstPoints) {
        if (srcPoints.length < 4 || dstPoints.length < 4) {
            throw new IllegalArgumentException("Não há pontos suficientes para calcular a transformação de perspectiva.");
        }

        // Criar uma transformação afim para cada par de pontos correspondentes
        AffineTransform transform1 = AffineTransform.getTranslateInstance(-srcPoints[0].x, -srcPoints[0].y);
        AffineTransform transform2 = AffineTransform.getScaleInstance(
                (dstPoints[1].x - dstPoints[0].x) / (srcPoints[1].x - srcPoints[0].x),
                (dstPoints[3].y - dstPoints[0].y) / (srcPoints[3].y - srcPoints[0].y));
        AffineTransform transform3 = AffineTransform.getTranslateInstance(dstPoints[0].x, dstPoints[0].y);

        // Combinar as transformações afins (na ordem correta)
        AffineTransform transform = new AffineTransform();
        transform.concatenate(transform3);
        transform.concatenate(transform2);
        transform.concatenate(transform1);

        return transform;
    }


    public static BufferedImage applyPerspectiveCorrectionIfNeeded(BufferedImage originalImage) {
        BufferedImage grayImage = convertToGrayscale(originalImage);
        BufferedImage edgesImage = detectEdges(grayImage);
        Line2D.Double[] lines = detectLines(edgesImage);

        double averageRotation = calculateAverageRotation(lines);

        if (Math.abs(averageRotation) > ROTATION_THRESHOLD) {
            System.out.println("Imagem está significativamente rotacionada. Aplicando correção de perspectiva...");

            // Calcular pontos de origem e destino com base na inclinação
            Point2D.Double[] srcPoints = calculateSourcePoints(originalImage, averageRotation);
            Point2D.Double[] dstPoints = calculateDestinationPoints(originalImage, averageRotation);

            BufferedImage correctedImage = applyPerspectiveCorrection(originalImage, srcPoints, dstPoints);
            return correctedImage;
        } else {
            System.out.println("Imagem não requer correção de perspectiva. Rotação média: " + Math.toDegrees(averageRotation));
            return originalImage;
        }
    }

    private static Point2D.Double[] calculateSourcePoints(BufferedImage image, double rotation) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Ponto superior esquerdo permanece inalterado
        Point2D.Double topLeft = new Point2D.Double(0, 0);

        // Ajustar os pontos superior direito e inferior esquerdo com base na inclinação
        double radians = -rotation; // A inclinação é negativa para ajustar na direção oposta
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        Point2D.Double topRight = new Point2D.Double(width * cos, width * sin);
        Point2D.Double bottomLeft = new Point2D.Double(-height * sin, height * cos);

        // Ponto inferior direito permanece inalterado
        Point2D.Double bottomRight = new Point2D.Double(width, height);

        return new Point2D.Double[] { topLeft, topRight, bottomRight, bottomLeft };
    }

    private static Point2D.Double[] calculateDestinationPoints(BufferedImage image, double rotation) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Ponto superior esquerdo permanece inalterado
        Point2D.Double topLeft = new Point2D.Double(0, 0);

        // Ajustar os pontos superior direito e inferior esquerdo com base na inclinação
        double radians = -rotation; // A inclinação é negativa para ajustar na direção oposta
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        Point2D.Double topRight = new Point2D.Double(width * cos, 0);
        Point2D.Double bottomLeft = new Point2D.Double(0, height * cos);

        // Ponto inferior direito permanece inalterado
        Point2D.Double bottomRight = new Point2D.Double(width, height);

        return new Point2D.Double[] { topLeft, topRight, bottomRight, bottomLeft };
    }


    private static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return grayImage;
    }

    private static BufferedImage detectEdges(BufferedImage image) {
        float[] matrix = {
            -1, -1, -1,
            -1,  8, -1,
            -1, -1, -1
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel);
        return op.filter(image, null);
    }

    public static Line2D.Double[] detectLines(BufferedImage image) {
        HoughTransform houghTransform = new HoughTransform(image.getWidth(), image.getHeight(), 180);

        // Detectar bordas e adicionar pontos ao transformador de Hough
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0xFF) > 128) { // Detectar bordas brancas
                    houghTransform.addPoint(x, y);
                }
            }
        }

        // Obter linhas do transformador de Hough
        Line2D.Double[] lines = new Line2D.Double[0];
        try {
            lines = convertToLine2D(houghTransform.getLines(50)); // Ajuste o valor de threshold conforme necessário
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    private static Line2D.Double[] convertToLine2D(HoughTransform.Line[] lines) {
        Line2D.Double[] result = new Line2D.Double[lines.length];
        for (int i = 0; i < lines.length; i++) {
            double theta = lines[i].getTheta();
            double rho = lines[i].getRho();
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a * rho;
            double y0 = b * rho;
            double x1 = x0 + 1000 * (-b);
            double y1 = y0 + 1000 * (a);
            double x2 = x0 - 1000 * (-b);
            double y2 = y0 - 1000 * (a);
            result[i] = new Line2D.Double(x1, y1, x2, y2);
        }
        return result;
    }

    private static double calculateAverageRotation(Line2D.Double[] lines) {
        if (lines.length == 0) {
            return 0; // Se não houver linhas detectadas, não há rotação
        }

        double totalRotation = 0;
        int count = 0; // Contador para linhas horizontais ou verticais

        for (Line2D.Double line : lines) {
            // Calcular a inclinação da linha
            double angle = Math.abs(Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1()));

            // Verificar se a linha está quase horizontal ou vertical
            if ((angle < Math.toRadians(10) || angle > Math.toRadians(170)) && line.getY1() != line.getY2()) { // Ângulo próximo a 0 ou 180 graus
                totalRotation += angle;
                count++;
            }
        }

        // Verificar se há linhas horizontais ou verticais detectadas
        if (count == 0) {
            return 0; // Se não houver linhas horizontais ou verticais detectadas, não há rotação
        }

        // Calcular a média das inclinações
        return totalRotation / count;
    }
}
