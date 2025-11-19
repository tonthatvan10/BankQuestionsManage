# BankQuestionsManage – Japanese Exam Bank Manager

## Introduction
BankQuestionsManage is a Java-based application designed to manage a Japanese exam question bank, perform AI-powered OCR, and automatically generate new exams with answer keys.

---

## Screenshots
<details>
<summary>▶ *Click to expand screenshots*</summary>	  
<img width="915" height="638" alt="image" src="https://github.com/user-attachments/assets/d3b2300b-1a20-40dc-864d-d2b71bf7ad07" />
<img width="915" height="638" alt="image" src="https://github.com/user-attachments/assets/0f439664-defc-485a-b964-e1f1d4a2b53a" />
<img width="915" height="638" alt="image" src="https://github.com/user-attachments/assets/9abfd5ee-c6ea-41b1-9685-52f81958c711" />
<img width="915" height="643" alt="image" src="https://github.com/user-attachments/assets/3db56d5c-c8aa-451c-9790-35cced30ed1a" />
<img width="915" height="635" alt="image" src="https://github.com/user-attachments/assets/7592803d-4573-4a19-8d22-5fb778d6b9f9" />
<img width="915" height="636" alt="image" src="https://github.com/user-attachments/assets/64919b6f-23df-4b42-8a3f-2640526c2e27" />
<img width="915" height="639" alt="image" src="https://github.com/user-attachments/assets/ef997b26-f055-42cc-a39b-f610d72df256" />
<img width="915" height="444" alt="image" src="https://github.com/user-attachments/assets/28d8224b-6102-471e-b70c-55277544c007" />
</details>	

---

## Development Time
- **2 weeks**  
- **Personal Project**

---

## Key Features
- Manage exams: view, add, edit, and delete  
- OCR Japanese exam images using **Gemini 2.x API**  
- Automatically extract questions and answers from images  
- Generate random exams based on selected number of questions  
- Export exams and answer keys in **PDF** or **DOCX** format  
- Save and track export paths for generated files  
- Clean and intuitive UI built with Java Swing

---

## Technologies Used
- **Language:** Java (JDK 17+)
- **GUI:** Java Swing
- **Database:** SQL Server & JDBC
- **AI Service:** Google Gemini API
- **Libraries:** Apache POI (Word), iText (PDF)

---

## Project Structure
- `src/**/Data` – Database connection & initialization (`DatabaseConnector`, `DatabaseInitializer`).
- `src/**/Service` – Business logic, AI processing (Gemini), and file exporting.
- `src/**/Model` – Data models (`Exam`, `Question`, `Answer`).
- `src/**/UI` – Java Swing User Interface forms.
- `Audio/` & `ExamImage/` – External media resources used in exams.
- `libs/` – External `.jar` dependencies.

---

## Setup & Run

### Prerequisites
- Java Development Kit (JDK) 17 or higher.
- Microsoft SQL Server.

### Installation Steps
1. **Dependencies:** Add all `.jar` files from the `libs/` directory to your project's classpath (Build Path).
2. **Database Setup:**
   - Create a database named **BankQuestions** in SQL Server.
   - Open `src/.../Data/DatabaseConnector.java` and update the `user` and `password` to match your SQL Server credentials.
   - Run `DatabaseInitializer.java` to automatically create tables and seed initial data.
3. **API Key Configuration:**
   - Get an API Key from [Google AI Studio](https://aistudio.google.com/).
   - Set an Environment Variable on your machine named `GEMINI_API_KEY` with your key value.
   - *(Alternatively, paste the key directly into `GeminiService.java` for testing)*.
4. **Run Application:**
   - Run `MainFrame.java` (or the main entry point) to start the application.

---

## Author
**Ton That Van**  
Full-stack developer of this project.

---


# ————————————————————————————

# BankQuestionsManage – Ứng dụng Quản lý Ngân hàng Đề thi Tiếng Nhật

## Giới thiệu
BankQuestionsManage là ứng dụng Java hỗ trợ quản lý ngân hàng đề thi tiếng Nhật, thực hiện OCR bằng AI và tự động sinh đề thi cùng đáp án.

---

## Hình ảnh minh họa
<details>
<summary>▶ *Click to expand screenshots*</summary>	  
<img width="915" height="638" alt="image" src="https://github.com/user-attachments/assets/d3b2300b-1a20-40dc-864d-d2b71bf7ad07" />
<img width="915" height="638" alt="image" src="https://github.com/user-attachments/assets/0f439664-defc-485a-b964-e1f1d4a2b53a" />
<img width="915" height="638" alt="image" src="https://github.com/user-attachments/assets/9abfd5ee-c6ea-41b1-9685-52f81958c711" />
<img width="915" height="643" alt="image" src="https://github.com/user-attachments/assets/3db56d5c-c8aa-451c-9790-35cced30ed1a" />
<img width="915" height="635" alt="image" src="https://github.com/user-attachments/assets/7592803d-4573-4a19-8d22-5fb778d6b9f9" />
<img width="915" height="636" alt="image" src="https://github.com/user-attachments/assets/64919b6f-23df-4b42-8a3f-2640526c2e27" />
<img width="915" height="639" alt="image" src="https://github.com/user-attachments/assets/ef997b26-f055-42cc-a39b-f610d72df256" />
<img width="915" height="444" alt="image" src="https://github.com/user-attachments/assets/28d8224b-6102-471e-b70c-55277544c007" />
</details>	

---

## Thời gian phát triển
- **2 tuần**  
- **Dự án cá nhân**

---

## Tính năng chính
- **Quản lý đề thi:** Xem danh sách, thêm mới, chỉnh sửa và xóa đề thi.
- **OCR thông minh:** Sử dụng **Google Gemini API** để quét và trích xuất câu hỏi/đáp án tiếng Nhật từ file ảnh.
- **Sinh đề tự động:** Tạo đề thi ngẫu nhiên từ ngân hàng câu hỏi có sẵn.
- **Xuất tài liệu:** Hỗ trợ xuất đề thi và đáp án ra file **PDF** và **Word (DOCX)**.
- **Quản lý file:** Lưu trữ và theo dõi lịch sử các file đã xuất.
- **Giao diện:** Giao diện Java Swing trực quan, dễ sử dụng.

---

## Công nghệ sử dụng
- **Ngôn ngữ:** Java (Khuyên dùng JDK 17 trở lên)
- **Giao diện:** Java Swing
- **Cơ sở dữ liệu:** SQL Server, JDBC
- **AI:** Google Gemini API (1.5 Pro/Flash)
- **Xử lý file:** Apache POI, iText
- **Thư viện:** Các file `.jar` trong thư mục `libs/`

---

## Cấu trúc dự án
- `src/**/Data` – Xử lý kết nối Database và khởi tạo dữ liệu.
- `src/**/Service` – Xử lý nghiệp vụ: gọi AI, xuất file, logic đề thi.
- `src/**/Model` – Các lớp đối tượng (Exam, Question, Answer).
- `src/**/UI` – Giao diện người dùng (Forms, Dialogs).
- `Audio/` & `ExamImage/` – Thư mục chứa file nghe và ảnh của đề thi.
- `libs/` – Thư viện ngoài cần thiết.

---

## Cài đặt & Chạy (Setup & Run)

### Yêu cầu tiên quyết (Prerequisites)
- Java Development Kit (JDK) 17 trở lên.
- Microsoft SQL Server.

### Các bước cài đặt
1. **Thư viện:** Thêm toàn bộ file `.jar` từ thư mục `libs/` vào Classpath (Build Path) của dự án.
2. **Cơ sở dữ liệu:**
   - Tạo một database tên là **BankQuestions** trong SQL Server.
   - Mở file `src/.../Data/DatabaseConnector.java` và cập nhật `user`, `password` khớp với tài khoản SQL Server của bạn.
   - Chạy file `DatabaseInitializer.java` để tự động tạo bảng và dữ liệu mẫu ban đầu.
3. **Cấu hình API:**
   - Lấy API Key từ [Google AI Studio](https://aistudio.google.com/).
   - Thiết lập biến môi trường (Environment Variable) trên máy tính tên là `GEMINI_API_KEY` chứa giá trị key của bạn.
   - *(Hoặc cách khác: dán trực tiếp key vào trong file `GeminiService.java` để chạy thử)*.
4. **Khởi chạy:**
   - Chạy file `MainFrame.java` để mở ứng dụng.

---

## Tác giả
**Tôn Thất Văn**  
Người phát triển toàn bộ dự án.
