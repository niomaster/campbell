package campbell.language.model.scoped;

import campbell.language.model.Statement;
import campbell.language.model.Symbol;
import campbell.language.model.unscoped.DeclStatement;
import campbell.language.types.Type;
import campbell.parser.gen.CampbellParser;
import campbell.roborovski.model.*;
import campbell.roborovski.model.Program;

import java.util.List;
import java.util.stream.Collectors;

public class TraitStatement extends Scope {
    private final Type type;
    private final List<? extends Type> of;
    private final List<? extends Statement> statements;

    public TraitStatement(Type type, List<? extends Statement> statements) {
        this.type = type;
        this.of = null;
        this.statements = statements;
    }

    public TraitStatement(Type type, List<? extends Type> of, List<? extends Statement> statements) {
        this.type = type;
        this.of = of;
        this.statements = statements;
    }

    public static Statement fromContext(CampbellParser.TraitContext trait) {
        Type type = Type.fromContext(trait.className());
        List<? extends Statement> statements = Statement.fromContexts(trait.block().statement());

        if(trait.classList() != null) {
            List<Type> of = Type.fromContexts(trait.classList().className());
            return at(trait.getStart(), new TraitStatement(type, of, statements));
        } else {
            return at(trait.getStart(), new TraitStatement(type, statements));
        }
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;

        for (Statement s : statements) {
            s.setScope(this);
        }
    }

    @Override
    public String toString(int indent) {
        String result = indent(indent) + "trait " + type.toString();

        if(of != null) {
            result += " of ";

            if(of.size() == 1) {
                result += of.get(0).toString();
            } else {
                result += "(";

                boolean firstType = true;

                for(Type t : of) {
                    if(!firstType) {
                        result += ", ";
                    }

                    firstType = false;

                    result += t.toString();
                }

                result += ")";
            }
        }

        result += " " + getComment();

        for(Statement stat : statements) {
            result += "\n"  + stat.toString(indent + 1);
        }

        return result;
    }

    @Override
    public void toRoborovski(Program program, Block block) {
        // nop
    }

    @Override
    public TraitStatement deepCopy() {
        if(of == null) {
            return new TraitStatement(type, statements.stream().map(Statement::deepCopy).collect(Collectors.toList()));
        } else {
            return new TraitStatement(type, of, statements.stream().map(Statement::deepCopy).collect(Collectors.toList()));
        }
    }

    @Override
    public void replaceType(Type replace, Type replaceWith) {
        type.replaceType(replace, replaceWith);

        if (of != null) {
            for (Type t : of) {
                t.replaceType(replace, replaceWith);
            }
        }

        for (Statement s : statements) {
            s.replaceType(replace, replaceWith);
        }
    }

    @Override
    public void findDefinitions() {
        for(Statement stat : statements) {
            if(stat instanceof FunStatement) {
                symbols.put(((FunStatement) stat).getName(), (Symbol) stat);
            } else if(stat instanceof DeclStatement) {
                symbols.put(((DeclStatement) stat).getName(), (Symbol) stat);
            } else if(stat instanceof ClassStatement) {
                types.put(((ClassStatement) stat).getType().getName(), ((ClassStatement) stat).getType());
            }

            if(stat instanceof Scope) {
                ((Scope) stat).findDefinitions();
            }
        }
    }

    @Override
    public void findImpls() {
        // Impossible
    }
}