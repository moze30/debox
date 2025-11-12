import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProgressBarOrange {
    private JFrame frame;
    private JProgressBar progressBar;
    private JLabel label;
    private JLabel percentageLabel;
    private volatile boolean running = true;
    
    public ProgressBarOrange(String title) {
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
                
                // 绘制背景
                g2d.setColor(new Color(250, 250, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // 绘制进度
                if (getValue() > 0) {
                    int width = (int) ((getWidth() - 4) * ((double) getValue() / getMaximum()));
                    
                    // 橙色到红色渐变
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 165, 0), 
                                                              width, 0, new Color(255, 100, 0));
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(2, 2, width, getHeight() - 4, 12, 12);
                    
                    // 内阴影效果
                    g2d.setColor(new Color(255, 200, 100));
                    g2d.drawRoundRect(2, 2, width, getHeight() - 4, 12, 12);
                }
                
                // 边框
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                g2d.dispose();
            }
        };
        
        progressBar.setPreferredSize(new Dimension(350, 30));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        
        // 百分比标签（单独显示）
        percentageLabel = new JLabel("0%", JLabel.CENTER);
        percentageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        percentageLabel.setForeground(new Color(255, 100, 0));
        
        // 状态标签
        label = new JLabel("正在初始化...", JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(new Color(80, 80, 80));
        
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(percentageLabel, BorderLayout.EAST);
        progressPanel.setBackground(Color.WHITE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(label, BorderLayout.NORTH);
        mainPanel.add(progressPanel, BorderLayout.CENTER);
        
        frame.getContentPane().add(mainPanel);
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
                percentageLabel.setText(value + "%");
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
            final ProgressBarOrange pb = new ProgressBarOrange(title);
            
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