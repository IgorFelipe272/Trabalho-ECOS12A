import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class BatalhaNavalUI extends JFrame {

    private JPanel playerPanel;
    private JPanel enemyPanel;
    private JButton[][] playerGrid = new JButton[10][10];  // Tabuleiro do jogador
    private JButton[][] enemyGrid = new JButton[10][10];   // Tabuleiro do oponente
    private Random random = new Random();
    private JButton selectedButton = null;  // Botão selecionado para o ataque

    public BatalhaNavalUI() {
        setTitle("Batalha Naval");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        playerPanel = createGridPanel(playerGrid, "Seu Tabuleiro");
        enemyPanel = createGridPanel(enemyGrid, "Tabuleiro do Oponente");

        mainPanel.add(playerPanel);
        mainPanel.add(enemyPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Botões para sortear barcos
        JButton sortearPlayerButton = new JButton("Sortear Barcos do Jogador");
        sortearPlayerButton.setFont(new Font("Arial", Font.BOLD, 14));
        sortearPlayerButton.addActionListener(e -> sortearBarcos(playerGrid));

        JButton sortearEnemyButton = new JButton("Sortear Barcos do Oponente");
        sortearEnemyButton.setFont(new Font("Arial", Font.BOLD, 14));
        sortearEnemyButton.addActionListener(e -> sortearBarcos(enemyGrid));

        // Botão de "Fogo"
        JButton fireButton = new JButton("Fogo");
        fireButton.setFont(new Font("Arial", Font.BOLD, 14));
        fireButton.addActionListener(e -> atirarNoOponente());

        // Adicionar os botões ao painel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sortearPlayerButton);
        buttonPanel.add(sortearEnemyButton);
        buttonPanel.add(fireButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Inicializar com barcos sorteados
        sortearBarcos(playerGrid);
        sortearBarcos(enemyGrid);

        setVisible(true);
    }

    // Método para criar um painel de tabuleiro com título
    private JPanel createGridPanel(JButton[][] grid, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel gridPanel = new JPanel(new GridLayout(10, 10, 2, 2));
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                grid[row][col] = new JButton();
                grid[row][col].setPreferredSize(new Dimension(40, 40));
                grid[row][col].setBackground(Color.CYAN); // Cor inicial (água)
                grid[row][col].setEnabled(grid == enemyGrid); // Habilitar somente o tabuleiro do oponente
                grid[row][col].addActionListener(new SquareSelectionListener(grid[row][col]));
                gridPanel.add(grid[row][col]);
            }
        }

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(gridPanel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        return panel;
    }

    // Método para sortear e posicionar os barcos em um tabuleiro
    private void sortearBarcos(JButton[][] grid) {
        limparTabuleiro(grid); // Limpa o tabuleiro antes de posicionar os novos barcos
        int[] tamanhosBarcos = {5, 4, 3, 3, 2}; // Tamanhos dos barcos

        for (int tamanho : tamanhosBarcos) {
            boolean posicionado = false;

            while (!posicionado) {
                int row = random.nextInt(10);
                int col = random.nextInt(10);
                boolean horizontal = random.nextBoolean();

                if (podeColocarBarco(grid, row, col, tamanho, horizontal)) {
                    colocarBarco(grid, row, col, tamanho, horizontal);
                    posicionado = true;
                }
            }
        }
    }

    // Método para limpar o tabuleiro (resetar para o estado inicial)
    private void limparTabuleiro(JButton[][] grid) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                grid[row][col].setBackground(Color.CYAN); // Reseta a cor para água
                grid[row][col].putClientProperty("barco", false); // Marca sem barco
            }
        }
    }

    // Verifica se o barco pode ser colocado na posição desejada
    private boolean podeColocarBarco(JButton[][] grid, int row, int col, int tamanho, boolean horizontal) {
        if (horizontal) {
            if (col + tamanho > 10) return false;
            for (int i = 0; i < tamanho; i++) {
                if (!grid[row][col + i].getBackground().equals(Color.CYAN)) return false;
            }
        } else {
            if (row + tamanho > 10) return false;
            for (int i = 0; i < tamanho; i++) {
                if (!grid[row + i][col].getBackground().equals(Color.CYAN)) return false;
            }
        }
        return true;
    }

    // Coloca o barco na posição determinada no tabuleiro
    private void colocarBarco(JButton[][] grid, int row, int col, int tamanho, boolean horizontal) {
        for (int i = 0; i < tamanho; i++) {
            JButton square = horizontal ? grid[row][col + i] : grid[row + i][col];
            square.setBackground(grid == playerGrid ? Color.GREEN : Color.ORANGE); // Cor diferente para oponente e jogador
            square.putClientProperty("barco", true); // Marcar o quadrado como contendo um barco
        }
    }

    // Método para atirar no oponente
    private void atirarNoOponente() {
        if (selectedButton != null) {
            boolean hasShip = (boolean) selectedButton.getClientProperty("barco");
            if (hasShip) {
                selectedButton.setBackground(Color.RED); // Acertou um barco
            } else {
                selectedButton.setBackground(Color.GRAY); // Errou (água)
            }
            // Remove a borda da seleção após o tiro
            selectedButton.setBorder(null);
            selectedButton = null; // Desseleciona o quadrado
        }
    }

    // Classe interna para gerenciar a seleção de quadrados
    private class SquareSelectionListener implements ActionListener {
        private final JButton button;

        public SquareSelectionListener(JButton button) {
            this.button = button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedButton != null) {
                selectedButton.setBorder(null); // Remove a borda da seleção anterior
            }
            selectedButton = button;
            selectedButton.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3)); // Destaca o quadrado selecionado
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BatalhaNavalUI::new);
    }
}
