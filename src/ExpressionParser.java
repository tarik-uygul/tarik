import java.util.Map;

// Takes a math equation written as a string and evaluates it into a number.
// Basically a mini calculator that also handles variables and named constants.
//
// Supports:
//   - Basic arithmetic: + - * /
//   - Parentheses for grouping
//   - Exponentiation: x^2
//   - Unary minus: -x or -(a+b)
//   - Math functions: sin, cos, tan, sqrt, abs, exp, log
public class ExpressionParser {

    public double evaluate(String equation, String[] names,
                           double[] state, Map<String, Double> constants) {

        // step 1: replace constants using whole-word matching
        // we use regex boundaries so that "t" doesn't accidentally replace
        // the "t" inside "time" for example
        // the pattern (?<![a-zA-Z0-9.]) means "not preceded by a letter/digit/dot"
        // and (?![a-zA-Z0-9.]) means "not followed by a letter/digit/dot"
        // this way only the standalone token gets replaced
        // IMPORTANT: wrap replacements in parentheses to preserve order of operations
        for (Map.Entry<String, Double> entry : constants.entrySet()) {
            equation = equation.replaceAll(
                "(?<![a-zA-Z0-9.])" + entry.getKey() + "(?![a-zA-Z0-9.])",
                "(" + entry.getValue() + ")"
            );
        }

        // step 2: replace variables using the same whole-word matching
        // i is the same index for both names and state so the right value
        // always gets substituted for the right variable name
        // IMPORTANT: wrap replacements in parentheses to preserve order of operations
        // e.g. "a*x + b*y" becomes "0.25*(1.0) + 0.15*(2.0)" not "0.25*1.0 + 0.15*2.0"
        // this prevents issues like "x+0.15" becoming "1.0+0.15" which could be misparsed
        for (int i = 0; i < names.length; i++) {
            equation = equation.replaceAll(
                "(?<![a-zA-Z0-9.])" + names[i] + "(?![a-zA-Z0-9.])",
                "(" + state[i] + ")"
            );
        }

        // step 3: remove all spaces
        equation = equation.replace(" ", "");

        // step 4: evaluate the resulting numeric string
        return calculate(equation);
    }

    // -----------------------------------------------------------------------
    // Recursive evaluator
    //
    // Operator precedence, lowest to highest:
    //   1. + and -
    //   2. * and /
    //   3. ^
    //   4. unary minus, math functions, parentheses, plain numbers
    //
    // We handle the lowest-precedence operators LAST by scanning right-to-left
    // and splitting the string there. Each half recurses into this same method.
    //
    // Example: "2*3+7"
    //   - finds + at depth 0, splits into "2*3" and "7"
    //   - "7" parses directly to 7.0
    //   - "2*3" finds * at depth 0, splits into "2" and "3"
    //   - both parse to 2.0 and 3.0, multiply to 6.0
    //   - final result: 6.0 + 7.0 = 13.0
    // -----------------------------------------------------------------------
    private double calculate(String equation) {

        if (equation.isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }

        // ---- handle named math functions first ----
        // sin(...), cos(...), tan(...), sqrt(...), abs(...), exp(...), log(...)
        // we check these before the operator scan so that "sin(x+1)" isn't
        // misread as a variable name followed by an operator
        String[] funcs = {"sin", "cos", "tan", "sqrt", "abs", "exp", "log"};
        for (String fn : funcs) {
            if (equation.startsWith(fn + "(") && equation.endsWith(")")) {
                // make sure the closing ) belongs to THIS function call
                // and not to something nested inside it
                int openParen = fn.length();
                if (matchingParen(equation, openParen) == equation.length() - 1) {
                    double inner = calculate(equation.substring(openParen + 1, equation.length() - 1));
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

        // ---- handle + and - (lowest priority) ----
        // scan right to left so left-associativity is preserved
        // e.g. 5-3-1 splits at the rightmost - giving (5-3)-1 = 1, not 5-(3-1) = 3
        // depth tracking: going right-to-left, ) opens a group and ( closes one
        // we only split on operators that are outside all brackets (depth == 0)
        int depth = 0;
        for (int i = equation.length() - 1; i >= 0; i--) {
            char c = equation.charAt(i);
            if (c == ')') depth++;
            if (c == '(') depth--;
            if (depth == 0 && (c == '+' || c == '-') && i > 0) {
                // make sure this is a binary operator, not a unary minus
                // a binary + or - must be preceded by a digit or closing paren
                char prev = equation.charAt(i - 1);
                if (Character.isDigit(prev) || prev == ')') {
                    double left  = calculate(equation.substring(0, i));
                    double right = calculate(equation.substring(i + 1));
                    return c == '+' ? left + right : left - right;
                }
            }
        }

        // ---- handle * and / (medium priority) ----
        depth = 0; // reset - forgetting this was the original bug
        for (int i = equation.length() - 1; i >= 0; i--) {
            char c = equation.charAt(i);
            if (c == ')') depth++;
            if (c == '(') depth--;
            if (depth == 0 && (c == '*' || c == '/') && i > 0) {
                double left  = calculate(equation.substring(0, i));
                double right = calculate(equation.substring(i + 1));
                return c == '*' ? left * right : left / right;
            }
        }

        // ---- handle ^ exponentiation ----
        // scan left to right so that 2^3^2 = 2^(3^2) = 512
        // which is the standard mathematical convention (right-associative)
        depth = 0; // reset
        for (int i = 0; i < equation.length(); i++) {
            char c = equation.charAt(i);
            if (c == '(') depth++;
            if (c == ')') depth--;
            if (depth == 0 && c == '^' && i > 0) {
                double base     = calculate(equation.substring(0, i));
                double exponent = calculate(equation.substring(i + 1));
                return Math.pow(base, exponent);
            }
        }

        // ---- strip outer parentheses ----
        // e.g. (2+3) becomes 2+3 and recurses normally
        if (equation.charAt(0) == '(' && equation.endsWith(")")) {
            if (matchingParen(equation, 0) == equation.length() - 1) {
                return calculate(equation.substring(1, equation.length() - 1));
            }
        }

        // ---- handle unary minus ----
        // e.g. -x or -(a+b)
        // only reached here if no binary operator was found above
        if (equation.charAt(0) == '-') {
            return -calculate(equation.substring(1));
        }

        // ---- base case: plain number ----
        try {
            return Double.parseDouble(equation);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Cannot parse: '" + equation + "' - check your equation for typos. " +
                "Math functions require parentheses: sin(x), cos(x), sqrt(x), abs(x), exp(x), log(x)");
        }
    }

    // finds the closing ) that matches the ( at openIndex
    private int matchingParen(String expr, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') depth++;
            if (expr.charAt(i) == ')') depth--;
            if (depth == 0) return i;
        }
        throw new IllegalArgumentException("Unbalanced parentheses in: " + expr);
    }
}
