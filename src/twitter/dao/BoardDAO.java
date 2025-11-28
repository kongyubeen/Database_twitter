package twitter.dao;

import java.sql.*;

public class BoardDAO {
    private Connection conn;

    public BoardDAO(Connection conn) {
        this.conn = conn;
    }

    // 게시판 전체 보기 (게시글 + 작성자 정보)
    public ResultSet getBoard() throws SQLException {
        String sql = """
            SELECT 
                p.post_id,
                p.writer_id,
                u.user_id,
                u.email,
                p.content,
                p.created_at
            FROM Posts p
            JOIN User u ON p.writer_id = u.user_id
            ORDER BY p.created_at DESC
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }

    // 특정 게시글의 상세보기 (게시글 + 댓글 + 대댓글 포함)
    public ResultSet getPostDetails(int postId) throws SQLException {
        String sql = """
            SELECT 
                p.post_id,
                p.writer_id,
                p.content AS post_content,
                p.created_at AS post_time,
                c.comment_id,
                c.writer_id AS comment_writer,
                c.content AS comment_content,
                cc.child_id,
                cc.writer_id AS child_writer,
                cc.content AS child_content
            FROM Posts p
            LEFT JOIN Comment c ON p.post_id = c.post_id
            LEFT JOIN Child_Comment cc ON c.comment_id = cc.comment_id
            WHERE p.post_id = ?
            ORDER BY c.created_at ASC, cc.created_at ASC
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, postId);
        return ps.executeQuery();
    }
}