import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class StackVisualizer extends JFrame {
    private ArrayList<Object> stack = new ArrayList<>();
    private StackPanel stackPanel;
    private JTextField inputField;
    private JComboBox<String> dataTypeCombo;
    private JButton pushButton, popButton, peekButton, clearButton, toggleThemeButton;
    private JSlider speedSlider;
    private JLabel operationLabel, titleLabel;
    private JTextArea historyArea;
    private Timer animationTimer, peekHighlightTimer;
    private Queue<String> historyQueue = new LinkedList<>();

    private Object elementToAnimate = null;
    private boolean isPushAnimation = false;
    private float animationAlpha = 1.0f;
    private int animationStep = 0;
    private int animationY = 0;
    private boolean showPushArrow = false;
    private boolean showPeekHighlight = false;
    private int highlightY = 0;
    private int pushArrowY = 0;

    private boolean isDarkMode = false;
    private String selectedDataType = null;
    private boolean isDataTypeLocked = false;

    private static final int ELEMENT_HEIGHT = 60;
    private static final int ELEMENT_WIDTH = 200;
    private static final int ANIMATION_STEPS = 25;
    private static final int ROUNDNESS = 15;
    private static final int MAX_HISTORY_ITEMS = 10;
    private static final int MAX_STACK_SIZE = 20;
    private static final int ARROW_SIZE = 30;
    private static final int HIGHLIGHT_DURATION = 3000;

    // Color schemes
    private final Color LIGHT_BG = new Color(245, 245, 245);
    private final Color DARK_BG = new Color(40, 42, 54);
    private final Color LIGHT_ELEMENT = new Color(220, 220, 220);
    private final Color DARK_ELEMENT = new Color(68, 71, 90);
    private final Color ACCENT_COLOR = new Color(100, 149, 237);
    private final Color DARK_ACCENT = new Color(80, 250, 123);
    private final Color PEEK_COLOR = new Color(255, 193, 7);
    private final Color HIGHLIGHT_COLOR = new Color(255, 215, 0, 150);

    public StackVisualizer() {
        setTitle("Stack Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 850);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // Custom title bar
        titleLabel = new JLabel("STACK VISUALIZER");
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 32));
        titleLabel.setOpaque(true);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 0));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBackground(ACCENT_COLOR);
        add(titleLabel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Stack panel with scroll
        stackPanel = new StackPanel();
        JScrollPane scrollPane = new JScrollPane(stackPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Control Panel (now at top)
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input components with rounded corners
        inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDNESS, ROUNDNESS);

                // Text
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        inputField.setText("Enter value");
        inputField.setForeground(Color.GRAY);
        inputField.setOpaque(false);
        inputField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (inputField.getText().equals("Enter value")) {
                    inputField.setText("");
                    inputField.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText("Enter value");
                    inputField.setForeground(Color.GRAY);
                }
            }
        });

        String[] dataTypes = {"Integer", "Character", "String"};
        dataTypeCombo = new JComboBox<String>(dataTypes) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with rounded corners
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDNESS, ROUNDNESS);

                // Text
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
        };
        dataTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        dataTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 20));
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        dataTypeCombo.setOpaque(false);
        dataTypeCombo.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Buttons with tooltips and mnemonics
        pushButton = createStyledButton("PUSH", ACCENT_COLOR);
        pushButton.setToolTipText("Add element to stack (Alt+P)");
        pushButton.setMnemonic(KeyEvent.VK_P);

        popButton = createStyledButton("POP", new Color(220, 53, 69));
        popButton.setToolTipText("Remove top element (Alt+O)");
        popButton.setMnemonic(KeyEvent.VK_O);

        peekButton = createStyledButton("PEEK", PEEK_COLOR);
        peekButton.setToolTipText("View top element (Alt+E)");
        peekButton.setMnemonic(KeyEvent.VK_E);

        clearButton = createStyledButton("CLEAR", new Color(108, 117, 125));
        clearButton.setToolTipText("Clear stack (Alt+C)");
        clearButton.setMnemonic(KeyEvent.VK_C);

        toggleThemeButton = createStyledButton("THEME", new Color(32, 201, 151));
        toggleThemeButton.setToolTipText("Toggle dark/light mode (Alt+T)");
        toggleThemeButton.setMnemonic(KeyEvent.VK_T);

        // Slider
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setPreferredSize(new Dimension(120, 40));
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Operation label
        operationLabel = new JLabel("Operations: 0");
        operationLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Layout components in control panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        controlPanel.add(inputField, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.2;
        controlPanel.add(dataTypeCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(pushButton, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(popButton, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(peekButton, gbc);

        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(clearButton, gbc);

        gbc.gridx = 6; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(toggleThemeButton, gbc);

        gbc.gridx = 7; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(new JLabel("Speed:"), gbc);

        gbc.gridx = 8; gbc.gridy = 0; gbc.weightx = 0.2;
        controlPanel.add(speedSlider, gbc);

        gbc.gridx = 9; gbc.gridy = 0; gbc.weightx = 0.1;
        controlPanel.add(operationLabel, gbc);

        // Add control panel to content panel (top)
        contentPanel.add(controlPanel, BorderLayout.NORTH);

        // History Panel
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(15, 20, 20, 20),
                "Operation History",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 18)
        ));

        historyArea = new JTextArea(4, 25);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        // Add panels to frame
        add(contentPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.SOUTH);

        // Listeners
        pushButton.addActionListener(e -> pushElement());
        popButton.addActionListener(e -> popElement());
        peekButton.addActionListener(e -> peekElement());
        clearButton.addActionListener(e -> clearStack());
        toggleThemeButton.addActionListener(e -> toggleTheme());
        speedSlider.addChangeListener(e -> updateAnimationSpeed());
        inputField.addActionListener(e -> pushElement());

        // Keyboard shortcuts
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), "push");
        getRootPane().getActionMap().put("push", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                pushElement();
            }
        });

        animationTimer = new Timer(25, e -> animate());
        peekHighlightTimer = new Timer(HIGHLIGHT_DURATION, e -> {
            showPeekHighlight = false;
            stackPanel.repaint();
            peekHighlightTimer.stop();
        });
        peekHighlightTimer.setRepeats(false);
        updateAnimationSpeed();
        updateTheme();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDNESS, ROUNDNESS);

                // Button text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 50));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void pushElement() {
        String input = inputField.getText().trim();
        if (input.equals("") || input.equals("Enter value")) {
            showMessage("Please enter a value");
            return;
        }

        if (stack.size() >= MAX_STACK_SIZE) {
            showMessage("Stack overflow - maximum size reached (" + MAX_STACK_SIZE + ")");
            return;
        }

        String dataType = (String) dataTypeCombo.getSelectedItem();
        try {
            Object value = parseInput(input, dataType);
            if (value == null) return;

            if (selectedDataType == null) {
                selectedDataType = dataType;
                isDataTypeLocked = true;
                dataTypeCombo.setEnabled(false);
            } else if (!dataType.equals(selectedDataType)) {
                showMessage("Stack type locked to " + selectedDataType);
                return;
            }

            stack.add(value);
            elementToAnimate = value;
            animationStep = 0;
            animationAlpha = 0.0f;
            animationY = (stackPanel.getHeight() - ELEMENT_HEIGHT - 20) - (stack.size() - 1) * ELEMENT_HEIGHT;
            isPushAnimation = true;
            animationTimer.start();
            inputField.setText("");
            addToHistory("Pushed: " + value);
            updateOperationCount();

            // Show push arrow indicator on left side
            showPushArrow = true;
            pushArrowY = (stackPanel.getHeight() - ELEMENT_HEIGHT - 20) - (stack.size() - 1) * ELEMENT_HEIGHT;
            stackPanel.repaint();
            new Timer(2000, e -> {
                showPushArrow = false;
                stackPanel.repaint();
                ((Timer)e.getSource()).stop();
            }).start();
        } catch (NumberFormatException e) {
            showMessage("Invalid number format");
        }
    }

    private Object parseInput(String input, String dataType) {
        switch (dataType) {
            case "Integer":
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    showMessage("Invalid integer format");
                    return null;
                }
            case "Character":
                if (input.length() != 1) {
                    showMessage("Please enter exactly one character");
                    return null;
                }
                return input.charAt(0);
            case "String":
                if (input.length() > 20) {
                    showMessage("String too long (max 20 chars)");
                    return null;
                }
                return input;
            default:
                return null;
        }
    }

    private void popElement() {
        if (stack.isEmpty()) {
            showMessage("Stack is empty");
            return;
        }
        elementToAnimate = stack.get(stack.size() - 1);
        animationStep = 0;
        animationAlpha = 1.0f;
        isPushAnimation = false;
        animationY = (stackPanel.getHeight() - ELEMENT_HEIGHT - 20) - (stack.size() - 1) * ELEMENT_HEIGHT;
        animationTimer.start();
    }

    private void peekElement() {
        if (stack.isEmpty()) {
            showMessage("Stack is empty");
            return;
        }
        Object top = stack.get(stack.size() - 1);
        addToHistory("Peeked: " + top.toString());

        // Show peek highlight
        showPeekHighlight = true;
        highlightY = (stackPanel.getHeight() - ELEMENT_HEIGHT - 20) - (stack.size() - 1) * ELEMENT_HEIGHT;
        stackPanel.repaint();
        peekHighlightTimer.start();
    }

    private void clearStack() {
        if (stack.isEmpty()) {
            showMessage("Stack is already empty");
            return;
        }
        stack.clear();
        elementToAnimate = null;
        selectedDataType = null;
        isDataTypeLocked = false;
        dataTypeCombo.setEnabled(true);
        historyQueue.clear();
        historyArea.setText("");
        updateOperationCount();
        stackPanel.repaint();
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        updateTheme();
    }

    private void updateTheme() {
        Color bgColor = isDarkMode ? DARK_BG : LIGHT_BG;
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;
        Color elementColor = isDarkMode ? DARK_ELEMENT : LIGHT_ELEMENT;

        getContentPane().setBackground(bgColor);
        stackPanel.setBackground(bgColor);

        inputField.setBackground(elementColor);
        inputField.setForeground(fgColor);

        dataTypeCombo.setBackground(elementColor);
        dataTypeCombo.setForeground(fgColor);

        historyArea.setBackground(elementColor);
        historyArea.setForeground(fgColor);

        titleLabel.setBackground(isDarkMode ? DARK_ACCENT : ACCENT_COLOR);

        stackPanel.repaint();
    }

    private void updateAnimationSpeed() {
        int speed = speedSlider.getValue();
        int delay = 50 - (speed * 4);
        animationTimer.setDelay(Math.max(10, delay));
    }

    private void animate() {
        animationStep++;
        if (animationStep <= ANIMATION_STEPS) {
            if (isPushAnimation) {
                animationAlpha = (float) Math.sin((animationStep / (float) ANIMATION_STEPS) * Math.PI / 2);
                animationY = (stackPanel.getHeight() - ELEMENT_HEIGHT - 20) - (stack.size() - 1) * ELEMENT_HEIGHT;
            } else {
                animationAlpha = 1.0f - (animationStep / (float) ANIMATION_STEPS);
            }
            stackPanel.repaint();
        } else {
            animationTimer.stop();
            if (!isPushAnimation) {
                stack.remove(stack.size() - 1);
                addToHistory("Popped: " + elementToAnimate);
                updateOperationCount();

                if (stack.isEmpty()) {
                    selectedDataType = null;
                    isDataTypeLocked = false;
                    dataTypeCombo.setEnabled(true);
                }
            }
            elementToAnimate = null;
            stackPanel.repaint();
        }
    }

    private void addToHistory(String message) {
        if (historyQueue.size() >= MAX_HISTORY_ITEMS) {
            historyQueue.poll();
        }
        historyQueue.add(message);
        updateHistoryArea();
    }

    private void updateHistoryArea() {
        StringBuilder sb = new StringBuilder();
        for (String item : historyQueue) {
            sb.append("• ").append(item).append("\n");
        }
        historyArea.setText(sb.toString());
        // Ensure history area stays visible
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    private void updateOperationCount() {
        operationLabel.setText("Operations: " + stack.size());
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Stack Visualizer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    class StackPanel extends JPanel {
        public StackPanel() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        public Dimension getPreferredSize() {
            int height = Math.max(600, (stack.size() + 2) * ELEMENT_HEIGHT + 40);
            return new Dimension(getWidth(), height);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth = getWidth();
            int x = (panelWidth - ELEMENT_WIDTH) / 2;
            int baseY = getHeight() - ELEMENT_HEIGHT - 20;

            // Draw stack base
            g2.setColor(isDarkMode ? DARK_ACCENT : ACCENT_COLOR);
            g2.fillRoundRect(x - 30, baseY + ELEMENT_HEIGHT - 15,
                    ELEMENT_WIDTH + 60, 20, 15, 15);
            g2.setColor(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x - 30, baseY + ELEMENT_HEIGHT - 15,
                    ELEMENT_WIDTH + 60, 20, 15, 15);

            // Draw stack elements
            Color elementColor = isDarkMode ? DARK_ELEMENT : LIGHT_ELEMENT;
            Color textColor = isDarkMode ? Color.WHITE : Color.BLACK;

            for (int i = 0; i < stack.size(); i++) {
                Object value = stack.get(i);
                boolean isAnimating = (value.equals(elementToAnimate) && animationStep <= ANIMATION_STEPS && isPushAnimation);

                int elementY = baseY - i * ELEMENT_HEIGHT;

                if (value.equals(elementToAnimate) && !isPushAnimation && animationStep <= ANIMATION_STEPS) {
                    continue;
                }

                // Element shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(x + 5, elementY + 5, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);

                // Element background
                g2.setColor(isAnimating ?
                        new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), (int)(animationAlpha * 255)) :
                        elementColor);
                g2.fillRoundRect(x, elementY, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);

                // Element border
                g2.setColor(isDarkMode ? Color.DARK_GRAY : Color.GRAY);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(x, elementY, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);

                // Element text
                g2.setColor(textColor);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                String text = value.toString();
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (ELEMENT_WIDTH - fm.stringWidth(text)) / 2;
                int textY = elementY + (ELEMENT_HEIGHT - 5 + fm.getAscent()) / 2;
                g2.drawString(text, textX, textY);
            }

            // Draw animating element (for pop)
            if (!isPushAnimation && elementToAnimate != null && animationStep <= ANIMATION_STEPS) {
                // Shadow
                g2.setColor(new Color(0, 0, 0, (int)(animationAlpha * 50)));
                g2.fillRoundRect(x + 5, animationY + 5, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);

                // Element
                g2.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), (int)(animationAlpha * 255)));
                g2.fillRoundRect(x, animationY, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);

                // Border
                g2.setColor(isDarkMode ? Color.DARK_GRAY : Color.GRAY);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(x, animationY, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);

                // Text
                g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)(animationAlpha * 255)));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                String text = elementToAnimate.toString();
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (ELEMENT_WIDTH - fm.stringWidth(text)) / 2;
                int textY = animationY + (ELEMENT_HEIGHT - 5 + fm.getAscent()) / 2;
                g2.drawString(text, textX, textY);
            }

            // Draw peek highlight
            if (showPeekHighlight) {
                g2.setColor(HIGHLIGHT_COLOR);
                g2.fillRoundRect(x, highlightY, ELEMENT_WIDTH, ELEMENT_HEIGHT - 5, ROUNDNESS, ROUNDNESS);
            }

            // Draw push arrow indicator (on left side)
            if (showPushArrow) {
                int arrowX = x - ARROW_SIZE - 20;
                int arrowY = pushArrowY + ELEMENT_HEIGHT / 2;

                g2.setColor(isDarkMode ? DARK_ACCENT : ACCENT_COLOR);
                g2.setStroke(new BasicStroke(3));

                // Arrow line
                g2.drawLine(arrowX, arrowY, arrowX + ARROW_SIZE, arrowY);

                // Arrow head
                Polygon arrowHead = new Polygon();
                arrowHead.addPoint(arrowX + ARROW_SIZE, arrowY);
                arrowHead.addPoint(arrowX + ARROW_SIZE - 10, arrowY - 7);
                arrowHead.addPoint(arrowX + ARROW_SIZE - 10, arrowY + 7);
                g2.fill(arrowHead);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            StackVisualizer app = new StackVisualizer();
            app.setVisible(true);
        });
    }
}