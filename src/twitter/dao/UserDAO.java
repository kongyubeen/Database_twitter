package twitter.dao;
import db.DBConnection;
import java.sql.*;  // 추가

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    // 로그인 기능
    public boolean login(String identifier, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User WHERE pwd=? AND (user_id=? OR phone=? OR email=?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, password);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ps.setString(4, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // 회원가입
    public String createUser(String userId, String pwd, String phone, String email) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM User WHERE user_id=? OR phone=? OR email=?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, userId);
            ps.setString(2, phone);
            ps.setString(3, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return "User already exists";

            String insertSql = "INSERT INTO User (user_id, pwd, phone, email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps2 = conn.prepareStatement(insertSql)) {
                ps2.setString(1, userId);
                ps2.setString(2, pwd);
                ps2.setString(3, phone);
                ps2.setString(4, email);
                ps2.executeUpdate();
            }
            return "User created successfully";
        }
    }

    // 비밀번호 변경
    public String changePassword(String userId, String oldPwd, String newPwd) throws SQLException {
        String sql = "UPDATE User SET pwd=? WHERE user_id=? AND pwd=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPwd);
            ps.setString(2, userId);
            ps.setString(3, oldPwd);
            int rows = ps.executeUpdate();
            return rows > 0 ? "Password changed successfully" : "Wrong ID or password";
        }
    }
}