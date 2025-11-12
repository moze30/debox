import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProgressBar {
    private JFrame frame;
    private JProgressBar progressBar;
    private JLabel label;
    
    public ProgressBar(String title) {
        // 直接创建GUI组件，不使用系统LookAndFeel
        frame = new JFrame(title);
        progressBar = new JProgressBar(0, 100);
        label = new JLabel("正在初始化...", JLabel.CENTER);
        
        // 设置进度条属性
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        
        // 设置字体
        Font font = new Font("SansSerif", Font.PLAIN, 12);
        label.setFont(font);
        progressBar.setFont(font);
        
        // 布局
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        frame.getContentPane().add(panel);
        
        // 窗口设置
        frame.setSize(400, 120);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        
        // 确保窗口显示在最前面
        frame.toFront();
    }
    
    public void updateProgress(int value, String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setValue(value);
            label.setText(message);
        } else {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(value);
                label.setText(message);
            });
        }
    }
    
    public void close() {
        if (SwingUtilities.isEventDispatchThread()) {
            frame.dispose();
        } else {
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
            });
        }
    }
    
    public static void main(String[] args) {
        // 在EDT中创建GUI
        SwingUtilities.invokeLater(() -> {
            String title = args.length > 0 ? args[0] : "进度";
            final ProgressBar pb = new ProgressBar(title);
            
            // 在后台线程中处理输入
            Thread inputThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("CLOSE")) {
                            pb.close();
                            break;
                        }
                        
                        // 解析进度和消息，格式: "百分比|消息"
                        String[] parts = line.split("\\|", 2);
                        if (parts.length == 2) {
                            try {
                                int progress = Integer.parseInt(parts[0].trim());
                                String message = parts[1].trim();
                                pb.updateProgress(progress, message);
                            } catch (NumberFormatException e) {
                                // 忽略格式错误
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略异常，正常退出
                } finally {
                    System.exit(0);
                }
            });
            inputThread.setDaemon(true);
            inputThread.start();
        });
    }
}