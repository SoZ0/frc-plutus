package ca.sozoservers.dev.database.models;

import java.lang.reflect.Field;

import ca.sozoservers.dev.database.models.DatabaseModel.*;

@Table("server")
public class ServerModel implements DatabaseModel {

    @Constraints("PRIMARY KEY")
    @DataType("INTERGER")
    public long server = 0;

    @DataType("INTERGER")
    public long template = 0;;

    @DataType("INTERGER")
    public long lfgChannel = 0;

    @DataType("TEXT")
    public String activeLFGs = "";

    @SqlType(SqlValue.Table)
    public static String sqlTable;
    @SqlType(SqlValue.Set)
    public static String sqlSet;
    @SqlType(SqlValue.Get)
    public static String sqlGet;
    @SqlType(SqlValue.Update)
    public static String sqlUpdate;
    @SqlType(SqlValue.Delete)
    public static String sqlDelete;

    static {
        Class<ServerModel> clazz = ServerModel.class;
        String TableName = clazz.getAnnotation(Table.class).value();
        sqlTable = "CREATE TABLE IF NOT EXISTS " + TableName + "(";
        sqlSet = "INSERT INTO " + TableName + "(";
        sqlGet = "SELECT ";
        sqlUpdate = "UPDATE " + TableName + " SET ";
        sqlDelete = "DELETE FROM " + TableName + " WHERE ";
        String setEnd = "";
        String PrimaryKey = "";
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DataType.class)) {
                String name = field.getName();
                sqlTable += name + " " + field.getAnnotation(DataType.class).value();
                sqlSet += name + ", ";
                sqlGet += name + ", ";
                setEnd += "?,";
                if (field.isAnnotationPresent(Constraints.class)) {
                    String constraint = field.getAnnotation(Constraints.class).value();
                    sqlTable += " " + constraint;
                    if (constraint.equalsIgnoreCase("PRIMARY KEY")) {
                        sqlDelete += name + " = ?";
                        PrimaryKey = name + " = ?";
                    } else {
                        sqlUpdate += name + " = ?, ";
                    }
                } else {
                    sqlUpdate += name + " = ?, ";
                }
                sqlTable += ", ";
            }
        }
        sqlTable = sqlTable.substring(0, sqlTable.lastIndexOf(",")) + ");";
        setEnd = setEnd.substring(0, setEnd.lastIndexOf(",")) + ")";
        sqlSet = sqlSet.substring(0, sqlSet.lastIndexOf(",")) + ") VALUES(" + setEnd;
        sqlGet = sqlGet.substring(0, sqlGet.lastIndexOf(",")) + " FROM " + TableName + " WHERE " + PrimaryKey;
        sqlUpdate = sqlUpdate.substring(0, sqlUpdate.lastIndexOf(",")) + " WHERE " + PrimaryKey;
    }

    @Override
    public String getSQL(SqlValue type) {
        switch (type) {
            case Delete:
                return sqlDelete;
            case Get:
                return sqlGet;
            case Set:
                return sqlSet;
            case Table:
                return sqlTable;
            case Update:
                return sqlUpdate;
        }
        return null;
    }

}
