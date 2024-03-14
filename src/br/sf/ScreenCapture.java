package br.sf;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ScreenCapture extends JPanel implements MouseListener, MouseMotionListener {
    private BufferedImage image;
    private Point startPoint;
    private Point endPoint;
    private JFrame frame;

    public ScreenCapture(BufferedImage image) {
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
        if (startPoint != null && endPoint != null) {
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            int width = Math.abs(startPoint.x - endPoint.x);
            int height = Math.abs(startPoint.y - endPoint.y);
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(x, y, width, height);
            g.setColor(Color.RED);
            g.drawRect(x, y, width, height);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        endPoint = null;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        endPoint = e.getPoint();
        repaint();
        saveSelectedArea();
        if (frame != null) {
            frame.dispose(); // Fecha a tela de captura se frame não for nulo
        }
        //showCapturedImage();
        restartCapture(); // Reinicia a captura após clicar em "OK"
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        endPoint = e.getPoint();
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    private void saveSelectedArea() {
        if (startPoint == null || endPoint == null) {
            return;
        }
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        try {
            BufferedImage selectedImage = image.getSubimage(x, y, width, height);
            File outputFile = new File("selected_area.png");
            ImageIO.write(selectedImage, "png", outputFile);
            System.out.println("Área selecionada salva com sucesso em: " + outputFile.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showCapturedImage() {
        final JFrame captureFrame = new JFrame("Imagem Capturada");
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartCapture();
                captureFrame.dispose();
            }
        });
        captureFrame.setLayout(new BorderLayout());
        captureFrame.add(imageLabel, BorderLayout.CENTER);
        captureFrame.add(okButton, BorderLayout.SOUTH);
        captureFrame.pack();
        captureFrame.setLocationRelativeTo(null);
        captureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        captureFrame.setVisible(true);
    }
    
    private void restartCapture() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Robot robot = new Robot();
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Rectangle screenRect = new Rectangle(screenSize);
                    BufferedImage image = robot.createScreenCapture(screenRect);

                    JFrame initialFrame = new JFrame("Captura Tela");
                    initialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    CaptureScreenPanel captureScreenPanel = new CaptureScreenPanel(image);
                    initialFrame.add(captureScreenPanel);
                    initialFrame.pack();
                    initialFrame.setLocationRelativeTo(null);
                    initialFrame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Robot robot = new Robot();
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Rectangle screenRect = new Rectangle(screenSize);
                    BufferedImage image = robot.createScreenCapture(screenRect);

                    JFrame initialFrame = new JFrame("Captura Tela");
                    initialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    CaptureScreenPanel captureScreenPanel = new CaptureScreenPanel(image);
                    initialFrame.add(captureScreenPanel);
                    initialFrame.pack();
                    initialFrame.setLocationRelativeTo(null);
                    initialFrame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}

class CaptureScreenPanel extends JPanel {
    private BufferedImage image;
    private JButton captureButton;

    public CaptureScreenPanel(BufferedImage image) {
        this.image = image;
        setLayout(new BorderLayout());
        captureButton = new JButton("Capturar");
        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Captura a tela somente após o clique no botão "Capturar"
                captureScreen();
            }
        });
        add(captureButton, BorderLayout.CENTER);
    }

    private void captureScreen() {
        // Obtém a janela pai do painel atual
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.dispose(); // Fecha a tela de captura

        // Inicia a captura da tela completa
        try {
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            BufferedImage image = robot.createScreenCapture(screenRect);

            JFrame captureFrame = new JFrame("Screen Capture");
            ScreenCapture screenCapturePanel = new ScreenCapture(image);
            captureFrame.add(screenCapturePanel);
            captureFrame.pack();
            captureFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            captureFrame.setLocationRelativeTo(null);
            captureFrame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}