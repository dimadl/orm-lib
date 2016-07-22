package by.dimadl.ormlib.util;

import by.dimadl.ormlib.exception.DAOException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class methods are useful for working for with database connections,
 * statements and result sets .
 */
public class DAOUtils {

    public static void closeResources(Connection conn, Statement st,
                                      ResultSet rs) throws DAOException {
        try {
            try {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } finally {
                    if (st != null) {
                        st.close();
                    }
                }
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

}