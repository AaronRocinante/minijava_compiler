package vaportree;

import vaporprintvisitor.PrinterVisitor;

public class Pair<A,B>
{
    private A first;
    private B second;

    public Pair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public void set_first(A first)
    {
        this.first = first;
    }
    public void set_second(B second)
    {
        this.second = second;
    }
    public A first()
    {
        return first;
    }
    public B second()
    {
        return second;
    }
    public boolean equals(Pair p2)
    {
        return (first.equals(p2.first) && second.equals(p2.second));
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
