package twitter.dao;

import java.sql.*;

public class FollowDAO {
    private Connection conn;

    public FollowDAO(Connection conn) {
        this.conn = conn;
    }

    public void followUser(String user_id, String following_id) throws SQLException {
        // Check if already following
        String checkSql = "SELECT COUNT(*) FROM Following WHERE user_id = ? AND following_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, user_id);
            checkStmt.setString(2, following_id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.printf(" -> %s is already following %s.%n", user_id, following_id);
                    return; // Skip insertion
                }
            }
        }

        // Insert new follow record
        String sql = "INSERT INTO Following (user_id, following_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user_id);
            pstmt.setString(2, following_id);
            pstmt.executeUpdate();
            System.out.printf(" -> %s successfully followed %s.%n", user_id, following_id);
        }
    }
    
    public void unfollowUser(String user_id, String following_id) throws SQLException {
        String sql = "DELETE FROM Following WHERE user_id = ? AND following_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user_id);
            pstmt.setString(2, following_id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.printf(" -> %s successfully unfollowed %s.%n", user_id, following_id);
            } else {
                System.out.printf(" -> %s is not following %s.%n", user_id, following_id);
            }
        }
    }

    public ResultSet listFollowings(String user_id) throws SQLException {
        String sql = "SELECT user_id, following_id, created_at FROM Following WHERE user_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, user_id);
        return pstmt.executeQuery();
    }

    public ResultSet listFollowers(String following_id) throws SQLException {
        String sql = "SELECT user_id, following_id, created_at FROM Following WHERE following_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, following_id);
        return pstmt.executeQuery();
    }

}