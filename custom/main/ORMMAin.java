package custom.main;

import java.lang.reflect.Field;
import java.util.List;

import custom.orm.db.QueryManager;
import custom.orm.test.ExampleEntity;
import custom.orm.test.Medicines;
import custom.orm.utils.ReflectUtil;

public class ORMMAin {
    public static void main(String[] args) throws Exception {
        ExampleEntity labType = new ExampleEntity();
        // labType.setEntityName("Cosmetology");

        QueryManager qm = new QueryManager();
        String[] condition = new String[] {
            "name LIKE ?"
        };
        Object[] values = new String[] {
            "%Company%"
        };
        try {
            List<Object> all = qm.find(null, labType.getClass(), condition, values);
            for (Object object : all) {
                ExampleEntity lt = (ExampleEntity) object;
                System.out.println(lt.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
