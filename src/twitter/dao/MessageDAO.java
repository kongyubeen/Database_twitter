package twitter.dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private Connection conn;
    public MessageDAO(Connection conn) {
        this.conn = conn;
    }

    // DM 보내기
    public void sendMessage(String senderId, String receiverId, String content) throws SQLException {
        String sql = "INSERT INTO Message (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, senderId);
            ps.setString(2, receiverId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    // 받은 메시지 목록
    // 받은 메시지
    public ResultSet getReceivedMessages(String receiverId) throws SQLException {
        String sql = """
        SELECT sender_id, content, sent_time
        FROM Message
        WHERE receiver_id = ?
        ORDER BY sent_time DESC
    """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, receiverId);
        return ps.executeQuery();
    }

    // 보낸 메시지
    public ResultSet getSentMessages(String senderId) throws SQLException {
        String sql = """
        SELECT receiver_id, content, sent_time
        FROM Message
        WHERE sender_id = ?
        ORDER BY sent_time DESC
    """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, senderId);
        return ps.executeQuery();
    }
    // recent chat users ordered by last message time
    public List<String> getRecentChatUsers(String myId) throws SQLException {
        String sql = """
            SELECT user_id
            FROM (
                SELECT 
                    CASE 
                        WHEN sender_id = ? THEN receiver_id
                        ELSE sender_id
                    END AS user_id,
                    MAX(sent_time) AS last_time
                FROM Message
                WHERE sender_id = ? OR receiver_id = ?
                GROUP BY user_id
            ) AS sub
            ORDER BY last_time DESC
        """;

        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, myId);
            ps.setString(2, myId);
            ps.setString(3, myId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("user_id"));
                }
            }
        }
        return result;
    }

    // one-to-one conversation messages
    public List<ChatMessage> getConversation(String myId, String otherId) throws SQLException {
        String sql = """
            SELECT sender_id, receiver_id, content, sent_time
            FROM Message
            WHERE (sender_id = ? AND receiver_id = ?)
               OR (sender_id = ? AND receiver_id = ?)
            ORDER BY sent_time ASC
        """;

        List<ChatMessage> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, myId);
            ps.setString(2, otherId);
            ps.setString(3, otherId);
            ps.setString(4, myId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessage m = new ChatMessage();
                    m.senderId = rs.getString("sender_id");
                    m.receiverId = rs.getString("receiver_id");
                    m.content = rs.getString("content");
                    m.sentTime = rs.getTimestamp("sent_time");
                    list.add(m);
                }
            }
        }
        return list;
    }

    public static class ChatMessage {
        public String senderId;
        public String receiverId;
        public String content;
        public Timestamp sentTime;
    }
}
