package org.gcalc;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class Equation {
    public Equation(String rawEquation) throws InvalidParameterException {
        rawEquation = rawEquation.replaceAll(" ", "");
        String[] equationParts = rawEquation.split("=");

        if (equationParts.length > 2)
            throw new InvalidParameterException(
                    "Equation must not contain multiple equalities");

        if (equationParts.length == 1)
            // We assume that if no equality is specified, that the entire
            // expression is equal to y
            equationParts = new String[]{"y", equationParts[0]};
        else {
            // If an equality is specified, we need to make sure that the
            // equation is expressed in terms of y, so that the evaluate()
            // method works properly (it's rather naive)
            equationParts[1] = this.rearrange(equationParts[0], equationParts[1]);
            equationParts[0] = "y";
        }
    }

    /**
     * Finds all y values which satisfy the equation for a given x value. The
     * returned array contains a value for each root of the equation. Each
     * array position is guaranteed to represent the same root for all x
     * values.
     *
     * A NaN value means that that root does not exist for the specified x
     * value.
     *
     * @param x The x value to insert into the equation
     * @return Array of roots - each root is either a valid number, or NaN
     */
    public double[] evaluate(double x) {
        return new double[]{Math.sin(x)};
    }

    /**
     * Rearranges an equation to be expressed in terms of y.
     *
     * @param lhs Expression on left side of equals sign
     * @param rhs Expression on right side of equals sign
     * @return The expression on the right side of the rearranged equation's
     *         equals sign (left side is implied to be `y=`)
     */
    private String rearrange(String lhs, String rhs) {
        return null;
    }

    public static class Expression {
        protected String rawExpression;

        protected ArrayList<Instruction> ops = new ArrayList<>();

        public Expression(String rawExpression) throws InvalidParameterException {
            this.rawExpression = rawExpression;

            this.parseRecursive();
        }

        @Override
        public String toString() {
            String ret = "Expression \"" + this.rawExpression + "\" {\n";

            int stackN = 0;
            for (Instruction i : this.ops) {
                ret += "    " + Integer.toString(stackN) + ": ";

                switch(i.instruction) {
                    case ADD:
                        ret += "ADD";
                        break;
                    case SUB:
                        ret += "SUB";
                        break;
                    case MUL:
                        ret += "MUL";
                        break;
                    case DIV:
                        ret += "DIV";
                        break;
                    case FACT:
                        ret += "FACT";
                        break;
                    case NATIVEFUNC:
                        ret += "NATIVEFUNC " + i.arg;
                        break;
                    case EXPR:
                        ret += "EXPR => " + i.arg.toString().replaceAll("\n", "\n    ");
                        break;
                    case PUSH:
                        ret += "PUSH " + Double.toString((Double) i.arg);
                        break;
                    case PUSHVAR:
                        ret += "PUSHVAR " + i.arg;
                        break;
                }

                ret += "\n";
                stackN++;
            }

            return ret + "}";
        }

        protected void parseRecursive() {
            String raw = this.rawExpression;

            while (raw.length() > 0) {
                // Create sub-expression from bracketed region
                int parenLoc = raw.indexOf("(");
                if (parenLoc > -1) {
                    int depth = 0;
                    for (int i = parenLoc; i < raw.length(); i++) {
                        char c = raw.charAt(i);

                        if (c == '(')
                            depth++;
                        else if (c == ')')
                            depth--;

                        if (depth == 0) {
                            String subExpr = raw.substring(parenLoc + 1, i);
                            ops.add(new Instruction(Instruction.InstType.EXPR,
                                                    new Expression(subExpr)));
                            return;
                        }
                    }
                } else return;
            }
        }
    }
}

/**
 * For internal use by Equation.Expression. Don't use this manually.
 */
class Instruction {
    public enum InstType {
        // No args
        ADD, SUB, MUL, DIV, FACT,
        // Takes a string naming a math function to execute (e.g. "sin")
        NATIVEFUNC,
        // Takes an Expression instance, which is evaluated, and the result pushed
        EXPR,
        // Takes a Double to push onto the operand stack
        PUSH,
        // Takes a string naming a variable which will be supplied at eval time
        PUSHVAR
    }

    public InstType instruction;
    public Object arg;

    /**
     * Creates a new instruction definition. Many instructions do not need an
     * argument, in which case, arg should be null. Others will take instruction-
     * specific arguments.
     *
     * @param instruction The instruction to represent
     * @param arg An optional argument for the ex
     */
    public Instruction(InstType instruction, Object arg) {
        this.instruction = instruction;
        this.arg = arg;
    }
}
