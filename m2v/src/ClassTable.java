/**
 * Created by Aaron on 10/14/16.
 */
import java.util.*;

public class ClassTable extends Table{
    ComplexType class_type;
    ComplexType parent_class_type;
    Boolean is_main_class = false;
    // class variables
    LinkedHashMap<String, ComplexType> fields = new LinkedHashMap<String, ComplexType>();
    // functions
    LinkedHashMap<String, MethodTable> methods = new LinkedHashMap<String, MethodTable>();

    // for translation to vapor
    LinkedHashMap<String, ComplexType> total_fields = new LinkedHashMap<String, ComplexType>();
    LinkedHashMap<String, MethodTable> total_methods = new LinkedHashMap<String, MethodTable>();
    LinkedHashMap<String, Integer> total_fields_numbering = new LinkedHashMap<String, Integer>();

    public ClassTable(){}
    public ClassTable(String name)
    {
        class_type = new ComplexType(name);
    }

    public ComplexType retrieve_type(String id, HashMap<String, ClassTable> symbol_table)
    {
        ComplexType t = fields.get(id);
        if(t == null && parent_class_type!=null)
        {
            ClassTable parent_class_table = symbol_table.get(parent_class_type.toString());
            if(parent_class_table!=null) {
                t = parent_class_table.retrieve_type(id, symbol_table);
            }
        }
        return t;
    }

    public ComplexType retrieve_class_containing_var(String id, HashMap<String, ClassTable> symbol_table)
    {
        ClassTable moving_table = this;
        while(moving_table != null)
        {
            if(moving_table.fields.get(id)!=null)
            {
                 return moving_table.class_type;
            }
            if(moving_table.parent_class_type==null)
            {
                break;
            }
            moving_table = symbol_table.get(moving_table.parent_class_type.toString());
        }
        return null;
    }

    public ComplexType retrieve_class_containing_method(String method_name, HashMap<String, ClassTable> symbol_table)
    {
        ClassTable moving_table = this;
        while(moving_table != null)
        {
            if(moving_table.methods.get(method_name)!=null)
            {
                return moving_table.class_type;
            }
            if(moving_table.parent_class_type==null)
            {
                break;
            }
            moving_table = symbol_table.get(moving_table.parent_class_type.toString());
        }
        return null;
    }

    // copies the fields and methods from the parent class
    public void inherit(ComplexType parent_class_type)
    {
        this.parent_class_type = parent_class_type;
    }
}
