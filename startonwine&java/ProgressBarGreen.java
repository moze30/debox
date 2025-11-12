import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProgressBarGreen {
    private JFrame frame;
    private JProgressBar progressBar;
    private JLabel label;
    private volatile boolean running = true;
    
    public ProgressBarGreen(String title) {
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
        
        // 绿色科技风格进度条
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 深色背景
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // 进度轨道
                g2d.setColor(new Color(60, 60, 60));
                g2d.fillRect(2, 2, getWidth() - 4, getHeight() - 4);
                
                if (getValue() > 0) {
                    int width = (int) ((getWidth() - 4) * ((double) getValue() / getMaximum()));
                    
                    // 科技绿色渐变
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 255, 150), 
                                                              width, 0, new Color(0, 200, 100));
                    g2d.setPaint(gradient);
                    g2d.fillRect(2, 2, width, getHeight() - 4);
                    
                    // 添加光晕效果
                    g2d.setColor(new Color(0, 255, 150, 100));
                    for (int i = 0; i < 3; i++) {
                        g2d.drawRect(2 + i, 2 + i, width - i * 2, getHeight() - 4 - i * 2);
                    }
                }
                
                // 白色进度文本
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                String text = getValue() + "%";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        progressBar.setPreferredSize(new Dimension(350, 20));
        
        label = new JLabel("正在初始化...", JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(0, 255, 150));
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.BLACK);
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
            final ProgressBarGreen pb = new ProgressBarGreen(title);
            
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
