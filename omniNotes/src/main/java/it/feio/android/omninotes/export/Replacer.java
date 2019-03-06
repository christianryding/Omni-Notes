package it.feio.android.omninotes.export;

import java.util.HashMap;

/**
 * Helper class for the HTML exporter. The purpose of the class to go through an array of strings
 * and replace all strings starting with $ with a information from the note. The caller registers
 * a number ReplaceFunc implementations, one for each specific $-strings. ReplaceFunc.replace is
 * called when the associated $-string is found, the returned text is sued to replace the $-string.
 */
class Replacer {
    /**
     * Function interface that is called when a $-string is found.
     */
    public interface ReplaceFunc {
        String replace();
    }

    /**
     * Maps $-strings to replace functions
     */
    private final HashMap<String, ReplaceFunc> replaceFuncs = new HashMap<>();
    /**
     * The template in use.
     */
    private final String[] template;

    /**
     * Constructs a replacer for a template string array.
     * @param template string array.
     */
    private Replacer(String[] template) {
        this.template = template;
    }

    /**
     * Constructs a replacer for a template string array.
     * @param template template string array containing $-strings to be replaced.
     * @return the constructed replacer instance.
     */
    public static Replacer make(String[] template) {
        return new Replacer(template);
    }

    /**
     * Registers a replacer function. This is later used when the template is processed.
     * @param variable the string variable should be matched for this function, excluding
     *                 $. For example <code>"TITLE"</code> would match all occurrences of
     *                 <code>"$TITLE"</code> in the template strings.
     * @param func called when <code>variable</code> is found to get the string that should replace
     *             the $-string.
     * @return
     */
    public Replacer variable(String variable, ReplaceFunc func) {
        replaceFuncs.put(variable, func);
        return this;
    }

    /**
     * Does the actual replacement of the strings. Returns a input template as a single stirng with
     * all variables replaced.
     * @return the final string.
     * @throws IllegalStateException if an registered $-string is found.
     */
    public String replace() throws IllegalStateException {
        StringBuilder sb = new StringBuilder();

        for (String line : template) {
            if (isVariable(line)) {
                String variable = getVariable(line);
                if (replaceFuncs.containsKey(variable)) {
                    sb.append(replaceFuncs.get(variable).replace());
                } else {
                    throw new IllegalStateException("No replace function for variable:" + line);
                }
            } else {
                sb.append(line).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Checks if a string is a $-string.
     * @param line the string that should be tested.
     * @return true if this is a $-string, otherwise false.
     */
    private boolean isVariable(String line) {
        return line.startsWith("$");
    }

    /**
     * Gets the variable part of the $-string, that is the string without $.
     * @param line the string
     * @return the string without $
     */
    private String getVariable(String line) {
        return line.substring(1);
    }
}