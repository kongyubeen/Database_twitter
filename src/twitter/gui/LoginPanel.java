package twitter.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import twitter.dao.UserDAO;

public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private UserDAO userDAO;

    private JTextField idField;
    private JPasswordField pwField;

    public class RoundedButton extends JButton {

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.dispose();

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

    public LoginPanel(MainFrame mainFrame) {

        this.mainFrame = mainFrame;

        Connection c = mainFrame.getConnection();

        if (c == null) {
            JOptionPane.showMessageDialog(this, "Failed to connect to DB");
            return;
        }

        userDAO = new UserDAO(c);

        setLayout(new BorderLayout());


        /* ===============================
               LEFT — Blue Panel
        =============================== */
        JPanel leftPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(29, 161, 242));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(400, 600));
        leftPanel.setLayout(new GridBagLayout());

        JPanel leftInner = new JPanel();
        leftInner.setOpaque(false);
        leftInner.setLayout(new BoxLayout(leftInner, BoxLayout.Y_AXIS));

        JLabel followLabel = new JLabel("♢   Follow your interests.");
        followLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        followLabel.setForeground(Color.WHITE);

        JLabel hearLabel = new JLabel("♢   Hear what people are talking about.");
        hearLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        hearLabel.setForeground(Color.WHITE);

        JLabel joinLabel = new JLabel("♢   Join the conversation.");
        joinLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        joinLabel.setForeground(Color.WHITE);

        leftInner.add(followLabel);
        leftInner.add(Box.createVerticalStrut(20));
        leftInner.add(hearLabel);
        leftInner.add(Box.createVerticalStrut(20));
        leftInner.add(joinLabel);

        leftPanel.add(leftInner);

        /* ===============================
               RIGHT — Login Panel
        =============================== */
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        JLabel logo = new JLabel("🐦", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 35));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title1 = new JLabel("See what's happening in");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title2 = new JLabel("the world right now");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel joinToday = new JLabel("Join Twitter today.");
        joinToday.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        joinToday.setAlignmentX(Component.CENTER_ALIGNMENT);

        idField = new JTextField();
        idField.setMaximumSize(new Dimension(300, 40));
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idField.setBorder(BorderFactory.createTitledBorder("Phone, email, or username"));

        pwField = new JPasswordField();
        pwField.setMaximumSize(new Dimension(300, 40));
        pwField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pwField.setBorder(BorderFactory.createTitledBorder("Password"));

        RoundedButton signupBtn = new RoundedButton("Sign up");
        signupBtn.setMaximumSize(new Dimension(300, 45));
        signupBtn.setBackground(new Color(29, 161, 242));
        signupBtn.setForeground(Color.WHITE);
        signupBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        signupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupBtn.addActionListener(e -> mainFrame.showPanel("register"));

        RoundedButton loginBtn = new RoundedButton("Log in");
        loginBtn.setMaximumSize(new Dimension(300, 45));
        loginBtn.setBackground(new Color(240, 240, 240));
        loginBtn.setForeground(new Color(29, 161, 242));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> handleLogin());

        formPanel.add(logo);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(title1);
        formPanel.add(title2);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(joinToday);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(idField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(pwField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(signupBtn);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(loginBtn);

        rightPanel.add(formPanel, gbc);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    /* ============================
       커스텀 메시지 팝업 (Yes 1개)
    ============================ */
    private void showCustomMessageDialog(String message) {

        JDialog dialog = new JDialog(mainFrame, true);
        dialog.setUndecorated(true);
        dialog.setSize(230, 120);
        dialog.setLocationRelativeTo(mainFrame);

        RoundedPanel background = new RoundedPanel();
        background.setLayout(new BorderLayout());
        background.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JButton closeBtn = new JButton("x");
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        top.add(closeBtn, BorderLayout.EAST);

        JLabel msgLabel = new JLabel(message, SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        msgLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        JPanel line = new JPanel();
        line.setBackground(new Color(200, 200, 200));
        line.setPreferredSize(new Dimension(1, 1));
        line.setMinimumSize(new Dimension(1, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JButton okBtn = new JButton("yes");
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setPreferredSize(new Dimension(1, 30));
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(okBtn, BorderLayout.CENTER);

        JPanel centerGroup = new JPanel();
        centerGroup.setOpaque(false);
        centerGroup.setLayout(new BoxLayout(centerGroup, BoxLayout.Y_AXIS));

        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerGroup.add(msgLabel);
        centerGroup.add(line);
        centerGroup.add(bottom);

        background.add(top, BorderLayout.NORTH);
        background.add(centerGroup, BorderLayout.CENTER);

        dialog.setContentPane(background);
        dialog.setVisible(true);
    }


    /* ===============================
           로그인 처리
    =============================== */
    private void handleLogin() {

        String id = idField.getText().trim();
        String password = new String(pwField.getPassword()).trim();

        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both ID and password.");
            return;
        }

        try {
            boolean ok = userDAO.login(id, password);

            if (ok) {
                showCustomMessageDialog("Login successful!");
                mainFrame.setLoggedInUserId(id);
                mainFrame.showPanel("mainFeed");
            }
            else{
                showCustomMessageDialog("Login failed!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
