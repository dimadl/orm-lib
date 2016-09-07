package by.dimadl.ormlib.dao;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import by.dimadl.ormlib.annotation.Column;
import by.dimadl.ormlib.annotation.Table;
import by.dimadl.ormlib.dto.Entity;
import by.dimadl.ormlib.exception.DAOException;
import by.dimadl.ormlib.util.AnnotationsUtil;
import by.dimadl.ormlib.util.DAOUtils;

/**
 * <p>
 * The Class AbstractDAO.
 * <p>
 * It is the common DAO class. It contains following five common methods:
 * <p>
 * <ul>
 * <li>Insert object into database</li>
 * <li>Update existing object</li>
 * <li>Remove object from database</li>
 * <li>Get all object</li>
 * <li>Fetch object from table by ID</li>
 * </ul>
 * <p>
 * All methods use {@link by.dimadl.ormlib.dao.QueryGenrator} for getting specific query.
 * <p>
 * Also some method use Reflection API for create instance of <b>T</b> type.
 *
 * @see by.dimadl.ormlib.dao.QueryGenrator
 *
 */
public abstract class AbstractDAO<T extends Entity> {

    /**
     * The name of field ID in Entity
     *
     * @see Entity
     *
     * */
    private static final String NAME_ID_FIELD = "id";

    /**
     * The enum contains two elements describing executing action, that use for
     * determine the value of return value of executeUpdate() method .
     */
    private enum TypeUpdateQuery {

        /** The save action. */
        ADD,
        /** The update action. */
        UPDATE
    }

    /** The data source. */
    protected DataSource dataSource;

    /**
     * Sets the data source.
     *
     * @param dataSource
     *            the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns the list containing all of the objects from table.
     *
     * @return the list containing all of the objects from table.
     * @throws DAOException
     *
     */
    public List<T> list() throws DAOException {

        Class<?> typeObject = getTypeArgument();
        String query = QueryGenrator.getSelectQuery(typeObject);

        List<T> list = new LinkedList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {

            connection = dataSource.getConnection();
            statement = connection.createStatement();

            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {

                T object = initInstanceFromResultSet(resultSet, typeObject);

                Field fieldId = typeObject.getSuperclass().getDeclaredField(
                        NAME_ID_FIELD);
                fieldId.setAccessible(true);
                fieldId.set(object, new Long(resultSet.getLong(getNameIdColumn())));

                list.add(object);
            }

        } catch (IllegalAccessException | SQLException | NoSuchFieldException
                | SecurityException e) {
            throw new DAOException(e);
        } finally {
            DAOUtils.closeResources(connection, statement, resultSet);
        }

        return list;

    }

    /**
     * Insert object into database.
     *
     * @param object
     *            -object to be inserted into database
     *
     * @return the ID inserted object
     * @throws DAOException
     *
     */
    public Long add(T object) throws DAOException {

        String query = QueryGenrator.getInsertQuery(object);

        Long id = executeUpdate(object, query,TypeUpdateQuery.ADD);

        return id;

    }

    /**
     * Delete object form database.
     *
     * @param id
     *            - the id deleted object
     * @throws DAOException
     *
     */
    public void delete(Long id) throws DAOException {

        Connection connection = null;
        PreparedStatement statement = null;

        try {

            Class<?> typeObject = getTypeArgument();
            String query = QueryGenrator.getDeleteQuery(typeObject);

            connection = dataSource.getConnection();
            statement = connection.prepareStatement(query);

            statement.setLong(1, id);

            statement.execute();

        } catch (SQLException e) {
            throw new DAOException(e);
        } finally {
            DAOUtils.closeResources(connection, statement, null);
        }

    };

    /**
     * Update existing object.
     *
     * @param object
     *            - the object to be update
     * @throws DAOException
     *
     */
    public void update(T object) throws DAOException {

        String query = QueryGenrator.getUpdateQuery(object);
        executeUpdate(object, query, TypeUpdateQuery.UPDATE);

    };

    /**
     * Fetch object form database by id.
     *
     * @param id
     *            the id search
     * @return the
     * @throws DAOException
     *             the DAO exception
     */
    public T fetchById(Long id) throws DAOException {

        Class<?> typeObject = getTypeArgument();
        String query = QueryGenrator.getSelectByIdQuery(typeObject, id);

        T object = null;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {

            connection = dataSource.getConnection();
            statement = connection.prepareStatement(query);

            statement.setLong(1, id);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                object = initInstanceFromResultSet(resultSet, typeObject);

                Field field = typeObject.getSuperclass().getDeclaredField(NAME_ID_FIELD);
                field.setAccessible(true);
                field.set(object, id);

            }

        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            throw new DAOException(e);
        } finally {
            DAOUtils.closeResources(connection, statement, resultSet);
        }

        return object;

    };

    /**
     * Method for create instance using result set.
     *
     * @param resultSet
     *            the result set for create instance
     * @param clazz
     *            the instance class
     * @return the object
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    private T initInstanceFromResultSet(ResultSet resultSet, Class<?> clazz)
            throws DAOException {

        // Get all fields annotated by @Column
        List<Field> fields = AnnotationsUtil.getAnnotatedFields(clazz,
                Column.class);

        if (fields.isEmpty()) {

            throw new DAOException("Not found fields with annotation 'Column' ");

        }

        T object = null;

        try {

            // Create instance
            object = (T) clazz.newInstance();

            for (Field field : fields) {

                Column ann = field.getAnnotation(Column.class);
                field.setAccessible(true);

                Class<?> typeField = field.getType();

                String valAnn = ann.name();

                if (Date.class == typeField) {

                    Timestamp timestamp = resultSet.getTimestamp(valAnn);
                    Date date = new Date(timestamp.getTime());
                    field.set(object, date);

                } else if (Long.class == typeField) {

                    field.set(object, new Long(resultSet.getLong(valAnn)));

                } else if (Integer.class == typeField) {

                    field.set(object, new Integer(resultSet.getInt(valAnn)));

                } else if (int.class == typeField) {

                    field.setInt(object, resultSet.getInt(valAnn));

                } else if (Double.class == typeField) {

                    field.set(object, new Double(resultSet.getDouble(valAnn)));

                } else if (double.class == typeField) {

                    field.setDouble(object, resultSet.getDouble(valAnn));

                } else if (Float.class == typeField) {

                    field.setFloat(object,
                            new Float(resultSet.getFloat(valAnn)));

                } else if (float.class == typeField) {

                    field.setFloat(object, resultSet.getFloat(valAnn));

                } else if (Short.class == typeField) {

                    field.setShort(object,
                            new Short(resultSet.getShort(valAnn)));

                } else if (short.class == typeField) {

                    field.setShort(object, resultSet.getShort(valAnn));

                } else if (boolean.class == typeField) {
                    field.setBoolean(object, resultSet.getBoolean(valAnn));
                } else if (Boolean.class == typeField) {
                    field.setBoolean(object, new Boolean(resultSet.getBoolean(valAnn)));
                } else {

                    field.set(object, resultSet.getObject(valAnn));
                }

            }
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | SQLException e) {

            throw new DAOException(e);
        }

        return object;

    }

    /**
     * Execute update.
     *
     * @param object
     *            the object
     * @param query
     *            the query for update
     * @param type
     *            the type
     * @return the long
     * @throws DAOException
     *             the DAO exception
     */
    private Long executeUpdate(T object, String query, TypeUpdateQuery type)
            throws DAOException {

        Class<?> clazz = getTypeArgument();
        List<Field> fields = AnnotationsUtil.getAnnotatedFields(clazz,
                Column.class);

        Connection conn = null;
        PreparedStatement st = null;
        ResultSet resultSet = null;

        Long id = null;

        try {

            conn = dataSource.getConnection();
            String nameColumnId = getNameIdColumn();
            st = conn.prepareStatement(query, new String[] { nameColumnId });

            int i;
            for (i = 0; i < fields.size(); i++) {

                Field field = fields.get(i);
                field.setAccessible(true);

                if (field.getType().getName().equals(Date.class.getName())) {

                    Timestamp timestamp = new Timestamp(
                            ((Date) field.get(object)).getTime());
                    st.setTimestamp(i+1, timestamp);

                }else {
                    st.setObject(i + 1, field.get(object));
                }

            }

            if(type == TypeUpdateQuery.UPDATE){
                Field field = object.getClass().getSuperclass().getDeclaredField(NAME_ID_FIELD);
                field.setAccessible(true);
                id = (Long) field.get(object);
                st.setLong(i+1, id);
            }

            st.execute();

            if (type == TypeUpdateQuery.ADD) {

                resultSet = st.getGeneratedKeys();
                resultSet.next();
                id = resultSet.getLong(1);

            }

        } catch (SQLException | IllegalArgumentException
                | IllegalAccessException e) {
            throw new DAOException(e);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } finally {

            DAOUtils.closeResources(conn, st, resultSet);

        }

        return id;

    }

    /**
     * Gets the argument type.
     *
     * @return the argument type
     */
    private Class<?> getTypeArgument() {
        ParameterizedType type = (ParameterizedType) getClass()
                .getGenericSuperclass();
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    private String getNameIdColumn() {

        StringBuilder builder = new StringBuilder();
        Class<?> clazz = getTypeArgument();
        String tableName = clazz.getAnnotation(Table.class).name();
        builder.append(tableName);
        builder.append("_id");

        return builder.toString();

    }

}