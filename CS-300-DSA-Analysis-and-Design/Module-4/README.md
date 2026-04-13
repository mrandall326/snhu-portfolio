# CS 300 – Module 4  
### Hash Table Implementation & Project One Milestone Two (Hash Table Data Structure)

This module contains two major components for **Module 4** of *CS 300: Data Structures and Algorithms*:

1. **The Hash Table implementation assignment**  
2. **Milestone Two for Project One**, which expands the pseudocode document to include the **Hash Table Data Structure**

Together, these artifacts demonstrate deeper understanding of hashing, collision handling, chaining, memory management, and algorithmic design.

---

## 📌 Part 1 — Hash Table Implementation (HashTable.cpp)

This assignment required building a **hash table with separate chaining** from scratch in C++.  
The program loads bid data from a CSV file and stores each record in a hash table where:

- Each bucket is a vector entry  
- Collisions are handled using a linked list (chaining)  
- Each node stores a bid and a pointer to the next node  

The hash table supports:

- **Insert** – hash the bid ID and place it in the correct bucket  
- **Search** – locate a bid by ID by traversing the chain  
- **Remove** – delete a bid from the correct bucket and update links  
- **PrintAll** – traverse all buckets and chains to display all bids  

### **What I Learned**
Through this assignment, I strengthened my ability to:

- Implement hashing logic and bucket indexing  
- Handle collisions using linked list chaining  
- Manage dynamic memory safely, including destructor cleanup  
- Debug removal logic for head, middle, and tail nodes  
- Combine arrays/vectors with linked structures for efficient lookups  
- Understand how hash tables achieve average‑case O(1) search and insert  

This module built on the linked list assignment and demonstrated how lists can be embedded inside more complex structures.

---

## 📄 Included Files

- `HashTable.cpp`
- `Code Reflection and Pseudocode.docx`
- `CS 300 Pseudocode Document.docx` (Updated with Milestone Two)

---


### **HashTable.cpp**
Contains the full implementation of:

- Hash function  
- Insert, Search, Remove, and PrintAll  
- Node structure for chaining  
- Bucket vector and size tracking  
- Destructor logic to free all chained nodes  

### **Code Reflection and Pseudocode.docx**
Includes:

- A written reflection on the development process  
- Discussion of collision handling, chaining, and memory management  
- Pseudocode for all hash table operations  
- An AI usage statement for academic transparency  

### **Updated CS 300 Pseudocode Document**
Now includes:

- **Milestone One:** Vector Data Structure pseudocode  
- **Milestone Two:** Hash Table Data Structure pseudocode  
- Runtime analysis examples and structure for Project One  

---

## 📌 Part 2 — Project One Milestone Two  
### Hash Table Data Structure Pseudocode

This milestone expands the Project One pseudocode to include:

- Loading course data into a hash table  
- Validating prerequisites  
- Inserting course objects into buckets  
- Searching for courses and printing prerequisite information  
- Handling missing or malformed data  
- Using hashing to improve lookup performance  

This pseudocode will be used later in Project One to compare runtime performance across vector, hash table, and tree structures.

---

## 🧩 Key Concepts Demonstrated

- **Hashing & Modulo Arithmetic**  
  Converting string IDs to numeric keys and mapping them to buckets

- **Collision Handling (Separate Chaining)**  
  Using linked lists to store multiple items in the same bucket

- **Memory Management**  
  Manual deletion of chained nodes in the destructor

- **Algorithmic Efficiency**  
  Understanding average‑case O(1) operations vs. worst‑case O(n)

- **Data Structure Integration**  
  Combining arrays/vectors with linked lists to create hybrid structures

---

## 🎯 Purpose of This Module

Module 4 demonstrates the ability to design and implement an efficient hash table, understand collision resolution strategies, and extend pseudocode for a larger system design project. These skills are essential for building scalable, high‑performance software systems.
