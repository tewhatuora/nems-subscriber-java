package nz.govt.tewhatuora.Database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import nz.govt.tewhatuora.Utilities.DatabaseUtil;

public class DeathDatabase {

    public static Boolean CallDeathDtProc(String env, String nhiId, String deathDt, String status, String source) {

        try (Connection connection = DatabaseUtil.GetConnection(env)) {

            try (CallableStatement deathProc = connection.prepareCall("{call death_event(?, ?, ?, ?, ?)}")) {

                // Set the input parameters
                deathProc.setString(1, nhiId);
                deathProc.setString(2, deathDt);
                deathProc.setString(3, status);
                deathProc.setString(4, source);

                // Set the output parameters
                deathProc.registerOutParameter(5, Types.NUMERIC);
                // Execute the stored procedure
                deathProc.execute();

                if (deathProc.getInt(5) == 1) {
                    System.out.println("Stored procedure executed successfully.");
                    return true;
                } else {
                    System.out.println("Stored procedure failed.");
                    return false;
                }

            }

        } catch (SQLException e) {
            System.out.println("Stored procedure failed " + e);
            return false;
        }

    }

}
