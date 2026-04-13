//============================================================================
// Name        : HashTable.cpp
// Author      : Matthew Randall
// Version     : 1.0
// Copyright   : Copyright © 2023 SNHU COCE
// Description : Lab 4-2 Hash Table
//============================================================================

#include <algorithm>
#include <climits>
#include <iostream>
#include <string> // atoi
#include <time.h>

#include "CSVparser.hpp"

using namespace std;

//============================================================================
// Global definitions visible to all methods and classes
//============================================================================

const unsigned int DEFAULT_SIZE = 179;

// forward declarations
double strToDouble(string str, char ch);

// define a structure to hold bid information
struct Bid {
    string bidId; // unique identifier
    string title;
    string fund;
    double amount;
    Bid() {
        amount = 0.0;
    }
};

//============================================================================
// Hash Table class definition
//============================================================================

/**
 * Define a class containing data members and methods to
 * implement a hash table with chaining.
 */
class HashTable {

private:
    // Define structures to hold bids
    struct Node {
        Bid bid;
        unsigned int key;
        Node *next;

        // default constructor
        Node() {
            key = UINT_MAX;
            next = nullptr;
        }

        // initialize with a bid
        Node(Bid aBid) : Node() {
            bid = aBid;
        }

        // initialize with a bid and a key
        Node(Bid aBid, unsigned int aKey) : Node(aBid) {
            key = aKey;
        }
    };

    vector<Node> nodes;

    unsigned int tableSize = DEFAULT_SIZE;

    // Tracks how many bids have been inserted into the hash table.
    // Starts at zero and increases each time a new bid is added so the
    // program can report how many total records were loaded.
    size_t currentSize = 0;

    unsigned int hash(int key);

public:
    HashTable();
    HashTable(unsigned int size);
    virtual ~HashTable();
    void Insert(Bid bid);
    void PrintAll();
    void Remove(string bidId);
    Bid Search(string bidId);
    size_t Size();
};

/**
 * Default constructor
 * 
 * Uses a vector of Node objects as base array.
 * Each element represents a buck, and each bucket may grow into a linked list
 * if collisions occur. Resizing the vector here ensures all buckets exist and
 * are initialized before any inserts happen.
 * 
 */
HashTable::HashTable() {
    
    nodes.resize(tableSize);
}

/**
* Constructor for specifying size of the table
* Use to improve efficiency of hashing algorithm
* by reducing collisions without wasting memory.
*
* Allowing a custom table size helps reduce collisions when working with large
* datasets. After updating tableSize, the vector is resized so the hash table
* has the correct number of buckets.
* 
*/
HashTable::HashTable(unsigned int size) {
    this->tableSize = size;
    nodes.resize(tableSize);
}

/*
* Returns the total number of bids currently stored in the hash table.
* This allows other parts of the program to access the running count that
* is updated each time a new bid is inserted and is displayed through menu 
* options.
*/
size_t HashTable::Size() {
    return currentSize;
}

/**
 * Destructor
 * 
 * Each bucket may contain a linked list created during collision handling. 
 * The vector itself cleans up automatically, but any nodes created with 'new'
 * must be deleted manually to avoid memory leaks.
 * 
 */
HashTable::~HashTable() {
    for (unsigned int i = 0; i < tableSize; i++) {
        Node* current = nodes[i].next;
        while (current != nullptr) {
            Node* temp = current;
            current = current->next;
            delete temp;  // free chained node
        }
    }
}

/**
 * Calculate the hash value of a given key.
 * Note that key is specifically defined as
 * unsigned int to prevent undefined results
 * of a negative list index.
 *
 * @param key The key to hash
 * @return The calculated hash
 * 
 * The modulo operator distributes keys across the available buckets.
 * This keeps the index within bounds and helps spread entries evenly.
 * 
 */
unsigned int HashTable::hash(int key) {
    return key % tableSize;
}

/**
 * Insert a bid
 *
 * @param bid The bid to insert
 * 
 * First, convert the bidId to an integer so it can be hashed.
 * Next, compute the bucket index using the hash function.
 * Then, if the bucket is empty, store the bid directly. Since this
 * represents a brand?new entry in the table, increment currentSize
 * to keep an accurate count of how many bids have been inserted.
 *
 * Otherwise, if the bucket is already used, append a new node
 * to the end of the linked list (chaining). This resolves collisions
 * without overwriting existing data. Because this is still a new bid
 * being added to the table, increment currentSize here as well.
 *
 * 
 */
void HashTable::Insert(Bid bid) {
    unsigned int key = hash(atoi(bid.bidId.c_str()));

    // If bucket is unused, store bid directly
    if (nodes[key].key == UINT_MAX) {
        nodes[key].key = key;
        nodes[key].bid = bid;
        nodes[key].next = nullptr;
        currentSize++;
        return;
    }

    // Otherwise, walk to the end of the chain and append new node
    Node* current = &nodes[key];
    while (current->next != nullptr) {
        current = current->next;
    }

    current->next = new Node(bid, key);
    currentSize++;
}

/**
 * Print all bids
 * 
 * Each bucket may contain a chain of nodes, so we must traverse both
 * the base array and any linked lists attached to each bucket.
 * 
 */
void HashTable::PrintAll() {
    for (unsigned int i = 0; i < tableSize; i++) {
        if (nodes[i].key != UINT_MAX) {
            Node* current = &nodes[i];

            // Print the bucket head and any chained nodes
            while (current != nullptr) {
                cout << current->bid.bidId << ": "
                    << current->bid.title << " | "
                    << current->bid.amount << " | "
                    << current->bid.fund << endl;

                current = current->next;
            }
        }
    }
}

/**
 * Remove a bid
 *
 * @param bidId The bid id to search for
 * 
 * Hash the bidId to find the correct bucket then check if the
 * bucket head matches the bidId. If not, walk the chain and 
 * remove the matching node. Special care is needed when 
 * removing the head node or the only node in a chain,
 * since the bucket must remain valid after removal.
 * 
 */
void HashTable::Remove(string bidId) {
    unsigned int key = hash(atoi(bidId.c_str()));

    Node* current = &nodes[key];

    // Case 1: bucket head matches
    if (current->key != UINT_MAX && current->bid.bidId == bidId) {

        if (current->next == nullptr) {
            // Reset bucket to empty state
            nodes[key] = Node();
        }
        else {
            // Copy next node into bucket and delete next node
            Node* temp = current->next;
            nodes[key].bid = temp->bid;
            nodes[key].next = temp->next;
            delete temp;
        }
        return;
    }

    // Case 2: search the chain
    Node* prev = current;
    current = current->next;

    while (current != nullptr) {
        if (current->bid.bidId == bidId) {
            prev->next = current->next;
            delete current;
            return;
        }
        prev = current;
        current = current->next;
    }
}

/**
 * Search for the specified bidId
 *
 * @param bidId The bid id to search for
 * 
 * Hash the bidId to locate the correct bucket. Check the bucket head. If not
 * found, walk the chain. If the bid is not found anywhere in the chain,
 * return as an empty Bid.
 * 
 */
Bid HashTable::Search(string bidId) {
    Bid bid;
    unsigned int key = hash(atoi(bidId.c_str()));

    Node* current = &nodes[key];

    // Check bucket head
    if (current->key != UINT_MAX && current->bid.bidId == bidId) {
        return current->bid;
    }

    // Search chain
    current = current->next;
    
    while (current != nullptr) {
        if (current->bid.bidId == bidId) {
            return current->bid;
        }
        current = current->next;
    }
    return bid; // Not found
}

//============================================================================
// Static methods used for testing
//============================================================================

/**
 * Display the bid information to the console (std::out)
 *
 * @param bid struct containing the bid info
 */
void displayBid(Bid bid) {
    cout << bid.bidId << ": " << bid.title << " | " << bid.amount << " | "
            << bid.fund << endl;
    return;
}

/**
 * Load a CSV file containing bids into a container
 *
 * @param csvPath the path to the CSV file to load
 * @return a container holding all the bids read
 */
void loadBids(string csvPath, HashTable* hashTable) {
    cout << "Loading CSV file " << csvPath << endl;

    // initialize the CSV Parser using the given path
    csv::Parser file = csv::Parser(csvPath);

    // read and display header row - optional
    /*
    
    vector<string> header = file.getHeader();
    for (auto const& c : header) {
        cout << c << " | ";
    }
    cout << "" << endl;
    
    */
    try {
        // loop to read rows of a CSV file
        for (unsigned int i = 0; i < file.rowCount(); i++) {

            // Create a data structure and add to the collection of bids
            Bid bid;
            bid.bidId = file[i][1];
            bid.title = file[i][0];
            bid.fund = file[i][8];
            bid.amount = strToDouble(file[i][4], '$');

            //cout << "Item: " << bid.title << ", Fund: " << bid.fund << ", Amount: " << bid.amount << endl;

            // push this bid to the end
            hashTable->Insert(bid);
        }
    } catch (csv::Error &e) {
        std::cerr << e.what() << std::endl;
    }
}

/**
 * Simple C function to convert a string to a double
 * after stripping out unwanted char
 *
 * credit: http://stackoverflow.com/a/24875936
 *
 * @param ch The character to strip out
 */
double strToDouble(string str, char ch) {
    str.erase(remove(str.begin(), str.end(), ch), str.end());
    return atof(str.c_str());
}

/**
 * The one and only main() method
 */
int main(int argc, char* argv[]) {

    // process command line arguments
    string csvPath, bidKey;
    switch (argc) {
    case 2:
        csvPath = argv[1];
        bidKey = "98223";
        break;
    case 3:
        csvPath = argv[1];
        bidKey = argv[2];
        break;
    default:
        csvPath = "eBid_Monthly_Sales.csv";
        bidKey = "98223";
    }

    // Define a timer variable
    clock_t ticks;

    // Define a hash table to hold all the bids
    HashTable* bidTable;

    Bid bid;
    bidTable = new HashTable();
    
    int choice = 0;
    while (choice != 9) {
        cout << "Menu:" << endl;
        cout << "  1. Load Bids" << endl;
        cout << "  2. Display All Bids" << endl;
        cout << "  3. Find Bid" << endl;
        cout << "  4. Remove Bid" << endl;
        cout << "  9. Exit" << endl;
        cout << "Enter choice: ";
        cin >> choice;

        switch (choice) {

        case 1:
            
            // Initialize a timer variable before loading bids
            ticks = clock();

            // Complete the method call to load the bids
            loadBids(csvPath, bidTable);

            // Calculate elapsed time and display result
            ticks = clock() - ticks; // current clock ticks minus starting clock ticks
            cout << bidTable->Size() << " bids read" << endl;
            cout << "time: " << ticks << " clock ticks" << endl;
            cout << "time: " << ticks * 1.0 / CLOCKS_PER_SEC << " seconds" << endl;
            break;

        case 2:
            bidTable->PrintAll();
            break;

        case 3:
            ticks = clock();

            bid = bidTable->Search(bidKey);

            ticks = clock() - ticks; // current clock ticks minus starting clock ticks

            if (!bid.bidId.empty()) {
                displayBid(bid);
            } else {
                cout << "Bid Id " << bidKey << " not found." << endl;
            }

            cout << "time: " << ticks << " clock ticks" << endl;
            cout << "time: " << ticks * 1.0 / CLOCKS_PER_SEC << " seconds" << endl;
            break;

        case 4:
            bidTable->Remove(bidKey);
            break;
        }
    }

    cout << "Good bye." << endl;

    return 0;
}
