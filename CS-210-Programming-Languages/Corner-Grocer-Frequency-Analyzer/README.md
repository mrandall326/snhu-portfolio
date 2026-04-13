# 🛒 Corner Grocer - Frequency Analyzer

## 📌 Summary
This C++ program analyzes item frequency from a dataset, simulating a basic inventory management system. It reads item names from a file, counts how often each item appears, and provides users with interactive options to view individual item frequencies or a full histogram.

## 🎯 Features
- Menu-driven interface for intuitive navigation
- Frequency tracking using `std::map`
- Input validation for robust user interaction
- Histogram display with scalable formatting
- File export functionality

## 🚀 Future Enhancements
- Dynamic histogram scaling for large datasets
- Support for CSV and JSON output formats
- Command-line argument support for automation

## 🧠 Challenges & Solutions
Formatting the histogram output was the trickiest part. I experimented with spacing and alignment to ensure clarity across varying item lengths and frequencies. I also refined control flow to keep the menu responsive and intuitive, even with edge cases.

## 🧰 Support Network
Resources I leaned on:
- [cplusplus.com](https://cplusplus.com)
- Stack Overflow
- Course materials and peer discussions

## 🔁 Transferable Skills
- File I/O and associative containers (`std::map`)
- Modular design and CLI development
- Input validation and clean output formatting

## 🧱 Code Structure
The program is organized into modular functions:
- `ToLower()`
- `LoadFrequencies()`
- `WriteBackupFile()`
- `SearchItem()`
- `PrintFrequencies()`
- `PrintHistogram()`

## 🧩 Maintainability, Readability & Adaptability

This program was built with long-term usability and clarity in mind. Key strategies include:

- **Modular Design**  
  Each core task—input handling, frequency analysis, histogram generation, and file export—is encapsulated in its own function. This separation of concerns simplifies debugging, testing, and future enhancements.

- **Descriptive Naming & Comments**  
  Functions and variables use clear, descriptive names, and inline comments explain logic where needed. This improves readability for both the original developer and future collaborators.

- **Avoidance of Hardcoded Values**  
  Constants are used where appropriate, reducing the risk of errors and making the code easier to adapt to new datasets or formats.

- **Scalable Output Formatting**  
  The histogram display is formatted to handle varying item lengths and frequencies, ensuring clarity even as the dataset grows.

- **User-Friendly Interface**  
  The menu system is intuitive and includes input validation to handle edge cases gracefully, enhancing both usability and robustness.

- **Future-Proofing**  
  The codebase is structured to support future enhancements like alternate output formats (CSV/JSON), dynamic histogram scaling, and command-line argument support—all without disrupting the core logic.

These principles ensure the program remains easy to maintain, extend, and integrate into broader systems or coursework.
