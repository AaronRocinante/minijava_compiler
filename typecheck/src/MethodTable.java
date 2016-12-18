/**
 * Created by Aaron on 10/14/16.
 */
import java.util.*;

public class MethodTable extends Table{
    public String method_name;
    // class variables
    HashMap<String, ComplexType> fields = new HashMap<String, ComplexType>();
    ComplexType class_type;
    ComplexType return_type;
    // function name -> formal parameter types
    ArrayList<Pair<String, ComplexType> > formal_params = new ArrayList<Pair<String, ComplexType> >();
    // formal parameter names, local variable names
    HashMap<String, ComplexType> variables = new HashMap<String, ComplexType>();

    public MethodTable(ClassTable class_table)
    {
        fields = class_table.fields;
        class_type = class_table.class_type;
    }
}
