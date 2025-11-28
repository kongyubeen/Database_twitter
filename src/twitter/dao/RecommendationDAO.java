package twitter.dao;

import java.sql.*;
import java.util.*;

public class RecommendationDAO {
    private Connection conn;

    public RecommendationDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * 현실적인 추천 알고리즘
     * 1) 공통 팔로우 기반
     * 2) 내 팔로워 → follow하는 사람
     * 3) 인기 유저 (팔로워 많은 순)
     * 4) 신규 유저
     */
    public List<String> getRecommendations(String myId) throws SQLException {

        Set<String> result = new LinkedHashSet<>();

        /* -------------------------------
           1) 공통 팔로우가 많은 사용자 추천
           ------------------------------- */
        String commonFollowSql = """
            SELECT f2.following_id AS user, COUNT(*) AS score
            FROM Following f1
            JOIN Following f2 ON f1.following_id = f2.user_id
            WHERE f1.user_id = ?
              AND f2.following_id != ?
              AND f2.following_id NOT IN (
                 SELECT following_id FROM Following WHERE user_id = ?
              )
            GROUP BY f2.following_id
            HAVING score >= 2
            ORDER BY score DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(commonFollowSql)) {
            ps.setString(1, myId);
            ps.setString(2, myId);
            ps.setString(3, myId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString("user"));
            }
        }

        /* ------------------------------------
           2) 나를 follow하는 사람 → follow하는 유저 추천
           ------------------------------------ */
        String followerBasedSql = """
            SELECT DISTINCT f2.following_id
            FROM Following f1
            JOIN Following f2 ON f1.user_id = f2.user_id
            WHERE f2.following_id != ?
              AND f2.following_id NOT IN (
                 SELECT following_id FROM Following WHERE user_id = ?
              )
              AND f1.following_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(followerBasedSql)) {
            ps.setString(1, myId);
            ps.setString(2, myId);
            ps.setString(3, myId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString(1));
            }
        }

        /* -------------------------------
           3) 인기 유저 추천 (팔로워 많은 순)
           ------------------------------- */
        String popularSql = """
            SELECT following_id AS user, COUNT(*) AS score
            FROM Following
            WHERE following_id != ?
              AND following_id NOT IN (
                 SELECT following_id FROM Following WHERE user_id = ?
              )
            GROUP BY following_id
            ORDER BY score DESC
            LIMIT 10
        """;

        try (PreparedStatement ps = conn.prepareStatement(popularSql)) {
            ps.setString(1, myId);
            ps.setString(2, myId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString("user"));
            }
        }

        /* -------------------------------
           4) 최근 가입한 유저 추천
           ------------------------------- */
        String newUserSql = """
            SELECT user_id
            FROM User
            WHERE user_id != ?
              AND user_id NOT IN (
                 SELECT following_id FROM Following WHERE user_id = ?
              )
            ORDER BY created_at DESC
            LIMIT 10
        """;

        try (PreparedStatement ps = conn.prepareStatement(newUserSql)) {
            ps.setString(1, myId);
            ps.setString(2, myId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(rs.getString("user_id"));
            }
        }

        return new ArrayList<>(result);
    }

}
