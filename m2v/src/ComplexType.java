class ComplexType
{
    final String name;

    public ComplexType(String name)
    {
        this.name = name;
    }
    public String toString()
    {
        return name;
    }
    public boolean equals(Object o)
    {
        if (o!= null && o instanceof ComplexType)
        {
            return name.equals(((ComplexType)o).name);
        }
        else
        {
            return false;
        }
    }
    // int
    static final ComplexType INT = new ComplexType("INT");
    //boolean
    static final ComplexType BOO = new ComplexType("BOO");
    // int[]
    static final ComplexType IARR = new ComplexType("IARR");

    public boolean is_primitive_type ()
    {
        return (this.equals(INT) || this.equals(BOO) || this.equals(IARR));
    }

}
