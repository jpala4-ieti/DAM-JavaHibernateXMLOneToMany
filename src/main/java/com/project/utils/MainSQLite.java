package com.project.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * Aquest exemple mostra les 
 * dades de SQLite quan hibernate
 * ja ha generat les taules
 */

public class MainSQLite {

    public static void main(String[] args) throws SQLException {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "database.db";
        ResultSet rs = null;

        // Connectar (crea la BBDD si no existeix)
        Connection conn = UtilsSQLite.connect(filePath);

        // Llistar les taules
        ArrayList<String> taules = UtilsSQLite.listTables(conn);
        System.out.println("Taules: " + taules);

        for (int cntTab = 0; cntTab < taules.size(); cntTab = cntTab + 1) { 
            String nomTaula = taules.get(cntTab);

            // Mostrar les columnes de la taula
            rs = UtilsSQLite.querySelect(conn, "SELECT * FROM " + nomTaula + ";");
            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("Columnes de la taula " + nomTaula + ":");
            for (int cntCol = 1; cntCol <= rsmd.getColumnCount(); cntCol = cntCol + 1) { 
                // Les columnes començen a 1, no hi ha columna 0!
                String label = rsmd.getColumnLabel(cntCol);
                String name = rsmd.getColumnName(cntCol);
                int type = rsmd.getColumnType(cntCol);
                System.out.println("    " + label + ", " + name + ", " + type);
            }

            // Mostrar la informació de la taula
            System.out.println("Continguts de la taula " + nomTaula + ":");
            rs = UtilsSQLite.querySelect(conn, "SELECT * FROM " + nomTaula + ";");
            while (rs.next()) {
                String txt = "";
                for (int cntCol = 1; cntCol <= rsmd.getColumnCount(); cntCol = cntCol + 1) {
                    if (cntCol == 1) { txt = txt + "    "; } else { txt = txt + ", "; }
                    int type = rsmd.getColumnType(cntCol);
                    String name = rsmd.getColumnName(cntCol);
                    switch (type) {
                        case java.sql.Types.INTEGER:
                            txt = txt + rs.getInt(name);
                            break;
                        case java.sql.Types.VARCHAR:
                            txt = txt + rs.getString(name);
                            break;
                        case java.sql.Types.REAL:
                            txt = txt + rs.getFloat(name);
                            break;
                        case java.sql.Types.BIGINT:
                            txt = txt + rs.getLong(name);
                            break;
                        case java.sql.Types.BOOLEAN:
                            txt = txt + rs.getBoolean(name);
                            break;
                        default:
                            txt = txt + "???";
                            break;
                    }
                }
                System.out.println(txt);
            }
        }

        // Desconnectar
        UtilsSQLite.disconnect(conn);
    }
}