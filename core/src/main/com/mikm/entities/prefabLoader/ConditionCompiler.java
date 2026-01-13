package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Entity;

import java.util.function.Predicate;

public class ConditionCompiler {
    private ConditionCompiler() {}

    public static Predicate<Entity> compile(String expr) {
        return e -> new Parser(expr).parseExpression().eval(e);
    }

    // ---------------- Parser ----------------
    private static class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) { this.input = input; }

        Expr parseExpression() { return parseOr(); }

        private Expr parseOr() {
            Expr left = parseAnd();
            while (match("||")) left = new BinaryExpr("||", left, parseAnd());
            return left;
        }

        private Expr parseAnd() {
            Expr left = parseEquality();
            while (match("&&")) left = new BinaryExpr("&&", left, parseEquality());
            return left;
        }

        private Expr parseEquality() {
            Expr left = parseAddSub();
            while (true) {
                if (match("==")) left = new BinaryExpr("==", left, parseAddSub());
                else if (match("!=")) left = new BinaryExpr("!=", left, parseAddSub());
                else if (match(">=")) left = new BinaryExpr(">=", left, parseAddSub());
                else if (match("<=")) left = new BinaryExpr("<=", left, parseAddSub());
                else if (match("<"))  left = new BinaryExpr("<", left, parseAddSub());
                else if (match(">"))  left = new BinaryExpr(">", left, parseAddSub());
                else break;
            }
            return left;
        }

        private Expr parseAddSub() {
            Expr left = parseMulDiv();
            while (true) {
                if (match("+")) left = new BinaryExpr("+", left, parseMulDiv());
                else if (match("-")) left = new BinaryExpr("-", left, parseMulDiv());
                else break;
            }
            return left;
        }

        private Expr parseMulDiv() {
            Expr left = parseUnary();
            while (true) {
                if (match("*")) left = new BinaryExpr("*", left, parseUnary());
                else if (match("/")) left = new BinaryExpr("/", left, parseUnary());
                else break;
            }
            return left;
        }

        private Expr parseUnary() {
            skipWhitespace();
            if (match("!")) return new UnaryExpr("!", parseUnary());
            if (match("-")) return new UnaryExpr("-", parseUnary());
            return parsePrimary();
        }

        private Expr parsePrimary() {
            skipWhitespace();
            if (match("(")) {
                Expr inside = parseExpression();
                expect(")");
                return inside;
            }
            return new ValueExpr(readToken());
        }

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
            if (!match(s))
                throw new IllegalArgumentException("Expected '" + s + "' at " + pos + " in: " + input);
        }

        private String readToken() {
            skipWhitespace();
            int start = pos;
            char c = peek();
            if (Character.isDigit(c) || c == '.' ||
                    (c == '-' && (Character.isDigit(peekNext()) || peekNext() == '.'))) {
                pos++;
                while (pos < input.length() && (Character.isDigit(peek()) || peek() == '.')) pos++;
                return input.substring(start, pos);
            }
            if (c == '"' || c == '\'') {
                char quote = c;
                pos++;
                int startContent = pos;
                while (pos < input.length() && input.charAt(pos) != quote) {
                    if (input.charAt(pos) == '\\' && pos + 1 < input.length()) pos++;
                    pos++;
                }
                if (pos >= input.length())
                    throw new IllegalArgumentException("Unterminated string literal at " + startContent);
                String str = input.substring(startContent, pos);
                pos++;
                return "'" + str + "'";
            }
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
            if ("()!<>=&|+-*/,".indexOf(c) >= 0) {
                pos++;
                return String.valueOf(c);
            }
            throw new IllegalArgumentException("Unexpected char '" + c + "' at " + pos);
        }

        private char peek() { return pos < input.length() ? input.charAt(pos) : '\0'; }
        private char peekNext() { return pos + 1 < input.length() ? input.charAt(pos + 1) : '\0'; }
        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
        }
    }

    // ---------------- Expression Interfaces ----------------
    private interface Expr { boolean eval(Entity e); }

    // ---------------- Expr Implementations ----------------
    private static class BinaryExpr implements Expr {
        private final String op;
        private final Expr left, right;
        BinaryExpr(String op, Expr l, Expr r) { this.op = op; left = l; right = r; }
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
                default: throw new IllegalArgumentException(op);
            }
        }
    }

    private static class UnaryExpr implements Expr {
        private final String op; private final Expr expr;
        UnaryExpr(String op, Expr e) { this.op = op; this.expr = e; }
        public boolean eval(Entity e) {
            return "!".equals(op) ? !expr.eval(e) : asDouble(resolve(expr, e)) < 0;
        }
    }

    private static class ValueExpr implements Expr {
        private final String token;
        ValueExpr(String t) { token = t; }
        public boolean eval(Entity e) {
            Object val = resolve(token, e);
            if (val instanceof Boolean) return (Boolean) val;
            if (val instanceof Number) return ((Number) val).doubleValue() != 0;
            return val != null;
        }
    }

    // ---------------- Resolve Helpers ----------------
    private static Object resolve(String token, Entity e) {
        token = token.trim();

        int idx = token.indexOf('(');
        if (idx > 0 && token.endsWith(")")) {
            String name = token.substring(0, idx);
            String argsString = token.substring(idx + 1, token.length() - 1).trim();

            java.util.List<String> args;
            if (argsString.isEmpty()) {
                args = java.util.Collections.emptyList();
            } else {
                args = java.util.Arrays.asList(argsString.split(","));
                for (int i = 0; i < args.size(); i++) args.set(i, args.get(i).trim());
            }

            return Blackboard.getInstance().callFunction(e, name, args);
        }


        if ((token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'")))
            return token.substring(1, token.length() - 1);
        try {
            if (token.contains(".")) return Double.parseDouble(token);
            return Integer.parseInt(token);
        } catch (NumberFormatException ignored) {}
        if ("true".equalsIgnoreCase(token)) return true;
        if ("false".equalsIgnoreCase(token)) return false;
        return Blackboard.getInstance().getVar(e, token);
    }


    private static Object resolve(Expr expr, Entity e) { return resolve(((ValueExpr) expr).token, e); }

    private static double asDouble(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        throw new IllegalArgumentException("Not a number: " + v);
    }
}
