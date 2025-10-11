package com.mikm.entityLoader;

import com.mikm.entities.Entity;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ConditionCompiler {
    //Doesn't allow nesting for numerical expressions like (a + b) * 2

    private ConditionCompiler() {}

    public static Predicate<Entity> compile(String expr) {
        return e -> new Parser(expr).parseExpression().eval(e);
    }

    // ---------------- Parser ----------------
    private static class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) {
            this.input = input;
        }

        Expr parseExpression() {
            return parseOr();
        }

        // ||
        private Expr parseOr() {
            Expr left = parseAnd();
            while (match("||")) {
                Expr right = parseAnd();
                left = new BinaryExpr("||", left, right);
            }
            return left;
        }

        // &&
        private Expr parseAnd() {
            Expr left = parseEquality();
            while (match("&&")) {
                Expr right = parseEquality();
                left = new BinaryExpr("&&", left, right);
            }
            return left;
        }

        // ==, !=, <, >, <=, >=
        private Expr parseEquality() {
            Expr left = parseAddSub();
            while (true) {
                if (match("==")) left = new BinaryExpr("==", left, parseAddSub());
                else if (match("!=")) left = new BinaryExpr("!=", left, parseAddSub());
                else if (match(">=")) left = new BinaryExpr(">=", left, parseAddSub());
                else if (match("<=")) left = new BinaryExpr("<=", left, parseAddSub());
                else if (match("<")) left = new BinaryExpr("<", left, parseAddSub());
                else if (match(">")) left = new BinaryExpr(">", left, parseAddSub());
                else break;
            }
            return left;
        }

        // +, -
        private Expr parseAddSub() {
            Expr left = parseMulDiv();
            while (true) {
                if (match("+")) left = new BinaryExpr("+", left, parseMulDiv());
                else if (match("-")) left = new BinaryExpr("-", left, parseMulDiv());
                else break;
            }
            return left;
        }

        // *, /
        private Expr parseMulDiv() {
            Expr left = parseUnary();
            while (true) {
                if (match("*")) left = new BinaryExpr("*", left, parseUnary());
                else if (match("/")) left = new BinaryExpr("/", left, parseUnary());
                else break;
            }
            return left;
        }

        // unary
        private Expr parseUnary() {
            skipWhitespace();
            if (match("!")) return new UnaryExpr("!", parseUnary());
            if (match("-")) return new UnaryExpr("-", parseUnary());
            return parsePrimary();
        }

        // literal, variable, or (expr)
        private Expr parsePrimary() {
            skipWhitespace();
            if (match("(")) {
                Expr inside = parseExpression();
                expect(")");
                return inside;
            }
            String token = readToken();
            return new ValueExpr(token);
        }

        // helpers
        private boolean match(String s) {
            skipWhitespace();
            if (input.startsWith(s, pos)) {
                pos += s.length();
                skipWhitespace();
                return true;
            }
            return false;
        }

        private void expect(String s) {
            if (!match(s)) {
                throw new IllegalArgumentException("Expected '" + s + "' at position " + pos + " in: " + input);
            }
        }

        private String readToken() {
            skipWhitespace();
            int start = pos;

            // handle numbers
            char c = peek();
            if (Character.isDigit(c) || c == '.' || (c == '-' && (Character.isDigit(peekNext()) || peekNext() == '.'))) {
                pos++;
                while (pos < input.length() && (Character.isDigit(peek()) || peek() == '.')) pos++;
                return input.substring(start, pos);
            }

            // string literals ('text' or "text")
            if (c == '"' || c == '\'') {
                char quote = c;
                pos++;
                int startContent = pos;
                while (pos < input.length() && input.charAt(pos) != quote) {
                    if (input.charAt(pos) == '\\' && pos + 1 < input.length()) pos++; // skip escaped chars
                    pos++;
                }
                if (pos >= input.length()) {
                    throw new IllegalArgumentException("Unterminated string literal at position " + startContent + " in: " + input);
                }
                String str = input.substring(startContent, pos);
                pos++; // skip closing quote
                return "'" + str + "'"; // preserve quotes for stripQuotes later
            }

            // identifiers / functions
            if (Character.isJavaIdentifierStart(c)) {
                while (pos < input.length() && (Character.isJavaIdentifierPart(peek()) || peek() == '_')) pos++;
                if (pos < input.length() && peek() == '(') {
                    int depth = 0;
                    while (pos < input.length()) {
                        char ch = peek();
                        if (ch == '(') depth++;
                        else if (ch == ')') {
                            depth--;
                            pos++;
                            if (depth == 0) break;
                        }
                        pos++;
                    }
                }
                return input.substring(start, pos).trim();
            }


            if (c == '(' || c == ')' || c == ',' || c == '!' || c == '<' || c == '>' || c == '=' ||
                    c == '&' || c == '|' || c == '+' || c == '-' || c == '*' || c == '/') {
                pos++;
                return String.valueOf(c);
            }

            throw new IllegalArgumentException("Unexpected character '" + c + "' at " + pos + " in: " + input);
        }

        private char peek() {
            return pos < input.length() ? input.charAt(pos) : '\0';
        }

        private char peekNext() {
            return pos + 1 < input.length() ? input.charAt(pos + 1) : '\0';
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
        }
    }

    // ---------------- Expressions ----------------
    private interface Expr {
        boolean eval(Entity e);
    }

    private static class BinaryExpr implements Expr {
        private final String op;
        private final Expr left, right;

        BinaryExpr(String op, Expr left, Expr right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean eval(Entity e) {
            switch (op) {
                case "&&": return left.eval(e) && right.eval(e);
                case "||": return left.eval(e) || right.eval(e);
                case "==": return resolve(left, e).equals(resolve(right, e));
                case "!=": return !resolve(left, e).equals(resolve(right, e));
                case "<":  return asDouble(resolve(left, e)) < asDouble(resolve(right, e));
                case ">":  return asDouble(resolve(left, e)) > asDouble(resolve(right, e));
                case "<=": return asDouble(resolve(left, e)) <= asDouble(resolve(right, e));
                case ">=": return asDouble(resolve(left, e)) >= asDouble(resolve(right, e));
                case "+":  return asDouble(resolve(left, e)) + asDouble(resolve(right, e)) != 0;
                case "-":  return asDouble(resolve(left, e)) - asDouble(resolve(right, e)) != 0;
                case "*":  return asDouble(resolve(left, e)) * asDouble(resolve(right, e)) != 0;
                case "/":  return asDouble(resolve(left, e)) / asDouble(resolve(right, e)) != 0;
                default: throw new IllegalArgumentException("Unknown operator: " + op);
            }
        }
    }

    private static class UnaryExpr implements Expr {
        private final String op;
        private final Expr expr;

        UnaryExpr(String op, Expr expr) {
            this.op = op;
            this.expr = expr;
        }

        @Override
        public boolean eval(Entity e) {
            switch (op) {
                case "!": return !expr.eval(e);
                case "-": return asDouble(resolve(expr, e)) != 0 ? asDouble(resolve(expr, e)) < 0 : false;
                default: throw new IllegalArgumentException("Unknown unary operator: " + op);
            }
        }
    }

    private static class ValueExpr implements Expr {
        private final String token;

        ValueExpr(String token) { this.token = token; }

        @Override
        public boolean eval(Entity e) {
            Object val = resolve(token, e);
            if (val instanceof Boolean) return (Boolean) val;
            if (val instanceof Number) return ((Number) val).doubleValue() != 0;
            return val != null;
        }
    }

    // ---------------- Resolve helpers ----------------
    private static Object resolve(String token, Entity e) {
        token = token.trim();
        // string literals
        if ((token.startsWith("\"") && token.endsWith("\"")) ||
                (token.startsWith("'") && token.endsWith("'"))) {
            return token.substring(1, token.length() - 1);
        }

        try {
            if (token.contains(".")) return Double.parseDouble(token);
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {}

        if ("true".equalsIgnoreCase(token)) return true;
        if ("false".equalsIgnoreCase(token)) return false;

        if (token.contains("(") && token.endsWith(")")) {
            String fName = token.substring(0, token.indexOf("(")).trim();
            String argStr = token.substring(token.indexOf("(") + 1, token.length() - 1);

            List<String> args = argStr.isEmpty()
                    ? Collections.emptyList()
                    : splitArgs(argStr);

            List<String> cleanArgs = new java.util.ArrayList<>();
            for (String arg : args) cleanArgs.add(stripQuotes(arg));

            return Blackboard.getInstance().callFunction(e, fName, cleanArgs);
        }

        return Blackboard.getInstance().getVar(e, token);
    }

    private static Object resolve(Expr expr, Entity e) {
        if (expr instanceof ValueExpr) return resolve(((ValueExpr) expr).token, e);
        if (expr instanceof BinaryExpr) return ((BinaryExpr) expr).eval(e);
        if (expr instanceof UnaryExpr) return ((UnaryExpr) expr).eval(e);
        throw new IllegalArgumentException("Unknown expr type: " + expr);
    }

    private static double asDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        throw new IllegalArgumentException("Not a number: " + val);
    }

    private static List<String> splitArgs(String argStr) {
        List<String> args = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < argStr.length(); i++) {
            char c = argStr.charAt(i);
            if ((c == '"' || c == '\'') && (i == 0 || argStr.charAt(i - 1) != '\\')) {
                if (inQuotes && c == quoteChar) inQuotes = false;
                else if (!inQuotes) { inQuotes = true; quoteChar = c; }
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                args.add(current.toString().trim());
                current.setLength(0);
            } else current.append(c);
        }
        if (current.length() > 0) args.add(current.toString().trim());
        return args;
    }

    private static String stripQuotes(String s) {
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))
            return s.substring(1, s.length() - 1);
        return s;
    }
}
