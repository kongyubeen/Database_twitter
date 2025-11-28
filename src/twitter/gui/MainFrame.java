package twitter.gui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

import db.DBConnection;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private Connection conn;

    private String loggedInUserId = null;

    private PostDetailPanel postDetailPanel;
    private SidebarPanel sidebar;


    public MainFrame() {

        /* ----------------------------
           1. DB Connection
        ---------------------------- */
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(null, "Failed to connect to DB! Exiting program.");
                System.exit(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load DB driver!");
            System.exit(0);
        }

        /* ----------------------------
           2. Layout Settings
        ---------------------------- */
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        /* ----------------------------
           3. Create Panels
        ---------------------------- */
        LoginPanel loginPanel = new LoginPanel(this);
        RegisterPanel registerPanel = new RegisterPanel(this);
        MainFeedPanel mainFeedPanel = new MainFeedPanel(this);
        UserActionPanel userActionPanel = new UserActionPanel(this);
        postDetailPanel = new PostDetailPanel(this);
        MessagePanel messagePanel = new MessagePanel(this);
        ExplorePanel explorePanel = new ExplorePanel(this);

        /* ----------------------------
           4. Add Panels to CardLayout
        ---------------------------- */
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        mainPanel.add(mainFeedPanel, "mainFeed");
        mainPanel.add(userActionPanel, "userActionPanel");
        mainPanel.add(postDetailPanel, "postDetail");
        mainPanel.add(messagePanel, "messagePanel");
        mainPanel.add(explorePanel, "explorePanel");

        /* ----------------------------
           5. Sidebar
        ---------------------------- */
        sidebar = new SidebarPanel(this);
        add(sidebar, BorderLayout.WEST);

        /* ----------------------------
           6. Center Panel
        ---------------------------- */
        add(mainPanel, BorderLayout.CENTER);

        /* ----------------------------
           7. Frame Settings
        ---------------------------- */
        setTitle("My Twitter");

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // First screen
        showPanel("login");
    }

    /* ----------------------------
       Common Methods
    ---------------------------- */

    public Connection getConnection() {
        return conn;
    }

    public void showPanel(String panelName) {

        // Hide sidebar for login/register
        if (panelName.equals("login") || panelName.equals("register")) {
            sidebar.setVisible(false);
        }
        // Show sidebar for all other screens
        else {
            sidebar.setVisible(true);
            sidebar.updateProfileName();
        }

        cardLayout.show(mainPanel, panelName);
    }

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
    }

    public String getLoggedInUserId() {
        return loggedInUserId;
    }

    public void showPostDetail(int postId) {
        postDetailPanel.loadPostDetails(postId);
        cardLayout.show(mainPanel, "postDetail");
    }

    private String lastPanelName = "mainFeed";

    public void setLastPanel(String name) {
        this.lastPanelName = name;
    }

    public String getLastPanel() {
        return lastPanelName;
    }

    /* ----------------------------
       main()
    ---------------------------- */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
