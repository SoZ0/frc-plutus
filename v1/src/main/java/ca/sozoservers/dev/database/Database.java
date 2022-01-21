package ca.sozoservers.dev.database;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;

import ca.sozoservers.dev.database.models.DatabaseModel;
import ca.sozoservers.dev.database.models.DatabaseModel.Constraints;
import ca.sozoservers.dev.database.models.DatabaseModel.DataType;
import ca.sozoservers.dev.database.models.DatabaseModel.SqlValue;

public class Database {

    private static File db;

    public Database(File dbPath) {
        db = dbPath;
    }

    public static boolean createDatabase() {
        try (Connection conn = connect()) {
            if (!db.exists()) {
                DatabaseMetaData meta = conn.getMetaData();
                meta.getDriverName();
            } 
        }catch (SQLException ex) {
            return false;
        }
        return true;
    }

    public static boolean createTable(DatabaseModel... models) {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            for (DatabaseModel model : models) {
                stmt.execute(model.getSQL(SqlValue.Table));
            }
            return true;
        } catch (SQLException ex) {
        }
        return false;
    }

    public static boolean set(DatabaseModel model) {

        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(model.getSQL(SqlValue.Set));) {
            Field[] fields = model.getClass().getDeclaredFields();
            int count = 0;
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(DataType.class)) {
                    count++;
                    pstmt.setObject(count, field.get(model));
                }
            }
            pstmt.executeUpdate();
            pstmt.closeOnCompletion();
            conn.close();
        } catch (SQLException | IllegalArgumentException | IllegalAccessException ex) {
            return false;
        }
        return true;

    }

    public static boolean get(DatabaseModel model, boolean supressError) {

        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(model.getSQL(SqlValue.Get));) {
            Field[] fields = model.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Constraints.class)) {
                    if (field.getAnnotation(Constraints.class).value().equalsIgnoreCase("PRIMARY KEY")) {
                        pstmt.setObject(1, field.get(model));
                        break;
                    }
                }
            }
            ResultSet rs = pstmt.executeQuery();
            for (Field field : fields) {
                if (field.isAnnotationPresent(DataType.class)) {
                    field.set(model, rs.getObject(field.getName()));
                }
            }
            rs.close();
            pstmt.closeOnCompletion();
            conn.close();
        } catch (SQLException | IllegalArgumentException | IllegalAccessException ex) {
            if (!supressError) {
            }
            return false;
        }
        return true;

    }

    public static boolean update(DatabaseModel model) {

        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(model.getSQL(SqlValue.Update));) {
            Field[] fields = model.getClass().getDeclaredFields();
            int count = 0;
            Object primaryKey = null;
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(DataType.class)) {
                    if (field.isAnnotationPresent(Constraints.class)) {
                        if (field.getAnnotation(Constraints.class).value().equalsIgnoreCase("PRIMARY KEY")) {
                            primaryKey = field.get(model);
                            continue;
                        }
                    }
                    count++;
                    pstmt.setObject(count, field.get(model));
                }
            }
            count++;
            pstmt.setObject(count, primaryKey);
            pstmt.executeUpdate();
            pstmt.closeOnCompletion();
            conn.close();
        } catch (SQLException | IllegalArgumentException | IllegalAccessException ex) {
            return false;
        }
        return true;
    }

    public static boolean delete(DatabaseModel model) {

        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(model.getSQL(SqlValue.Delete));) {
            Field[] fields = model.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Constraints.class)) {
                    if (field.getAnnotation(Constraints.class).value().equalsIgnoreCase("PRIMARY KEY")) {
                        pstmt.setObject(1, field.get(model));
                        break;
                    }
                }
            }
            pstmt.executeUpdate();
            pstmt.closeOnCompletion();
            conn.close();
        } catch (SQLException | IllegalArgumentException | IllegalAccessException ex) {
            return false;
        }
        return true;

    }

    public static boolean exists(DatabaseModel model) {
        try (Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(model.getSQL(SqlValue.Get));) {
            Field[] fields = model.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Constraints.class)) {
                    if (field.getAnnotation(Constraints.class).value().equalsIgnoreCase("PRIMARY KEY")) {
                        pstmt.setObject(1, field.get(model));
                        break;
                    }
                }
            }
            ResultSet rs = pstmt.executeQuery();
            rs.close();
            pstmt.closeOnCompletion();
            conn.close();
        } catch (SQLException | IllegalArgumentException | IllegalAccessException ex) {
            return false;
        }
        return true;
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + db);
    }
}