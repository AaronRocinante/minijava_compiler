/**
 * Created by Aaron on 10/14/16.
 */
import java.util.*;

public class MethodTable extends Table{
    public String method_name;
    public Integer position_in_vmt;
    // class variables
    LinkedHashMap<String, ComplexType> fields = new LinkedHashMap<String, ComplexType>();
    ComplexType class_type;
    ClassTable class_table;
    ComplexType return_type;
    // function name -> formal parameter types
    ArrayList<Pair<String, ComplexType> > formal_params = new ArrayList<Pair<String, ComplexType> >();
    // formal parameter names, local variable names
    HashMap<String, ComplexType> variables = new HashMap<String, ComplexType>();

    public ComplexType retrieve_type(String id, HashMap<String, ClassTable> symbol_table)
    {
        ComplexType t;
        t = variables.get(id);
        if(t == null)
        {
            t = fields.get(id);
        }

        if (t == null && class_table.parent_class_type!=null)
        {
            ClassTable parent_class_table = symbol_table.get(class_table.parent_class_type.toString());
            if(parent_class_table!=null) {
                t = parent_class_table.retrieve_type(id, symbol_table);
            }
        }
        return t;
    }

    public boolean variable_present(String id)
    {
        return variables.get(id) != null;
    }


    public MethodTable(ClassTable class_table)
    {
        fields = class_table.fields;
        class_type = class_table.class_type;
        this.class_table = class_table;
    }
}
