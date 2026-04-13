# CS 300 – Module 5  
### Binary Search Tree Implementation & Project One Milestone Three (BST Data Structure)

This module contains two major components for **Module 5** of *CS 300: Data Structures and Algorithms*:

1. **The Binary Search Tree (BST) implementation assignment**  
2. **Milestone Three for Project One**, which adds the **BST Data Structure pseudocode** to the evolving CS 300 Pseudocode Document

Together, these artifacts demonstrate mastery of hierarchical data structures, recursion, dynamic memory management, and algorithmic design.

---

## 📌 Part 1 — Binary Search Tree Implementation (BinarySearchTree.cpp)

This assignment required building a **Binary Search Tree** from scratch in C++.  
The program stores bid records in a BST keyed by bid ID, supporting:

- **Insert** – recursively place each bid in the correct subtree  
- **Search** – efficiently locate a bid by traversing left or right  
- **Remove** – delete nodes while preserving BST structure  
- **InOrder Traversal** – print bids in sorted order  
- **PreOrder & PostOrder Traversal** – explore the tree in different patterns  

The BST provides faster search and ordered output without requiring additional sorting.

### **What I Learned**
Through this assignment, I strengthened my ability to:

- Implement recursive algorithms for insertion, searching, and traversal  
- Handle all three removal cases (leaf, one child, two children)  
- Use successor replacement to maintain BST validity  
- Manage dynamic memory safely using a postorder destructor  
- Understand how hierarchical structures store and organize data efficiently  

This module deepened my understanding of tree‑based data structures and recursive problem‑solving.

---

## 📄 Included Files

- `BinarySearchTree.cpp`
- `Code Reflection and Pseudocode.docx`
- `CS 300 Pseudocode Document.docx` (Updated to include Milestone Three)

---


### **BinarySearchTree.cpp**
Contains the full implementation of:

- Node structure  
- Insert, Search, Remove, and removeNode logic  
- InOrder, PreOrder, and PostOrder traversal  
- Recursive destroyTree() destructor  
- Tree size tracking and user interaction  

### **Code Reflection and Pseudocode.docx**
Includes:

- A written reflection on the development process  
- Discussion of recursive logic, removal cases, and memory management  
- Pseudocode for all BST operations  
- An AI usage statement for academic transparency  

### **Updated CS 300 Pseudocode Document**
Now includes:

- **Milestone One:** Vector Data Structure  
- **Milestone Two:** Hash Table Data Structure  
- **Milestone Three:** Binary Search Tree Data Structure  
- Runtime analysis examples for Project One  

---

## 📌 Part 2 — Project One Milestone Three  
### Binary Search Tree Data Structure Pseudocode

This milestone expands the Project One pseudocode to include:

- Loading course data into a BST  
- Validating prerequisites  
- Inserting course objects based on course number  
- Searching for courses and printing prerequisite information  
- Recursive search and insert helpers  
- BST‑based traversal and lookup logic  

This completes the data structure pseudocode required for Project One.

---

## 🧩 Key Concepts Demonstrated

- **Recursive Algorithms**  
  Clean, elegant logic for insertion, searching, and traversal

- **Tree‑Based Organization**  
  Efficient O(log n) average‑case search and insert

- **Complex Removal Logic**  
  Handling leaf, one‑child, and two‑child deletion cases

- **Memory Management**  
  Safe deletion of all nodes using postorder traversal

- **Comparative Data Structure Design**  
  Understanding how vectors, hash tables, and BSTs differ in performance and use cases

---

## 🎯 Purpose of This Module

Module 5 demonstrates the ability to design and implement a hierarchical data structure, manage dynamic memory safely, and extend pseudocode for a larger system design project. These skills are essential for building efficient, scalable software systems and for completing Project One.
