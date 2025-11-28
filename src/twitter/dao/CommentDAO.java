package twitter.dao;

import java.sql.*;

public class CommentDAO {
    private final Connection conn;

    public CommentDAO(Connection conn) {
        this.conn = conn;
    }

    // 댓글 작성
    public void writeComment(int postId, String writerId, String content) throws SQLException {
        String sql = "INSERT INTO Comment (post_id, writer_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setString(2, writerId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    // 대댓글 작성
    public void writeChildComment(int parentId, String userId, String content) throws SQLException {

        // 1) 부모 존재 검사 (부모가 Comment인지 Child_Comment인지 둘 다 검사)
        boolean exists = false;

        String sql1 = "SELECT 1 FROM Comment WHERE comment_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) exists = true;
            }
        }

        String sql2 = "SELECT 1 FROM Child_Comment WHERE child_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) exists = true;
            }
        }

        if (!exists) {
            throw new SQLException("Parent comment not found: " + parentId);
        }

        // 2) Insert child comment
        String insert = "INSERT INTO Child_Comment (parent_comment_id, writer_id, content) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, parentId);
            ps.setString(2, userId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }


    // 특정 게시글 댓글 목록
    public ResultSet getCommentsByPost(int postId) throws SQLException {
        String sql = """
            SELECT c.comment_id, c.writer_id, c.content, c.created_at, c.num_of_likes
            FROM Comment c
            WHERE c.post_id = ?
            ORDER BY c.created_at ASC
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, postId);
        return ps.executeQuery();
    }

    // 특정 댓글의 대댓글 목록
    public ResultSet getChildComments(int commentId) throws SQLException {
        String sql = """
            SELECT cc.child_id, cc.writer_id, cc.content, cc.created_at, cc.num_of_likes
            FROM Child_Comment cc
            WHERE cc.parent_comment_id = ?
            ORDER BY cc.created_at ASC
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, commentId);
        return ps.executeQuery();
    }

    // 댓글 삭제
    public void deleteComment(int commentId, String writerId) throws SQLException {
        String sql = "DELETE FROM Comment WHERE comment_id = ? AND writer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setString(2, writerId);
            ps.executeUpdate();
        }
    }

    // 대댓글 삭제
    public void deleteChildComment(int childId, String writerId) throws SQLException {
        String sql = "DELETE FROM Child_Comment WHERE child_id = ? AND writer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, childId);
            ps.setString(2, writerId);
            ps.executeUpdate();
        }
    }
}
