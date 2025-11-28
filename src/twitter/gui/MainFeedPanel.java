// 이 파일은 twitter.gui 패키지에 속합니다.
package twitter.gui;

import twitter.dao.PostDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainFeedPanel extends JPanel {

    private final MainFrame mainFrame;
    private final PostDAO postDAO;
    private final Connection conn;

    // 작성 영역
    private JTextArea postTextArea;
    private JButton postButton;

    // 피드 영역
    private JPanel feedListPanel;
    private JScrollPane feedScrollPane;

    public MainFeedPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.conn = mainFrame.getConnection();
        this.postDAO = new PostDAO(conn);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        add(center, BorderLayout.CENTER);

        JPanel composePanel = createComposePanel();
        center.add(composePanel, BorderLayout.NORTH);

        feedListPanel = new JPanel();
        feedListPanel.setLayout(new BoxLayout(feedListPanel, BoxLayout.Y_AXIS));
        feedListPanel.setBackground(Color.WHITE);
        feedListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedListPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        feedScrollPane = new JScrollPane(feedListPanel);
        feedScrollPane.setBorder(BorderFactory.createTitledBorder("My Feed"));
        feedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        feedScrollPane.getVerticalScrollBar().setUnitIncrement(25);
        feedScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        feedScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        center.add(feedScrollPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadFeed();
            }
        });
    }

    private JLabel makeMenuLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(8, 5, 8, 5));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onMenuClicked(text);
            }
        });

        return label;
    }

    private void onMenuClicked(String menu) {
        switch (menu) {
            case "Home" -> loadFeed();
            case "Explore" -> mainFrame.showPanel("explorePanel");
            case "Message" -> mainFrame.showPanel("messagePanel");
            case "People" -> mainFrame.showPanel("userActionPanel");
        }
    }

    private void showProfileMenu(Component invoker, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem changePw = new JMenuItem("Change Password");
        JMenuItem logout = new JMenuItem("Log out");

        changePw.addActionListener(e -> openChangePasswordDialog());
        logout.addActionListener(e -> performLogout());

        menu.add(changePw);
        menu.addSeparator();
        menu.add(logout);

        menu.show(invoker, x, -menu.getPreferredSize().height);
    }

    private void performLogout() {
        int ans = JOptionPane.showConfirmDialog(
                this,
                "log out?",
                "Log out",
                JOptionPane.YES_NO_OPTION
        );
        if (ans == JOptionPane.YES_OPTION) {
            mainFrame.setLoggedInUserId(null);
            mainFrame.showPanel("login");
        }
    }


    /* ==============================
       상단 작성 패널
       ============================== */
    private JPanel createComposePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)));

        postTextArea = new JTextArea(3, 30);
        postTextArea.setLineWrap(true);
        postTextArea.setWrapStyleWord(true);
        postTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(postTextArea);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        panel.add(scroll, BorderLayout.CENTER);

        postButton = createPostButton();

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 10, 10, 20));
        right.add(postButton, BorderLayout.SOUTH);

        panel.add(right, BorderLayout.EAST);
        postButton.addActionListener(e -> handlePost());

        return panel;
    }

    private JButton createPostButton() {
        JButton btn = new JButton("post") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(152, 187, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String text = "post";
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(text)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(90, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handlePost() {
        String content = postTextArea.getText().trim();
        String userId = mainFrame.getLoggedInUserId();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter content.", "Post Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            postDAO.writePost(userId, content);

            showRoundedMessage("Post successfully created.");

            postTextArea.setText("");
            loadFeed();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showRoundedMessage("Error while creating post.");
        }

    }
    public void showRoundedMessage(String message) {

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(8, 12, 10, 12));

        // ===== X Button =====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JButton closeBtn = new JButton("x");
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        top.add(closeBtn, BorderLayout.EAST);

        root.add(top);

        // ===== Message =====
        JLabel msg = new JLabel(message, SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);
        msgPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        msgPanel.add(msg, BorderLayout.CENTER);

        root.add(msgPanel);

        // ===== Divider Line =====
        JComponent line = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(230, 1);
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(200, 200, 200));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        root.add(line);

        // ===== yes Button =====
        JButton yesBtn = new JButton("yes");
        yesBtn.setFocusPainted(false);
        yesBtn.setBorderPainted(false);
        yesBtn.setContentAreaFilled(false);
        yesBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        yesBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        bottom.setOpaque(false);
        bottom.add(yesBtn);

        root.add(bottom);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setSize(230, 120);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }



    /* ==============================
       피드 로드
       ============================== */
    private void loadFeed() {
        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return;

        feedListPanel.removeAll();

        try {
            ResultSet rs = postDAO.getFeed(userId);

            if (!rs.isBeforeFirst()) {
                JLabel empty = new JLabel("There is no feed to display. (Try posting or following someone)");
                empty.setBorder(new EmptyBorder(20, 20, 20, 20));
                feedListPanel.add(empty);
            } else {
                while (rs.next()) {
                    int postId = rs.getInt("post_id");
                    String writerId = rs.getString("writer_id");
                    String content = rs.getString("content");
                    String createdAt = rs.getString("created_at");
                    int likes = getPostLikeCount(postId);
                    int comments = rs.getInt("num_of_comments");

                    JPanel card = createPostCard(postId, writerId, content, createdAt, likes, comments);
                    feedListPanel.add(card);
                    feedListPanel.add(Box.createVerticalStrut(5));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JLabel err = new JLabel("Error loading feed: " + e.getMessage());
            err.setForeground(Color.RED);
            err.setBorder(new EmptyBorder(20, 20, 20, 20));
            feedListPanel.add(err);
        }

        feedListPanel.revalidate();
        feedListPanel.repaint();
    }


    /* ==============================
       카드형 포스트 UI
       ============================== */
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
            mainFrame.setLastPanel("mainFeed");
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

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Dialog", Font.PLAIN, 12));
        deleteButton.setForeground(Color.RED);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder());
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.setVisible(false);

        JButton moreButton = new JButton("...");
        moreButton.setFont(new Font("Dialog", Font.BOLD, 16));
        moreButton.setContentAreaFilled(false);
        moreButton.setBorder(BorderFactory.createEmptyBorder());
        moreButton.setFocusPainted(false);
        moreButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String currentUser = mainFrame.getLoggedInUserId();
        boolean myPost = currentUser != null && currentUser.equals(writer);
        moreButton.setVisible(myPost);

        moreButton.addActionListener(e -> {
            JDialog popup = new JDialog(SwingUtilities.getWindowAncestor(this));
            popup.setUndecorated(true);
            popup.setSize(110, 45);
            popup.setBackground(new Color(0, 0, 0, 0));

            JPanel bg = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                }
            };
            bg.setOpaque(false);
            bg.setLayout(new BorderLayout());
            bg.setBorder(new EmptyBorder(6, 10, 6, 10));

            JButton deleteBtn = new JButton("Delete");
            deleteBtn.setForeground(Color.RED);
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setContentAreaFilled(false);
            deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            deleteBtn.addActionListener(ev -> {
                popup.dispose();
                showDeleteConfirmPopup("delete this post?", () -> deletePost(postId));
            });

            bg.add(deleteBtn, BorderLayout.CENTER);
            popup.setContentPane(bg);

            Point p = moreButton.getLocationOnScreen();
            popup.setLocation(p.x - popup.getWidth() + 35, p.y + moreButton.getHeight() + 4);

            popup.setVisible(true);

            AWTEventListener listener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                        Component clicked = me.getComponent();
                        if (SwingUtilities.isDescendingFrom(clicked, bg)) return;
                        popup.dispose();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    }
                }
            };

            Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK);

            popup.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
                }
            });
        });

        right.add(Box.createHorizontalGlue());
        right.add(deleteButton);
        right.add(Box.createHorizontalStrut(8));
        right.add(moreButton);

        card.add(right, BorderLayout.EAST);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        return card;
    }

    private int getPostLikeCount(int postId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM Post_Like WHERE post_id = ?"
        )) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /* ==============================
       좋아요 토글
       ============================== */
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

    /* ==============================
       게시글 삭제
       ============================== */
    private void deletePost(int postId) {
        String userId = mainFrame.getLoggedInUserId();
        String sql = "DELETE FROM Posts WHERE post_id = ? AND writer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setString(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                showRoundedMessage("Post deleted.");
                loadFeed();
            } else {
                showRoundedMessage("You can delete only your own posts.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showRoundedMessage("Error while deleting: " + e.getMessage());
        }
    }

    /* ==============================
       비밀번호 변경 다이얼로그
       ============================== */
    private void openChangePasswordDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Change Password", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(Color.WHITE);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);

        JLabel title = new JLabel("Change Password", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBounds(0, 20, 420, 30);
        dialog.add(title);

        JLabel curLabel = new JLabel("Current Password");
        curLabel.setFont(labelFont);
        curLabel.setBounds(40, 80, 200, 25);
        dialog.add(curLabel);

        JPasswordField curField = new JPasswordField();
        curField.setBounds(40, 110, 340, 40);
        curField.setBackground(new Color(245, 245, 245));
        curField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dialog.add(curField);

        JLabel newLabel = new JLabel("New Password");
        newLabel.setFont(labelFont);
        newLabel.setBounds(40, 160, 200, 25);
        dialog.add(newLabel);

        JPasswordField newField = new JPasswordField();
        newField.setBounds(40, 190, 340, 40);
        newField.setBackground(new Color(245, 245, 245));
        newField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dialog.add(newField);

        JLabel chkLabel = new JLabel("Check Password");
        chkLabel.setFont(labelFont);
        chkLabel.setBounds(40, 240, 200, 25);
        dialog.add(chkLabel);

        JPasswordField chkField = new JPasswordField();
        chkField.setBounds(40, 270, 340, 40);
        chkField.setBackground(new Color(245, 245, 245));
        chkField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dialog.add(chkField);

        JButton changeBtn = new JButton("change") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(152, 187, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));

                String text = "change";
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(text)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        changeBtn.setBounds(90, 330, 240, 55);
        changeBtn.setFocusPainted(false);
        changeBtn.setBorderPainted(false);
        changeBtn.setContentAreaFilled(false);
        changeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dialog.add(changeBtn);

        changeBtn.addActionListener(e -> {
            String curPw = new String(curField.getPassword());
            String newPw = new String(newField.getPassword());
            String chkPw = new String(chkField.getPassword());
            String uid = mainFrame.getLoggedInUserId();

            if (curPw.isEmpty() || newPw.isEmpty() || chkPw.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                return;
            }
            if (!newPw.equals(chkPw)) {
                JOptionPane.showMessageDialog(dialog, "New Password and Check Password do not match.");
                return;
            }

            try {
                String sqlCheck = "SELECT 1 FROM User WHERE user_id=? AND pwd=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                    ps.setString(1, uid);
                    ps.setString(2, curPw);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(dialog, "Current Password is incorrect.");
                            return;
                        }
                    }
                }

                String sqlUpdate = "UPDATE User SET pwd=? WHERE user_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, newPw);
                    ps.setString(2, uid);
                    ps.executeUpdate();
                }

                JOptionPane.showMessageDialog(dialog, "Password changed!");
                dialog.dispose();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "DB Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }


    /* ==============================
       HTML escape
       ============================== */
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

    private void showDeleteConfirmPopup(String message, Runnable onYes) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, (String) null, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(8, 12, 10, 12));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JButton closeBtn = new JButton("x");
        styleDialogButton(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());
        topRow.add(closeBtn, BorderLayout.EAST);
        root.add(topRow);

        JLabel msg = new JLabel(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);
        msgPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        msgPanel.add(msg, BorderLayout.CENTER);
        root.add(msgPanel);

        JComponent topLine = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(230, 1);
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(200, 200, 200));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        root.add(topLine);

        JPanel buttonRow = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(200, 200, 200));
                g.fillRect(getWidth() / 2 - 1, 0, 1, getHeight());
            }
        };
        buttonRow.setOpaque(false);
        buttonRow.setBorder(new EmptyBorder(4, 0, 2, 0));

        JButton yesBtn = new JButton("yes");
        styleDialogButton(yesBtn);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            onYes.run();
        });

        JButton noBtn = new JButton("no");
        styleDialogButton(noBtn);
        noBtn.addActionListener(e -> dialog.dispose());

        buttonRow.add(yesBtn);
        buttonRow.add(noBtn);
        root.add(buttonRow);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setSize(230, 120);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void styleDialogButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
