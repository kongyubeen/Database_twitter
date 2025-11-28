package twitter.gui;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    private Color bgColor;
    private Color fgColor;

    public RoundedButton(String text, Color bgColor, Color fgColor) {
        super(text);
        this.bgColor = bgColor;
        this.fgColor = fgColor;

        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        setForeground(fgColor);
        setContentAreaFilled(false);
        setOpaque(false);
    }

    public RoundedButton(String text) {
        this(text, new Color(152, 187, 255), Color.WHITE);
    }

    public void setBackgroundColor(Color bg) {
        this.bgColor = bg;
        repaint();
    }

    public void setForegroundColor(Color fg) {
        this.fgColor = fg;
        setForeground(fg);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        g2.dispose();
        super.paintComponent(g);
    }
}
