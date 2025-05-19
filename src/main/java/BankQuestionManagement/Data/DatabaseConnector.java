package BankQuestionManagement.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:sqlserver://DESKTOP-T0OKSLM;databaseName=BankQuestions;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Kiểm tra kết nối
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Kết nối thành công đến cơ sở dữ liệu!");
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại:");
            e.printStackTrace();
        }
    }
}
