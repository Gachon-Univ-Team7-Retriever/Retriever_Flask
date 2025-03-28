import sys
import os
from PyQt5.QtWebEngineWidgets import QWebEngineView
from PyQt5.QtWidgets import QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QPushButton, QTextEdit, QFileDialog, QLabel
from PyQt5.QtCore import QUrl
from preprocess.extractor import extract_promotion_content 
from crawl.crawler import google_search
from bs4 import BeautifulSoup
import pandas as pd

class DrugPromotionDetector(QMainWindow):
    def __init__(self):
        super().__init__()
        self.initUI()
        self.current_file = ""
        self.html_files = []
        self.current_index = 0
        self.results = []
        self.fp_count = 0
        self.fn_count = 0
        self.t_count = 0

    def initUI(self):
        self.setWindowTitle('Drug Promotion Detector')
        self.setGeometry(100, 100, 1200, 800)

        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        layout = QHBoxLayout()

        left_layout = QVBoxLayout()
        self.web_view = QWebEngineView()
        left_layout.addWidget(self.web_view)

        right_layout = QVBoxLayout()
        
        self.extracted_text = QTextEdit()
        self.extracted_text.setReadOnly(True)
        right_layout.addWidget(QLabel("추출된 본문:"))
        right_layout.addWidget(self.extracted_text)

        button_layout = QHBoxLayout()
        self.file_index_label = QLabel("0 / 0")
        button_layout.addWidget(self.file_index_label)
        self.fp_button = QPushButton("FP (오탐)")
        self.fn_button = QPushButton("FN (미탐)")
        self.t_button = QPushButton("T (정상탐지)")
        button_layout.addWidget(self.fp_button)
        button_layout.addWidget(self.fn_button)
        button_layout.addWidget(self.t_button)
        right_layout.addLayout(button_layout)

        self.comment = QTextEdit()
        self.comment.setPlaceholderText("비고")
        right_layout.addWidget(QLabel("비고:"))
        right_layout.addWidget(self.comment)

        # 이전/건너뛰기 버튼 추가
        navigation_layout = QHBoxLayout()
        self.prev_button = QPushButton("이전")
        self.skip_button = QPushButton("건너뛰기")
        navigation_layout.addWidget(self.prev_button)
        navigation_layout.addWidget(self.skip_button)
        right_layout.addLayout(navigation_layout)

        layout.addLayout(left_layout, 2)
        layout.addLayout(right_layout, 1)

        central_widget.setLayout(layout)

        self.fp_button.clicked.connect(lambda: self.save_result("FP"))
        self.fn_button.clicked.connect(lambda: self.save_result("FN"))
        self.t_button.clicked.connect(lambda: self.save_result("T"))
        self.prev_button.clicked.connect(self.prev_file)
        self.skip_button.clicked.connect(self.next_file)

        menubar = self.menuBar()
        file_menu = menubar.addMenu('File')
        
        open_action = file_menu.addAction('Open HTML Folder')
        open_action.triggered.connect(self.open_folder)

        save_action = file_menu.addAction('Save Results')
        save_action.triggered.connect(self.save_to_excel)

    def open_folder(self):
        folder_path = QFileDialog.getExistingDirectory(self, "Select HTML Folder")
        if folder_path:
            self.html_files = [os.path.join(folder_path, f) for f in os.listdir(folder_path) if f.endswith('.html')]
            if self.html_files:
                self.current_index = 0
                self.load_current_file()

    def load_current_file(self):
        if self.current_index < len(self.html_files):
            self.current_file = self.html_files[self.current_index]

            self.file_index_label.setText(f"{self.current_index + 1} / {len(self.html_files)}")
            
            with open(self.current_file, 'r', encoding='utf-8') as file:
                html_content = file.read()
            
            # extractor.py의 함수 호출로 본문 추출
            result = extract_promotion_content(html_content)
            extracted_content = result.get('promotion_content', '본문을 추출할 수 없습니다.')

            if not extracted_content:
                extracted_content = "홍보글이 아닙니다!"

            # 텔레그램 주소들 추출 (추후 활용)
            telegram_links = result.get('telegrams', [])

            self.extracted_text.setText(extracted_content)

            # 웹 페이지 링크 로딩
            original_url = self.extract_original_url(html_content)
            if original_url:
                self.web_view.load(QUrl(original_url))
            else:
                self.web_view.setHtml(html_content)

    def extract_original_url(self, html_content):
        soup = BeautifulSoup(html_content, 'html.parser')
        og_url = soup.find('meta', property='og:url')
        if og_url and og_url.get('content'):
            return og_url['content']
        a_tag = soup.find('a', href=True)
        if a_tag:
            return a_tag['href']
        return None

    def save_result(self, result):
        comment = self.comment.toPlainText()
        self.results.append({
            "링크": self.current_file,
            "추출된 본문": self.extracted_text.toPlainText(),
            "탐지 결과": result,
            "비고": comment
        })

        if result == "FP":
            self.fp_count += 1
        elif result == "FN":
            self.fn_count += 1
        elif result == "T":
            self.t_count += 1

        self.comment.clear()
        self.next_file()

    def prev_file(self):
        """이전 파일로 이동"""
        if self.html_files:
            self.current_index = (self.current_index - 1) % len(self.html_files)
            self.load_current_file()

    def next_file(self):
        """다음 파일로 이동"""
        if self.html_files:
            self.current_index = (self.current_index + 1) % len(self.html_files)
            self.load_current_file()

    def save_to_excel(self):
        if not self.results:
            self.show_message("저장할 결과가 없습니다.")
            return

        file_path, _ = QFileDialog.getSaveFileName(self, "Save Excel File", "", "Excel Files (*.xlsx)")
        if file_path:
            total_files = len(self.results)
            fp_rate = (self.fp_count / total_files) * 100 if total_files else 0
            fn_rate = (self.fn_count / total_files) * 100 if total_files else 0
            t_rate = (self.t_count / total_files) * 100 if total_files else 0
            
            df = pd.DataFrame(self.results)
            df.loc[len(df)] = ["", "", "", ""]  
            df.loc[len(df)] = ["총 오탐(FP)", self.fp_count, f"{fp_rate:.2f}%", ""]
            df.loc[len(df)] = ["총 미탐(FN)", self.fn_count, f"{fn_rate:.2f}%", ""]
            df.loc[len(df)] = ["정상탐지", self.t_count, f"{t_rate:.2f}%", ""]
            
            df.to_excel(file_path, index=False)
            self.show_message(f"결과가 {file_path}에 저장되었습니다.")

    def show_message(self, message):
        self.statusBar().showMessage(message, 3000)

if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = DrugPromotionDetector()
    ex.show()
    sys.exit(app.exec_())
