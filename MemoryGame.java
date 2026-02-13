import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MemoryGame extends JFrame implements ActionListener {

    JButton[] cards = new JButton[16];
    int[] values = new int[16];
    boolean[] matched = new boolean[16];

    int firstIndex = -1;
    int secondIndex = -1;
    boolean lock = false;

    JButton restartBtn, endBtn;
    JLabel status;

    public MemoryGame() {
        setTitle("Memory Card Game");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel for Status
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(30, 30, 30));
        status = new JLabel("Match all pairs!", JLabel.CENTER);
        status.setForeground(Color.WHITE);
        status.setFont(new Font("Segoe UI", Font.BOLD, 20));
        top.add(status, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // Game Grid
        JPanel grid = new JPanel(new GridLayout(4, 4, 15, 15));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        grid.setBackground(new Color(45, 45, 45));

        for (int i = 0; i < 16; i++) {
            cards[i] = new JButton("");
            cards[i].setFont(new Font("Segoe UI", Font.BOLD, 28));
            cards[i].setBackground(new Color(70, 70, 70));
            cards[i].setForeground(Color.WHITE);
            cards[i].setFocusPainted(false);
            cards[i].addActionListener(this);
            grid.add(cards[i]);
        }
        add(grid, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(30, 30, 30));
        restartBtn = new JButton("Restart");
        endBtn = new JButton("End Game");

        styleButton(restartBtn, new Color(0, 150, 0)); // Greenish
        styleButton(endBtn, new Color(180, 0, 0));     // Reddish

        restartBtn.addActionListener(e -> initGame());
        endBtn.addActionListener(e -> System.exit(0));

        bottom.add(restartBtn);
        bottom.add(endBtn);
        add(bottom, BorderLayout.SOUTH);

        initGame();
        setVisible(true);
    }

    void styleButton(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 40));
    }

    void initGame() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            list.add(i);
            list.add(i);
        }
        Collections.shuffle(list);

        for (int i = 0; i < 16; i++) {
            values[i] = list.get(i);
            matched[i] = false;
            cards[i].setText("");
            cards[i].setEnabled(true);
            cards[i].setBackground(new Color(70, 70, 70));
        }

        firstIndex = -1;
        secondIndex = -1;
        lock = false;
        status.setText("Match all pairs!");
    }

    public void actionPerformed(ActionEvent e) {
        if (lock) return;

        for (int i = 0; i < 16; i++) {
            if (e.getSource() == cards[i]) {
                if (matched[i] || i == firstIndex) return;

                reveal(i);

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

    void reveal(int i) {
        cards[i].setText(String.valueOf(values[i]));
        cards[i].setBackground(new Color(100, 100, 100));
    }

    void checkMatch() {
        if (values[firstIndex] == values[secondIndex]) {
            // MATCH FOUND
            matched[firstIndex] = true;
            matched[secondIndex] = true;
            
            // Turn green immediately
            cards[firstIndex].setBackground(new Color(40, 167, 69));
            cards[secondIndex].setBackground(new Color(40, 167, 69));
            
            status.setText("It's a Match!");
            
            // Brief pause so the user sees the green before continuing
            javax.swing.Timer t = new javax.swing.Timer(500, e -> {
                lock = false;
                firstIndex = -1;
                secondIndex = -1;
                checkWin();
            });
            t.setRepeats(false);
            t.start();
        } else {
            // NO MATCH
            status.setText("Not quite! Try again.");
            javax.swing.Timer t = new javax.swing.Timer(800, e -> {
                hide(firstIndex);
                hide(secondIndex);
                firstIndex = -1;
                secondIndex = -1;
                lock = false;
            });
            t.setRepeats(false);
            t.start();
        }
    }

    void hide(int i) {
        cards[i].setText("");
        cards[i].setBackground(new Color(70, 70, 70));
    }

    void checkWin() {
        boolean win = true;
        for (boolean b : matched) {
            if (!b) win = false;
        }
        if (win) {
            status.setText("You cleared the board!");
            showCelebration();
        }
    }

    void showCelebration() {
        JOptionPane.showMessageDialog(this, "🎉 Congratulations! You found all pairs! 🎉", "Victory", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Run UI on Event Dispatch Thread
        SwingUtilities.invokeLater(MemoryGame::new);
    }
}