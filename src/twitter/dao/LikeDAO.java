package twitter.dao;

import java.sql.*;

public class LikeDAO {
    private final Connection conn;

    public LikeDAO(Connection conn) {
        this.conn = conn;
    }

    // ===================== 게시글 좋아요 =====================

    public void likePost(String userId, int postId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Post_Like WHERE liker_id = ? AND post_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, userId);
            ps.setInt(2, postId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        boolean ac = conn.getAutoCommit();
        String insertSql = "INSERT INTO Post_Like (liker_id, post_id) VALUES (?, ?)";
        String updateSql = "UPDATE Posts SET num_of_likes = num_of_likes + 1 WHERE post_id = ?";

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, userId);
                ps.setInt(2, postId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, postId);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(ac);
        }
    }

    public void unlikePost(String userId, int postId) throws SQLException {
        boolean ac = conn.getAutoCommit();
        String deleteSql = "DELETE FROM Post_Like WHERE liker_id = ? AND post_id = ?";
        String updateSql = "UPDATE Posts SET num_of_likes = num_of_likes - 1 WHERE post_id = ? AND num_of_likes > 0";

        try {
            conn.setAutoCommit(false);
            int rows;
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, userId);
                ps.setInt(2, postId);
                rows = ps.executeUpdate();
            }
            if (rows > 0) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, postId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(ac);
        }
    }
    public boolean isCommentLiked(String userId, int commentId) throws SQLException {
        String sql = "SELECT 1 FROM Comment_Like WHERE liker_id = ? AND comment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    public boolean isChildCommentLiked(String userId, int childCommentId) throws SQLException {
        String sql = "SELECT 1 FROM Child_Like WHERE liker_id = ? AND child_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, childCommentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }



    public boolean hasUserLikedPost(String userId, int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Post_Like WHERE liker_id = ? AND post_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, postId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // ===================== 댓글 좋아요 =====================

    public void likeComment(String userId, int commentId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Comment_Like WHERE liker_id = ? AND comment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, userId);
            ps.setInt(2, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertSql = "INSERT INTO Comment_Like (liker_id, comment_id) VALUES (?, ?)";
        String updateSql = "UPDATE Comment SET num_of_likes = num_of_likes + 1 WHERE comment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, userId);
            ps.setInt(2, commentId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, commentId);
            ps.executeUpdate();
        }
    }

    public void unlikeComment(String userId, int commentId) throws SQLException {
        String deleteSql = "DELETE FROM Comment_Like WHERE liker_id = ? AND comment_id = ?";
        int deleted = 0;

        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, userId);
            ps.setInt(2, commentId);
            deleted = ps.executeUpdate();
        }

        if (deleted > 0) {
            String updateSql = "UPDATE Comment SET num_of_likes = num_of_likes - 1 WHERE comment_id = ? AND num_of_likes > 0";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, commentId);
                ps.executeUpdate();
            }
        }
    }

    public int getCommentLikeCount(int commentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Comment_Like WHERE comment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ===================== 대댓글 좋아요 =====================

    public void likeChildComment(String userId, int childCommentId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Child_Like WHERE liker_id = ? AND child_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, userId);
            ps.setInt(2, childCommentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertSql = "INSERT INTO Child_Like (liker_id, child_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, userId);
            ps.setInt(2, childCommentId);
            ps.executeUpdate();
        }

        String updateSql = "UPDATE Child_Comment SET num_of_likes = num_of_likes + 1 WHERE child_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, childCommentId);
            ps.executeUpdate();
        }
    }

    public void unlikeChildComment(String userId, int childCommentId) throws SQLException {
        String deleteSql = "DELETE FROM Child_Like WHERE liker_id = ? AND child_id = ?";
        int deleted;

        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, userId);
            ps.setInt(2, childCommentId);
            deleted = ps.executeUpdate();
        }

        if (deleted > 0) {
            String updateSql = "UPDATE Child_Comment SET num_of_likes = num_of_likes - 1 WHERE child_id = ? AND num_of_likes > 0";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, childCommentId);
                ps.executeUpdate();
            }
        }
    }

    public int getChildCommentLikeCount(int childCommentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Child_Like WHERE child_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, childCommentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    // ===================== 게시글 좋아요 개수 조회 =====================
    public int getPostLikeCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Post_Like WHERE post_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

}
