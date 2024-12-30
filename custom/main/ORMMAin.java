package custom.main;

import java.lang.reflect.Field;
import java.util.List;

import custom.orm.db.QueryManager;
import custom.orm.test.ExampleEntity;
import custom.orm.test.Medicines;
import custom.orm.utils.ReflectUtil;

public class ORMMAin {
    public static void main(String[] args) {
        QueryManager qm = new QueryManager();
        ExampleEntity labType = new ExampleEntity(1, "Homeopathy");

        try {
            Object result = qm.insert(labType, null);
            // System.out.println(affectedRows);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
