import syntaxtree.*;
import visitor.*;
import java.util.*;

// used to check rule (39)
class ZipperVisitor extends GJVoidDepthFirst<ArrayList<Pair<String, ComplexType> > >
{
    int num_params;
    int counter;
    HashSet<ArrayList<String> > link_set;
    Table table;
    public ZipperVisitor(int num_params, HashSet<ArrayList<String> > link_set, Table table)
    {
        this.num_params = num_params;
        this.link_set = link_set;
        this.table = table;
        counter = 0;
    }

    // true if a is a subtype of b i.e. a is a derived class of b i.e. a <= b
    private boolean subtype(ComplexType a, ComplexType b)
    {
        if(a.toString().equals(b.toString()))
        {
            return true;
        }
        String parent;
        for (ArrayList<String> pair:link_set)
        {
            if(a.toString().equals(pair.get(0))) {
                parent = pair.get(1);
                return subtype(new ComplexType(parent), b);
            }
        }
        return false;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    public void visit (ExpressionList n, ArrayList<Pair<String, ComplexType>> formal_params)
    {
        n.f0.accept(this, formal_params);
        counter++;
        for(Node node: n.f1.nodes)
        {
            node.accept(this, formal_params);
            counter++;
        }
        if (counter != formal_params.size())
        {
            throw new TypeException();
        }
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    public void visit (Expression n, ArrayList<Pair<String, ComplexType> > formal_params)
    {
        if(counter >= formal_params.size())
        {
            throw new TypeException();
        }
        ComplexType t = n.f0.accept(new GetExpressionTypeVisitor(table));
        if(!subtype(t, formal_params.get(counter).second()))
        {
            throw new TypeException();
        }
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public void visit(ExpressionRest n, ArrayList<Pair<String, ComplexType> > formal_params) {
        n.f1.accept(this, formal_params);
    }
}
