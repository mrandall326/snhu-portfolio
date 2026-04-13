# CS 255 – Module Six Assignment  
### Interpreting UML Activity and Sequence Diagrams

This repository contains my completed Module Six assignment for *CS 255: Systems Analysis and Design*.  
Module Six focuses on interpreting UML diagrams—specifically activity diagrams and sequence diagrams—and evaluating how well they represent a system’s behavior.

---

## 📌 Assignment Overview
The goal of this module is to analyze the UML diagrams for an **ATM cash‑withdrawal use case** and identify:

- What the diagrams show  
- How the system behaves according to the diagrams  
- Missing steps, inconsistencies, or design deficiencies  
- How the diagrams could be improved to better reflect real‑world system behavior  

This assignment strengthens the ability to read, critique, and refine UML models.

---

## 🧩 Key Components of the Analysis

### **1. Interpretation of the ATM Use Case**
The diagrams describe the standard ATM withdrawal workflow:

- User inserts card  
- User enters PIN  
- ATM sends PIN to the bank  
- Bank validates the PIN  
- User requests withdrawal amount  
- ATM verifies funds and dispenses cash  
- Receipt is generated  

The interactions involve three actors:
- **User**
- **ATM**
- **Bank**

Information flows between these actors include the PIN, validation responses, withdrawal amounts, and confirmation messages.

---

### **2. Identified Deficiencies in the UML Diagrams**
Two major issues were found:

#### **Missing PIN Retry Logic**
The activity diagram ends immediately after an incorrect PIN, which is unrealistic.  
Real ATMs allow multiple attempts (usually three) before retaining the card or ending the session.

#### **Sequence Diagram Inconsistencies**
The sequence diagram is missing:
- Balance checking  
- Error handling  
- Receipt generation  

These steps appear in the activity diagram but not in the sequence diagram, creating a mismatch between the two models.

---

### **3. Recommended Improvements**
To improve accuracy and usability:

- Add a **decision loop** in the activity diagram allowing up to three PIN attempts  
- Update the sequence diagram to include:  
  - Balance verification  
  - Error paths  
  - Receipt printing  
  - Session termination logic  

These changes make the diagrams more realistic and aligned with actual ATM behavior.

---

## 📁 Files Included
- `6-3 Assignment - Interpreting UML Diagrams.docx`

---

## 🎯 Purpose of This Module
Module Six develops the ability to:

- Interpret UML activity and sequence diagrams  
- Identify gaps between intended system behavior and diagrammed behavior  
- Recommend improvements to enhance clarity and accuracy  
- Strengthen modeling skills needed for Project Two  

This module bridges the gap between understanding system workflows and representing them accurately through UML.
