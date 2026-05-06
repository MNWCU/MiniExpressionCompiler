import java.util.*;

/**
 * Mini Expression Compiler
 * - Lexical Analysis (tokenize)
 *  --> Convert the input string into a list of valid tokens: number,and (+, -, *, /) 
 * - Parsing (recursive descent) with unary + and - support
 * - AST Generator --> Syntax Tree Builder
 * - Parse tree printer
 * - Evaluation
 * - Trace Output 
 * --> WARNING ERROR:
 * - FIXING Nested parentheses --> invalid e.g., ((3) but ((4*5)+5)*2-10 is still VALID to evaluate
 * - Parser reports syntax errors for invalid inputs e.g "3 + * 5", "()", "3 + (4 - )"
 */
public class MiniCompilerD {
    // COLOR setup
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String GREEN = "\u001B[32m";
    public static final String RESET = "\u001B[0m";

    // --- LEXER ---
    enum TokenType { NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE, LPAREN, RPAREN, EOF }

    static class Token {
        TokenType type;
        String value;
        Token(TokenType type, String value) { this.type = type; this.value = value; }
        @Override public String toString() { return value; }
    }

    static class Lexer {
        private final String input;
        private int pos = 0;
        Lexer(String input) { this.input = input; }

        List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (Character.isWhitespace(c)) { pos++; continue; }
                if (Character.isDigit(c)) {
                    StringBuilder sb = new StringBuilder();
                    while (pos < input.length() && Character.isDigit(input.charAt(pos))) sb.append(input.charAt(pos++));
                    tokens.add(new Token(TokenType.NUMBER, sb.toString()));
                    continue;
                }
                switch (c) {
                    case '+': tokens.add(new Token(TokenType.PLUS, "+")); break;
                    case '-': tokens.add(new Token(TokenType.MINUS, "-")); break;
                    case '*': tokens.add(new Token(TokenType.MULTIPLY, "*")); break;
                    case '/': tokens.add(new Token(TokenType.DIVIDE, "/")); break;
                    case '(': tokens.add(new Token(TokenType.LPAREN, "(")); break;
                    case ')': tokens.add(new Token(TokenType.RPAREN, ")")); break;
                    default: throw new RuntimeException("Lexical Error: Unknown character '" + c + "'");
                }
                pos++;
            }
            tokens.add(new Token(TokenType.EOF, ""));
            return tokens;
        }
    }

    // --- AST NODES & EVALUATOR ---
    abstract static class Node {
        abstract double evaluate();
    }

    static class NumberNode extends Node {
        double value;
        NumberNode(double value) { this.value = value; }
        @Override double evaluate() { return value; }
    }

    static class BinaryOpNode extends Node {
        Node left, right;
        TokenType op;
        BinaryOpNode(Node left, TokenType op, Node right) { this.left = left; this.op = op; this.right = right; }
        @Override
        double evaluate() {
            switch (op) {
                case PLUS: return left.evaluate() + right.evaluate();
                case MINUS: return left.evaluate() - right.evaluate();
                case MULTIPLY: return left.evaluate() * right.evaluate();
                case DIVIDE: return left.evaluate() / right.evaluate();
                default: throw new RuntimeException("Unknown operator");
            }
        }
        String opSymbol() {
            switch (op) {
                case PLUS: return "+";
                case MINUS: return "-";
                case MULTIPLY: return "*";
                case DIVIDE: return "/";
            }
            return "?";
        }
    }

    static class UnaryOpNode extends Node {
    TokenType op;
    Node expr;

    UnaryOpNode(TokenType op, Node expr) {
        this.op = op;
        this.expr = expr;
    }

    @Override
    double evaluate() {
        switch (op) {
            case PLUS:  return +expr.evaluate();
            case MINUS: return -expr.evaluate();
        }
        throw new RuntimeException("Unknown unary operator");
    }

    String opSymbol() {
        return op == TokenType.MINUS ? "-" : "+";
    }
}

    // --- PARSER (recursive descent) ---
    static class Parser {
        private final List<Token> tokens;
        private int pos = 0;

        Parser(List<Token> tokens) { this.tokens = tokens; }

        Node parse() {
            Node expr = parseExpression();
            if (!isAtEnd()) throw new RuntimeException("Syntax Error: Unexpected token '" + peek().value + "'");
            return expr;
        }

        // expression -> term ( ( + | - ) term )*
        private Node parseExpression() {
            Node node = parseTerm();
            while (match(TokenType.PLUS, TokenType.MINUS)) {
                TokenType op = prev().type;
                Node right = parseTerm();
                node = new BinaryOpNode(node, op, right);
            }
            return node;
        }

        // term -> factor ( ( * | / ) factor )*
        private Node parseTerm() {
            Node node = parseFactor();
            while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
                TokenType op = prev().type;
                Node right = parseFactor();
                node = new BinaryOpNode(node, op, right);
            }
            return node;
        }

        // factor -> ( + | - ) factor | NUMBER | ( expression )
        // Disallow nested parentheses: if LPAREN immediately followed by LPAREN -> error
       private Node parseFactor() {

    // --- Unary + ---
    if (match(TokenType.PLUS)) {
        return new UnaryOpNode(TokenType.PLUS, parseFactor());
    }

    // --- Unary - ---
    if (match(TokenType.MINUS)) {
        return new UnaryOpNode(TokenType.MINUS, parseFactor());
    }

    // --- Number ---
    if (match(TokenType.NUMBER)) {
        return new NumberNode(Double.parseDouble(prev().value));
    }

    // --- Parentheses (now supports nested) ---
    if (match(TokenType.LPAREN)) {
        Node node = parseExpression();

        if (!match(TokenType.RPAREN)) {
            throw new RuntimeException("Syntax Error: Missing closing parenthesis ')'");
        }

        return node;
    }

    // --- Otherwise: invalid token ---
    throw new RuntimeException("Syntax Error: Unexpected token '" + peek().value + "'");
}

        // helpers
        private boolean match(TokenType... types) {
            for (TokenType t : types) {
                if (check(t)) { advance(); return true; }
            }
            return false;
        }
        private boolean check(TokenType t) { return !isAtEnd() && peek().type == t; }
        private Token advance() { if (!isAtEnd()) pos++; return prev(); }
        private boolean isAtEnd() { return peek().type == TokenType.EOF; }
        private Token peek() { return tokens.get(pos); }
        private Token prev() { return tokens.get(pos - 1); }
    }

    // --- TREE PRINTER (operators centered above child centers) ---
    static class TreePrinter {

        static class Box {
            List<String> lines;
            int width;
            int height;
            int rootPos; // index of operator center within first line
            Box(List<String> lines, int width, int height, int rootPos) {
                this.lines = lines; this.width = width; this.height = height; this.rootPos = rootPos;
            }
        }

        static String print(Node node) {
            Box b = build(node);
            StringBuilder sb = new StringBuilder();
            for (String line : b.lines) sb.append(line).append("\n");
            return sb.toString();
        }

        private static Box build(Node node) {
           if (node instanceof NumberNode) {
    double v = ((NumberNode) node).value;

    // Format: remove .0 but keep decimals if needed
    String s = (v == Math.floor(v)) ? String.valueOf((int)v) : String.valueOf(v);

    return new Box(List.of(s), s.length(), 1, s.length() / 2);
}

            
            if (node instanceof UnaryOpNode) {
    UnaryOpNode un = (UnaryOpNode) node;

    // SPECIAL CASE: unary minus applied directly to a number → print as "-3"
    if (un.expr instanceof NumberNode) {
        double v = un.expr.evaluate();
        double neg = (un.op == TokenType.MINUS) ? -v : v;

        String s = (neg == Math.floor(neg)) ? String.valueOf((int)neg) : String.valueOf(neg);
        return new Box(List.of(s), s.length(), 1, s.length() / 2);
    }

    // NORMAL CASE: print vertical unary operator
    Box child = build(un.expr);
    String root = un.opSymbol();

    int gap = 1;
    int width = child.width + gap;

    int childCenter = child.rootPos;
    int rootPos = childCenter;

    char[] first = spaces(width);
    putString(first, root, rootPos - root.length() / 2);

    char[] second = spaces(width);
    second[childCenter] = '|';

    List<String> merged = new ArrayList<>();
    merged.add(new String(first));
    merged.add(new String(second));

    for (String line : child.lines) {
        merged.add(line + " ".repeat(gap));
    }

    return new Box(merged, width, merged.size(), rootPos);
}
   

            BinaryOpNode bin = (BinaryOpNode) node;
            Box left = build(bin.left);
            Box right = build(bin.right);
            String root = bin.opSymbol();  //BLUE color for operator in the TREE

            int gap = 3;
            int width = left.width + gap + right.width;

            // compute centers
            int leftCenter = left.rootPos;
            int rightCenter = left.width + gap + right.rootPos;
            int rootPos = (leftCenter + rightCenter) / 2;

            // first line: place operator centered at rootPos
            char[] first = spaces(width);
            putString(first, root, rootPos - root.length() / 2);

            // second line: branches at child centers
            char[] second = spaces(width);
            if (leftCenter >= 0 && leftCenter < width) second[leftCenter] = '/';
            if (rightCenter >= 0 && rightCenter < width) second[rightCenter] = '\\';

            // normalize heights
            int maxH = Math.max(left.height, right.height);
            List<String> leftLines = new ArrayList<>(left.lines);
            List<String> rightLines = new ArrayList<>(right.lines);
            while (leftLines.size() < maxH) leftLines.add(" ".repeat(left.width));
            while (rightLines.size() < maxH) rightLines.add(" ".repeat(right.width));

            List<String> merged = new ArrayList<>();
            merged.add(new String(first));
            merged.add(new String(second));
            for (int i = 0; i < maxH; i++) {
                String l = padRight(leftLines.get(i), left.width);
                String r = padRight(rightLines.get(i), right.width);
                merged.add(l + " ".repeat(gap) + r);
            }

            return new Box(merged, width, merged.size(), rootPos);
        }

        private static char[] spaces(int n) {
            char[] a = new char[n];
            Arrays.fill(a, ' ');
            return a;
        }

        private static void putString(char[] arr, String s, int pos) {
            if (pos < 0) pos = 0;
            for (int i = 0; i < s.length() && pos + i < arr.length; i++) arr[pos + i] = s.charAt(i);
        }

        private static String padRight(String s, int width) {
            if (s.length() >= width) return s;
            StringBuilder sb = new StringBuilder(s);
            while (sb.length() < width) sb.append(' ');
            return sb.toString();
        }
    }

    // --- MAIN & TEST CASES ---
    public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println(BLUE+"\t |********"+RED+"---C--S--C--2--2--0~~~~~"+RESET);
    System.out.println(BLUE+"\t |********"+RESET+"~~~~~~~~~~~~~~~~~~~~~~~~"); 
    System.out.println(RED+"\t |~~~ "+YELLOW+"MINI Expression COMPILER "+ RED+"~~~" +RESET);
    System.out.println("\t |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"); 
    System.out.println(RED+"\t |~S--P--R--I--N--G---2--0--2--6~~\n"+RESET);  
    System.out.println("Type an expression to evaluate, or type " +YELLOW+ "EXIT" +RESET + " to quit \n");
    
    while (true) {
        System.out.print(BLUE + "Enter expression: " + RESET);
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("exit")) {
            System.out.println(YELLOW + "GOODBYE!" + RESET);
            break;
        }

        try {
            // Lexing
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            System.out.print(BLUE + "Tokens:" + RESET + "[");
            for (int i = 0; i < tokens.size() - 1; i++) {
                System.out.print(tokens.get(i).value);
                if (i < tokens.size() - 2) System.out.print(", ");
            }
            System.out.println("]");

            // Parsing
            Parser parser = new Parser(tokens);
            Node root = parser.parse();

            // Print parse tree
            System.out.println(BLUE + "Parse Tree:" + RESET);  // Color BLUE 
            System.out.print(GREEN + TreePrinter.print(root) + RESET); //Color GREEN

            // Evaluate
            double result = root.evaluate();

// If result has no fractional part, print as integer
if (result % 1 == 0) {
    System.out.println(BLUE + "Evaluation Result: " + RESET + (int) result);
} else {
    System.out.println(BLUE + "Evaluation Result: " + RESET + String.format("%.1f", result));
}

        } catch (Exception e) {
            System.out.println(YELLOW + "INVALID input\n" +RED+ e.getMessage() + RESET); //Color Warning Message
        }

        System.out.println(GREEN + "---------------------------" + RESET);
    }

    scanner.close();
}

}
