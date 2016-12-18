/**
 * Created by Aaron on 10/14/16.
 */

import java.util.*;

public class ClassTable extends Table{
    ComplexType class_type;
    // class variables
    HashMap<String, ComplexType> fields = new HashMap<String, ComplexType>();
    // functions
    HashMap<String, MethodTable> methods = new HashMap<String, MethodTable>();

    public ClassTable(){}
    public ClassTable(String name)
    {
        class_type = new ComplexType(name);
    }

    // copies the fields and methods from the parent class
    public void inherit(ClassTable parent)
    {
        for(Map.Entry<String, ComplexType> entry: parent.fields.entrySet())
        {
            if (fields.get(entry.getKey()) == null) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        for(Map.Entry<String, MethodTable> entry: parent.methods.entrySet())
        {
            if(methods.get(entry.getKey()) == null) {
                methods.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
