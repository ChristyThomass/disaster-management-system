package disaster.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class BaseFrame extends JFrame {

    public BaseFrame(String title) {
        this(title, 1100, 780);
    }

    public BaseFrame(String title, int width, int height) {
        setTitle(title);
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);

        // Pressing Escape closes this frame and returns to portal
        getRootPane().registerKeyboardAction(
                e -> closeAndReturnToPortal(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void closeAndReturnToPortal() {
        dispose();
        if (DashboardFrame.getActiveInstance() != null) {
            DashboardFrame.getActiveInstance().toFront();
            DashboardFrame.getActiveInstance().requestFocus();
        }
    }

    protected JButton createBackButton() {
        return createBackButton("← Back to Portal");
    }

    protected JButton createBackButton(String text) {
        JButton btn = createOutlinedButton(text, UITheme.BORDER_DARK, UITheme.TEXT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(140, 34));
        btn.addActionListener(e -> closeAndReturnToPortal());
        return btn;
    }

    // Standard primary button (Crimson)
    protected JButton button(String text) {
        return button(text, UITheme.BRAND_CRIMSON);
    }

    // Custom colored primary button
    protected JButton button(String text, Color bgColor) {
        return new RoundedButton(text, bgColor, bgColor.darker(), Color.WHITE);
    }

    // Custom colored outlined button (e.g. White with Red border)
    protected JButton createOutlinedButton(String text, Color borderColor, Color textColor) {
        RoundedButton btn = new RoundedButton(text, Color.WHITE, UITheme.CARD_HOVER, textColor);
        btn.setDrawBorder(true);
        btn.setBorderColor(borderColor);
        return btn;
    }

    protected JButton createOutlineButton(String text, Color borderColor) {
        return createOutlinedButton(text, borderColor, UITheme.TEXT);
    }

    protected JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.TITLE_FONT);
        label.setForeground(UITheme.TEXT);
        return label;
    }

    protected JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.SUBTITLE_FONT);
        label.setForeground(UITheme.SECONDARY_TEXT);
        return label;
    }

    protected JLabel normalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BOLD_FONT);
        label.setForeground(UITheme.TEXT);
        return label;
    }

    protected JPanel createCard() {
        return new RoundedCardPanel(8, UITheme.CARD, UITheme.BORDER);
    }

    protected JPanel createCard(int radius) {
        return new RoundedCardPanel(radius, UITheme.CARD, UITheme.BORDER);
    }

    protected JPanel createCustomCard(int radius, Color bg, Color border) {
        return new RoundedCardPanel(radius, bg, border);
    }

    protected JTextField createTextField(String placeholder) {
        return new RoundedTextField(placeholder);
    }

    protected JPasswordField createPasswordField(String placeholder) {
        return new RoundedPasswordField(placeholder);
    }

    protected JScrollPane createStandardScrollPane(JComponent component) {
        JScrollPane sp = new JScrollPane(component);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getHorizontalScrollBar().setUnitIncrement(20);
        return sp;
    }

    protected JScrollPane createTableScrollPane(JTable table) {
        table.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getHorizontalScrollBar().setUnitIncrement(20);
        return sp;
    }

    protected JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        int maxH = (field instanceof JScrollPane || field instanceof JTextArea) ? 240 : 70;
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, maxH));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.BOLD_FONT);
        lbl.setForeground(UITheme.TEXT);
        row.add(lbl, BorderLayout.NORTH);

        row.add(field, BorderLayout.CENTER);

        return row;
    }

    protected JPanel createStandardHeader(String titleText, String subtitleText, String badgeText, Color badgeColor) {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);

        JPanel leftRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftRow.setOpaque(false);

        JButton backBtn = createBackButton("← Back");
        backBtn.setPreferredSize(new Dimension(85, 34));
        leftRow.add(backBtn);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel t = heading(titleText);
        JLabel s = subtitle(subtitleText);
        titles.add(t);
        titles.add(Box.createVerticalStrut(3));
        titles.add(s);
        leftRow.add(titles);

        header.add(leftRow, BorderLayout.WEST);

        if (badgeText != null) {
            JPanel b = createBadge(badgeText, badgeColor, new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 25));
            header.add(b, BorderLayout.EAST);
        }

        return header;
    }

    public static JPanel createBadge(String text, Color fgColor, Color bgColor) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityHints(g2);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 120));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 3));
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.BADGE_FONT);
        lbl.setForeground(fgColor);
        badge.add(lbl);
        return badge;
    }

    // --------------------------------------------------------------------------------
    // Custom Nested Swing Controls
    // --------------------------------------------------------------------------------

    public static class RoundedCardPanel extends JPanel {
        private final int radius;
        private final Color bgColor;
        private final Color borderColor;

        public RoundedCardPanel(int radius, Color bgColor, Color borderColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, radius, radius));

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 2f, h - 2f, radius, radius));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedButton extends JButton {
        private final Color normalColor;
        private final Color hoverColor;
        private final Color textColor;
        private boolean isHovered = false;
        private boolean drawBorder = false;
        private Color borderColor = UITheme.BORDER;

        public RoundedButton(String text, Color normalColor, Color hoverColor, Color textColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;

            setFont(UITheme.BUTTON_FONT);
            setForeground(textColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 38));
            setMinimumSize(new Dimension(100, 36));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setDrawBorder(boolean draw) {
            this.drawBorder = draw;
        }

        public void setBorderColor(Color c) {
            this.borderColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(isHovered ? hoverColor : normalColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 6, 6));

            if (drawBorder) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 6, 6));
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setFont(getFont());
            g2.setColor(textColor);
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    public static class RoundedTextField extends JTextField {
        private final String placeholder;
        private boolean isFocused = false;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(UITheme.NORMAL_FONT);
            setForeground(UITheme.TEXT);
            setCaretColor(UITheme.PRIMARY);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setPreferredSize(new Dimension(280, 38));
            setMinimumSize(new Dimension(100, 36));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(UITheme.INPUT_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 6, 6));

            g2.setColor(isFocused ? UITheme.INPUT_FOCUS : UITheme.INPUT_BORDER);
            g2.setStroke(new BasicStroke(isFocused ? 1.5f : 1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 6, 6));

            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocused && placeholder != null) {
                Graphics2D gp = (Graphics2D) g.create();
                UITheme.applyQualityHints(gp);
                gp.setFont(getFont());
                gp.setColor(UITheme.MUTED);
                FontMetrics fm = gp.getFontMetrics();
                gp.drawString(placeholder, 12, (h - fm.getHeight()) / 2 + fm.getAscent());
                gp.dispose();
            }
        }
    }

    public static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean isFocused = false;

        public RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(UITheme.NORMAL_FONT);
            setForeground(UITheme.TEXT);
            setCaretColor(UITheme.PRIMARY);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setPreferredSize(new Dimension(280, 38));
            setMinimumSize(new Dimension(100, 36));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.applyQualityHints(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(UITheme.INPUT_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 6, 6));

            g2.setColor(isFocused ? UITheme.INPUT_FOCUS : UITheme.INPUT_BORDER);
            g2.setStroke(new BasicStroke(isFocused ? 1.5f : 1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 6, 6));

            g2.dispose();
            super.paintComponent(g);

            if (getPassword().length == 0 && !isFocused && placeholder != null) {
                Graphics2D gp = (Graphics2D) g.create();
                UITheme.applyQualityHints(gp);
                gp.setFont(getFont());
                gp.setColor(UITheme.MUTED);
                FontMetrics fm = gp.getFontMetrics();
                gp.drawString(placeholder, 12, (h - fm.getHeight()) / 2 + fm.getAscent());
                gp.dispose();
            }
        }
    }
}