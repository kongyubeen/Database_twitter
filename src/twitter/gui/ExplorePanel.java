package twitter.gui;

import twitter.dao.PostDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExplorePanel extends JPanel {

    private final MainFrame mainFrame;
    private final PostDAO postDAO;
    private final Connection conn;

    private JTextField searchField;
    private RoundedButton searchButton;

    private JPanel feedListPanel;
    private JScrollPane feedScrollPane;

    public ExplorePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.conn = mainFrame.getConnection();
        this.postDAO = new PostDAO(conn);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel top = createSearchPanel();
        add(top, BorderLayout.NORTH);

        feedListPanel = new JPanel();
        feedListPanel.setLayout(new BoxLayout(feedListPanel, BoxLayout.Y_AXIS));
        feedListPanel.setBackground(Color.WHITE);
        feedListPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        feedScrollPane = new JScrollPane(feedListPanel);
        feedScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230,230,230)));
        feedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        feedScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(feedScrollPane, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadPopularPosts();
            }
        });
    }

    private JPanel createSearchPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200,200,200)));

        searchButton = new RoundedButton("search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBackground(new Color(152,180,255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setPreferredSize(new Dimension(90, 38));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        searchButton.addActionListener(e -> searchUserPosts());

        p.add(searchField, BorderLayout.CENTER);
        p.add(searchButton, BorderLayout.EAST);
        return p;
    }

    private void loadPopularPosts() {
        feedListPanel.removeAll();

        String sql = """
            SELECT p.post_id, p.writer_id, p.content, p.created_at,
                   (SELECT COUNT(*) FROM Post_Like WHERE post_id = p.post_id) AS likes,
                   (SELECT COUNT(*) FROM Comment WHERE post_id = p.post_id) AS comments
            FROM Posts p
            ORDER BY likes DESC, created_at DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int postId = rs.getInt("post_id");
                String writer = rs.getString("writer_id");
                String content = rs.getString("content");
                String time = rs.getString("created_at");
                int likes = rs.getInt("likes");
                int comments = rs.getInt("comments");

                JPanel card = createPostCard(postId, writer, content, time, likes, comments);
                feedListPanel.add(card);
                feedListPanel.add(Box.createVerticalStrut(5));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        feedListPanel.revalidate();
        feedListPanel.repaint();
    }

    // ExplorePanel 안에 있는 기존 createUserCard(...) 를 아래 코드로 교체
    private JPanel createUserCard(String userId, boolean isFollowing) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        /* ========== LEFT : 프로필 원 ========== */
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(70, 70));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0xEEEEEE));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(46, 46));
        avatar.setOpaque(false);

        left.add(avatar);
        card.add(left, BorderLayout.WEST);

        /* ========== CENTER : 유저 이름 ========== */
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(8, 0, 8, 0));
        center.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(userId);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(nameLabel);
        card.add(center, BorderLayout.CENTER);

        /* ========== RIGHT : follow 버튼 ========== */
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setBorder(new EmptyBorder(0, 0, 0, 20));

        RoundedButton followBtn = new RoundedButton(isFollowing ? "unfollow" : "follow");
        followBtn.setPreferredSize(new Dimension(90, 36));
        followBtn.setMaximumSize(new Dimension(90, 36));
        followBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        followBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (isFollowing) {
            followBtn.setText("unfollow");
            followBtn.setBackgroundColor(new Color(230, 230, 230));
            followBtn.setForegroundColor(new Color(100, 100, 100));
        } else {
            followBtn.setText("follow");
            followBtn.setBackgroundColor(new Color(152, 187, 255));
            followBtn.setForegroundColor(Color.WHITE);
        }

        followBtn.addActionListener(e -> toggleFollow(userId, followBtn));

        right.add(Box.createHorizontalGlue());
        right.add(followBtn);

        card.add(right, BorderLayout.EAST);

        return card;
    }


    private boolean checkFollowing(String myId, String targetId) {
        if (myId == null || targetId == null) return false;

        String sql = "SELECT 1 FROM Following WHERE user_id=? AND following_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, myId);
            ps.setString(2, targetId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void toggleFollow(String targetId, RoundedButton btn) {
        String myId = mainFrame.getLoggedInUserId();
        if (myId == null) return;

        boolean already = checkFollowing(myId, targetId);

        try {
            if (already) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Following WHERE user_id=? AND following_id=?"
                );
                ps.setString(1, myId);
                ps.setString(2, targetId);
                ps.executeUpdate();

                btn.setText("follow");
                btn.setBackgroundColor(new Color(152, 187, 255));
                btn.setForegroundColor(Color.WHITE);

            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Following(user_id, following_id) VALUES(?, ?)"
                );
                ps.setString(1, myId);
                ps.setString(2, targetId);
                ps.executeUpdate();

                btn.setText("unfollow");
                btn.setBackgroundColor(new Color(230, 230, 230));
                btn.setForegroundColor(new Color(100, 100, 100));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void searchUserPosts() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        feedListPanel.removeAll();

        String userSql = """
        SELECT user_id
        FROM User
        WHERE user_id LIKE ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(userSql)) {
            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                boolean foundUser = false;

                while (rs.next()) {
                    foundUser = true;

                    String uid = rs.getString("user_id");

                    boolean isFollowing = checkFollowing(mainFrame.getLoggedInUserId(), uid);

                    JPanel userCard = createUserCard(uid, isFollowing);
                    feedListPanel.add(userCard);
                    feedListPanel.add(Box.createVerticalStrut(10));
                }

                if (!foundUser) {
                    JLabel noUser = new JLabel("No users found.");
                    noUser.setBorder(new EmptyBorder(10, 10, 10, 10));
                    feedListPanel.add(noUser);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = """
            SELECT p.post_id, p.writer_id, p.content, p.created_at,
                   (SELECT COUNT(*) FROM Post_Like WHERE post_id = p.post_id) AS likes,
                   (SELECT COUNT(*) FROM Comment WHERE post_id = p.post_id) AS comments
            FROM Posts p
            WHERE p.writer_id LIKE ?
            ORDER BY p.created_at DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int postId = rs.getInt("post_id");
                    String writer = rs.getString("writer_id");
                    String content = rs.getString("content");
                    String time = rs.getString("created_at");
                    int likes = rs.getInt("likes");
                    int comments = rs.getInt("comments");

                    JPanel card = createPostCard(postId, writer, content, time, likes, comments);
                    feedListPanel.add(card);
                    feedListPanel.add(Box.createVerticalStrut(5));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        feedListPanel.revalidate();
        feedListPanel.repaint();
    }

    private JPanel createPostCard(int postId,
                                  String writer,
                                  String content,
                                  String createdAt,
                                  int likeCount,
                                  int commentCount) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)));

        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(70, 70));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0xEEEEEE));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(46, 46));
        avatar.setOpaque(false);

        left.add(avatar);
        card.add(left, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(8, 0, 8, 0));
        center.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel writerLabel = new JLabel(writer);
        writerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel timeLabel = new JLabel(createdAt);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        topRow.add(writerLabel, BorderLayout.WEST);
        topRow.add(timeLabel, BorderLayout.EAST);

        center.add(topRow);

        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setFocusable(false);
        contentArea.setBorder(new EmptyBorder(5, 0, 5, 0));
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        center.add(contentArea);

        JPanel actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setBackground(Color.WHITE);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean liked = isLikedByCurrentUser(postId);
        JButton heartButton = new JButton(liked ? "♥" : "♡");
        heartButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        heartButton.setContentAreaFilled(false);
        heartButton.setBorder(BorderFactory.createEmptyBorder());
        heartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        heartButton.setFocusPainted(false);
        heartButton.setForeground(liked ? Color.RED : Color.BLACK);

        JLabel likeLabel = new JLabel(String.valueOf(likeCount));

        heartButton.addActionListener(e -> toggleLike(postId, heartButton, likeLabel));

        JButton commentButton = new JButton("▢");
        commentButton.setFont(new Font("Dialog", Font.PLAIN, 14));
        commentButton.setContentAreaFilled(false);
        commentButton.setBorder(BorderFactory.createEmptyBorder());
        commentButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        commentButton.setFocusPainted(false);

        JLabel commentLabel = new JLabel(String.valueOf(commentCount));

        commentButton.addActionListener(e -> {
            mainFrame.setLastPanel("explorePanel");
            mainFrame.showPostDetail(postId);
        });

        actionRow.add(heartButton);
        actionRow.add(Box.createHorizontalStrut(4));
        actionRow.add(likeLabel);
        actionRow.add(Box.createHorizontalStrut(18));
        actionRow.add(commentButton);
        actionRow.add(Box.createHorizontalStrut(4));
        actionRow.add(commentLabel);

        center.add(actionRow);

        card.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setBorder(new EmptyBorder(0, 0, 0, 8));

        JButton moreButton = new JButton("...");
        moreButton.setFont(new Font("Dialog", Font.BOLD, 16));
        moreButton.setContentAreaFilled(false);
        moreButton.setBorder(BorderFactory.createEmptyBorder());
        moreButton.setFocusPainted(false);
        moreButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String currentUser = mainFrame.getLoggedInUserId();
        boolean myPost = currentUser != null && currentUser.equals(writer);
        moreButton.setVisible(myPost);

        card.add(right, BorderLayout.EAST);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        return card;
    }

    private boolean isLikedByCurrentUser(int postId) {
        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return false;

        String sql = "SELECT 1 FROM Post_Like WHERE post_id = ? AND liker_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void toggleLike(int postId, JButton heartButton, JLabel likeCountLabel) {
        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return;

        boolean alreadyLiked = isLikedByCurrentUser(postId);

        try {
            if (alreadyLiked) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Post_Like WHERE post_id = ? AND liker_id = ?")) {
                    ps.setInt(1, postId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Post_Like(post_id, liker_id) VALUES(?, ?)")) {
                    ps.setInt(1, postId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
            }

            int newCount = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Post_Like WHERE post_id = ?")) {
                ps.setInt(1, postId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) newCount = rs.getInt(1);
                }
            }

            likeCountLabel.setText(String.valueOf(newCount));

            if (alreadyLiked) {
                heartButton.setText("♡");
                heartButton.setForeground(Color.BLACK);
            } else {
                heartButton.setText("♥");
                heartButton.setForeground(Color.RED);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while processing like: " + e.getMessage());
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(190,190,190);
            this.trackColor = new Color(245,245,245);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0,0));
            b.setMinimumSize(new Dimension(0,0));
            b.setMaximumSize(new Dimension(0,0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x+3, r.y+3, r.width-6, r.height-6, 10,10);
            g2.dispose();
        }
    }
}
