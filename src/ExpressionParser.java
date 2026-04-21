import java.util.Map;

/**
 * Parses and evaluates a mathematical expression given as a String.
 *
 * Supports:
 *  - Basic arithmetic:  +  -  *  /
 *  - Parentheses for grouping
 *  - Math functions:    sin, cos, tan, sqrt, abs, exp, log
 *  - Unary minus:       e.g. -x or -(a+b)
 *  - Integer exponents: e.g. x^2  (positive integers only)
 *
 * Usage:
 *   ExpressionParser p = new ExpressionParser();
 *   double result = p.evaluate("a*x - b*x*y", names, state, constants);
 */
public class ExpressionParser {

    /**
     * Main entry point.
     * Substitutes constants and variable values into the equation string,
     * then evaluates the resulting numeric expression.
     *
     * @param equation  the equation string, e.g. "a*x - b*x*y"
     * @param names     variable names matching the state array, e.g. {"x","y"}
     * @param state     current values of those variables, e.g. {20.0, 2.0}
     * @param constants named constants, e.g. {"a"->0.25, "b"->0.15}
     * @return          the evaluated numeric result
     */
    public double evaluate(String equation,
                           String[] names,
                           double[] state,
                           Map<String, Double> constants) {

        // Work on a local copy so we never modify the caller's string
        String expr = equation;

        // 1. Substitute constants first.
        //    Sort by key length DESCENDING so that longer names are replaced first.
        //    e.g. "alpha" is replaced before "a", preventing "alpha" -> "a<number>lpha"
        String[] constKeys = constants.keySet().toArray(new String[0]);
        java.util.Arrays.sort(constKeys, (a, b) -> b.length() - a.length());
        for (String key : constKeys) {
            expr = expr.replace(key, Double.toString(constants.get(key)));
        }

        // 2. Substitute variables - again longer names first.
        //    e.g. {"vx","vy","x","y"}: replace "vx" before "x" so "vx" doesn't become
        //    "<number>x" with a stray "x" left behind.
        String[] sortedNames = names.clone();
        java.util.Arrays.sort(sortedNames,
                (a, b) -> b.length() - a.length()); // descending length

        for (String sortedName : sortedNames) {
            int stateIndex = indexOf(names, sortedName);
            expr = expr.replace(sortedName, Double.toString(state[stateIndex]));
        }

        // 3. Strip all spaces
        expr = expr.replace(" ", "");

        // 4. Recursively evaluate
        return calculate(expr);
    }

    // -----------------------------------------------------------------------
    // Helper: find index of a string in an array
    // -----------------------------------------------------------------------
    private int indexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) return i;
        }
        throw new IllegalArgumentException("Variable not found: " + target);
    }

    // -----------------------------------------------------------------------
    // Core recursive evaluator
    // Operator precedence (lowest to highest):
    //   1. + and -
    //   2. * and /
    //   3. ^ (exponentiation)
    //   4. unary minus, functions, parentheses, numbers
    // We handle lowest precedence LAST by scanning right-to-left and splitting.
    // -----------------------------------------------------------------------
    private double calculate(String expr) {

        if (expr.isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }

        // ---- STEP 1: strip outer parentheses if the whole expr is wrapped ----
        if (expr.charAt(0) == '(' && matchingParen(expr, 0) == expr.length() - 1) {
            return calculate(expr.substring(1, expr.length() - 1));
        }

        // ---- STEP 2: handle named math functions ----
        // sin(...), cos(...), tan(...), sqrt(...), abs(...), exp(...), log(...)
        String[] funcs = {"sin", "cos", "tan", "sqrt", "abs", "exp", "log"};
        for (String fn : funcs) {
            if (expr.startsWith(fn + "(") && expr.endsWith(")")) {
                // make sure the closing ) belongs to this function call, not something inside
                int openParen = fn.length(); // index of '('
                if (matchingParen(expr, openParen) == expr.length() - 1) {
                    double inner = calculate(expr.substring(openParen + 1, expr.length() - 1));
                    switch (fn) {
                        case "sin":  return Math.sin(inner);
                        case "cos":  return Math.cos(inner);
                        case "tan":  return Math.tan(inner);
                        case "sqrt": return Math.sqrt(inner);
                        case "abs":  return Math.abs(inner);
                        case "exp":  return Math.exp(inner);
                        case "log":  return Math.log(inner);
                    }
                }
            }
        }

        // ---- STEP 3: find lowest-precedence operator (+/-) outside parentheses ----
        // Scan RIGHT to LEFT so that left-associativity is preserved when we recurse.
        // Bug fix from original: depth is reset to 0 before each scan pass.
        int depth = 0;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            if (c == '(') depth--;
            // only split on + or - that are:
            //   - at depth 0 (not inside parentheses)
            //   - not at position 0 (that would be a unary operator, handled below)
            //   - preceded by a digit or closing paren (so we know it's binary, not unary)
            if (depth == 0 && i > 0 && (c == '+' || c == '-')) {
                char prev = expr.charAt(i - 1);
                // Binary + or - must follow a number or closing paren
                // If it follows an operator or opening paren, it's unary
                if (Character.isDigit(prev) || prev == ')') {
                    double left  = calculate(expr.substring(0, i));
                    double right = calculate(expr.substring(i + 1));
                    return c == '+' ? left + right : left - right;
                }
            }
        }

        // ---- STEP 4: find * or / outside parentheses ----
        depth = 0; // RESET - this was the bug in the original code
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            if (c == '(') depth--;
            if (depth == 0 && i > 0 && (c == '*' || c == '/')) {
                // Safety check: ensure right side is non-empty
                if (i < expr.length() - 1) {
                    double left  = calculate(expr.substring(0, i));
                    double right = calculate(expr.substring(i + 1));
                    return c == '*' ? left * right : left / right;
                }
            }
        }

        // ---- STEP 5: handle ^ (exponentiation) ----
        depth = 0; // RESET
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            if (c == '(') depth--;
            if (depth == 0 && c == '^' && i > 0) {
                // Safety check: ensure right side is non-empty
                if (i < expr.length() - 1) {
                    double base     = calculate(expr.substring(0, i));
                    double exponent = calculate(expr.substring(i + 1));
                    return Math.pow(base, exponent);
                }
            }
        }

        // ---- STEP 6: handle unary minus, e.g. -x or -(a+b) ----
        if (expr.charAt(0) == '-') {
            return -calculate(expr.substring(1));
        }

        // ---- STEP 7: base case - must be a plain number ----
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Cannot parse token: '" + expr + "' — check your equation string for typos.");
        }
    }

    /**
     * Given an expression and the index of an opening '(',
     * returns the index of its matching closing ')'.
     * Throws if parentheses are unbalanced.
     */
    private int matchingParen(String expr, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') depth++;
            if (expr.charAt(i) == ')') depth--;
            if (depth == 0) return i;
        }
        throw new IllegalArgumentException(
            "Unbalanced parentheses in: " + expr);
    }
}
