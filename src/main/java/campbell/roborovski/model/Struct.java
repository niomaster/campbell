package campbell.roborovski.model;

import campbell.language.model.unscoped.*;
import campbell.language.model.unscoped.Expression;

import java.util.LinkedList;

public class Struct {
    private final LinkedList<Variable> variables = new LinkedList<>();

    public void addVariable(Variable var) {
        variables.add(var);
    }

    public int getOffset(String varName) {
        int current = 0;

        for(Variable var : variables) {
            if(var.getName().equals(varName)) {
                return current;
            } else {
                current += var.getSize();
            }
        }

        throw new RuntimeException("Internal error: Unknown variable " + varName + " in struct");
    }

    public int getSize() {
        int size = 0;

        for(Variable var : variables) {
            size += var.getSize();
        }

        return size;
    }
}
