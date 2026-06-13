import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Java Swing user interface for the AI Chatbot Assistant.
 */
public class ChatBotGUI extends JFrame {
    private final ChatBot chatBot;
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private final JButton clearButton;
    private final JButton exitButton;
    private final JButton faqButton;
    private final JButton exportButton;
    private final JCheckBox darkModeToggle;
    private final JLabel statusLabel;

    private boolean darkMode;

    public ChatBotGUI() {
        this.chatBot = new ChatBot();
        this.chatArea = new JTextArea();
        this.inputField = new JTextField();
        this.sendButton = createButton("Send");
        this.clearButton = createButton("Clear Chat");
        this.exitButton = createButton("Exit");
        this.faqButton = createButton("FAQ Panel");
        this.exportButton = createButton("Export History");
        this.darkModeToggle = new JCheckBox("Dark Mode");
        this.statusLabel = new JLabel("Ready | Sentiment: Neutral");

        configureWindow();
        buildInterface();
        registerEvents();
        applyTheme(false);
        appendBotMessage("Hello! I am AI Chatbot Assistant. Type help to see commands.");
    }

    private void configureWindow() {
        setTitle("AI Chatbot Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
    }

    private void buildInterface() {
        setLayout(new BorderLayout(12, 12));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("AI Chatbot Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subtitleLabel = new JLabel("Java Swing | OOP | NLP | File Handling");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(darkModeToggle, BorderLayout.EAST);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(205, 213, 224)));

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(174, 184, 196)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.add(faqButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return button;
    }

    private void registerEvents() {
        sendButton.addActionListener(this::handleSend);
        inputField.addActionListener(this::handleSend);
        clearButton.addActionListener(event -> clearChat());
        exitButton.addActionListener(event -> exitApplication());
        faqButton.addActionListener(event -> showFAQPanel());
        exportButton.addActionListener(event -> exportChatHistory());
        darkModeToggle.addActionListener(event -> applyTheme(darkModeToggle.isSelected()));
    }

    private void handleSend(ActionEvent event) {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message.", "Empty Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        appendUserMessage(userInput);
        inputField.setText("");

        ChatBot.ChatResponse response = chatBot.respond(userInput);
        statusLabel.setText("Ready | Sentiment: " + formatSentiment(response.getSentiment().name()));

        if (response.getIntent() == NLPProcessor.Intent.CLEAR) {
            clearChat();
            return;
        }

        appendBotMessage(response.getMessage());

        if (response.getIntent() == NLPProcessor.Intent.LEARN) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Learning", JOptionPane.INFORMATION_MESSAGE);
        }
        if (response.getIntent() == NLPProcessor.Intent.EXIT) {
            exitApplication();
        }
    }

    private void appendUserMessage(String message) {
        appendChatBubble("You", message, true);
    }

    private void appendBotMessage(String message) {
        appendChatBubble("Bot", message, false);
    }

    private void appendChatBubble(String sender, String message, boolean user) {
        String border = user ? ">> " : "<< ";
        chatArea.append(border + sender + "\n");
        chatArea.append(wrapWithIndent(message, "   ") + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private String wrapWithIndent(String message, String indent) {
        StringBuilder builder = new StringBuilder();
        String[] lines = message.split("\\n");
        for (String line : lines) {
            builder.append(indent).append(line).append('\n');
        }
        return builder.toString().trim();
    }

    private void clearChat() {
        chatArea.setText("");
        appendBotMessage("Chat cleared. How can I help you next?");
        JOptionPane.showMessageDialog(this, "Chat display cleared. History file is still saved.", "Clear Chat",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this, "Exit AI Chatbot Assistant?", "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void showFAQPanel() {
        Map<String, String> responses = chatBot.getKnowledgeBase().getAllResponses();
        JTextArea faqArea = new JTextArea();
        faqArea.setEditable(false);
        faqArea.setLineWrap(true);
        faqArea.setWrapStyleWord(true);
        faqArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            content.append("Q: ").append(entry.getKey()).append('\n');
            content.append("A: ").append(entry.getValue()).append("\n\n");
        }
        faqArea.setText(content.toString());
        faqArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(faqArea);
        scrollPane.setPreferredSize(new Dimension(620, 420));

        int option = JOptionPane.showConfirmDialog(this, scrollPane, "FAQ Management Panel",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            showAddFAQDialog();
        }
    }

    private void showAddFAQDialog() {
        JTextField questionField = new JTextField();
        JTextField answerField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.add(new JLabel("New Question"));
        panel.add(questionField);
        panel.add(new JLabel("Answer"));
        panel.add(answerField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add FAQ", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                chatBot.getKnowledgeBase().addKnowledge(questionField.getText(), answerField.getText());
                JOptionPane.showMessageDialog(this, "FAQ saved to knowledge_base.txt.", "FAQ Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportChatHistory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Chat History");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setSelectedFile(Paths.get("chat_history_export_" + timestamp + ".txt").toFile());

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                Path source = Paths.get("data", "chat_history.txt");
                Path target = fileChooser.getSelectedFile().toPath();
                if (!Files.exists(source)) {
                    Files.write(source, "No chat history available.".getBytes(StandardCharsets.UTF_8));
                }
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Chat history exported successfully.", "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(this, "Export failed: " + exception.getMessage(), "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyTheme(boolean enabled) {
        darkMode = enabled;
        Color background = darkMode ? new Color(28, 32, 38) : new Color(245, 247, 250);
        Color panel = darkMode ? new Color(39, 45, 54) : Color.WHITE;
        Color text = darkMode ? new Color(232, 236, 241) : new Color(31, 41, 55);
        Color accent = darkMode ? new Color(89, 147, 255) : new Color(37, 99, 235);
        Color secondary = darkMode ? new Color(64, 72, 84) : new Color(229, 234, 242);

        getContentPane().setBackground(background);
        updateComponentTheme(getContentPane(), background, text);
        chatArea.setBackground(panel);
        chatArea.setForeground(text);
        chatArea.setCaretColor(text);
        inputField.setBackground(panel);
        inputField.setForeground(text);
        inputField.setCaretColor(text);
        darkModeToggle.setBackground(background);
        darkModeToggle.setForeground(text);
        statusLabel.setForeground(darkMode ? new Color(180, 190, 204) : new Color(75, 85, 99));

        JButton[] primaryButtons = {sendButton};
        for (JButton button : primaryButtons) {
            button.setBackground(accent);
            button.setForeground(Color.WHITE);
        }

        JButton[] secondaryButtons = {clearButton, exitButton, faqButton, exportButton};
        for (JButton button : secondaryButtons) {
            button.setBackground(secondary);
            button.setForeground(text);
        }
        repaint();
    }

    private void updateComponentTheme(java.awt.Container container, Color background, Color text) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JPanel) {
                component.setBackground(background);
                updateComponentTheme((java.awt.Container) component, background, text);
            } else if (component instanceof JLabel) {
                component.setForeground(text);
            } else if (component instanceof JCheckBox) {
                component.setBackground(background);
                component.setForeground(text);
            }
        }
    }

    private String formatSentiment(String sentiment) {
        return sentiment.substring(0, 1) + sentiment.substring(1).toLowerCase();
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            System.err.println("Unable to set system look and feel: " + exception.getMessage());
        }
        SwingUtilities.invokeLater(() -> new ChatBotGUI().setVisible(true));
    }
}
