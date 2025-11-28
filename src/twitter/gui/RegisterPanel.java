package twitter.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import twitter.dao.UserDAO;

public class RegisterPanel extends JPanel {

    private MainFrame mainFrame;
    private UserDAO userDAO;

    private JTextField userIdField;
    private JPasswordField pwField;
    private JPasswordField pwConfirmField;
    private JTextField phoneField;
    private JTextField emailField;
    private RoundedButton registerButton;
    private RoundedButton loginButton;


    /* ===============================
    공통 RoundedButton 클래스
=============================== */
    private static class RoundedButton extends JButton {
        private Color bgColor;
        private Color textColor;

        public RoundedButton(String text, Color bgColor, Color textColor) {
            super(text);
            this.bgColor = bgColor;
            this.textColor = textColor;

            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(textColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            super.paintComponent(g);
        }
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

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO(mainFrame.getConnection());

        setLayout(null);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        /* ===============================
              LOGO
        =============================== */
        JLabel logo = new JLabel("🐦", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        logo.setBounds(0, 30, 800, 60); // 위치 약간 아래로
        add(logo);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);

        // ★★★ 여기만 수정됨 ★★★
        int xLabel = 200;     // 동일
        int xField = 170;     // 140 → 170 중앙으로 이동
        int width = 460;      // 520 → 460으로 축소
        int height = 45;
        int gap = 80;         // 70 → 80 간격 확대
        int y = 120;          // 130 → 150 아래로 이동

        /* ===============================
              USER ID
        =============================== */
        JLabel idLabel = new JLabel("User_id:");
        idLabel.setFont(labelFont);
        idLabel.setBounds(xLabel, y - 30, 200, 20);
        add(idLabel);

        userIdField = new JTextField();
        styleField(userIdField, xField, y, width, height);
        add(userIdField);

        y += gap;

        /* ===============================
              PASSWORD
        =============================== */
        JLabel pwLabel = new JLabel("Password:");
        pwLabel.setFont(labelFont);
        pwLabel.setBounds(xLabel, y - 30, 200, 20);
        add(pwLabel);

        pwField = new JPasswordField();
        styleField(pwField, xField, y, width, height);
        add(pwField);

        y += gap;

        /* ===============================
              CHECK PASSWORD
        =============================== */
        JLabel pwcLabel = new JLabel("Check Password:");
        pwcLabel.setFont(labelFont);
        pwcLabel.setBounds(xLabel, y - 30, 200, 20);
        add(pwcLabel);

        pwConfirmField = new JPasswordField();
        styleField(pwConfirmField, xField, y, width, height);
        add(pwConfirmField);

        y += gap;

        /* ===============================
              PHONE
        =============================== */
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(labelFont);
        phoneLabel.setBounds(xLabel, y - 30, 200, 20);
        add(phoneLabel);

        phoneField = new JTextField();
        styleField(phoneField, xField, y, width, height);
        add(phoneField);

        y += gap;

        /* ===============================
              EMAIL
        =============================== */
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setBounds(xLabel, y - 30, 200, 20);
        add(emailLabel);

        emailField = new JTextField();
        styleField(emailField, xField, y, width, height);
        add(emailField);

        /* ===============================
              BUTTONS
        =============================== */

        // ★★★ 버튼 위치만 수정됨 ★★★
        RoundedButton registerButton = new RoundedButton(
                "Registration",
                new Color(29, 161, 242),   // 파란색
                Color.WHITE                 // 흰색 글씨
        );
        registerButton.setBounds(200, y + 60, 180, 50);
        add(registerButton);

        RoundedButton loginButton = new RoundedButton(
                "Log In",
                new Color(240, 240, 240),   // 연한 회색
                new Color(29, 161, 242)    // 파란 글씨
        );
        loginButton.setBounds(420, y + 60, 180, 50);
        add(loginButton);

        registerButton.addActionListener(e -> handleRegister());
        loginButton.addActionListener(e -> mainFrame.showPanel("login"));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsiveLayout();
            }
        });
    }
    private void updateResponsiveLayout() {

        // 🔒 아직 생성 전이면 실행 중지 (NPE 방지)
        if (userIdField == null || pwField == null || pwConfirmField == null ||
                phoneField == null || emailField == null ||
                registerButton == null || loginButton == null) {
            return;
        }

        int panelWidth = getWidth();

        // 입력 필드 박스 영역 전체 폭
        int formWidth = 460;  // UI 유지
        int fieldX = (panelWidth - formWidth) / 2;  // ★★★ 가로 중앙 정렬 핵심

        int height = 45;
        int y = 120;
        int gap = 80;

        // 입력 필드 중앙 정렬
        userIdField.setBounds(fieldX, y, formWidth, height);
        y += gap;
        pwField.setBounds(fieldX, y, formWidth, height);
        y += gap;
        pwConfirmField.setBounds(fieldX, y, formWidth, height);
        y += gap;
        phoneField.setBounds(fieldX, y, formWidth, height);
        y += gap;
        emailField.setBounds(fieldX, y, formWidth, height);

        // 버튼 2개 중앙 정렬
        int btnWidth = 180;
        int spacing = 40;  // 두 버튼 사이 간격

        int totalBtnWidth = btnWidth * 2 + spacing;
        int btnStartX = (panelWidth - totalBtnWidth) / 2;  // ★★★ 버튼도 중앙 정렬

        int btnY = y + 60;

        registerButton.setBounds(btnStartX, btnY, btnWidth, 50);
        loginButton.setBounds(btnStartX + btnWidth + spacing, btnY, btnWidth, 50);
    }

    /* ===============================
       INPUT FIELD STYLE 함수
    =============================== */
    private void styleField(JTextField tf, int x, int y, int w, int h) {
        tf.setBounds(x, y, w, h);
        tf.setBackground(new Color(245, 245, 245));
        tf.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    }

    /* ===============================
        BLUE PILL BUTTON
    =============================== */
    private JButton makeBlueButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 180, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(text)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /* ===============================
        GRAY PILL BUTTON
    =============================== */
    private JButton makeGrayButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 235, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(new Color(150, 180, 255));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(text)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /* ============================
       커스텀 메시지 팝업 (Yes 1개)
   ============================ */
    private void showCustomMessageDialog(String message) {

        JDialog dialog = new JDialog(mainFrame, true);
        dialog.setUndecorated(true);
        dialog.setSize(260, 150);
        dialog.setLocationRelativeTo(mainFrame);

        RoundedPanel background = new RoundedPanel();
        background.setLayout(new BorderLayout());
        background.setBorder(new EmptyBorder(10, 15, 10, 15));

    /* ------------------------
       🔹 상단 X 버튼
       ------------------------ */
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JButton closeBtn = new JButton("x");
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        top.add(closeBtn, BorderLayout.EAST);

    /* ------------------------
       🔹 메시지
       ------------------------ */
        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        msgLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

    /* ------------------------
       🔹 구분선
       ------------------------ */
        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(new Color(200, 200, 200));

    /* ------------------------
       🔹 yes 버튼
       ------------------------ */
        JButton okBtn = new JButton("yes");
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setPreferredSize(new Dimension(1, 35));
        okBtn.addActionListener(e -> dialog.dispose());

        // 버튼만 들어있는 영역
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(okBtn, BorderLayout.CENTER);

    /* ------------------------
       🔹 메시지 + 구분선 + 버튼 묶기 (중요!)
       ------------------------ */
        /* ------------------------
   🔹 메시지 + 구분선 + 버튼 묶기
   ------------------------ */
        JPanel centerGroup = new JPanel();
        centerGroup.setOpaque(false);
        centerGroup.setLayout(new BoxLayout(centerGroup, BoxLayout.Y_AXIS));

        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerGroup.add(msgLabel);
        centerGroup.add(line);      // 이제 1px로 고정됨
        centerGroup.add(bottom);


    /* ------------------------
       🔹 배경에 배치
       ------------------------ */
        background.add(top, BorderLayout.NORTH);
        background.add(centerGroup, BorderLayout.CENTER);

        dialog.setContentPane(background);
        dialog.setVisible(true);
    }

    /* ===============================
         회원가입 처리
    =============================== */
    private void handleRegister() {
        String userId = userIdField.getText().trim();
        String pwd = new String(pwField.getPassword()).trim();
        String pwdConfirm = new String(pwConfirmField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (userId.isEmpty() || pwd.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (!pwd.equals(pwdConfirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        try {
            String result = userDAO.createUser(userId, pwd, phone, email);

            if (result.equals("User created successfully")) {
                showCustomMessageDialog("Registration successful!");
                mainFrame.showPanel("login");
            } else {
                JOptionPane.showMessageDialog(this, result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }
}
