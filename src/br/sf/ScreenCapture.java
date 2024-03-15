package br.sf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ScreenCapture extends JPanel implements MouseListener, MouseMotionListener {
    private BufferedImage image;
    private Point startPoint;
    private Point endPoint;
    private JFrame initialFrame; // Referência ao JFrame initialFrame

    public ScreenCapture(BufferedImage image, JFrame initialFrame) {
        this.image = image;
        this.initialFrame = initialFrame; // Inicializa a initialFrame
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
        if (initialFrame != null) { 
            initialFrame.dispose(); // Fecha o initialFrame
        }
        setCursor(Cursor.getDefaultCursor());
        try {
        	showCapturedImage(); // Mostra apenas a imagem selecionada
        }catch(Exception ee) {
        	//null
        }
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
        if (startPoint == null || endPoint == null || endPoint.x==startPoint.x
        		|| endPoint.y==startPoint.y) {
        	restartCapture();
            return;
        }
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        try {
            BufferedImage selectedImage = image.getSubimage(x, y, width, height);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void closePreviousFrames() {
        Frame[] frames = Frame.getFrames();
        for (Frame frame : frames) {
            if (frame instanceof JFrame) {
                frame.dispose();
            }
        }
    }

    private void showCapturedImage() throws IOException {
        // Determina a área selecionada
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        closePreviousFrames();

        // Obtém a subimagem da área selecionada
        BufferedImage selectedImage = image.getSubimage(x, y, width, height);

        StringBuilder barcode = new StringBuilder("");
        
        BufferedImage newImage =  BarcodeSearch.stringCode(selectedImage, barcode);

        // Exibe a imagem selecionada em um novo JFrame
        final JFrame selectedImageFrame = new JFrame("Imagem Selecionada");
        String iconName = "icon.png";
        URL iconURL = ScreenCapture.class.getResource("/" + iconName);
        if (iconURL != null) {
            BufferedImage iconImage = ImageIO.read(iconURL);
            selectedImageFrame.setIconImage(iconImage);
        } 
        // Cria um JPanel para organizar os componentes
        JPanel panel = new JPanel(new BorderLayout());
        
        // Adiciona o JPanel ao JFrame
        selectedImageFrame.add(panel);

        JLabel lb1 = new JLabel("Código: ");
        JLabel lb2 = new JLabel(barcode.toString());
        StringSelection stringSelection = new StringSelection(barcode.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        lb2.setForeground(Color.RED); // Define a cor para lb2, se desejar
        
        // Adiciona lb1 e lb2 ao JPanel
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(lb1);
        labelPanel.add(lb2);
        panel.add(labelPanel, BorderLayout.NORTH);
        
        JLabel selectedImageLabel = new JLabel(new ImageIcon(newImage));
        
        // Adiciona a imagem ao JPanel
        panel.add(selectedImageLabel, BorderLayout.CENTER);

        // Cria e adiciona o botão "Fechar"
        JButton closeButton = new JButton("Voltar");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedImageFrame.dispose(); // Fecha a janela da imagem selecionada
                restartCapture(); // Reinicia a captura após fechar a imagem selecionada
            }
        });
        panel.add(closeButton, BorderLayout.SOUTH);

        selectedImageFrame.pack();
        //selectedImageFrame.setLocationRelativeTo(null);
        selectedImageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Centraliza o JFrame horizontalmente e posiciona-o no topo da tela
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int xx = (dim.width - selectedImageFrame.getSize().width) / 2;
        if(xx<0) xx = 0;
        int yy = 50; // Posiciona o JFrame no topo da tela
        selectedImageFrame.setLocation(xx, yy);
        
        selectedImageFrame.setVisible(true);
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

                    JFrame captureFrame = new JFrame("Captura Tela");
                    captureFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    CaptureScreenPanel captureScreenPanel = new CaptureScreenPanel(image);
                    captureFrame.add(captureScreenPanel);
                    String iconName = "icon.png";
                    URL iconURL = ScreenCapture.class.getResource("/" + iconName);
                    if (iconURL != null) {
                        BufferedImage iconImage = ImageIO.read(iconURL);
                        captureFrame.setIconImage(iconImage);
                    } 

                    captureFrame.pack();
                    //captureFrame.setLocationRelativeTo(null);
                    // Centraliza o JFrame horizontalmente e posiciona-o no topo da tela
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = (dim.width - captureFrame.getSize().width) / 2;
                    int y = 50; // Posiciona o JFrame no topo da tela
                    captureFrame.setLocation(x, y);
                    
                    captureFrame.setVisible(true);
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

                    final JFrame initialFrame = new JFrame("Captura Tela");
                    initialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    CaptureScreenPanel captureScreenPanel = new CaptureScreenPanel(image);
                    initialFrame.add(captureScreenPanel);
                    initialFrame.pack();
                    initialFrame.setLocationRelativeTo(null);
                    String iconName = "icon.png";
                    URL iconURL = ScreenCapture.class.getResource("/" + iconName);
                    if (iconURL != null) {
                        BufferedImage iconImage = ImageIO.read(iconURL);
                        initialFrame.setIconImage(iconImage);
                    } 
                    
                 // Centraliza o JFrame horizontalmente e posiciona-o no topo da tela
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = (dim.width - initialFrame.getSize().width) / 2;
                    int y = 50; // Posiciona o JFrame no topo da tela
                    initialFrame.setLocation(x, y);
                    
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
        setPreferredSize(new Dimension(250, 50));
        captureButton = new JButton("Capturar");
        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureScreen();
            }
        });
        add(captureButton, BorderLayout.CENTER);
    }

    private void captureScreen() {
        try {
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            BufferedImage image = robot.createScreenCapture(screenRect);

            JFrame captureFrame = new JFrame("Screen Capture");
            ScreenCapture screenCapturePanel = new ScreenCapture(image, (JFrame) SwingUtilities.getWindowAncestor(this));
            captureFrame.add(screenCapturePanel);
            captureFrame.setUndecorated(true);
            captureFrame.pack();
            captureFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            captureFrame.setLocationRelativeTo(null);
            captureFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            captureFrame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
