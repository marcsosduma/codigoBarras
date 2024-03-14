package br.sf;

import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageRotation {

    public static BufferedImage rotateImage(BufferedImage image) {
        BufferedImage grayscaleImage = convertToGrayscale(image);
        BufferedImage edgeDetectedImage = detectEdges(grayscaleImage);
        double angle = detectRotationAngle(edgeDetectedImage);
        BufferedImage rotatedImage = rotate(image, angle);
        return rotatedImage;
    }

    private static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                grayscaleImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return grayscaleImage;
    }

    private static double detectRotationAngle(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Acumulador de votos para ângulos
        int[] votes = new int[180]; // Para ângulos de 0 a 179 graus

        // Detectar linhas usando a Transformada de Hough
        List<Line2D.Double> lines = detectLines(image, 100);

        // Considerar apenas 80% das linhas a partir do centro
        int startIndex = (int) (0.1 * lines.size());
        int endIndex = (int) (0.9 * lines.size());

        for (int i = startIndex; i < endIndex; i++) {
            Line2D.Double line = lines.get(i);
            double theta = Math.toDegrees(Math.atan2(line.y2 - line.y1, line.x2 - line.x1));
            int index = (int) Math.round(theta) + 90;
            votes[index]++;
        }

        // Encontrar o ângulo com o maior número de votos
        int maxVotesIndex = 0;
        int maxVotes = 0;
        for (int i = 0; i < votes.length; i++) {
            if (votes[i] > maxVotes) {
                maxVotes = votes[i];
                maxVotesIndex = i;
            }
        }

        // Converter o ângulo para a faixa de -90 a 89 graus
        double angle = maxVotesIndex - 90;

        return angle;
    }


        public static List<Line2D.Double> detectLines(BufferedImage image, int threshold) {
            // Convertendo a imagem para escala de cinza e aplicando detecção de bordas (Sobel, Canny, etc.)
            BufferedImage edgesImage = detectEdges(image);

            // Inicializando o acumulador de votos
            int maxTheta = 180; // Faixa de ângulos de 0 a 179 graus
            int width = image.getWidth();
            int height = image.getHeight();
            int maxRho = (int) Math.sqrt(width * width + height * height); // Comprimento máximo para rho
            int[][] accumulator = new int[maxTheta][maxRho];

            // Loop através de todos os pixels de borda e vote nos parâmetros das possíveis linhas
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((edgesImage.getRGB(x, y) & 0xFF) == 255) { // Se o pixel for uma borda
                        for (int theta = 0; theta < maxTheta; theta++) {
                            double radians = Math.toRadians(theta);
                            int rho = (int) (x * Math.cos(radians) + y * Math.sin(radians));
                            if (rho >= 0 && rho < maxRho) { // Verificando os limites do acumulador
                                accumulator[theta][rho]++; // Incrementando o voto para o ângulo atual e rho
                            }
                        }
                    }
                }
            }

            // Identificando os picos no acumulador
            List<Line2D.Double> lines = new ArrayList();
            for (int theta = 0; theta < maxTheta; theta++) {
                for (int rho = 0; rho < maxRho; rho++) {
                    if (accumulator[theta][rho] > threshold) { // Se o voto exceder o limiar, consideramos uma linha
                        double radians = Math.toRadians(theta);
                        double cosTheta = Math.cos(radians);
                        double sinTheta = Math.sin(radians);
                        double x0 = rho * cosTheta;
                        double y0 = rho * sinTheta;
                        double x1 = x0 + 1000 * (-sinTheta); // Comprimento suficiente para desenhar a linha
                        double y1 = y0 + 1000 * (cosTheta);
                        double x2 = x0 - 1000 * (-sinTheta);
                        double y2 = y0 - 1000 * (cosTheta);
                        lines.add(new Line2D.Double(x1, y1, x2, y2)); // Adicionando a linha detectada à lista
                    }
                }
            }

            return lines;
        }

            public static BufferedImage detectEdges(BufferedImage image) {
                int width = image.getWidth();
                int height = image.getHeight();

                BufferedImage edgesImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

                // Máscaras Sobel para detecção de bordas
                int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
                int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

                for (int y = 1; y < height - 1; y++) {
                    for (int x = 1; x < width - 1; x++) {
                        int gx = 0;
                        int gy = 0;

                        // Aplicando a máscara Sobel em cada pixel
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                gx += (image.getRGB(x + i, y + j) & 0xFF) * sobelX[i + 1][j + 1];
                                gy += (image.getRGB(x + i, y + j) & 0xFF) * sobelY[i + 1][j + 1];
                            }
                        }

                        // Calculando o gradiente
                        int gradient = (int) Math.sqrt(gx * gx + gy * gy);

                        // Limitando o valor do gradiente entre 0 e 255
                        gradient = Math.min(255, Math.max(0, gradient));

                        // Definindo o valor do gradiente como intensidade do pixel na imagem de bordas
                        edgesImage.setRGB(x, y, (gradient << 16) | (gradient << 8) | gradient);
                    }
                }

                return edgesImage;
            }



    private static BufferedImage rotate(BufferedImage image, double angle) {
        // Implemente aqui a rotação da imagem
        // Aqui, uma implementação simples de rotação
        // Você pode melhorar a qualidade da rotação usando algoritmos mais avançados
        BufferedImage rotatedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        double radians = Math.toRadians(angle);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double x0 = 0.5 * (image.getWidth() - 1);
        double y0 = 0.5 * (image.getHeight() - 1);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double a = x - x0;
                double b = y - y0;
                int xx = (int) (+a * cos - b * sin + x0);
                int yy = (int) (+a * sin + b * cos + y0);

                if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight()) {
                    rotatedImage.setRGB(x, y, image.getRGB(xx, yy));
                }
            }
        }

        return rotatedImage;
    }
}
