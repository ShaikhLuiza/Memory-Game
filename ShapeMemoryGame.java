import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ShapeMemoryGame extends JFrame implements ActionListener {

    private JButton[] cards = new JButton[16];
    private ShapeIcon[] cardIcons = new ShapeIcon[16];
    private boolean[] matched = new boolean[16];

    private int firstIndex = -1;
    private int secondIndex = -1;
    private boolean lock = false;

    // Game State
    private int player1Score = 0;
    private int player2Score = 0;
    private int currentPlayer = 1;
    private boolean isSinglePlayer = false;

    // UI Components
    private JLabel p1Label, p2Label, status;
    private JPanel menuPanel, gamePanel;
    
    // UI Theme Constants
    private final Color HIDDEN_COLOR = new Color(65, 65, 65); 
    private final Color MATCHED_BG_COLOR = new Color(34, 139, 34);
    private final Color ERROR_COLOR = new Color(220, 53, 69);
    private final Color GRID_BG = new Color(40, 40, 40);
    private final Color ACCENT_BLUE = new Color(0, 120, 215);
    private final Color BG_DARK = new Color(25, 25, 25);

    private enum ShapeType { HEXAGON, SQUARE, TRIANGLE, DIAMOND, CROSS, STAR, RING, OVAL }

    public ShapeMemoryGame() {
        setTitle("Lets Test Your Memory....");
        setSize(600, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new CardLayout()); // Uses CardLayout to swap between Menu and Game

        createMenuPanel();
        createGamePanel();

        add(menuPanel, "MENU");
        add(gamePanel, "GAME");

        showPanel("MENU");
        setVisible(true);
    }

    private void createMenuPanel() {
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Let's Test Your Memory", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        menuPanel.add(title, gbc);

        JButton btn1P = createStyledButton("ONE PLAYER (VS CPU)", new Color(40, 167, 69));
        btn1P.addActionListener(e -> startGame(true));
        gbc.gridy = 1;
        menuPanel.add(btn1P, gbc);

        JButton btn2P = createStyledButton("TWO PLAYERS", ACCENT_BLUE);
        btn2P.addActionListener(e -> startGame(false));
        gbc.gridy = 2;
        menuPanel.add(btn2P, gbc);

        JButton btnQuit = createStyledButton("QUIT GAME", ERROR_COLOR);
        btnQuit.addActionListener(e -> System.exit(0));
        gbc.gridy = 3;
        menuPanel.add(btnQuit, gbc);
    }

    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(BG_DARK);
        JPanel scorePanel = new JPanel(new GridLayout(1, 2));
        p1Label = createScoreLabel("Player 1: 0");
        p2Label = createScoreLabel("Player 2: 0");
        scorePanel.add(p1Label);
        scorePanel.add(p2Label);
        scorePanel.setOpaque(false);

        status = new JLabel(" ", JLabel.CENTER);
        status.setForeground(Color.WHITE);
        status.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        header.add(scorePanel);
        header.add(status);

        // Grid
        JPanel grid = new JPanel(new GridLayout(4, 4, 12, 12));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        grid.setBackground(GRID_BG);
        for (int i = 0; i < 16; i++) {
            cards[i] = new JButton();
            cards[i].setFocusPainted(false);
            cards[i].setBackground(HIDDEN_COLOR);
            cards[i].setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 2));
            cards[i].addActionListener(this);
            grid.add(cards[i]);
        }

        // Footer
        JPanel footer = new JPanel(new FlowLayout());
        footer.setBackground(BG_DARK);
        JButton backBtn = createStyledButton("BACK TO MENU", new Color(100, 100, 100));
        backBtn.setPreferredSize(new Dimension(180, 40));
        backBtn.addActionListener(e -> showPanel("MENU"));
        footer.add(backBtn);

        gamePanel.add(header, BorderLayout.NORTH);
        gamePanel.add(grid, BorderLayout.CENTER);
        gamePanel.add(footer, BorderLayout.SOUTH);
    }

    private void startGame(boolean singlePlayer) {
        this.isSinglePlayer = singlePlayer;
        initGameLogic();
        showPanel("GAME");
    }

    private void showPanel(String name) {
        CardLayout cl = (CardLayout) (getContentPane().getLayout());
        cl.show(getContentPane(), name);
    }

    private void initGameLogic() {
        ArrayList<ShapeIcon> iconList = new ArrayList<>();
        ShapeType[] types = ShapeType.values();
        Color[] colors = {Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.WHITE, new Color(170, 170, 255)};

        for (int i = 0; i < 8; i++) {
            iconList.add(new ShapeIcon(types[i], colors[i]));
            iconList.add(new ShapeIcon(types[i], colors[i]));
        }
        Collections.shuffle(iconList);

        for (int i = 0; i < 16; i++) {
            cardIcons[i] = iconList.get(i);
            matched[i] = false;
            cards[i].setIcon(null);
            cards[i].setBackground(HIDDEN_COLOR);
            cards[i].setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 2));
            cards[i].setEnabled(true);
        }

        player1Score = 0;
        player2Score = 0;
        currentPlayer = 1;
        firstIndex = -1;
        lock = false;
        updateUI();
    }

    private void updateUI() {
        p1Label.setText("Player 1: " + player1Score);
        p2Label.setText((isSinglePlayer ? "CPU: " : "Player 2: ") + player2Score);
        
        if (currentPlayer == 1) {
            status.setText("Player 1's Turn");
            p1Label.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));
            p2Label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        } else {
            status.setText(isSinglePlayer ? "CPU is thinking..." : "Player 2's Turn");
            p2Label.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));
            p1Label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (lock || (isSinglePlayer && currentPlayer == 2)) return;
        handleSelection(e.getSource());
    }

    private void handleSelection(Object source) {
        for (int i = 0; i < 16; i++) {
            if (source == cards[i]) {
                if (matched[i] || i == firstIndex) return;
                cards[i].setIcon(cardIcons[i]);
                if (firstIndex == -1) {
                    firstIndex = i;
                } else {
                    secondIndex = i;
                    lock = true;
                    checkMatch();
                }
            }
        }
    }

    private void checkMatch() {
        if (cardIcons[firstIndex].type == cardIcons[secondIndex].type) {
            matched[firstIndex] = true;
            matched[secondIndex] = true;
            cards[firstIndex].setBackground(MATCHED_BG_COLOR);
            cards[secondIndex].setBackground(MATCHED_BG_COLOR);
            
            if (currentPlayer == 1) player1Score++;
            else player2Score++;

            javax.swing.Timer t = new javax.swing.Timer(600, e -> {
                lock = false;
                firstIndex = -1;
                updateUI();
                if (!checkWin() && isSinglePlayer && currentPlayer == 2) cpuMove();
            });
            t.setRepeats(false);
            t.start();
        } else {
            cards[firstIndex].setBorder(BorderFactory.createLineBorder(ERROR_COLOR, 3));
            cards[secondIndex].setBorder(BorderFactory.createLineBorder(ERROR_COLOR, 3));

            javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
                cards[firstIndex].setIcon(null);
                cards[secondIndex].setIcon(null);
                cards[firstIndex].setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 2));
                cards[secondIndex].setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90), 2));
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
                firstIndex = -1;
                lock = false;
                updateUI();
                if (isSinglePlayer && currentPlayer == 2) cpuMove();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void cpuMove() {
        ArrayList<Integer> available = new ArrayList<>();
        for (int i = 0; i < 16; i++) if (!matched[i]) available.add(i);
        if (available.size() < 2) return;

        Collections.shuffle(available);
        int c1 = available.get(0);
        int c2 = available.get(1);

        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            handleSelection(cards[c1]);
            javax.swing.Timer t2 = new javax.swing.Timer(800, e2 -> handleSelection(cards[c2]));
            t2.setRepeats(false);
            t2.start();
        });
        t.setRepeats(false);
        t.start();
    }

    private boolean checkWin() {
        for (boolean b : matched) if (!b) return false;
        String winner = player1Score > player2Score ? "Player 1 Wins!" : 
                       (player2Score > player1Score ? (isSinglePlayer ? "CPU Wins!" : "Player 2 Wins!") : "Draw!");
        JOptionPane.showMessageDialog(this, "Game Over! " + winner);
        showPanel("MENU");
        return true;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createScoreLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setOpaque(true);
        label.setBackground(BG_DARK);
        return label;
    }

    // ORIGINAL SHAPE DRAWING RESTORED
    class ShapeIcon implements Icon {
        private final ShapeType type;
        private final Color shapeColor;
        public ShapeIcon(ShapeType type, Color color) { this.type = type; this.shapeColor = color; }

        @Override public int getIconWidth() { return 80; }
        @Override public int getIconHeight() { return 80; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = 25;
            int size = Math.min(c.getWidth(), c.getHeight()) - (pad * 2);
            int cx = (c.getWidth() - size) / 2;
            int cy = (c.getHeight() - size) / 2;
            g2.setColor(shapeColor);

            switch (type) {
                case HEXAGON -> {
                    Polygon hex = new Polygon();
                    for (int i = 0; i < 6; i++) {
                        hex.addPoint((int) (cx + size/2 + size/2 * Math.cos(i * 2 * Math.PI / 6)),
                                     (int) (cy + size/2 + size/2 * Math.sin(i * 2 * Math.PI / 6)));
                    }
                    g2.fillPolygon(hex);
                }
                case SQUARE -> g2.fillRect(cx, cy, size, size);
                case TRIANGLE -> {
                    int[] xPts = {cx + size/2, cx, cx + size};
                    int[] yPts = {cy, cy + size, cy + size};
                    g2.fillPolygon(xPts, yPts, 3);
                }
                case DIAMOND -> {
                    int[] xPts = {cx + size/2, cx + size, cx + size/2, cx};
                    int[] yPts = {cy, cy + size/2, cy + size, cy + size/2};
                    g2.fillPolygon(xPts, yPts, 4);
                }
                case CROSS -> {
                    g2.setStroke(new BasicStroke(size/4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx, cy, cx + size, cy + size);
                    g2.drawLine(cx + size, cy, cx, cy + size);
                }
                case RING -> {
                    g2.setStroke(new BasicStroke(size/5f));
                    g2.drawOval(cx + 4, cy + 4, size - 8, size - 8);
                }
                case OVAL -> g2.fillOval(cx, cy + size/4, size, size/2);
                case STAR -> {
                    g2.fillRect(cx + size/3, cy, size/3, size);
                    g2.fillRect(cx, cy + size/3, size, size/3);
                }
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShapeMemoryGame::new);
    }
}