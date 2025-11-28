package twitter.dao;

import java.sql.*;

public class ExploreDAO {
    private Connection conn;

    public ExploreDAO(Connection conn) {
        this.conn = conn;
    }

    /** 전체 게시물: like 수 내림차순 정렬 */
    public ResultSet getAllPostsByLike() throws SQLException {
        String sql = """
            SELECT
                p.post_id,
                p.writer_id,
                p.content,
                p.created_at,
                (SELECT COUNT(*) FROM Post_Like pl WHERE pl.post_id = p.post_id) AS like_count,
                (SELECT COUNT(*) FROM Comment c WHERE c.post_id = p.post_id) AS comment_count
            FROM Posts p
            ORDER BY like_count DESC
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }

    /** 검색: 특정 유저의 게시물만 가져오기 */
    public ResultSet searchUserPosts(String name) throws SQLException {
        String sql = """
            SELECT
                p.post_id,
                p.writer_id,
                p.content,
                p.created_at,
                (SELECT COUNT(*) FROM Post_Like pl WHERE pl.post_id = p.post_id) AS like_count,
                (SELECT COUNT(*) FROM Comment c WHERE c.post_id = p.post_id) AS comment_count
            FROM Posts p
            WHERE p.writer_id LIKE ?
            ORDER BY like_count DESC
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + name + "%");
        return ps.executeQuery();
    }
}
