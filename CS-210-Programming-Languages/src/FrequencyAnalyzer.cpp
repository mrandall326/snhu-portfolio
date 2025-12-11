#include <iostream>
#include <fstream>
#include <map>
#include <string>
#include <iomanip>
#include <algorithm>
#include <cctype>
using namespace std;

/*
   Program: Corner Grocer Frequency Analyzer
   Author:  Matthew Randall
   Due Date: 10/19/25
   Purpose: Reads a list of purchased items from a file, counts frequencies,
            and provides menu options for querying, listing, and histogram output.
*/

// Constant for column width formatting
const int COLUMN_WIDTH = 12;

/*
   Function: ToLower
   Purpose:  Converts a string to lowercase for case-insensitive comparisons.
*/
string ToLower(const string& str) {
    string lowerStr = str;
    transform(lowerStr.begin(), lowerStr.end(), lowerStr.begin(),
        [](unsigned char c) { return tolower(c); });
    return lowerStr;
}

/*
   Function: LoadFrequencies
   Purpose:  Reads items from the input file and counts their frequency.
             Stores lowercase keys for lookup, but preserves original
             capitalization for display.
   Returns:  A map<string, pair<string,int>> where:
             - key = lowercase item name
             - value.first = original item name (for display)
             - value.second = frequency count
*/
map<string, pair<string, int>> LoadFrequencies(const string& inputFile) {
    map<string, pair<string, int>> frequencyMap;
    ifstream inFS(inputFile);

    if (!inFS.is_open()) {
        cerr << "Error: Could not open input file " << inputFile << endl;
        exit(1);
    }

    string item;
    while (inFS >> item) {
        string lowerItem = ToLower(item);
        if (frequencyMap.find(lowerItem) == frequencyMap.end()) {
            frequencyMap[lowerItem] = make_pair(item, 1);
        }
        else {
            frequencyMap[lowerItem].second++;
        }
    }

    inFS.close();
    return frequencyMap;
}

/*
   Function: WriteBackupFile
   Purpose:  Writes the frequency map to a backup file for persistence.
*/
void WriteBackupFile(const map<string, pair<string, int>>& frequencyMap, const string& outputFile) {
    ofstream outFS(outputFile);

    if (!outFS.is_open()) {
        cerr << "Error: Could not create backup file " << outputFile << endl;
        exit(1);
    }

    for (const auto& pair : frequencyMap) {
        outFS << pair.second.first << " " << pair.second.second << endl;
    }

    outFS.close();
}

/*
   Function: SearchItem
   Purpose:  Prompts the user for an item and prints its frequency.
             Search is case-insensitive.
*/
void SearchItem(const map<string, pair<string, int>>& frequencyMap) {
    string searchItem;
    cout << "Enter item to search for: ";
    cin >> searchItem;

    string lowerSearch = ToLower(searchItem);
    auto it = frequencyMap.find(lowerSearch);
    if (it != frequencyMap.end()) {
        cout << it->second.first << " purchased " << it->second.second << " time(s)." << endl;
    }
    else {
        cout << searchItem << " not purchased today." << endl;
    }
}

/*
   Function: PrintFrequencies
   Purpose:  Prints all items and their frequencies in aligned columns.
*/
void PrintFrequencies(const map<string, pair<string, int>>& frequencyMap) {
    cout << "\nItem Purchase Frequencies:\n";
    for (const auto& pair : frequencyMap) {
        cout << setw(COLUMN_WIDTH) << left << pair.second.first << " " << pair.second.second << endl;
    }
}

/*
   Function: PrintHistogram
   Purpose:  Prints a histogram of items using asterisks to represent frequency.
*/
void PrintHistogram(const map<string, pair<string, int>>& frequencyMap) {
    cout << "\nPurchase Histogram:\n";
    for (const auto& pair : frequencyMap) {
        cout << setw(COLUMN_WIDTH) << left << pair.second.first << " ";
        for (int i = 0; i < pair.second.second; ++i) {
            cout << "*";
        }
        cout << endl;
    }
}

int main() {
    const string inputFile = "Input_Data.txt";
    const string backupFile = "frequency.dat";

    // Load data from input file
    map<string, pair<string, int>> frequencyMap = LoadFrequencies(inputFile);

    // Write backup file immediately
    WriteBackupFile(frequencyMap, backupFile);

    int choice = 0; // Menu choice variable

    // Menu loop with input validation
    while (true) {
        cout << "\n==== Corner Grocer Menu ====\n";
        cout << "1. Search for item frequency\n";
        cout << "2. Print all item frequencies\n";
        cout << "3. Print histogram of items\n";
        cout << "4. Exit\n";
        cout << "Enter choice: ";

        if (!(cin >> choice)) {
            cin.clear(); // clear error flag
            cin.ignore(100, '\n'); // discard invalid input
            cout << "Invalid input. Please enter a number between 1 and 4.\n";
            continue;
        }

        switch (choice) {
        case 1:
            SearchItem(frequencyMap);
            break;
        case 2:
            PrintFrequencies(frequencyMap);
            break;
        case 3:
            PrintHistogram(frequencyMap);
            break;
        case 4:
            cout << "Exiting program. Goodbye!\n";
            return 0;
        default:
            cout << "Invalid choice. Please select 1, 2, 3 or 4.\n";
        }
    }

    return 0;
}
