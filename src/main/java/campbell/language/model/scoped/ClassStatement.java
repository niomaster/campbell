package campbell.language.model.scoped;

import campbell.language.model.CompileException;
import campbell.language.model.Statement;
import campbell.language.model.Symbol;
import campbell.language.model.unscoped.DeclStatement;
import campbell.language.types.ClassType;
import campbell.language.types.GenericType;
import campbell.language.types.Type;
import campbell.parser.gen.CampbellParser;

import java.util.List;

public class ClassStatement extends Scope {
    private ClassType type;
    private final List<? extends Statement> statements;

    public ClassStatement(ClassType type, List<? extends Statement> statements) {
        this.type = type;
        this.statements = statements;
    }

    public static Statement fromContext(CampbellParser.ClassNodeContext classNodeContext) {
        Type type = Type.fromContext(classNodeContext.className());

        if(type instanceof ClassType) {
            ClassStatement stat = new ClassStatement((ClassType) type, Statement.fromContexts(classNodeContext.block().statement()));
            type.setImplementation(stat);
            return at(classNodeContext.getStart(), stat);
        } else {
            throw new CompileException(classNodeContext.getStart(), "ClassStatement", "Expected a class type, but got " + type.toString());
        }

    }

    public Type getType() {
        return type;
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;

        for(Statement stat : statements) {
            stat.setScope(this);
        }
    }

    @Override
    public String toString(int indent) {
        String result = indent(indent) + "class " + type + " " + getComment();

        for(Statement stat : statements) {
            result += "\n" + stat.toString(indent + 1);
        }

        return result;
    }

    @Override
    public void findDefinitions() {
        for(Type param : type.getParametricTypes()) {
            if(!(param instanceof ClassType) || ((ClassType) param).getParametricTypes().size() != 0) {
                throw new CompileException(this, "A type in the generic arguments of this class is '" + param.getName() + "' instead of a generic argument.");
            }

            types.put(param.getName(), new GenericType(param.getName()));
        }

        for(Statement stat : statements) {
            if(stat instanceof DeclStatement) {
                symbols.put(((DeclStatement) stat).getName(), (Symbol) stat);
            } else if(stat instanceof FunStatement) {
                symbols.put(((FunStatement) stat).getName(), (Symbol) stat);
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
        for(Statement stat : statements) {
            if(stat instanceof Scope) {
                ((Scope) stat).findImpls();
            }
        }
    }

    public String getName() {
        return type.getName();
    }
}
