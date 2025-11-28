package twitter.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import twitter.dao.FollowDAO;
import twitter.dao.RecommendationDAO;

public class UserActionPanel extends JPanel {

    private MainFrame mainFrame;
    private FollowDAO followDAO;
    private RecommendationDAO recommendationDAO;

    private JTextField searchField;
    private RoundedButton searchFollowBtn;

    private JPanel cardListPanel;

    private JButton followerTab;
    private JButton followingTab;
    private JButton recommendTab;

    public UserActionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.followDAO = new FollowDAO(mainFrame.getConnection());
        this.recommendationDAO = new RecommendationDAO(mainFrame.getConnection());

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createTopArea(), BorderLayout.NORTH);
        add(createCardListArea(), BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadFollowers();
                selectTab(followerTab);
            }
        });
    }

    /* ---------------------------------------
     * TOP 전체 구성 (검색바 + 탭)
     * --------------------------------------- */
    private JPanel createTopArea() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);

        container.add(createSearchBar(), BorderLayout.NORTH);
        container.add(createTopTabs(), BorderLayout.SOUTH);

        return container;
    }

    /* ---------------------------------------
     * 검색바 UI 개선
     * --------------------------------------- */
    private JPanel createSearchBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 5, 20));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setPreferredSize(new Dimension(240, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        searchFollowBtn = new RoundedButton("follow");
        searchFollowBtn.setPreferredSize(new Dimension(85, 40));
        searchFollowBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchFollowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchFollowBtn.addActionListener(e -> followBySearch());

        panel.add(searchField, BorderLayout.CENTER);
        panel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        panel.add(searchFollowBtn, BorderLayout.EAST);

        return panel;
    }

    private void followBySearch() {
        String myId = mainFrame.getLoggedInUserId();
        String targetId = searchField.getText().trim();

        if (targetId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a user ID.");
            return;
        }

        if (myId.equals(targetId)) {
            JOptionPane.showMessageDialog(this, "You cannot follow yourself.");
            return;
        }

        try {
            followDAO.followUser(myId, targetId);
            JOptionPane.showMessageDialog(this, "You followed" + targetId + ".");
            loadFollowers();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "follow failed: " + ex.getMessage());
        }
    }

    /* ---------------------------------------
     * 탭 디자인 개선
     * --------------------------------------- */
    private JPanel createTopTabs() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(5, 20, 10, 20));
        panel.setBackground(Color.WHITE);

        panel.add(Box.createHorizontalGlue());

        followerTab = createTabButton("followers");
        followingTab = createTabButton("following");
        recommendTab = createTabButton("recommend");

        followerTab.addActionListener(e -> {
            selectTab(followerTab);
            loadFollowers();
        });
        followingTab.addActionListener(e -> {
            selectTab(followingTab);
            loadFollowings();
        });
        recommendTab.addActionListener(e -> {
            selectTab(recommendTab);
            loadRecommends();
        });

        panel.add(followerTab);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(followingTab);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(recommendTab);

        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectTab(JButton selected) {
        JButton[] tabs = {followerTab, followingTab, recommendTab};

        for (JButton b : tabs) {
            if (b == selected) {
                b.setFont(new Font("Segoe UI", Font.BOLD, 18));
                b.setForeground(new Color(60, 120, 255));
            } else {
                b.setFont(new Font("Segoe UI", Font.PLAIN, 17));
                b.setForeground(Color.GRAY);
            }
        }
    }

    /* ---------------------------------------
     * 카드 리스트 스크롤 패널
     * --------------------------------------- */
    private JScrollPane createCardListArea() {
        cardListPanel = new JPanel();
        cardListPanel.setLayout(new BoxLayout(cardListPanel, BoxLayout.Y_AXIS));
        cardListPanel.setBackground(Color.WHITE);
        cardListPanel.setBorder(new EmptyBorder(5, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(cardListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        // ⭐ 여기 추가해야 함!!
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        return scroll;
    }
    private void refreshCardPanel() {
        cardListPanel.revalidate();
        cardListPanel.repaint();
    }

    /* ---------------------------------------
     * 카드 디자인 (ExplorePanel 스타일 적용)
     * --------------------------------------- */
    private JPanel createUserCard(String userId, boolean isFollowing) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        /* LEFT: Avatar */
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(70, 70));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(230, 230, 230));
                g.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        avatar.setPreferredSize(new Dimension(46, 46));
        avatar.setOpaque(false);

        left.add(avatar);
        card.add(left, BorderLayout.WEST);

        /* CENTER: User ID */
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(15, 5, 15, 5));

        JLabel nameLabel = new JLabel(userId);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        center.add(nameLabel);
        card.add(center, BorderLayout.CENTER);

        /* RIGHT: Follow Button */
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setBorder(new EmptyBorder(0, 0, 0, 15));

        RoundedButton btn = new RoundedButton("follow");
        styleFollowButton(btn, isFollowing);

        btn.addActionListener(e -> handleFollowButton(userId, btn));

        right.add(btn);
        card.add(right, BorderLayout.EAST);

        return card;
    }
    private static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '&' -> out.append("&amp;");
                case '"' -> out.append("&quot;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }


    /* ==============================
       스크롤바 UI 커스텀
       ============================== */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(245, 245, 245);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (!scrollbar.isEnabled() || r.width > r.height) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(thumbColor);
            int x = r.x + 3;
            int y = r.y + 3;
            int w = r.width - 6;
            int h = r.height - 6;
            g2.fillRoundRect(x, y, w, h, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.dispose();
        }
    }

    /* ---------------------------------------
     * follow 버튼 스타일 개선 (ExplorePanel과 동일)
     * --------------------------------------- */
    private void styleFollowButton(RoundedButton btn, boolean isFollowing) {

        btn.setPreferredSize(new Dimension(90, 36));
        btn.setMaximumSize(new Dimension(90, 36));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));

        if (isFollowing) {
            btn.setText("unfollow");
            btn.setBackgroundColor(new Color(230, 230, 230));
            btn.setForegroundColor(new Color(80, 80, 80));
        } else {
            btn.setText("follow");
            btn.setBackgroundColor(new Color(152, 187, 255));
            btn.setForegroundColor(Color.WHITE);
        }
    }

    /* ---------------------------------------
     * follow / unfollow 동작
     * --------------------------------------- */
    private void handleFollowButton(String targetId, RoundedButton btn) {
        String myId = mainFrame.getLoggedInUserId();

        try {
            if (btn.getText().equals("follow")) {
                followDAO.followUser(myId, targetId);
                styleFollowButton(btn, true);

            } else {
                followDAO.unfollowUser(myId, targetId);
                styleFollowButton(btn, false);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /* ---------------------------------------
     * 리스트 로딩 (기능 변경 없음)
     * --------------------------------------- */

    private void loadFollowers() {
        cardListPanel.removeAll();

        try (ResultSet rs = followDAO.listFollowers(mainFrame.getLoggedInUserId())) {
            while (rs.next()) {
                String uid = rs.getString("user_id");
                boolean iFollow = isIFollow(uid);
                cardListPanel.add(createUserCard(uid, iFollow));
            }
        } catch (Exception e) { e.printStackTrace(); }

        refreshCardPanel();
    }

    private void loadFollowings() {
        cardListPanel.removeAll();

        try (ResultSet rs = followDAO.listFollowings(mainFrame.getLoggedInUserId())) {
            while (rs.next()) {
                String uid = rs.getString("following_id");
                cardListPanel.add(createUserCard(uid, true));
            }
        } catch (Exception e) { e.printStackTrace(); }

        refreshCardPanel();
    }

    private void loadRecommends() {
        cardListPanel.removeAll();

        try {
            List<String> recs = recommendationDAO.getRecommendations(mainFrame.getLoggedInUserId());

            for (String uid : recs) {
                boolean iFollow = isIFollow(uid);
                cardListPanel.add(createUserCard(uid, iFollow));
            }

        } catch (Exception e) { e.printStackTrace(); }

        refreshCardPanel();
    }


    private boolean isIFollow(String targetId) throws SQLException {
        ResultSet rs = followDAO.listFollowings(mainFrame.getLoggedInUserId());
        while (rs.next()) {
            if (rs.getString("following_id").equals(targetId)) return true;
        }
        return false;
    }
}
