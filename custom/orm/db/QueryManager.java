package custom.orm.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import custom.orm.db.utils.DBConnector;
import custom.orm.utils.AccessMethods;
import custom.orm.utils.ReflectUtil;

public class QueryManager {
    public void setStatementValues(PreparedStatement pst, Object obj, Field[] fields) throws Exception {
        for (int i = 1; i <= fields.length; i++) {
            Field f = fields[i-1];
            String getter = ReflectUtil.getGetSetName(f, AccessMethods.GET.getValue());

            Method m = obj.getClass().getDeclaredMethod(getter, null);
            Object value = m.invoke(obj, null);

            if(f.getType() == LocalDate.class) {
                value = Date.valueOf((LocalDate) value);
            }
            if(f.getType() == LocalDateTime.class) {
                value = Timestamp.valueOf((LocalDateTime) value);
            }

            pst.setObject(i, value);
        }
        
        System.out.println(pst.toString());

    }

    public Object insert(Object obj, Connection c) throws Exception {
        boolean shouldClose = false;
        if (c == null) {
            shouldClose = true;
            c = new DBConnector(DBConnector.PROPERTIES_PATH).getConnection();
        }

        ReflectUtil reflectUtil = new ReflectUtil(obj.getClass());

        Field[] fields = reflectUtil.getColumns(true);
        Field idField = reflectUtil.getIdCol();

        String sql = reflectUtil.formInsertQuery(fields);

        PreparedStatement pst = c.prepareStatement(sql, new String[] { reflectUtil.getIdColName(idField) });

        this.setStatementValues(pst, obj, fields);

        int affectedRows = pst.executeUpdate();

        if (affectedRows > 0) {
            // Retrieve the generated key
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Object generatedId = generatedKeys.getObject(1);
                    String setter = ReflectUtil.getGetSetName(idField, AccessMethods.SET.getValue());

                    Method m = obj.getClass().getDeclaredMethod(setter, idField.getType());
                    m.invoke(obj, generatedId);
                }
            }
        }

        if (shouldClose) {
            c.close();
        }

        return obj;
    }
}
