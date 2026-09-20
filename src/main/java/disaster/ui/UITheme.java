package disaster.ui;

import java.awt.*;

/**
 * Design system matching the Kerala SDMA Disaster Management System portal.
 * Warm crimson, clean white surfaces, subtle rose tints, and high readability.
 */
public class UITheme {

    // Backgrounds & Surfaces
    public static final Color BACKGROUND = new Color(250, 250, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color CARD_HOVER = new Color(248, 250, 252);
    public static final Color PANEL_BG = new Color(245, 245, 245);

    // Crimson Brand Colors (Matching Screenshot)
    public static final Color BRAND_CRIMSON = new Color(153, 27, 27);     // #991B1B
    public static final Color BRAND_CRIMSON_DARK = new Color(127, 29, 29); // #7F1D1D
    public static final Color TOP_BAR_BG = new Color(136, 19, 55);         // #881337
    public static final Color PRIMARY = new Color(153, 27, 27);
    public static final Color PRIMARY_DARK = new Color(127, 29, 29);
    public static final Color PRIMARY_LIGHT = new Color(254, 242, 242);
    public static final Color PRIMARY_BORDER = new Color(254, 205, 211);

    // Hero Section Colors
    public static final Color HERO_BG = new Color(255, 245, 245);          // #FFF5F5
    public static final Color HERO_BORDER = new Color(254, 215, 215);      // #FED7D7
    public static final Color HERO_BADGE_BG = new Color(255, 228, 230);    // #FFE4E6
    public static final Color HERO_BADGE_TEXT = new Color(159, 18, 57);    // #9F1239

    // Hotline Colors
    public static final Color HOTLINE_RED = new Color(220, 38, 38);        // #DC2626
    public static final Color HOTLINE_BLUE = new Color(37, 99, 235);       // #2563EB
    public static final Color HOTLINE_TEAL = new Color(13, 148, 136);      // #0D9488

    // Status Colors
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color SUCCESS_LIGHT = new Color(240, 253, 244);
    public static final Color INFO = new Color(37, 99, 235);

    // Text Colors
    public static final Color TEXT = new Color(15, 23, 42);                // #0F172A
    public static final Color SECONDARY_TEXT = new Color(71, 85, 105);     // #475569
    public static final Color MUTED = new Color(148, 163, 184);            // #94A3B8

    // Borders
    public static final Color BORDER = new Color(226, 232, 240);          // #E2E8F0
    public static final Color BORDER_DARK = new Color(203, 213, 225);
    public static final Color INPUT_BG = Color.WHITE;
    public static final Color INPUT_BORDER = new Color(203, 213, 225);
    public static final Color INPUT_FOCUS = new Color(153, 27, 27);

    // Typography
    public static final Font HERO_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font BADGE_FONT = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font KPI_NUMBER_FONT = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font CODE_FONT = new Font("Consolas", Font.PLAIN, 12);

    private UITheme() {
    }

    public static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}