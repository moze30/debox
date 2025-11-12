import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProgressBarPurple {
    private JFrame frame;
    private JProgressBar progressBar;
    private JLabel label;
    private volatile boolean running = true;
    
    public ProgressBarPurple(String title) {
        // 直接在EDT中创建GUI
        if (SwingUtilities.isEventDispatchThread()) {
            createAndShowGUI(title);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> createAndShowGUI(title));
            } catch (Exception e) {
                System.err.println("GUI初始化失败: " + e.getMessage());
                System.exit(1);
            }
        }
    }
    
    private void createAndShowGUI(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 简约风格背景
                g2d.setColor(new Color(245, 245, 245));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // 细边框
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                
                if (getValue() > 0) {
                    int width = (int) ((getWidth() - 2) * ((double) getValue() / getMaximum()));
                    
                    // 紫色填充
                    g2d.setColor(new Color(138, 43, 226)); // 紫罗兰色
                    g2d.fillRect(1, 1, width, getHeight() - 2);
                    
                    // 进度文本
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                    String text = getValue() + "%";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int x = (getWidth() - textWidth) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    
                    // 只在进度条上绘制文本
                    if (width > textWidth + 10) {
                        g2d.drawString(text, x, y);
                    }
                }
                
                g2d.dispose();
            }
        };
        
        progressBar.setPreferredSize(new Dimension(350, 18));
        
        label = new JLabel("正在初始化...", JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(new Color(100, 100, 100));
        
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.toFront();
    }
    
    public void updateProgress(int value, String message) {
        if (!running) return;
        
        SwingUtilities.invokeLater(() -> {
            if (progressBar != null) {
                progressBar.setValue(Math.min(100, Math.max(0, value)));
                label.setText(message);
            }
        });
    }
    
    public void close() {
        running = false;
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
            }
        });
    }
    
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("未处理的异常: " + throwable.getMessage());
            System.exit(1);
        });
        
        SwingUtilities.invokeLater(() -> {
            String title = args.length > 0 ? args[0] : "进度";
            final ProgressBarPurple pb = new ProgressBarPurple(title);
            
            Thread inputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                    String line;
                    while ((line = reader.readLine()) != null && pb.running) {
                        if (line.equals("CLOSE")) {
                            pb.close();
                            break;
                        }
                        
                        String[] parts = line.split("\\|", 2);
                        if (parts.length == 2) {
                            try {
                                int progress = Integer.parseInt(parts[0].trim());
                                String message = parts[1].trim();
                                pb.updateProgress(progress, message);
                            } catch (NumberFormatException e) {
                                System.err.println("进度格式错误: " + line);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("输入处理错误: " + e.getMessage());
                } finally {
                    pb.close();
                    System.exit(0);
                }
            });
            
            inputThread.setDaemon(true);
            inputThread.start();
        });
    }
}
