# CS 255 – Module Four Assignment  
### Evaluation of an Object Model for the Online Storefront

This repository contains my completed Module Four assignment for *CS 255: Systems Analysis and Design*.  
The focus of this module is to interpret and evaluate the UML object model for Hamp Crafts’ proposed online storefront and compare it to the previously analyzed process model.

---

## 📌 Assignment Overview
Module Four examines how an object-oriented model represents system structure, user roles, data, and behaviors. The assignment evaluates:

- The functions of the online storefront as represented in the UML class diagram  
- The different user classes and their relationships  
- How objects use attributes and methods  
- Whether the model fully captures Hamp Crafts’ required functionality  
- The meaning of composition (solid diamond) in the diagram  
- A comparison between process models and object models  

---

## 🧩 Key Components of the Analysis

### **1. Interpretation of the Object Model**
The UML class diagram organizes the online storefront into classes such as:

- **User → Customer / Administrator (inheritance)**  
- **Shopping Cart**  
- **Order and Order Details (composition)**  
- **Shipping Info**

Customer-facing functions (account creation, login, shopping cart management, placing orders) and administrative functions (catalog updates) are represented through attributes and methods within these classes.

---

### **2. User Classes and Associations**
The model includes two primary user types:

- **Customer** – interacts with carts and orders  
- **Administrator** – manages catalog updates  

Both inherit from the **User** superclass, which contains shared attributes like `userId`, `password`, `loginStatus`, and `registerDate`.

Associations show:
- A Customer can have multiple shopping carts  
- A Customer can place multiple orders  
- Order → Order Details is a composition relationship  

---

### **3. Object Behavior**
Each object uses:

- **Attributes** to store state (e.g., productID, quantity, shippingInfo)  
- **Methods** to perform actions (e.g., `addCartItem()`, `checkOut()`, `placeOrder()`, `updateCatalog()`)

Encapsulation ensures each class manages its own data and behavior.

---

### **4. Completeness of the Model**
The model captures core storefront functionality but lacks:

- Integration with a third‑party payment processor  
- Notification or messaging components  
- Administrative tools beyond catalog updates  
- Customer service or account management features  

It provides a strong foundation but does not fully represent all requirements described in the Hamp Crafts scenario.

---

### **5. Composition (Solid Diamond) Explanation**
The solid diamond in the diagram represents **composition**, the strongest form of aggregation.  
In this model:

- **Order** *owns* **Order Details**  
- Order Details cannot exist independently  
- Deleting an Order should delete its Order Details  

This is the correct use of composition because order details are meaningless without their parent order.

---

### **6. Comparison: Process Model vs. Object Model**

#### **Process Model Strengths**
- Shows workflow and sequence of activities  
- Clarifies how data moves through the system  
- Helps visualize integration with existing in‑store processes  

**Limitations:**  
Does not show data structure, object relationships, or internal system behavior.

#### **Object Model Strengths**
- Shows system architecture, classes, attributes, and methods  
- Clarifies data organization and object responsibilities  

**Limitations:**  
Does not show workflow, sequence of operations, or external service interactions.

---

## 📁 Files Included
- `4-2 Evaluate an Object Model.docx`  
- `CS 255 Module Four Online Shopping Cart Object Diagram.pdf`

---

## 🎯 Purpose of This Module
This assignment builds skills in:

- Interpreting UML class diagrams  
- Understanding object‑oriented system structure  
- Comparing structural and process‑based modeling approaches  
- Identifying gaps between system requirements and system design  

These insights prepare for the more advanced modeling and design work in Project Two.
