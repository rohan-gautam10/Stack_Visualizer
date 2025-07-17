import javax.swing.*;
import java.awt.*;

public class StyledComponents {
    public static class StyledButton extends JButton {
        private static final int ROUNDNESS = 15;
        private Color bgColor;

        public StyledButton(String text, Color bgColor) {
            super(text);
            this.bgColor = bgColor;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(120, 50));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDNESS, ROUNDNESS);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    public static class StyledTextField extends JTextField {
        private static final int ROUNDNESS = 15;
        private boolean isDarkMode;

        public StyledTextField(boolean isDarkMode) {
            super();
            this.isDarkMode = isDarkMode;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            setBackground(isDarkMode ? new Color(68, 71, 90) : new Color(220, 220, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDNESS, ROUNDNESS);

            super.paintComponent(g2);
            g2.dispose();
        }

        public void setDarkMode(boolean isDarkMode) {
            this.isDarkMode = isDarkMode;
            setBackground(isDarkMode ? new Color(68, 71, 90) : new Color(220, 220, 220));
        }
    }

    public static class StyledComboBox extends JComboBox<String> {
        private static final int ROUNDNESS = 15;
        private boolean isDarkMode;

        public StyledComboBox(String[] items, boolean isDarkMode) {
            super(items);
            this.isDarkMode = isDarkMode;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setBackground(isDarkMode ? new Color(68, 71, 90) : new Color(220, 220, 220));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setFont(new Font("Segoe UI", Font.PLAIN, 20));
                    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    return this;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDNESS, ROUNDNESS);

            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDarkMode ? Color.GRAY : Color.DARK_GRAY);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, ROUNDNESS, ROUNDNESS);
            g2.dispose();
        }

        public void setDarkMode(boolean isDarkMode) {
            this.isDarkMode = isDarkMode;
            setBackground(isDarkMode ? new Color(68, 71, 90) : new Color(220, 220, 220));
            repaint();
        }
    }
}