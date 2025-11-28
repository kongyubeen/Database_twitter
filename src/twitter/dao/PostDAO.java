package twitter.dao;

import java.sql.*;

public class PostDAO {
    private Connection conn;

    public PostDAO(Connection conn) {
        this.conn = conn;
    }

    /** 글 작성 */
    public void writePost(String userId, String content) throws SQLException {
        String sql = "INSERT INTO Posts (writer_id, content) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, content);
            ps.executeUpdate();
        }
    }

    /** 글 삭제 */
    public void deletePost(int postId, String userId) throws SQLException {
        String sql = "DELETE FROM Posts WHERE post_id=? AND writer_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * 특정 사용자 게시글 목록 (좋아요 수 + 댓글 수 포함)
     */
    public ResultSet getUserPosts(String userId) throws SQLException {
        String sql = """
            SELECT 
                p.post_id,
                p.writer_id,
                p.content,
                p.num_of_likes,
                p.created_at,
                (SELECT COUNT(*) 
                 FROM Comment c 
                 WHERE c.post_id = p.post_id) AS num_of_comments
            FROM Posts p
            WHERE p.writer_id=?
            ORDER BY p.created_at DESC
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, userId);
        return ps.executeQuery();
    }

    /**
     * 피드 조회: 내 글 + 내가 팔로우하는 사람들 글
     * 좋아요 수 + 댓글 수 포함
     */
    public ResultSet getFeed(String userId) throws SQLException {
        String sql = """
            SELECT 
                p.post_id,
                p.writer_id,
                p.content,
                p.num_of_likes,
                p.created_at,
                (SELECT COUNT(*) 
                 FROM Comment c 
                 WHERE c.post_id = p.post_id) AS num_of_comments
            FROM Posts p
            WHERE p.writer_id = ?
            
            UNION
            
            SELECT 
                p.post_id,
                p.writer_id,
                p.content,
                p.num_of_likes,
                p.created_at,
                (SELECT COUNT(*) 
                 FROM Comment c 
                 WHERE c.post_id = p.post_id) AS num_of_comments
            FROM Posts p
            JOIN Following f ON f.following_id = p.writer_id
            WHERE f.user_id = ?
            
            ORDER BY created_at DESC
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, userId); // 내 글
        ps.setString(2, userId); // 내가 팔로우한 글
        return ps.executeQuery();
    }

    /**
     * 단일 게시글 조회 (상세 페이지용)
     * 좋아요 수 + 댓글 수 포함
     */
    public ResultSet getPostById(int postId) throws SQLException {
        String sql = """
            SELECT 
                p.post_id,
                p.writer_id,
                p.content,
                p.num_of_likes,
                p.created_at,
                (SELECT COUNT(*) 
                 FROM Comment c 
                 WHERE c.post_id = p.post_id) AS num_of_comments
            FROM Posts p
            WHERE p.post_id = ?
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, postId);
        return ps.executeQuery();
    }
}
