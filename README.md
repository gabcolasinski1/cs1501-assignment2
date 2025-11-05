# CS 1501 – Assignment 2: Detecting Hotspot Substrings in Leaked Passwords using DLB Tries

> **Note:** This assignment was developed with the help of OpenAI ChatGPT to brainstorm and generate parts of the scaffolding and documentation files to speedup prototyping.

## Context

Passwords remain one of the weakest links in modern security because many users continue to rely on predictable or recycled secrets. Massive breaches such as the **RockYou (2009)** leak, the **LinkedIn (2012)** dump, and more recent collections like **Collection #1–5 (2019)** have exposed billions of real-world passwords, allowing attackers to analyze them for recurring patterns. These leaks show that users often choose passwords with common substrings like `"123"`, `"password"`, or years like `"2024"`, making them highly vulnerable to guessing and cracking. Studying these leaked databases has become central to both attacker and defender strategies: attackers use them to optimize password-guessing tools, while defenders use them to evaluate password strength and design better security policies. In this assignment, you will explore password weakness by detecting frequently reused **hotspot substrings** within leaked passwords, and then apply that knowledge to evaluate the security of candidate passwords.

## Hotspot Substrings

A **hotspot** is a short substring (or n-gram) that appears frequently across many leaked passwords.  
For example, substrings like `"123"`, `"pass"`, `"qwe"`, or `"2024"` are considered hotspots because they are reused by large numbers of users when creating their passwords.  
Detecting these hotspots is important because they reveal common, predictable building blocks that attackers may exploit when generating password guesses.  
In this assignment, hotspots will be identified by scanning leaked passwords for recurring substrings of configurable length (e.g., 3–6 characters) and recording their global frequency and distribution.

Each hotspot substring is associated with the following statistics:

- **`freq`**: The total number of times this substring occurs across *all* leaked passwords (counting duplicates within the same password).  
- **`docFreq`**: The number of *distinct leaked passwords* that contain this substring at least once (ignores duplicates within a single password).  
- **`beginCount`**: How many times this substring appeared at the **beginning** of leaked passwords.  
- **`middleCount`**: How many times this substring appeared in the **middle** of leaked passwords.  
- **`endCount`**: How many times this substring appeared at the **end** of leaked passwords.  


## Required Task

Implement a **DLB-based hotspot detector** (`DLBHotspotDetector`) that ingests leaked passwords, extracts recurring substrings (hotspots), and evaluates candidate passwords. Your code must expose (and correctly implement) the following interface methods:

1. **Index leaked passwords**
   - **Method:** `void addLeakedPassword(String leakedPassword, int minN, int maxN)`
   - **Behavior:** For each `leakedPassword`, extract all substrings whose lengths are `minN ≤ n ≤ maxN` and insert them into your **DLB trie**, updating global stats.

2. **Analyze candidate passwords**
   - **Method:** `Set<Hotspot> hotspotsIn(String candidatePassword)`
   - **Behavior:** Return a **de-duplicated** set of `Hotspot` objects for all hotspot substrings found **anywhere** in `candidatePassword`.  
     - Each hotspot substring appears **at most once** in the returned set.
     - Populate candidate-specific fields in each `Hotspot`:
       - `candidateAtBegin` (true if it appears at the beginning at least once),
       - `candidateMiddleCount` (number of middle occurrences),
       - `candidateAtEnd` (true if it appears at the end at least once).

- You **must** use a **DLB trie** for substring indexing and lookup.

### Example

Suppose the candidate password is: `"123abc123"` and the hotspot being analyzed is `"123"`.

| Field                   | Value | Explanation                                                                 |
|--------------------------|-------|-----------------------------------------------------------------------------|
| `freq`                  | 500   | `"123"` appeared 500 times in the entire leaked password dataset.           |
| `docFreq`               | 200   | `"123"` appeared in 200 distinct leaked passwords.                          |
| `beginCount`            | 120   | `"123"` appeared at the start of 120 leaked passwords.                       |
| `middleCount`           | 250   | `"123"` appeared in the middle of 250 leaked passwords.                      |
| `endCount`              | 130   | `"123"` appeared at the end of 130 leaked passwords.                         |
| `candidateAtBegin`      | true  | `"123"` occurs at the start of `123abc123`.                                  |
| `candidateMiddleCount`  | 0     | `"123"` does not occur in the middle of `123abc123`.                         |
| `candidateAtEnd`        | true  | `"123"` occurs at the end of `123abc123`.                                    |

This way, both **global stats** (from the leak database) and **candidate-specific stats** are captured in the `Hotspot` object.


## 🛠️ Hints

- **DLB node design is up to you.**  
  You may define any DLB node layout as long as it’s a **child/sibling** structure (not an R-way array). Typical fields:
  - `char ch`
  - `Node child`, `Node sibling`
  - `boolean isTerminal`
  - **Stats you choose to track** (see below)

- **About `passwords.txt`.**  
  The provided file is a **toy, sanitized dataset** for development. Your code should:
  - Handle **larger files** without modification (read line by line).  
  - Be robust to **blank lines** and **whitespace** (trim and skip).  
  - Support **configurable n-gram ranges** (e.g., 3–6) passed to your indexing method.  
  - Avoid keeping the entire file in memory—streaming is fine.
---
## Folder Structure

```plain
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       ├── Hotspot.java
    │       ├── HotspotDetector.java
    │       ├── DLBHotspotDetector.java
    └── test/
        └── java/
            └── TestHotspotDetector.java
└── passwords.txt
```
---

## **⚙️ Compilation & Running Tests**

You can use GitHub Codespaces to run, compile, and test this assignment entirely in the cloud — no local setup required.

If you choose to work on your **local machine**, you must have Maven installed. If not:

### **Linux/macOS**

```
sudo apt install maven   # on Ubuntu/Debian
brew install maven       # on macOS

```

### **Windows**

1. Download from: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
2. Extract to a folder like `C:\\Program Files\\Apache\\maven`
3. Set environment variable `MAVEN_HOME` to that folder
4. Add `%MAVEN_HOME%\\bin` to your system `PATH`
5. Open a new command prompt and type `mvn -v` to verify installation

### **🔨 Compile the Project**

```
mvn compile

```

### **✅ Run Tests**

```
mvn test

```

---

## **🔍 Debugging Test Cases in VS Code with Test Runner**

To debug JUnit test cases in VS Code, follow these steps:

### **Prerequisites:**

* Install the **Java Extension Pack** in VS Code.
* You may need to install version **0.40.0** of the **Test Runner for Java** extension if debugging options do not appear.

#### **Steps:**

1. Open a test file in the editor.
2. Set breakpoints by clicking on the gutter next to the line numbers.
3. Right-click on the gutter next to the line number of the test method name and select **Debug Test**.
4. Use the debug toolbar to step through code, inspect variables, and view call stacks.

This allows you to easily verify internal state, control flow, and ensure correctness of your implementation.

---

## Additional Resources

### Maven
Maven is used to build and manage the project. You can download it from: https://maven.apache.org/

### JUnit
JUnit 4 is used for testing. It is automatically included via Maven (see `pom.xml`). You do not need to install it separately.

---

## 📤 Gradescope Autograder
- This assignment will be autograded on **Gradescope**.
- The autograder reads only your `DLBHotspotDetector.java` file.
- The autograder will:
  - Compile your code using Maven
  - Run unit tests using JUnit
  - Check for correctness and edge cases

💡 You can submit as many times as you'd like before the deadline — only the *active* submission counts.

---
## **📊 Grading Rubric**

| Item.                                                   |  Points |
| ------------------------------------------------------- | ------- |
| Autograder Tests.                                       | 90      |
| Code style, comments, and modularity                    | 10      |

### **💡 Grading Guidelines**

* Test cases include both visible and hidden scenarios to assess correctness, edge handling, and boundary conditions.
* If your autograder score is below 60%, your code will be manually reviewed for partial credit.

  * However, **manual grading can award no more than 60% of the total autograder points**.
* `Code style, comments, and modularity` is graded manually and includes:

  * Clear and meaningful variable/method names
  * Proper indentation and formatting
  * Use of helper methods to reduce duplication
  * Inline comments explaining non-obvious logic
  * Adherence to Java naming conventions

---

