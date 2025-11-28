package twitter.gui;

import twitter.dao.PostDAO;
import twitter.dao.CommentDAO;
import twitter.dao.LikeDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 트위터 스타일의 Post Detail 화면
 *  - 상단 ← post 헤더
 *  - 상단에 원글 카드 (좋아요 / 댓글 수)
 *  - 아래로 댓글 + 대댓글 카드 리스트
 *  - 맨 아래 댓글 작성 입력창 + post 버튼
 */
public class PostDetailPanel extends JPanel {

    private final MainFrame mainFrame;
    private final PostDAO postDAO;
    private final CommentDAO commentDAO;
    private final LikeDAO likeDAO;
    private final Connection conn;

    // 현재 보고 있는 게시글 ID
    private int currentPostId = -1;

    // ==== 상단 원글 카드 UI ====
    private JLabel postWriterLabel;
    private JLabel postTimeLabel;
    private JTextArea postContentArea;
    private JButton postLikeButton;
    private JLabel postLikeCountLabel;
    private JLabel postCommentCountLabel;

    // ==== 댓글 리스트 ====
    private JPanel contentPanel;        // 원글 + 댓글 전체를 담는 패널 (스크롤 안)
    private JPanel commentListPanel;    // 댓글/대댓글 카드들이 들어가는 패널

    // ==== 하단 댓글 작성 ====
    private JTextArea commentInputArea;
    private JButton commentPostButton;

    // 대댓글 reply 상태
    private Integer replyingToCommentId = null;
    private String replyingToUser = null;

    public PostDetailPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.conn = mainFrame.getConnection();
        this.postDAO = new PostDAO(conn);
        this.commentDAO = new CommentDAO(conn);
        this.likeDAO = new LikeDAO(conn);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 헤더
        add(createHeaderBar(), BorderLayout.NORTH);

        // 중앙(원글 + 댓글 리스트)
        add(createCenterArea(), BorderLayout.CENTER);

        // 하단 댓글 작성 영역
        add(createBottomComposer(), BorderLayout.SOUTH);
    }

    /* ================================
       상단 ← post 헤더
       ================================ */
    private JComponent createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JButton backBtn = new JButton("←");
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setMargin(new Insets(0, 15, 0, 15));

        backBtn.addActionListener(e -> {
            String last = mainFrame.getLastPanel();
            mainFrame.showPanel(last);
        });

        JLabel title = new JLabel("post");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 10, 0, 0));

        header.add(backBtn, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);

        return header;
    }

    /* ================================
       중앙: 원글 카드 + 댓글 리스트
       ================================ */
    private JComponent createCenterArea() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // 원글 카드
        JPanel postCard = createPostCardPanel();
        contentPanel.add(postCard);
        contentPanel.add(Box.createVerticalStrut(10));

        // 댓글 리스트 패널
        commentListPanel = new JPanel();
        commentListPanel.setLayout(new BoxLayout(commentListPanel, BoxLayout.Y_AXIS));
        commentListPanel.setBackground(Color.WHITE);

        contentPanel.add(commentListPanel);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return scroll;
    }

    /* ================================
       원글 카드 UI
       ================================ */
    private JPanel createPostCardPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(10, 20, 10, 20));

        // 왼쪽 아바타
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(70, 70));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(240, 240, 240));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(50, 50));
        avatar.setOpaque(false);
        left.add(avatar);

        card.add(left, BorderLayout.WEST);

        // 가운데: 작성자, 내용, 액션
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        postWriterLabel = new JLabel("User Name");
        postWriterLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        postTimeLabel = new JLabel("");
        postTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        postTimeLabel.setForeground(Color.GRAY);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.add(postWriterLabel, BorderLayout.WEST);
        topRow.add(postTimeLabel, BorderLayout.EAST);

        center.add(topRow);

        postContentArea = new JTextArea();
        postContentArea.setEditable(false);
        postContentArea.setLineWrap(true);
        postContentArea.setWrapStyleWord(true);
        postContentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        postContentArea.setOpaque(false);
        postContentArea.setBorder(new EmptyBorder(5, 0, 5, 0));
        postContentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        postContentArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        center.add(postContentArea);

        // 좋아요 / 댓글 수 줄
        JPanel actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setBackground(Color.WHITE);

        postLikeButton = new JButton("♡");
        postLikeButton.setFocusPainted(false);
        postLikeButton.setContentAreaFilled(false);
        postLikeButton.setBorder(BorderFactory.createEmptyBorder());
        postLikeButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        postLikeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        postLikeButton.addActionListener(e -> togglePostLike());

        postLikeCountLabel = new JLabel("0");
        postLikeCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton commentIcon = new JButton("▢");
        commentIcon.setFocusPainted(false);
        commentIcon.setContentAreaFilled(false);
        commentIcon.setBorder(BorderFactory.createEmptyBorder());
        commentIcon.setFont(new Font("Dialog", Font.PLAIN, 14));
        commentIcon.setEnabled(false); // 아이콘 용도

        postCommentCountLabel = new JLabel("0");
        postCommentCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        actionRow.add(postLikeButton);
        actionRow.add(Box.createHorizontalStrut(4));
        actionRow.add(postLikeCountLabel);
        actionRow.add(Box.createHorizontalStrut(18));
        actionRow.add(commentIcon);
        actionRow.add(Box.createHorizontalStrut(4));
        actionRow.add(postCommentCountLabel);

        center.add(actionRow);

        card.add(center, BorderLayout.CENTER);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    /* ================================
       하단 댓글 작성 영역
       ================================ */
    private JComponent createBottomComposer() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        // 왼쪽 아바타
        JPanel avatarWrapper = new JPanel();
        avatarWrapper.setOpaque(false);
        avatarWrapper.setBorder(new EmptyBorder(10, 20, 10, 10));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(240, 240, 240));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(false);

        avatarWrapper.add(avatar);
        bottom.add(avatarWrapper, BorderLayout.WEST);

        // 중앙: 입력창
        commentInputArea = new JTextArea(2, 20);
        commentInputArea.setLineWrap(true);
        commentInputArea.setWrapStyleWord(true);
        commentInputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        commentInputArea.setBorder(new EmptyBorder(15, 5, 5, 5));
        commentInputArea.setText(" ");
        commentInputArea.setForeground(new Color(180, 180, 180));

        // placeholder 처리
        commentInputArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (commentInputArea.getForeground().equals(new Color(180, 180, 180))) {
                    commentInputArea.setText("");
                    commentInputArea.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (commentInputArea.getText().trim().isEmpty()) {
                    commentInputArea.setText(" ");
                    commentInputArea.setForeground(new Color(180, 180, 180));
                }
            }
        });

        bottom.add(commentInputArea, BorderLayout.CENTER);

        // 오른쪽: post 버튼 (파란 pill)
        commentPostButton = new JButton("post") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(152, 187, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String text = "post";
                int tx = (getWidth() - fm.stringWidth(text)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);

                g2.dispose();
            }
        };
        commentPostButton.setPreferredSize(new Dimension(90, 40));
        commentPostButton.setContentAreaFilled(false);
        commentPostButton.setBorderPainted(false);
        commentPostButton.setFocusPainted(false);
        commentPostButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        commentPostButton.addActionListener(e -> addNewComment());

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 0, 10, 20));
        right.add(commentPostButton, BorderLayout.SOUTH);

        bottom.add(right, BorderLayout.EAST);

        return bottom;
    }

    /* ================================
       외부에서 호출: 특정 post 로딩
       ================================ */
    public void loadPostDetails(int postId) {
        this.currentPostId = postId;
        String myId = mainFrame.getLoggedInUserId();
        if (myId == null) return;

        loadPostCard();
        loadComments();
        revalidate();
        repaint();
    }

    /* ================================
       원글 데이터 로딩
       ================================ */
    private void loadPostCard() {
        if (currentPostId == -1) return;

        try (ResultSet rsPost = postDAO.getPostById(currentPostId)) {
            if (rsPost.next()) {
                String writer = rsPost.getString("writer_id");
                String createdAt = rsPost.getString("created_at");
                String content = rsPost.getString("content");

                postWriterLabel.setText(writer);
                postTimeLabel.setText(createdAt);
                postContentArea.setText(content);

                // 좋아요 수
                int likeCount = likeDAO.getPostLikeCount(currentPostId);
                postLikeCountLabel.setText(String.valueOf(likeCount));

                // 댓글 수는 loadComments()에서 계산 후 설정
            } else {
                postContentArea.setText("Failed to load the post.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            postContentArea.setText("DB Error (post): " + e.getMessage());
        }

        // 좋아요 상태에 따라 아이콘 색/문자 변경
        boolean liked = isPostLikedByCurrentUser(currentPostId);
        if (liked) {
            postLikeButton.setText("♥");
            postLikeButton.setForeground(Color.RED);
        } else {
            postLikeButton.setText("♡");
            postLikeButton.setForeground(Color.BLACK);
        }
    }

    /* ================================
       댓글 + 대댓글 로딩
       ================================ */
    private void loadComments() {
        commentListPanel.removeAll();
        if (currentPostId == -1) return;

        int topLevelCommentCount = 0;

        try (ResultSet rsComments = commentDAO.getCommentsByPost(currentPostId)) {
            if (!rsComments.isBeforeFirst()) {
                JLabel empty = new JLabel("(No comments yet.)");
                empty.setBorder(new EmptyBorder(10, 20, 10, 0));
                commentListPanel.add(empty);
            } else {
                while (rsComments.next()) {
                    topLevelCommentCount++;

                    int commentId = rsComments.getInt("comment_id");
                    String writer = rsComments.getString("writer_id");
                    String createdAt = rsComments.getString("created_at");
                    String content = rsComments.getString("content");
                    int likeCount = likeDAO.getCommentLikeCount(commentId);

                    JPanel commentCard = createCommentCard(commentId, writer, createdAt, content, likeCount);
                    commentListPanel.add(commentCard);

                    // 대댓글들
                    int replyCount = 0;
                    try (ResultSet rsChild = commentDAO.getChildComments(commentId)) {
                        while (rsChild.next()) {
                            replyCount++;
                            int childId = rsChild.getInt("child_id");
                            String childWriter = rsChild.getString("writer_id");
                            String childCreated = rsChild.getString("created_at");
                            String childContent = rsChild.getString("content");
                            int childLikeCount = likeDAO.getChildCommentLikeCount(childId);

                            JPanel childCard = createChildCommentCard(childId, childWriter, childCreated,
                                    childContent, childLikeCount);
                            commentListPanel.add(childCard);
                        }
                    }

                    // 댓글 카드의 replyCountLabel 업데이트
                    JLabel replyCountLabel = (JLabel) commentCard.getClientProperty("replyCountLabel");
                    if (replyCountLabel != null) {
                        replyCountLabel.setText(String.valueOf(replyCount));
                    }

                    commentListPanel.add(Box.createVerticalStrut(5));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JLabel err = new JLabel("Error loading comments: "  + e.getMessage());
            err.setForeground(Color.RED);
            err.setBorder(new EmptyBorder(10, 20, 10, 0));
            commentListPanel.add(err);
        }

        postCommentCountLabel.setText(String.valueOf(topLevelCommentCount));

        commentListPanel.revalidate();
        commentListPanel.repaint();
    }

    private void prepareReply(String writer, int commentId) {
        replyingToCommentId = commentId;
        replyingToUser = writer;

        commentInputArea.setForeground(Color.BLACK);
        commentInputArea.setText("@" + writer + " ");
        commentInputArea.requestFocus();
    }

    /* ================================
       댓글 카드
       ================================ */
    private JPanel createCommentCard(int commentId,
                                     String writer,
                                     String createdAt,
                                     String content,
                                     int likeCount) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(8, 12, 8, 12));

        String myId = mainFrame.getLoggedInUserId();

        // LEFT Avatar
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(45, 60));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0xEEEEEE));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };

        avatar.setPreferredSize(new Dimension(42, 42));
        avatar.setOpaque(false);
        left.add(avatar);

        card.add(left, BorderLayout.WEST);

        // CENTER
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(0, 5, 0, 0));
        center.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ===== top row: writer + time + (…) menu =====
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel writerLabel = new JLabel(writer);
        writerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel timeLabel = new JLabel(createdAt);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        topRow.add(writerLabel, BorderLayout.WEST);
        topRow.add(timeLabel, BorderLayout.EAST);

        // ===== Delete menu (only for my own comment) =====
        if (myId != null && myId.equals(writer)) {

            JButton menuBtn = new JButton("⋯");
            menuBtn.setFocusPainted(false);
            menuBtn.setContentAreaFilled(false);
            menuBtn.setBorder(BorderFactory.createEmptyBorder());
            menuBtn.setFont(new Font("Dialog", Font.BOLD, 16));
            menuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            menuBtn.addActionListener(e -> {

                // 🔥 작은 모서리 둥근 흰색 팝업
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
                    showDeleteConfirmPopup("delete this comment?", () -> {
                        try {
                            commentDAO.deleteComment(commentId, myId);
                            loadPostDetails(currentPostId);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });
                });

                bg.add(deleteBtn, BorderLayout.CENTER);
                popup.setContentPane(bg);

                // 🔥 … 버튼 바로 아래 위치
                Point p = menuBtn.getLocationOnScreen();
                popup.setLocation(p.x - popup.getWidth() + 30, p.y + menuBtn.getHeight() + 4);

                popup.setVisible(true);

                // 🔥 다른 곳 클릭하면 자동 닫기
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

            topRow.add(menuBtn, BorderLayout.EAST);
        }
        center.add(topRow);

        // ===== Content =====
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(5, 0, 5, 0));
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(contentArea);

        // ===== Action Row =====
        JPanel actionRow = new JPanel();
        actionRow.setLayout(new BoxLayout(actionRow, BoxLayout.X_AXIS));
        actionRow.setBackground(Color.WHITE);

        JButton heartBtn = new JButton("♥");
        heartBtn.setFocusPainted(false);
        heartBtn.setContentAreaFilled(false);
        heartBtn.setBorder(BorderFactory.createEmptyBorder());
        heartBtn.setFont(new Font("Dialog", Font.PLAIN, 14));

        JLabel likeLabel = new JLabel(" " + likeCount);
        heartBtn.addActionListener(e -> likeComment(commentId));

        JButton replyBtn = new JButton("▢");
        replyBtn.setFocusPainted(false);
        replyBtn.setContentAreaFilled(false);
        replyBtn.setBorder(BorderFactory.createEmptyBorder());
        replyBtn.setFont(new Font("Dialog", Font.PLAIN, 14));
        replyBtn.addActionListener(e -> prepareReply(writer, commentId));

        JLabel replyCountLabel = new JLabel("0");

        actionRow.add(heartBtn);
        actionRow.add(likeLabel);
        actionRow.add(Box.createHorizontalStrut(15));
        actionRow.add(replyBtn);
        actionRow.add(replyCountLabel);

        center.add(actionRow);

        card.putClientProperty("replyCountLabel", replyCountLabel);

        card.add(center, BorderLayout.CENTER);
        int minHeight = 90; // 댓글 최소 높이
        int contentHeight = contentArea.getPreferredSize().height;
        int totalHeight = Math.max(minHeight, contentHeight + 45);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalHeight));

        return card;
    }

    /* ================================
       대댓글 카드
       ================================ */
    private JPanel createChildCommentCard(int childId,
                                          String writer,
                                          String createdAt,
                                          String content,
                                          int likeCount) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(0, 60, 10, 20));

        String myId = mainFrame.getLoggedInUserId();

        // LEFT Avatar
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(50, 50));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0xEEEEEE));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        left.add(avatar);
        card.add(left, BorderLayout.WEST);

        // CENTER
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel writerLabel = new JLabel(writer);
        writerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel timeLabel = new JLabel(createdAt);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        topRow.add(writerLabel, BorderLayout.WEST);
        topRow.add(timeLabel, BorderLayout.EAST);

        // ===== Delete menu =====
        if (myId != null && myId.equals(writer)) {

            JButton menuBtn = new JButton("⋯");
            menuBtn.setFocusPainted(false);
            menuBtn.setContentAreaFilled(false);
            menuBtn.setBorder(BorderFactory.createEmptyBorder());
            menuBtn.setFont(new Font("Dialog", Font.BOLD, 16));
            menuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            menuBtn.addActionListener(e -> {

                // 🔥 작은 둥근 흰색 팝업
                JDialog popup = new JDialog(SwingUtilities.getWindowAncestor(this));
                popup.setUndecorated(true);
                popup.setSize(110, 45);   // 대댓글도 댓글과 동일한 크기
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
                    showDeleteConfirmPopup("delete this reply?", () -> {
                        try {
                            commentDAO.deleteChildComment(childId, myId);
                            loadPostDetails(currentPostId);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });
                });

                bg.add(deleteBtn, BorderLayout.CENTER);
                popup.setContentPane(bg);

                // 🔥 “…” 버튼 바로 아래에 뜨도록 조정
                Point p = menuBtn.getLocationOnScreen();
                popup.setLocation(p.x - popup.getWidth() + 30, p.y + menuBtn.getHeight() + 4);

                popup.setVisible(true);

                // 🔥 팝업 외부 클릭 시 자동 닫힘(전역 이벤트)
                AWTEventListener listener = new AWTEventListener() {
                    @Override
                    public void eventDispatched(AWTEvent event) {
                        if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {

                            Component clicked = me.getComponent();

                            // 팝업 내부 클릭이면 유지
                            if (SwingUtilities.isDescendingFrom(clicked, bg)) return;

                            // 아니면 닫기
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

            topRow.add(menuBtn, BorderLayout.EAST);
        }

        center.add(topRow);

        // Content
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(4, 0, 4, 0));
        center.add(contentArea);

        // Action
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionRow.setOpaque(false);

        JButton heartBtn = new JButton("♥");
        heartBtn.setFocusPainted(false);
        heartBtn.setContentAreaFilled(false);
        heartBtn.setBorder(BorderFactory.createEmptyBorder());
        heartBtn.setFont(new Font("Dialog", Font.PLAIN, 13));

        JLabel likeLabel = new JLabel(" " + likeCount);
        heartBtn.addActionListener(e -> likeChildComment(childId));

        actionRow.add(heartBtn);
        actionRow.add(likeLabel);

        center.add(actionRow);

        card.add(center, BorderLayout.CENTER);
        int minHeight = 90; // 댓글 최소 높이
        int contentHeight = contentArea.getPreferredSize().height;
        int totalHeight = Math.max(minHeight, contentHeight + 45);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalHeight));
        return card;
    }

    /* ================================
       새 댓글 작성 (하단 post 버튼)
       ================================ */
    private void addNewComment() {
        if (currentPostId == -1) return;

        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return;

        String text = commentInputArea.getText().trim();
        if (text.isEmpty()) return;

        try {
            if (replyingToCommentId != null &&
                    text.startsWith("@" + replyingToUser)) {

                // 대댓글
                commentDAO.writeChildComment(replyingToCommentId, userId,
                        text.substring(replyingToUser.length() + 2));
            } else {
                // 일반 댓글
                commentDAO.writeComment(currentPostId, userId, text);
            }

            // 초기화
            replyingToCommentId = null;
            replyingToUser = null;
            commentInputArea.setText(" ");
            commentInputArea.setForeground(new Color(180, 180, 180));

            loadPostDetails(currentPostId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ================================
       댓글 좋아요 (♥)
       ================================ */
    private void likeComment(int commentId) {
        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return;

        try {
            boolean alreadyLiked = likeDAO.isCommentLiked(userId, commentId);
            boolean likedNow = !alreadyLiked;

            if (likedNow) {
                likeDAO.likeComment(userId, commentId);
            } else {
                likeDAO.unlikeComment(userId, commentId);
            }

            loadPostDetails(currentPostId);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error processing comment like: " + e.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================================
       대댓글 좋아요 (♥)
       ================================ */
    private void likeChildComment(int childId) {
        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return;

        try {
            boolean alreadyLiked = likeDAO.isChildCommentLiked(userId, childId);
            boolean likedNow = !alreadyLiked;

            if (likedNow) {
                likeDAO.likeChildComment(userId, childId);
            } else {
                likeDAO.unlikeChildComment(userId, childId);
            }

            loadPostDetails(currentPostId);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error processing reply like: " + e.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================================
       게시글 좋아요 토글 (상단 ♥)
       ================================ */
    private boolean isPostLikedByCurrentUser(int postId) {
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

    private void togglePostLike() {
        if (currentPostId == -1) return;
        String userId = mainFrame.getLoggedInUserId();
        if (userId == null) return;

        boolean alreadyLiked = isPostLikedByCurrentUser(currentPostId);

        try {
            if (alreadyLiked) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Post_Like WHERE post_id = ? AND liker_id = ?")) {
                    ps.setInt(1, currentPostId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Post_Like(post_id, liker_id) VALUES(?, ?)")) {
                    ps.setInt(1, currentPostId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
            }

            // 좋아요 수 다시 계산
            int newCount = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM Post_Like WHERE post_id = ?")) {
                ps.setInt(1, currentPostId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) newCount = rs.getInt(1);
                }
            }
            postLikeCountLabel.setText(String.valueOf(newCount));

            // 버튼 상태 업데이트
            boolean likedNow = !alreadyLiked;
            if (likedNow) {
                postLikeButton.setText("♥");
                postLikeButton.setForeground(Color.RED);
            } else {
                postLikeButton.setText("♡");
                postLikeButton.setForeground(Color.BLACK);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing post like: " + e.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ================================
       공통: 둥근 삭제 확인 팝업
       ================================ */
    private void showDeleteConfirmPopup(String message, Runnable onYes) {

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, (String) null, Dialog.ModalityType.APPLICATION_MODAL);
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

        // 상단 X
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JButton closeBtn = new JButton("x");
        styleDialogButton(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());

        topRow.add(closeBtn, BorderLayout.EAST);
        root.add(topRow);

        // 메시지
        JLabel msg = new JLabel(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);
        msgPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        msgPanel.add(msg, BorderLayout.CENTER);
        root.add(msgPanel);

        // 가로 구분선
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

        // 버튼 영역 (yes / No + 세로선)
        JPanel buttonRow = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(200, 200, 200));
                int x = getWidth() / 2;
                g.fillRect(x - 1, 0, 1, getHeight());
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

        JButton noBtn = new JButton("No");
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
