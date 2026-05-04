# MiniExpressionCompiler - CSC220 - Foundation of CS

**Project Summary:** *The goal of this project is to build a simple compiler component that takes an arithmetic expression as input and processes it through multiple compilation phases such as lexical analysis, parsing, and evaluation. This project simulates how modern compilers break down and understand programming instructions.*
**Key Concepts:**
• Lexical Analysis (tokenization using finite automata concepts)
• Context-Free Grammar (expression parsing using recursive descent)
• Expression Trees (abstract syntax trees)
• Java Programming and OOP
**User can run this program by using online Java compiler (https://www.programiz.com/java-programming/online-compiler/) or (https://www.onlinegdb.com/online_java_compiler#) or on your own computer that is preinstalled JDK for runing java file. The output displays colorful result**
Description of the Task:
You will implement a system that reads a mathematical expression string and simulates the core phases of compilation:
**1. Lexical Analyzer (Tokenizer)**
Convert the input string into a list of valid tokens such as:
• Numbers
• Operators (+, -, *, /)
• Parentheses
**2. Parser (Grammar-Based Validator)**
Check whether the input follows a correct grammar:
E → E + T | E - T | T
T → T * F | T / F | F
F → (E) | number
Use recursive descent parsing to validate the input.
• Lexical Analysis (tokenization using finite automata concepts)
• Context-Free Grammar (expression parsing using recursive descent)
• Expression Trees (abstract syntax trees)
• Java Programming and OOP
**3. Syntax Tree Builder (AST Generator)**
Construct an expression tree from the parsed input.
**4. Evaluator**
Traverse the AST to compute and print the result. If there is deciamal is .0, the result will display as integer. Otherwise, the result will round up to 1 decimal. For example: *24.0 will display as 24 and 2.5556 will display as 2.6*
**5. Trace Output**
Print:
• Token stream
• Parse result (success/failure)
• Expression tree (text format)
• Final evaluated result
The project should Include support for unary operators (e.g., -3) and include error messages for invalid expressions.
**Sample Input/Output:**
Input:
Expression: (3 + 2) * 5 - 1
Output:
Tokens: [(, 3, +, 2, ), *, 5, -, 1]
Parse Tree:
Evaluation Result: 24
**Test Case Scenarios:** This program let user input the expression they want to evaluate. Following is some examples of valid and invalid inputs:
  • Valid input: 3 + 4 * 2
  • Input with parentheses: (1 + 2) * (3 + 4)
  • Invalid input: 3 + * 5, (), 3 + (4 - ) >> Syntax error
  • Nested input: ((3) >> Error: nested disallowed
