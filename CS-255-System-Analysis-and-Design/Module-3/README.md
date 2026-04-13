# CS 255 – Module Three Assignment  
### Evaluation of Hamp Crafts’ Purchase & Supply Process

This repository contains my completed Module Three assignment for *CS 255: Systems Analysis and Design*.  
The focus of this module is to evaluate Hamp Crafts’ current purchase and supply workflow using the provided Data Flow Diagram (DFD) and to determine what changes are required to support a new online storefront.

---

## 📌 Assignment Overview
Module Three centers on interpreting an existing process model and identifying how the business workflow must evolve to support online operations. The analysis includes:

- Understanding the current purchase and supply process  
- Identifying existing data sources  
- Determining additional processes and data needs for an online storefront  
- Recommending an integration approach for the new system  

---

## 🧩 Key Components of the Analysis

### **1. Interpretation of the Current Process Model**
The provided DFD illustrates four major activities in Hamp Crafts’ workflow:
- Receiving customer orders  
- Processing checkout  
- Fulfilling orders  
- Coordinating with suppliers and carriers  

The analysis highlights that many steps—such as inventory checks and supplier communication—are manual and dependent on staff intervention.  
A line from the assignment captures this clearly:  
> “Communication regarding inventory shortages or delays in delivery is a manual process for Hamp Crafts if a potential shortage occurs.”

---

### **2. Existing Data Sources**
The current system relies on:
- Customer order information  
- Local merchant account for payment processing  
- Local inventory database  
- Supplier contracts, shipment schedules, and delivery plans  
- Carrier shipping information  

These sources support in‑store operations but are not connected to any online system.

---

### **3. Requirements for an Online Storefront**
To support online sales, Hamp Crafts will need new processes such as:
- Online product browsing and shopping cart functionality  
- Online checkout and automated order confirmations  
- Real‑time inventory updates across in‑store and online channels  
- Administrative tools for product listings, customer accounts, and support  
- Automated low‑inventory alerts or supplier notifications  

---

### **4. Additional Data Sources & Databases**
An online storefront requires:
- A structured product catalog (descriptions, images, pricing, availability)  
- Customer accounts and order history  
- Online order records  
- Payment gateway data  
- Optional analytics for customer behavior  

A more scalable database approach is recommended, such as:
- A centralized shared database for both store and website  
- A dedicated product catalog database  
- A customer/CRM database  
- A content management system for the website  

---

### **5. Integration Recommendation**
The analysis recommends **integrating the online storefront with the existing system**, rather than building a separate standalone solution.  
A unified system ensures:
- Shared inventory  
- Consistent order fulfillment  
- Centralized payment processing  
- Reduced data duplication  
- A smoother customer experience  

New modules (online checkout, customer accounts, website management) can be added without forcing staff to manage two separate workflows.

---

## 📁 Files Included
- `3-2 Evaluate a Process Model.docx`  
- `Data Flow Diagram.pdf`

---

## 🎯 Purpose of This Module
This assignment builds skills in:
- Reading and interpreting process models  
- Identifying system gaps and inefficiencies  
- Translating business needs into system requirements  
- Preparing for later design work in Module Four and Project Two  
