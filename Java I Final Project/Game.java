import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static java.lang.Math.abs;

class Piece {
    int x, y, row, col, kind, match;

    public Piece() {
        match = 0;
    }
}

public class Game extends JPanel implements Runnable, MouseListener {
    final int WIDTH = 740;
    final int HEIGHT = 480;

    boolean isRunning;
    Thread thread;
    BufferedImage view;

    Piece[][] grid;
    BufferedImage background, cursor;
    BufferedImage[] gemImages = new BufferedImage[7]; // Array for gem images

    MouseEvent mouse;
    int tileSize = 54;
    int offsetX = 48, offsetY = 24;
    int x0, y0, x, y;
    int click = 0;
    int posX, posY;
    int speedSwapAnimation = 4;
    boolean isSwap = false, isMoving = false;

    int score = 0;
    int level = 1;
    int goalScore = 10000;

    JLabel scoreLabel;
    JLabel levelLabel;
    JLabel goalLabel;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
    }

    public static void main(String[] args) {
        JFrame w = new JFrame("Match-3 Game");
        w.setResizable(false);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Game game = new Game();
        w.add(game);
        w.pack();
        w.setLocationRelativeTo(null);
        w.setVisible(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    public void swap(Piece p1, Piece p2) {
        int rowAux = p1.row;
        p1.row = p2.row;
        p2.row = rowAux;

        int colAux = p1.col;
        p1.col = p2.col;
        p2.col = colAux;

        grid[p1.row][p1.col] = p1;
        grid[p2.row][p2.col] = p2;
    }

    public void start() {
        try {
            view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            grid = new Piece[10][10];

            background = ImageIO.read(getClass().getResource("/background.png"));
            cursor = ImageIO.read(getClass().getResource("/cursor.png"));

            // Load each gem image
            for (int i = 0; i < gemImages.length; i++) {
                gemImages[i] = ImageIO.read(getClass().getResource("/gems" + i + ".png"));
            }

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    grid[i][j] = new Piece();
                }
            }

            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    grid[i][j].kind = (new Random().nextInt(7));
                    grid[i][j].row = i;
                    grid[i][j].col = j;
                    grid[i][j].x = j * tileSize;
                    grid[i][j].y = i * tileSize;
                }
            }

            // Initialize and configure the scoreboard, level, and goal labels
            scoreLabel = new JLabel("Score: " + score);
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            scoreLabel.setForeground(Color.WHITE);
            scoreLabel.setBounds(600, 10, 120, 30);
            add(scoreLabel);

            levelLabel = new JLabel("Level: " + level);
            levelLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            levelLabel.setForeground(Color.WHITE);
            levelLabel.setBounds(600, 50, 120, 30);
            add(levelLabel);

            goalLabel = new JLabel("Target: " + goalScore);
            goalLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            goalLabel.setForeground(Color.WHITE);
            goalLabel.setBounds(600, 90, 120, 30);
            add(goalLabel);

            // Display welcome message and instructions
            displayWelcomeAndInstructions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayWelcomeAndInstructions() {
        String welcomeMessage = "Welcome to the Match-3 Game!";
        JOptionPane.showMessageDialog(this, welcomeMessage, "Match-3 Game", JOptionPane.INFORMATION_MESSAGE);

        String instructions = "Instructions:\n"
                + "- Click on two adjacent gems to swap them.\n"
                + "- Match 3 or more gems of the same kind horizontally or vertically to score points.\n"
                + "- Reach the goal score to advance to the next level.\n"
                + "- The game consists of 5 levels with normal difficulty.\n"
                + "- To complete this game, finish level 5.\n"
                + "- Have fun and enjoy the game!";
        JOptionPane.showMessageDialog(this, instructions, "Match-3 Game", JOptionPane.INFORMATION_MESSAGE);
    }




    public void update() {
        if (mouse != null && mouse.getID() == MouseEvent.MOUSE_PRESSED) {
            if (mouse.getButton() == MouseEvent.BUTTON1) {
                if (!isSwap && !isMoving) {
                    click++;
                }
                posX = mouse.getX() - offsetX;
                posY = mouse.getY() - offsetY;

                if (click == 1) {
                    x0 = posX / tileSize + 1;
                    y0 = posY / tileSize + 1;
                }
                if (click == 2) {
                    x = posX / tileSize + 1;
                    y = posY / tileSize + 1;
                    if (abs(x - x0) + abs(y - y0) == 1) {
                        swap(grid[y0][x0], grid[y][x]);
                        isSwap = true;
                        click = 0;
                    } else {
                        click = 1;
                    }
                }
            }
            mouse = null;
        }

        // Match finding
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if (grid[i][j].kind == grid[i + 1][j].kind) {
                    if (grid[i][j].kind == grid[i - 1][j].kind) {
                        for (int n = -1; n <= 1; n++) {
                            grid[i + n][j].match++;
                        }
                    }
                }
                if (grid[i][j].kind == grid[i][j + 1].kind) {
                    if (grid[i][j].kind == grid[i][j - 1].kind) {
                        for (int n = -1; n <= 1; n++) {
                            grid[i][j + n].match++;
                        }
                    }
                }
            }
        }

        // Moving animation
        isMoving = false;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                Piece p = grid[i][j];
                int dx = 0, dy = 0;
                for (int n = 0; n < speedSwapAnimation; n++) {
                    dx = p.x - p.col * tileSize;
                    dy = p.y - p.row * tileSize;
                    if (dx != 0) {
                        p.x -= dx / abs(dx);
                    }
                    if (dy != 0) {
                        p.y -= dy / abs(dy);
                    }
                }
                if (dx != 0 || dy != 0) {
                    isMoving = true;
                }
            }
        }

        // Get score from gem matches
        int matchScore = 0;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if (grid[i][j].match >= 3) {
                    // Award points for matches (adjust the points as needed)
                    if (grid[i][j].match == 3) {
                        matchScore += 90;
                    } else if (grid[i][j].match == 4) {
                        matchScore += 250;
                    }
                }
            }
        }

        // Update the total score
        score += matchScore;
        scoreLabel.setText("Score: " + score);

        // Check if the goal score is reached
        if (score >= goalScore) {
            level++;
            levelLabel.setText("Level: " + level);
            goalScore += 10000; // Increase the goal score for the next level
            goalLabel.setText("Goal: " + goalScore);
            if (level > 5) {
                isRunning = false; // Stop the game
                int option = JOptionPane.showOptionDialog(this,
                        "Congratulations! You have completed level 5. Would you like to start again?",
                        "Level Completed",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Start Again", "Exit"},
                        "Start Again");
        
                if (option == JOptionPane.YES_OPTION) {
                    restartGame(); // Restart the game without showing welcome and instructions
                } else {
                    System.exit(0);
                }
                return; // Exit the update method
            }
        }

        // Second swap if no match
        if (isSwap && !isMoving) {
            if (matchScore == 0) {
                swap(grid[y0][x0], grid[y][x]);
            }
            isSwap = false;
        }

        // Update grid
        if (!isMoving) {
            for (int i = 8; i > 0; i--) {
                for (int j = 1; j <= 8; j++) {
                    if (grid[i][j].match != 0) {
                        for (int n = i; n > 0; n--) {
                            if (grid[n][j].match == 0) {
                                swap(grid[n][j], grid[i][j]);
                                break;
                            }
                        }
                    }
                }
            }
            for (int j = 1; j <= 8; j++) {
                for (int i = 8, n = 0; i > 0; i--) {
                    if (grid[i][j].match != 0) {
                        grid[i][j].kind = new Random().nextInt(7);
                        grid[i][j].y = -tileSize * n++;
                        grid[i][j].match = 0;
                    }
                }
            }
        }
    }


    private void restartGame() {
        score = 0;
        level = 1;
        goalScore = 10000;
        scoreLabel.setText("Score: " + score);
        levelLabel.setText("Level: " + level);
        goalLabel.setText("Goal: " + goalScore);
    
        // Reset the grid and gems as you did in the start() method
    
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }
       

    public void draw() {
        Graphics2D g2 = (Graphics2D) view.getGraphics();
        g2.drawImage(background, 0, 0, WIDTH, HEIGHT, null);
    
        // Define the colors for the checkerboard
        Color darkColor = new Color(169, 58, 58); // Dark squares
        Color lightColor = new Color(255, 229, 229); // Light squares
    
        // Draw the checkerboard pattern
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    g2.setColor(darkColor);
                } else {
                    g2.setColor(lightColor);
                }
                g2.fillRect(offsetX + j * tileSize, offsetY + i * tileSize, tileSize, tileSize);
            }
        }
    
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                BufferedImage gemImage = gemImages[grid[i][j].kind];
                int gemX = grid[i][j].x + (offsetX - tileSize);
                int gemY = grid[i][j].y + (offsetY - tileSize);
        
                g2.drawImage(gemImage, gemX, gemY, tileSize, tileSize, null);
        
                // Show cursor
                if (click == 1) {
                    if (x0 == j && y0 == i) {
                        g2.drawImage(cursor, gemX, gemY, tileSize, tileSize, null);
                    }
                }
            }
        }


        // Draw the scoreboard, level, and goal labels
        g2.setColor(Color.RED);
        g2.fillRect(600, 10, 120, 30);
        g2.fillRect(600, 50, 120, 30);
        g2.fillRect(600, 90, 120, 30);
        g2.drawImage(scoreLabelToImage(scoreLabel), 600, 10, null);
        g2.drawImage(scoreLabelToImage(levelLabel), 600, 50, null);
        g2.drawImage(scoreLabelToImage(goalLabel), 600, 90, null);

        Graphics g = getGraphics();
        g.drawImage(view, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
    }

    // Convert JLabel to BufferedImage
    private BufferedImage scoreLabelToImage(JLabel label) {
        int width = label.getWidth();
        int height = label.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        label.paint(g2);
        g2.dispose();
        return image;
    }

    @Override
    public void run() {
        try {
            start();
            while (isRunning) {
                update();
                draw();
                Thread.sleep(1000 / 60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouse = e;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}