package by.dimadl.ormlib.dao;

import by.dimadl.ormlib.annotation.Column;
import by.dimadl.ormlib.annotation.Table;
import by.dimadl.ormlib.exception.DAOException;
import by.dimadl.ormlib.util.AnnotationsUtil;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Methods of class provides opportunity generate SQL-queries using object
 * metadata.
 *
 * Methods use Reflection API.
 *
 *
 */
class QueryGenrator {

    /** The Constant NAME_ID_FIELD. */
    private static final String NAME_ID_FIELD = "id";

    /**
     * Private constructor since it is a static only class .
     */
    private QueryGenrator() {

    }

    /**
     * Method generate INSERT SQL-query.
     *
     * @param object
     *            the object for which will generated query. The class of this
     *            object must annotated by Table annotation {@link by.dimadl.ormlib.annotation.Table}. Field
     *            in the class that conforms to the fields in a database table
     *            must be annotated Column annotation {@link by.dimadl.ormlib.annotation.Column}
     * @return the INSERT SQL-query
     * @throws DAOException
     *             the DAO exception
     */
    public static String getInsertQuery(Object object) throws DAOException {

        // Start query
        StringBuilder query = new StringBuilder("INSERT INTO ");
        StringBuilder tableName = new StringBuilder();
        StringBuilder dataForQuery = new StringBuilder();

        // Get table name
        Class<?> clazz = object.getClass();
        tableName.append(clazz.getAnnotation(Table.class).name());

        List<Field> fields = AnnotationsUtil.getAnnotatedFields(clazz,
                Column.class);

        if (fields.isEmpty()) {

            throw new RuntimeException(clazz.getName()
                    + " haven't @Column annotation");
        }

        tableName.append("(");
        dataForQuery.append(" VALUES(");

        try {

            Iterator<Field> itr = fields.listIterator();
            while (itr.hasNext()) {

                Field field = itr.next();

                Column ann = field.getAnnotation(Column.class);

                field.setAccessible(true);

                tableName.append(ann.name());
                dataForQuery.append("?");

                if (itr.hasNext()) {
                    tableName.append(",");
                    dataForQuery.append(",");
                }
            }

        } catch (IllegalArgumentException e) {
            throw new DAOException(e);
        }

        dataForQuery.append(")");
        tableName.append(")");

        query.append(tableName);
        query.append(dataForQuery);

        return query.toString();

    }

    /**
     * Method generate UPDATE SQL-query..
     *
     * @param object
     *            the object for which will generated query. The class of this
     *            object must annotated by Table annotation {@link by.dimadl.ormlib.annotation.Table}. Field
     *            in the class that conforms to the fields in a database table
     *            must annotated by Column annotation {@link by.dimadl.ormlib.annotation.Column}
     * @return the UPDATE SQL-query
     * @throws DAOException
     *             the DAO exception
     */
    public static String getUpdateQuery(Object object) throws DAOException {

        Class<?> clazz = object.getClass();

        StringBuilder query = new StringBuilder("UPDATE ");
        String tableName = clazz.getAnnotation(Table.class).name();
        StringBuilder dataForQuery = new StringBuilder();

        String id = null;

        List<Field> fields = AnnotationsUtil.getAnnotatedFields(clazz,
                Column.class);

        dataForQuery.append(" SET ");
        try {

            Field fieldId = clazz.getSuperclass().getDeclaredField(
                    NAME_ID_FIELD);
            fieldId.setAccessible(true);
            id = fieldId.get(object).toString();

            Iterator<Field> itr = fields.listIterator();
            while (itr.hasNext()) {

                Field field = itr.next();

                Column ann = field.getAnnotation(Column.class);

                field.setAccessible(true);

                dataForQuery.append(ann.name());
                dataForQuery.append("=?");

                if (itr.hasNext()) {
                    dataForQuery.append(",");
                }

            }
        } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e) {
            throw new DAOException(e);
        }

        dataForQuery.append(" WHERE ");
        dataForQuery.append(tableName).append("_id = ?");

        query.append(tableName);
        query.append(dataForQuery);

        return query.toString();

    }

    /**
     * Method generate DELETE SQL-query.
     *
     *            the id deleting object
     * @param clazz
     *            the class deleting object. The class must annotated by Table
     *            annotation {@link by.dimadl.ormlib.annotation.Table}.
     * @return the DELETE SQL-query
     */
    public static String getDeleteQuery(Class<?> clazz) {

        StringBuilder query = new StringBuilder("DELETE FROM ");
        String tableName = clazz.getAnnotation(Table.class).name();

        query.append(tableName);
        query.append(" WHERE ").append(tableName).append("_id = ?");

        return query.toString();

    }

    /**
     * Method generate SQL-query for fetch objects from database by id.
     *
     * @param clazz
     *            Class objects necessary.
     * @param id
     *            the id necessary object.
     * @return the query
     */
    public static String getSelectByIdQuery(Class<?> clazz, Long id) {

        StringBuilder query = new StringBuilder("SELECT ");
        String tableName = clazz.getAnnotation(Table.class).name();

        String nameColumnId = tableName + "_id";

        query.append(nameColumnId).append(", ");

        List<Field> fields = AnnotationsUtil.getAnnotatedFields(clazz, Column.class);

        Iterator<Field> itr = fields.listIterator();

        while (itr.hasNext()){

            Field field = itr.next();

            Column ann = field.getAnnotation(Column.class);

            field.setAccessible(true);

            query.append(ann.name());

            if (itr.hasNext()){
                query.append(",");
            }
        }

        query.append(" FROM ");
        query.append(tableName);
        query.append(" WHERE ").append(nameColumnId).append(" = ?");

        return query.toString();

    }

    /**
     * Method generate SELECT ALL SQL-query.
     *
     * @param clazz
     *             Class objects necessary.
     * @return the select SQL-query
     */
    public static String getSelectQuery(Class<?> clazz) {

        StringBuilder query = new StringBuilder("SELECT * FROM ");
        String tableName = clazz.getAnnotation(Table.class).name();

        query.append(tableName);

        return query.toString();

    }


}
