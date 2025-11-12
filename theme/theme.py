#作者:@纆泽@DeepSeek-R1
import os
import sys
import shutil
from PyQt5.QtWidgets import (QApplication, QWidget, QVBoxLayout, QHBoxLayout, 
                             QLabel, QRadioButton, QButtonGroup, QGroupBox,
                             QMessageBox, QTabWidget)
from PyQt5.QtCore import Qt
from PyQt5.QtGui import QFont

class ThemeSettings(QWidget):
    def __init__(self):
        super().__init__()
        # 配置文件路径 - 现在放在menu目录下
        self.config_file = "/data/data/com.termux/files/usr/change/menu/current_theme"
        
        # 从配置文件读取当前主题，如果没有则默认为原版
        self.current_theme = self.load_current_theme()
        
        self.init_ui()
        
    def init_ui(self):
        self.setWindowTitle('菜单主题设置')
        self.setGeometry(300, 300, 400, 350)
        
        # 创建主布局
        main_layout = QVBoxLayout()
        
        # 创建选项卡
        tab_widget = QTabWidget()
        
        # 主题设置选项卡
        theme_tab = QWidget()
        theme_layout = QVBoxLayout()
        
        # 当前主题显示区域
        current_theme_group = QGroupBox("当前主题")
        current_theme_layout = QVBoxLayout()
        
        self.current_theme_label = QLabel(f"当前主题: {self.current_theme}")
        self.current_theme_label.setFont(QFont("Arial", 12, QFont.Bold))
        self.current_theme_label.setAlignment(Qt.AlignCenter)
        self.current_theme_label.setStyleSheet("background-color: #f0f0f0; padding: 10px; border: 1px solid #ccc;")
        
        current_theme_layout.addWidget(self.current_theme_label)
        current_theme_group.setLayout(current_theme_layout)
        
        # 主题选项区域
        theme_options_group = QGroupBox("主题选项")
        theme_options_layout = QVBoxLayout()
        
        # 创建单选按钮组
        self.button_group = QButtonGroup(self)
        
        self.theme_original = QRadioButton("原版")
        self.theme_white = QRadioButton("纯净白")
        self.theme_orange = QRadioButton("活力橙")
        self.theme_blue = QRadioButton("深空蓝")
        self.theme_green = QRadioButton("森林绿")
        self.theme_purple = QRadioButton("温柔紫")
        
        # 根据当前主题设置选中状态
        self.set_theme_radio(self.current_theme)
        
        # 添加到按钮组
        self.button_group.addButton(self.theme_original, 1)
        self.button_group.addButton(self.theme_white, 2)
        self.button_group.addButton(self.theme_orange, 3)
        self.button_group.addButton(self.theme_blue, 4)
        self.button_group.addButton(self.theme_green, 5)
        self.button_group.addButton(self.theme_purple, 6)
        
        # 连接信号
        self.button_group.buttonClicked.connect(self.on_theme_changed)
        
        # 添加到布局
        theme_options_layout.addWidget(self.theme_original)
        theme_options_layout.addWidget(self.theme_white)
        theme_options_layout.addWidget(self.theme_orange)
        theme_options_layout.addWidget(self.theme_blue)
        theme_options_layout.addWidget(self.theme_green)
        theme_options_layout.addWidget(self.theme_purple)
        
        theme_options_group.setLayout(theme_options_layout)
        
        # 添加到主题选项卡布局
        theme_layout.addWidget(current_theme_group)
        theme_layout.addWidget(theme_options_group)
        theme_tab.setLayout(theme_layout)
        
        # 将选项卡添加到主选项卡控件
        tab_widget.addTab(theme_tab, "菜单主题设置")
        
        # 添加到主布局
        main_layout.addWidget(tab_widget)
        self.setLayout(main_layout)
    
    def load_current_theme(self):
        """从配置文件加载当前主题"""
        try:
            if os.path.exists(self.config_file):
                with open(self.config_file, 'r', encoding='utf-8') as f:
                    theme = f.read().strip()
                    return theme if theme else "原版"
        except Exception as e:
            print(f"读取主题配置文件失败: {e}")
        return "原版"
    
    def save_current_theme(self, theme):
        """保存当前主题到配置文件"""
        try:
            # 确保目录存在
            os.makedirs(os.path.dirname(self.config_file), exist_ok=True)
            with open(self.config_file, 'w', encoding='utf-8') as f:
                f.write(theme)
        except Exception as e:
            print(f"保存主题配置文件失败: {e}")
    
    def set_theme_radio(self, theme):
        """根据当前主题设置单选按钮状态"""
        theme_radio_map = {
            "原版": self.theme_original,
            "纯净白": self.theme_white,
            "活力橙": self.theme_orange,
            "深空蓝": self.theme_blue,
            "森林绿": self.theme_green,
            "温柔紫": self.theme_purple
        }
        
        radio = theme_radio_map.get(theme, self.theme_original)
        radio.setChecked(True)
    
    def on_theme_changed(self, button):
        """处理主题变更"""
        theme_map = {
            self.theme_original: ("原版", None),
            self.theme_white: ("纯净白", "white.png"),
            self.theme_orange: ("活力橙", "orange.png"),
            self.theme_blue: ("深空蓝", "blue.png"),
            self.theme_green: ("森林绿", "green.png"),
            self.theme_purple: ("温柔紫", "purple.png")
        }
        
        theme_name, wallpaper_file = theme_map.get(button, ("原版", None))
        
        # 更新当前主题显示
        self.current_theme = theme_name
        self.current_theme_label.setText(f"当前主题: {self.current_theme}")
        
        try:
            # 执行文件操作和配置更新
            self.perform_theme_change(theme_name, wallpaper_file)
            
            # 保存当前主题到配置文件
            self.save_current_theme(theme_name)
            
            QMessageBox.information(self, "成功", f"主题已切换为: {theme_name}")
            
        except Exception as e:
            QMessageBox.critical(self, "错误", f"切换主题时发生错误: {str(e)}")
    
    def perform_theme_change(self, theme_name, wallpaper_file):
        """执行主题变更的文件操作和配置修改"""
        
        # 定义路径
        bin_path = "/data/data/com.termux/files/usr/bin"
        menu_path = "/data/data/com.termux/files/usr/change/menu"
        wallpaper_source_path = "/data/data/com.termux/files/usr/change/wallpaper"
        wallpaper_target_path = "/data/data/com.termux/files/usr/wine-png"
        
        debox_py_path = os.path.join(bin_path, "debox.py")
        
        # 删除原有的debox.py
        if os.path.exists(debox_py_path):
            os.remove(debox_py_path)
        
        # 根据主题选择复制不同的文件
        if theme_name == "原版":
            source_file = os.path.join(menu_path, "debox-yb.py")
        else:
            source_file = os.path.join(menu_path, "debox-theme.py")
        
        # 复制文件并重命名
        if os.path.exists(source_file):
            destination_file = os.path.join(bin_path, "debox.py")
            shutil.copy2(source_file, destination_file)
            # 设置debox.py的执行权限
            os.chmod(destination_file, 0o755)
        else:
            raise FileNotFoundError(f"源文件不存在: {source_file}")
        
        # 对于非原版主题，更换壁纸
        if theme_name != "原版" and wallpaper_file:
            self.change_wallpaper(wallpaper_source_path, wallpaper_target_path, wallpaper_file)
    
    def change_wallpaper(self, source_path, target_path, wallpaper_file):
        """更换壁纸"""
        # 确保目标目录存在
        if not os.path.exists(target_path):
            os.makedirs(target_path)
        
        # 删除原有的壁纸文件
        old_wallpaper = os.path.join(target_path, "wallpaper.png")
        if os.path.exists(old_wallpaper):
            os.remove(old_wallpaper)
        
        # 复制新的壁纸文件
        source_wallpaper = os.path.join(source_path, wallpaper_file)
        if os.path.exists(source_wallpaper):
            target_wallpaper = os.path.join(target_path, "wallpaper.png")
            shutil.copy2(source_wallpaper, target_wallpaper)
            print(f"已更换壁纸: {source_wallpaper} -> {target_wallpaper}")
        else:
            raise FileNotFoundError(f"壁纸文件不存在: {source_wallpaper}")

def main():
    app = QApplication(sys.argv)
    
    # 设置应用样式
    app.setStyle('Fusion')
    
    window = ThemeSettings()
    window.show()
    
    sys.exit(app.exec_())

if __name__ == '__main__':
    main()