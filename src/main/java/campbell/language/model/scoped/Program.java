package campbell.language.model.scoped;

import campbell.language.model.unscoped.SharedDeclStatement;
import campbell.language.model.Statement;
import campbell.language.model.Symbol;
import campbell.language.model.unscoped.DeclStatement;
import campbell.language.types.Type;
import campbell.parser.CampbellStreamParser;
import campbell.parser.gen.CampbellParser;
import sprockell.SprockellEmitter;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Program represents a Campbell program
 */
public class Program extends Scope {
    /**
     * List containing all statements in this program
     */
    private List<? extends Statement> statements;

    public Program(List<? extends Statement> statements) {
        this.statements = statements;
    }

    /**
     * Tries to parse a Program from a given context
     * @param context
     * @return
     */
    public static Program fromContext(CampbellParser.ProgramContext context) {
        return at(context.getStart(), new Program(context.topLevelStatement().stream().map(Statement::fromContext).collect(Collectors.toList())));
    }

    /**
     * Tries to parse a Program from a given inputstream
     * @param input
     * @return
     */
    public static Program parseFrom(InputStream input) {
        return Program.fromContext(CampbellStreamParser.parse(input));
    }

    /**
     * Sets the scope for this program and all its statements
     * @param scope - Scope of this program
     */
    @Override
    public void setScope(Scope scope) {
        this.scope = scope;

        for (Statement s : statements) {
            s.setScope(this);
        }
    }

    /**
     * Makes a string representation of this program with correct indenting and all its statements
     * @param indent - indent level of this program
     * @return string representation of this program
     */
    @Override
    public String toString(int indent) {
        String result = indent(indent) + getComment();

        for(Statement stat : statements) {
            result += "\n";
            result += stat.toString(indent);
        }

        return result;
    }

    /**
     * Merges two programs
     *
     * This enables the including of other .ham files and compile them together (enables use of libraries)
     * @param other
     * @return
     */
    public Program merge(Program other) {
        return new Program(Stream.concat(statements.stream(), other.statements.stream()).collect(Collectors.toList()));
    }

    /**
     * Finds definitions in this program.
     * Definition can be a function, declaration or a class.
     */
    @Override
    public void findDefinitions() {
        for(Statement stat : statements) {
            if(stat instanceof FunStatement) {
                symbols.put(((FunStatement) stat).getName(), (Symbol) stat);
            } else if(stat instanceof DeclStatement) {
                symbols.put(((DeclStatement) stat).getName(), (Symbol) stat);
            } else if(stat instanceof SharedDeclStatement) {
                symbols.put(((SharedDeclStatement) stat).getName(), (Symbol) stat);
            } else if(stat instanceof ClassStatement) {
                types.put(((ClassStatement) stat).getType().getName(), ((ClassStatement) stat).getType());
            }

            if(stat instanceof Scope) {
                ((Scope) stat).findDefinitions();
            }
        }
    }

    /**
     * Finds implementations in this scope
     */
    @Override
    public void findImpls() {
        for(Statement stat : statements) {
            if(stat instanceof Scope) {
                ((Scope) stat).findImpls();
            }
        }
    }

    /**
     * Converts this program to the IR Roborovski
     * @param program
     * @param block
     */
    @Override
    public void toRoborovski(campbell.roborovski.model.Program program, campbell.roborovski.model.Block block) {
        /**
         * ClassStatement -> functies en een struct
         * ForStatement -> compilet naar een while
         * FunStatement -> functies
         * If -> compilet naar een if
         * Program -> block
         * Unsafe -> block
         * While -> compilet naar een while, block
         * Assign
         * Return
         * Expressions
         */

        for(Statement statement : statements) {
            statement.toRoborovski(program, block);
        }
    }

    /**
     * Makes a deep copy of this program
     * @return deep copy of this program
     */
    @Override
    public Statement deepCopy() {
        return new Program(statements.stream().map(Statement::deepCopy).collect(Collectors.toList()));
    }

    /**
     * Replaces a given type by another given type within this program
     * @param replace - type that should be replaced
     * @param replaceWith - replacement type
     */
    @Override
    public void replaceType(Type replace, Type replaceWith) {
        for (Statement s : statements) {
            s.replaceType(replace, replaceWith);
        }
    }

    /**
     * Method that checks whether this program returns
     *
     * Program inevitably returns
     * @return
     */
    @Override
    public boolean returns() {
        return true;
    }

    /**
     * Type checking for a program
     */
    @Override
    public void checkType() {
        if (statements != null) {
            for (Statement stat : statements) {
                stat.checkType();
            }
        }
    }
}