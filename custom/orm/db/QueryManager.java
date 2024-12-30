package custom.orm.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import custom.orm.db.utils.DBConnector;
import custom.orm.utils.AccessMethods;
import custom.orm.utils.ReflectUtil;

public class QueryManager {
    private void setStatementValues(PreparedStatement pst, Object obj, Field[] fields) throws Exception {
        for (int i = 1; i <= fields.length; i++) {
            Field f = fields[i - 1];
            String getter = ReflectUtil.getAccessMethodName(f, AccessMethods.GET.getValue());

            Method m = obj.getClass().getDeclaredMethod(getter, null);
            Object value = m.invoke(obj, null);

            if (f.getType() == LocalDate.class) {
                value = Date.valueOf((LocalDate) value);
            } else if (f.getType() == LocalDateTime.class) {
                value = Timestamp.valueOf((LocalDateTime) value);
            }

            pst.setObject(i, value);
        }

        // System.out.println(pst.toString());

    }

    private void setStatementValues(PreparedStatement pst, Object[] args) throws Exception {
        for (int i = 1; i <= args.length; i++) {
            Object value = args[i - 1];

            if (value instanceof LocalDate) {
                value = Date.valueOf((LocalDate) value);
            } else if (value instanceof LocalDateTime) {
                value = Timestamp.valueOf((LocalDateTime) value);
            }

            pst.setObject(i, value);
        }

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

        PreparedStatement pst = null;
        ResultSet generatedKeys = null;
        
        try {
            pst = c.prepareStatement(sql, new String[] { reflectUtil.getIdColName(idField) });
            this.setStatementValues(pst, obj, fields);
            int affectedRows = pst.executeUpdate();
            
            if (affectedRows > 0) {
                c.commit();

                generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Object generatedId = generatedKeys.getObject(1);
                    String setter = ReflectUtil.getAccessMethodName(idField, AccessMethods.SET.getValue());

                    Method m = obj.getClass().getDeclaredMethod(setter, idField.getType());
                    m.invoke(obj, generatedId);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (shouldClose) {
                c.close();
            }
        }

        return obj;
    }

    public List<Object> find(Connection c, Class<?> objectClass, String[] conditions, Object[] args,
            String[] afterWhere) throws Exception {
        List<Object> results = new ArrayList<>();

        boolean shouldClose = false;
        if (c == null) {
            c = new DBConnector(DBConnector.PROPERTIES_PATH).getConnection();
            shouldClose = true;
        }

        ReflectUtil reflectUtil = new ReflectUtil(objectClass);
        Field[] fields = reflectUtil.getColumns(false);
        String selectSql = reflectUtil.formSelectQuery(fields, conditions, afterWhere);

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = c.prepareStatement(selectSql);
            if (conditions != null && args != null) {
                setStatementValues(pst, args);
            }

            rs = pst.executeQuery();
            while (rs.next()) {
                Object newInst = objectClass.getConstructor(null).newInstance();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    String setterName = ReflectUtil.getAccessMethodName(field, AccessMethods.SET.getValue());
                    Method setter = objectClass.getMethod(setterName, field.getType());

                    setter.invoke(newInst, rs.getObject(i + 1));
                }
                results.add(newInst);
            }
        } catch (Exception e) {
            throw new Exception("Error during find for class " + objectClass.getSimpleName(), e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (shouldClose) {
                c.close();
            }
        }

        return results;
    }

    public List<Object> find(Connection c, Class<?> objectClass, String[] conditions, Object[] args,
            String[] afterWhere, int start, int limit) throws Exception {
        String[] newAfterWhere = null;
        if (afterWhere != null) {
            newAfterWhere = Arrays.copyOf(afterWhere, afterWhere.length + 1);
        } else {
            newAfterWhere = new String[1];
        }
        newAfterWhere[newAfterWhere.length - 1] = "LIMIT " + limit + " OFFSET " + start;

        return find(c, objectClass, conditions, args, newAfterWhere);
    }

    public Object findById(Connection c, Object objToPopulate) throws Exception {
        ReflectUtil reflectUtil = new ReflectUtil(objToPopulate.getClass());

        Field idField = reflectUtil.getIdCol();
        if (idField == null) {
            throw new IllegalArgumentException("Object in parameters must have a field annotated as Id");
        }

        String idColName = reflectUtil.getIdColName(idField);

        Object idValue = reflectUtil.getIdValue(objToPopulate);

        if (idValue == null) {
            throw new Exception("The id of the object in parameters must be set before calling findById");
        }

        String[] conditions = new String[] { idColName + " = ?" };
        Object[] args = new Object[] { idValue };

        List<Object> asList = find(c, objToPopulate.getClass(), conditions, args, null, 0, 1);
        if (asList.size() == 0) {
            return null;
        } else {
            return asList.get(0);
        }
    }

    public int update(Connection c, Object newVersion) throws Exception {
        ReflectUtil ru = new ReflectUtil(newVersion.getClass());
        String sql = ru.formUpdateQuery();

        List<Object> argsList = new ArrayList<>();

        Field[] allFields = ru.getColumns(false);
        for (Field field : allFields) {
            String getterName = ReflectUtil.getAccessMethodName(field, AccessMethods.GET.getValue());
            Method getter = newVersion.getClass().getDeclaredMethod(getterName, null);
            Object o = getter.invoke(newVersion, null);

            argsList.add(o);
        }

        Object idValue = ru.getIdValue(newVersion);
        argsList.add(idValue); // last value to be set in the preparedStatement

        Object[] args = argsList.toArray();

        boolean shouldClose = false;
        if (c == null) {
            shouldClose = true;
            c = new DBConnector(DBConnector.PROPERTIES_PATH).getConnection();
        }

        PreparedStatement pst = null;

        try {
            pst = c.prepareStatement(sql);
            setStatementValues(pst, args);

            int affectedRows = pst.executeUpdate();

            c.commit();
            return affectedRows;
        } catch (Exception e) {
            throw new Exception("Error during update", e);
        } finally {
            if (pst != null) {
                pst.close();
            }
            if (shouldClose && c != null) {
                c.close();
            }
        }

    }
}
