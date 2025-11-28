// MessagePanel.java
package twitter.gui;

import twitter.dao.MessageDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessagePanel extends JPanel {

    private final MainFrame mainFrame;
    private final MessageDAO messageDAO;

    private JTextField searchField;
    private JButton searchButton;

    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    // chat card
    private JLabel chatUserLabel;
    private JPanel chatContentPanel;
    private JScrollPane chatScrollPane;
    private JTextArea inputArea;
    private JButton sendButton;

    private String currentChatUserId;
    private java.util.List<String> lines = new ArrayList<>();


    public MessagePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.messageDAO = new MessageDAO(mainFrame.getConnection());

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createUserListPanel(), BorderLayout.WEST);
        add(createChatCardPanel(), BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadRecentUsers();
                showEmptyChat();
            }
        });
    }

    private JPanel createUserListPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(220, 0));
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        searchButton = new RoundedButton("search");
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.setMaximumSize(new Dimension(120, 36));


// ★★★ 입력창 기준 가운데 정렬
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);

// 버튼 안에 여백 넓게
        searchButton.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));


        top.add(Box.createVerticalStrut(5));
        top.add(searchField);
        top.add(Box.createVerticalStrut(8));
        top.add(searchButton);
        searchButton.setBorder(new EmptyBorder(3, 0, 3, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserCellRenderer());
        JScrollPane listScroll = new JScrollPane(userList);
        listScroll.setBorder(BorderFactory.createEmptyBorder());

        left.add(top, BorderLayout.NORTH);
        left.add(listScroll, BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String name = searchField.getText().trim();
            if (!name.isEmpty()) {
                openConversation(name);
            }
        });

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = userList.getSelectedValue();
                if (selected != null) {
                    openConversation(selected);
                }
            }
        });

        return left;
    }

    private JPanel createChatCardPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createEmptyChatPanel(), "EMPTY");
        cardPanel.add(createChatPanel(), "CHAT");

        return cardPanel;
    }

    private JPanel createEmptyChatPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        JPanel center = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int size = 160;

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = (getWidth() - size) / 2;
                int y = 0;     // 🔥 그림을 패널 맨 위에 그리기

                g2.setColor(new Color(230, 230, 230));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, size, size);

                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
                FontMetrics fm = g2.getFontMetrics();
                String bird = "🐦";
                int tx = x + (size - fm.stringWidth(bird)) / 2;
                int ty = y + (size + fm.getAscent()) / 2 - 10;
                g2.drawString(bird, tx, ty);
            }
        };

        // 🔥 그림 패널이 너무 크게 공간 차지하지 않도록
        center.setMaximumSize(new Dimension(2000, 180));
        center.setPreferredSize(new Dimension(200, 180));
        center.setOpaque(false);
        center.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("send some message to your friend!");
        msg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalStrut(40));
        p.add(center);
        p.add(Box.createVerticalStrut(5));   // 🔥 그림과 문장 사이 최소 간격
        p.add(msg);

        return p;
    }


    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 20, 10, 20));
        header.setBackground(Color.WHITE);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(238, 238, 238));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(false);

        chatUserLabel = new JLabel("");
        chatUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatUserLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        header.add(avatar, BorderLayout.WEST);
        header.add(chatUserLabel, BorderLayout.CENTER);

        chatContentPanel = new JPanel();
        chatContentPanel.setLayout(new BoxLayout(chatContentPanel, BoxLayout.Y_AXIS));
        chatContentPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatContentPanel);
        chatScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        inputPanel.setBackground(Color.WHITE);

        inputArea = new JTextArea(2, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(220, 220, 220)));

// ✔ send 버튼
        sendButton = new RoundedButton("send");
        sendButton.setPreferredSize(new Dimension(90, 40));
        sendButton.setMinimumSize(new Dimension(90, 40));
        sendButton.setMaximumSize(new Dimension(90, 40));
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel sendWrapper = new JPanel(new BorderLayout());
        sendWrapper.setOpaque(false);
        sendWrapper.setBorder(new EmptyBorder(0, 3, 0, 0));  // 왼쪽 3px 여백
        sendWrapper.add(sendButton, BorderLayout.CENTER);

// ✔ ★★★ 여기 매우 중요 — inputScroll 을 반드시 CENTER에 먼저 추가!!!
        inputPanel.add(inputScroll, BorderLayout.CENTER);

// ✔ send 버튼을 오른쪽에 추가
        inputPanel.add(sendWrapper, BorderLayout.EAST);

        sendButton.addActionListener(e -> sendCurrentMessage());

// chatPanel에 부착
        chatPanel.add(header, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        return chatPanel;

    }

    private void loadRecentUsers() {
        String myId = mainFrame.getLoggedInUserId();
        if (myId == null) return;

        userListModel.clear();
        try {
            List<String> users = messageDAO.getRecentChatUsers(myId);
            for (String u : users) {
                userListModel.addElement(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openConversation(String otherUserId) {
        String myId = mainFrame.getLoggedInUserId();
        if (myId == null || otherUserId == null || otherUserId.isEmpty()) {
            return;
        }

        currentChatUserId = otherUserId;
        chatUserLabel.setText(otherUserId);
        cardLayout.show(cardPanel, "CHAT");

        chatContentPanel.removeAll();

        try {
            java.util.List<MessageDAO.ChatMessage> messages =
                    messageDAO.getConversation(myId, otherUserId);

            for (MessageDAO.ChatMessage m : messages) {
                boolean fromMe = myId.equals(m.senderId);
                MessageBubble bubble = new MessageBubble(m.content, fromMe);
                Dimension pref = bubble.getPreferredSize();
                bubble.setMaximumSize(pref);
                bubble.setMinimumSize(pref);

                JPanel row = new JPanel();
                row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setBorder(new EmptyBorder(5, 10, 5, 10));

                if (fromMe) {
                    row.add(Box.createHorizontalGlue());  // 오른쪽 정렬
                    row.add(bubble);
                } else {
                    row.add(bubble);
                    row.add(Box.createHorizontalGlue());  // 왼쪽 정렬
                }

// ✔ 여기! chatContentPanel에 추가해야 함
                chatContentPanel.add(row);
                chatContentPanel.add(Box.createVerticalStrut(8));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        chatContentPanel.add(Box.createVerticalStrut(10));
        chatContentPanel.revalidate();
        chatContentPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void showEmptyChat() {
        currentChatUserId = null;
        chatUserLabel.setText("");
        cardLayout.show(cardPanel, "EMPTY");
    }

    private void sendCurrentMessage() {
        String myId = mainFrame.getLoggedInUserId();
        if (myId == null) return;

        if (currentChatUserId == null || currentChatUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user or search first.");
            return;
        }

        String text = inputArea.getText().trim();
        if (text.isEmpty()) return;

        try {
            messageDAO.sendMessage(myId, currentChatUserId, text);
            inputArea.setText("");
            openConversation(currentChatUserId);
            loadRecentUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while sending message: " + e.getMessage());
        }
    }

    private static class UserCellRenderer extends JPanel implements ListCellRenderer<String> {

        private final JPanel avatar;
        private final JLabel nameLabel;

        public UserCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(6, 8, 6, 8));

            avatar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new Color(238, 238, 238));
                    int d = Math.min(getWidth(), getHeight());
                    g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
                }
            };
            avatar.setPreferredSize(new Dimension(32, 32));
            avatar.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

            add(avatar, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                                                      String value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            nameLabel.setText(value);

            if (isSelected) {
                setBackground(new Color(230, 240, 255));
            } else {
                setBackground(Color.WHITE);
            }
            setOpaque(true);
            return this;
        }
    }
    /* ===============================
        채팅 말풍선(MessageBubble)
   =============================== */
    private static class MessageBubble extends JComponent {

        private static final int MAX_WIDTH = 260;
        private static final int MIN_HEIGHT = 40;
        private static final int PADDING = 12;

        private final String text;
        private final boolean fromMe;
        private java.util.List<String> lines;

        public MessageBubble(String text, boolean fromMe) {
            this.text = text;
            this.fromMe = fromMe;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setOpaque(false);
            computeLines();
        }

        private void computeLines() {
            lines = new ArrayList<>();
            FontMetrics fm = getFontMetrics(getFont());
            int maxTextWidth=176;

            String[] hardLines = text.split("\n");

            for (String hl : hardLines) {
                String[] words = hl.split(" ");
                StringBuilder sb = new StringBuilder();

                for (String w : words) {
                    while (fm.stringWidth(w) > maxTextWidth) {
                        int cut = 1;
                        while (cut < w.length() && fm.stringWidth(w.substring(0, cut)) < maxTextWidth) {
                            cut++;
                        }

                        lines.add(w.substring(0, cut - 1));
                        w = w.substring(cut - 1); // 남은 부분 계속 처리
                    }

                    // 2) 현재 줄에 단어 추가 시 길이 초과 → 새 줄
                    if (fm.stringWidth(sb.toString() + w) > maxTextWidth) {
                        lines.add(sb.toString());
                        sb = new StringBuilder();
                    }

                    sb.append(w).append(" ");
                }

                if (!sb.isEmpty())
                    lines.add(sb.toString());
            }

            if (lines.isEmpty())
                lines.add("");
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int lineHeight = fm.getHeight();

            int height = lines.size() * lineHeight + PADDING * 2;
            height = Math.max(height, MIN_HEIGHT);

            int width = 0;
            for (String line : lines) {
                width = Math.max(width, fm.stringWidth(line));
            }

            width += PADDING * 2;
            width = Math.min(width, MAX_WIDTH);  // 최대 폭 제한

            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(fromMe ? new Color(152, 187, 255) : new Color(240, 240, 240));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(Color.BLACK);
            FontMetrics fm = g2.getFontMetrics();
            int y = PADDING + fm.getAscent();

            for (String line : lines) {
                g2.drawString(line, PADDING, y);
                y += fm.getHeight();
            }
        }
    }

}
