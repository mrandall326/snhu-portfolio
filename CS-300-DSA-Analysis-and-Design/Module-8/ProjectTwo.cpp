/*
    CS 300: Data Structures and Algorithms: Analysis and Design
    Project Two: Advising Assistance Program
    Due Date: April 19, 2026

    Author: Matthew Randall
    Description:
        This program loads course data from a CSV file into a Binary Search Tree (BST),
        prints an alphabetized course list, and allows users to search for individual
        courses and view their prerequisites.
*/

#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <fstream>
#include <algorithm>

using namespace std;

/*
    trim()
    Removes leading and trailing whitespace from a string.
    This prevents lookup failures caused by extra spaces or newline characters.
*/
string trim(const string& s) {
    size_t start = s.find_first_not_of(" \t\r\n");
    size_t end = s.find_last_not_of(" \t\r\n");
    return (start == string::npos) ? "" : s.substr(start, end - start + 1);
}

/*
    Course
    Represents a single course with:
    - courseNumber: unique identifier (e.g., "CSCI200")
    - name: course title
    - prerequisites: list of prerequisite course numbers
*/
struct Course {
    string courseNumber;
    string name;
    vector<string> prerequisites;
};

/*
    Node
    Represents a node in the Binary Search Tree.
    Each node stores a Course and pointers to left and right children.
*/
struct Node {
    Course course;
    Node* left;
    Node* right;

    Node(Course c) {
        course = c;
        left = nullptr;
        right = nullptr;
    }
};

/*
    CourseBST
    Implements a Binary Search Tree (BST) to store and retrieve courses.
    Courses are ordered by courseNumber (alphanumeric).
*/
class CourseBST {
private:
    Node* root;

    /*
        addNode()
        Recursively inserts a new course into the BST.
        Left subtree: smaller course numbers
        Right subtree: larger course numbers
    */
    void addNode(Node* node, Course course) {
        if (course.courseNumber < node->course.courseNumber) {
            if (node->left == nullptr) {
                node->left = new Node(course);
            }
            else {
                addNode(node->left, course);
            }
        }
        else {
            if (node->right == nullptr) {
                node->right = new Node(course);
            }
            else {
                addNode(node->right, course);
            }
        }
    }

    /*
        inOrder()
        Performs an in-order traversal of the BST.
        This prints courses in sorted alphanumeric order.
    */
    void inOrder(Node* node) {
        if (node != nullptr) {
            inOrder(node->left);
            cout << node->course.courseNumber << ", " << node->course.name << endl;
            inOrder(node->right);
        }
    }

    /*
        searchNode()
        Recursively searches the BST for a course by courseNumber.
        Returns an empty Course object if not found.
    */
    Course searchNode(Node* node, string courseNumber) {
        if (node == nullptr) {
            return Course();
        }
        if (node->course.courseNumber == courseNumber) {
            return node->course;
        }
        if (courseNumber < node->course.courseNumber) {
            return searchNode(node->left, courseNumber);
        }
        return searchNode(node->right, courseNumber);
    }

public:
    CourseBST() {
        root = nullptr;
    }

    /*
        Insert()
        Public wrapper for inserting a course into the BST.
    */
    void Insert(Course course) {
        if (root == nullptr) {
            root = new Node(course);
        }
        else {
            addNode(root, course);
        }
    }

    /*
        PrintCourseList()
        Prints all courses in sorted order.
    */
    void PrintCourseList() {
        inOrder(root);
    }

    /*
        Search()
        Searches for a course by courseNumber.
    */
    Course Search(string courseNumber) {
        return searchNode(root, courseNumber);
    }
};

/*
    split()
    Splits a CSV line into tokens based on a delimiter.
    Also trims whitespace from each token.
*/
vector<string> split(const string& line, char delimiter) {
    vector<string> tokens;
    string token;
    stringstream ss(line);

    while (getline(ss, token, delimiter)) {
        tokens.push_back(trim(token));
    }
    return tokens;
}

/*
    loadCourses()
    Loads course data from a CSV file.
    Expected format:
        courseNumber, courseName, prereq1, prereq2, ...
    Each course is inserted into the BST.
*/
void loadCourses(string filename, CourseBST& bst) {
    ifstream file(filename);

    if (!file.is_open()) {
        cout << "Error opening file." << endl;
        return;
    }

    string line;
    int count = 0;

    while (getline(file, line)) {
        vector<string> tokens = split(line, ',');

        if (tokens.size() >= 2) {
            Course course;
            course.courseNumber = trim(tokens[0]);
            course.name = trim(tokens[1]);

            // Load prerequisites if present
            for (size_t i = 2; i < tokens.size(); i++) {
                if (!tokens[i].empty()) {
                    course.prerequisites.push_back(trim(tokens[i]));
                }
            }

            bst.Insert(course);
            count++;
        }
    }

    file.close();
}

/*
    main()
    Provides a menu-driven interface for:
    - Loading course data
    - Printing all courses
    - Searching for a specific course
*/
int main() {
    CourseBST bst;
    bool dataLoaded = false;

    cout << "Welcome to the course planner." << endl;

    int choice = 0;

    while (choice != 9) {
        cout << "\n1. Load Data Structure." << endl;
        cout << "2. Print Course List." << endl;
        cout << "3. Print Course." << endl;
        cout << "9. Exit" << endl;

        cout << "\nWhat would you like to do? ";
        cin >> choice;

        // Input validation
        if (cin.fail()) {
            cin.clear();
            cin.ignore(1000, '\n');
            cout << "Invalid input." << endl;
            continue;
        }

        switch (choice) {

        case 1: {
            // Manual file load
            string filename;
            cout << "Enter the file name: ";
            cin >> filename;

            loadCourses(filename, bst);
            dataLoaded = true;
            break;
        }

        case 2:
            // Print all courses
            if (!dataLoaded) {
                cout << "Please load the data structure first." << endl;
            }
            else {
                cout << "Here is a sample schedule:" << endl;
                bst.PrintCourseList();
            }
            break;

        case 3:
            // Search for a specific course
            if (!dataLoaded) {
                cout << "Please load the data structure first." << endl;
            }
            else {
                cout << "What course do you want to know about? ";
                string courseNum;
                cin >> courseNum;

                courseNum = trim(courseNum);
                transform(courseNum.begin(), courseNum.end(), courseNum.begin(), ::toupper);

                Course course = bst.Search(courseNum);

                if (course.courseNumber.empty()) {
                    cout << "Course not found." << endl;
                }
                else {
                    cout << course.courseNumber << ", " << course.name << endl;

                    if (course.prerequisites.empty()) {
                        cout << "Prerequisites: None" << endl;
                    }
                    else {
                        cout << "Prerequisites: ";
                        for (size_t i = 0; i < course.prerequisites.size(); i++) {
                            cout << course.prerequisites[i];
                            if (i < course.prerequisites.size() - 1) cout << ", ";
                        }
                        cout << endl;
                    }
                }
            }
            break;

        case 9:
            cout << "Thank you for using the course planner!" << endl;
            break;

        default:
            cout << choice << " is not a valid option." << endl;
        }
    }

    return 0;
}