package twitter.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SidebarPanel extends JPanel {

    private final MainFrame mainFrame;
    private JLabel profileNameLabel;

    public SidebarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setPreferredSize(new Dimension(180, 0));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230,230,230)));

        add(createTopMenu(), BorderLayout.NORTH);
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(Color.WHITE);
        bottomWrapper.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230,230,230)));
        bottomWrapper.add(createBottomProfile(), BorderLayout.CENTER);
        add(bottomWrapper, BorderLayout.SOUTH);
    }

    class RoundedPanel extends JPanel {
        private int radius = 20;

        public RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(180, 180, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            super.paintComponent(g2);
        }
    }


    /* ------------------------
       상단 메뉴 영역
       ------------------------ */
    private JPanel createTopMenu() {
        JPanel top = new JPanel();
        top.setBackground(Color.WHITE);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel bird = new JLabel("🐦");
        bird.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        bird.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(bird);
        top.add(Box.createVerticalStrut(25));

        top.add(makeMenuLabel("Home"));
        top.add(Box.createVerticalStrut(15));
        top.add(makeMenuLabel("Explore"));
        top.add(Box.createVerticalStrut(15));
        top.add(makeMenuLabel("Message"));
        top.add(Box.createVerticalStrut(15));
        top.add(makeMenuLabel("People"));

        return top;
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
            case "Home" -> mainFrame.showPanel("mainFeed");
            case "Explore" -> mainFrame.showPanel("explorePanel");
            case "Message" -> mainFrame.showPanel("messagePanel");
            case "People" -> mainFrame.showPanel("userActionPanel");
        }
    }

    /* ------------------------
       하단 프로필 영역
       ------------------------ */
    private JPanel createBottomProfile() {
        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(0, 20, 20, 20));

        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        profilePanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0xEEEEEE));
                int d = Math.min(getWidth(), getHeight());
                g.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(false);

        profileNameLabel = new JLabel();
        profileNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profileNameLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        updateProfileName();

        // 클릭 시 팝업(비번 변경 + 로그아웃)
        MouseAdapter profileClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfileMenu(e.getComponent(), e.getX(), e.getY());
            }
        };
        profilePanel.addMouseListener(profileClick);
        avatar.addMouseListener(profileClick);
        profileNameLabel.addMouseListener(profileClick);

        profilePanel.add(avatar, BorderLayout.WEST);
        profilePanel.add(profileNameLabel, BorderLayout.CENTER);

        bottom.add(Box.createVerticalGlue());
        bottom.add(profilePanel);

        return bottom;
    }

    /* ------------------------
       프로필 메뉴
       ------------------------ */
    private void showProfileMenu(Component invoker, int x, int y) {

        JWindow popup = new JWindow();
        popup.setBackground(new Color(0,0,0,0)); // 투명 배경

        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(8, 15, 8, 15));

        // 버튼 공통 스타일
        Font font = new Font("Segoe UI", Font.PLAIN, 13);

        JButton changePwBtn = new JButton("Change Password");
        changePwBtn.setFont(font);
        changePwBtn.setFocusPainted(false);
        changePwBtn.setBorderPainted(false);
        changePwBtn.setContentAreaFilled(false);
        changePwBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changePwBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePwBtn.setMaximumSize(new Dimension(150, 28)); // 더 작은 버튼

        changePwBtn.addActionListener(e -> {
            popup.dispose();
            openChangePasswordDialog();
        });

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setFont(font);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(150, 28)); // 더 작게

        logoutBtn.addActionListener(e -> {
            showLogoutConfirmPopup();
        });

        // 구분선 직접 그리기
        JPanel divider = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(200,200,200));
                g.fillRect(0, getHeight()/2, getWidth(), 1);
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        panel.add(changePwBtn);
        panel.add(divider);
        panel.add(logoutBtn);

        popup.add(panel);
        popup.pack();

        // 화면 위치 계산 (프로필 클릭 위치 바로 위에)
        Point loc = invoker.getLocationOnScreen();
        popup.setLocation(loc.x, loc.y - popup.getHeight() - 5);

        popup.setVisible(true);

        // 팝업 외부 클릭 시 자동 닫기
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                if (!panel.contains(SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), panel))) {
                    popup.dispose();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }


    /* ------------------------
       비밀번호 변경 다이얼로그
       ------------------------ */
    private void openChangePasswordDialog() {
        Connection conn = mainFrame.getConnection();
        String uid = mainFrame.getLoggedInUserId();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Change Password", true);
        dialog.setSize(420, 420);
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

        JButton changeBtn = new JButton("change");
        changeBtn.setBounds(90, 330, 240, 50);
        changeBtn.setFocusPainted(false);
        changeBtn.setBackground(new Color(152, 187, 255));
        changeBtn.setForeground(Color.WHITE);
        changeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        changeBtn.setBorderPainted(false);
        dialog.add(changeBtn);

        changeBtn.addActionListener(e -> {
            String curPw = new String(curField.getPassword());
            String newPw = new String(newField.getPassword());
            String chkPw = new String(chkField.getPassword());

            if (curPw.isEmpty() || newPw.isEmpty() || chkPw.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                return;
            }

            if (!newPw.equals(chkPw)) {
                JOptionPane.showMessageDialog(dialog, "New Password and Check Password do not match.");
                return;
            }

            try {
                // 현재 PW 확인
                String sqlCheck = "SELECT 1 FROM User WHERE user_id=? AND pwd=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                    ps.setString(1, uid);
                    ps.setString(2, curPw);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(dialog, "Current Password is incorrect.");
                        return;
                    }
                }

                // 변경 수행
                String sqlUpdate = "UPDATE User SET pwd=? WHERE user_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, newPw);
                    ps.setString(2, uid);
                    ps.executeUpdate();
                }

                dialog.dispose();
                showPasswordChangedPopup();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "DB Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }
    private void showPasswordChangedPopup() {

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(230, 120);
        dialog.setLocationRelativeTo(owner);

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

        // 메시지
        JLabel msg = new JLabel("Password has been changed!", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);
        msgPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        msgPanel.add(msg, BorderLayout.CENTER);

        root.add(msgPanel);

        // 가로 구분선
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

        // yes 버튼
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
        dialog.setVisible(true);
    }

    // SidebarPanel 안에 넣기 (기존 showLogoutConfirmPopup 대체)
    private void showLogoutConfirmPopup() {

        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner, (String) null, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);          // 제목줄 없음
        dialog.setBackground(new Color(0,0,0,0));

        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 흰색 둥근 네모
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                // 테두리
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(8, 12, 10, 12));

        /* ---------- 상단 X 버튼 (오른쪽 위) ---------- */
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JButton closeBtn = new JButton("x");
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        topRow.add(closeBtn, BorderLayout.EAST);
        root.add(topRow);

        /* ---------- 질문 텍스트 ---------- */
        JLabel msg = new JLabel("log out?");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setOpaque(false);
        msgPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        msgPanel.add(msg, BorderLayout.CENTER);

        root.add(msgPanel);

        /* ---------- 상단 가로 구분선 ---------- */
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

        /* ---------- yes / No 버튼 + 가운데 세로선 ---------- */
        JPanel buttonRow = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 가운데 세로 구분선
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
            mainFrame.setLoggedInUserId(null);
            mainFrame.showPanel("login");
        });

        JButton noBtn = new JButton("No");
        styleDialogButton(noBtn);
        noBtn.addActionListener(e -> dialog.dispose());

        buttonRow.add(yesBtn);
        buttonRow.add(noBtn);

        root.add(buttonRow);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setSize(230, 120);  // 크기 살짝 고정해서 더 작게

        // 🔹 메인 프레임 기준 중앙 배치
        dialog.setLocationRelativeTo(owner);

        dialog.setVisible(true);
    }

    // 공통 버튼 스타일
    private void styleDialogButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* ------------------------
       로그인한 User ID 표시 갱신
       ------------------------ */
    public void updateProfileName() {
        String uid = mainFrame.getLoggedInUserId();
        if (uid == null || uid.isEmpty()) {
            profileNameLabel.setText("Not logged in");
        } else {
            profileNameLabel.setText(uid);
        }

    }
}
