package it.feio.android.omninotes.export;

import java.util.HashMap;

class Replacer {
    public interface ReplaceFunc {
        String replace();
    }

    private final HashMap<String, ReplaceFunc> replaceFuncs = new HashMap<>();
    private final String[] template;

    private Replacer(String[] template) {
        this.template = template;
    }

    public static Replacer make(String[] template) {
        return new Replacer(template);
    }

    public Replacer variable(String variable, ReplaceFunc func) {
        replaceFuncs.put(variable, func);
        return this;
    }

    public String replace() {
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

    private boolean isVariable(String line) {
        return line.startsWith("$");
    }

    private String getVariable(String line) {
        return line.substring(1);
    }
}