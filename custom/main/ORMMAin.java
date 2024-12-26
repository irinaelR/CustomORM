package custom.main;

import java.lang.reflect.Field;

import custom.orm.db.QueryManager;
import custom.orm.test.ExampleEntity;
import custom.orm.test.Medicines;
import custom.orm.utils.ReflectUtil;

public class ORMMAin {
    public static void main(String[] args) throws Exception {
        ExampleEntity labType = new ExampleEntity();
        labType.setEntityName("Cosmetology");

        QueryManager qm = new QueryManager();
        try {
            labType = (ExampleEntity) qm.insert(labType, null);
            System.out.println(labType.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
