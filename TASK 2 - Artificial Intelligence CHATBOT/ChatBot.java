import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Main chatbot brain that combines NLP, rule-based responses, learning, and history saving.
 */
public class ChatBot {
    private static final Path HISTORY_FILE = Paths.get("data", "chat_history.txt");
    private static final DateTimeFormatter HISTORY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NLPProcessor nlpProcessor;
    private final KnowledgeBase knowledgeBase;
    private final Random random = new Random();
    private String lastTopic = "";

    private final List<String> greetings = Arrays.asList(
            "Hello! What would you like to learn today?",
            "Hi! I can answer questions about Java, AI, programming, and career preparation.",
            "Welcome back. Ask me a question or type help for commands."
    );

    private final List<String> thanksResponses = Arrays.asList(
            "You are welcome!",
            "Happy to help.",
            "Anytime. Keep going with your learning."
    );

    private final List<String> unknownResponses = Arrays.asList(
            "I do not know that yet. You can teach me using: learn question = answer",
            "I am still learning. Try asking in another way, or teach me with the learn command.",
            "That is outside my current knowledge base. Type help to see what I can do."
    );

    public ChatBot() {
        this.nlpProcessor = new NLPProcessor();
        this.knowledgeBase = new KnowledgeBase(nlpProcessor);
        ensureHistoryFile();
    }

    public ChatResponse respond(String userInput) {
        NLPProcessor.Intent intent = nlpProcessor.recognizeIntent(userInput);
        NLPProcessor.Sentiment sentiment = nlpProcessor.analyzeSentiment(userInput);
        String response;
        boolean command = false;

        try {
            switch (intent) {
                case EMPTY:
                    response = "Please type a message first.";
                    break;
                case GREETING:
                    response = randomItem(greetings);
                    lastTopic = "greeting";
                    break;
                case THANKS:
                    response = randomItem(thanksResponses);
                    break;
                case HELP:
                    response = getHelpMessage();
                    command = true;
                    break;
                case CLEAR:
                    response = "clear";
                    command = true;
                    break;
                case TIME:
                    response = "Current time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
                    command = true;
                    break;
                case DATE:
                    response = "Today's date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
                    command = true;
                    break;
                case EXIT:
                    response = "Goodbye! Your chat history has been saved.";
                    command = true;
                    break;
                case LEARN:
                    response = learn(userInput);
                    command = true;
                    break;
                default:
                    response = answerQuestion(userInput, sentiment);
                    break;
            }
        } catch (Exception exception) {
            response = "Sorry, an error occurred: " + exception.getMessage();
        }

        saveConversation("User", userInput);
        if (!"clear".equals(response)) {
            saveConversation("Bot", response);
        }
        return new ChatResponse(response, intent, sentiment, command);
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public void saveConversation(String sender, String message) {
        try {
            Files.createDirectories(HISTORY_FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(HISTORY_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write("[" + LocalDateTime.now().format(HISTORY_FORMAT) + "] " + sender + ": " + message);
                writer.newLine();
            }
        } catch (IOException exception) {
            System.err.println("Unable to save chat history: " + exception.getMessage());
        }
    }

    private String answerQuestion(String userInput, NLPProcessor.Sentiment sentiment) {
        KnowledgeBase.MatchResult match = knowledgeBase.findBestMatch(userInput);
        if (match != null) {
            lastTopic = inferTopic(userInput);
            String prefix = "";
            if (sentiment == NLPProcessor.Sentiment.NEGATIVE) {
                prefix = "No worries, we can make it simpler. ";
            }
            return prefix + match.getAnswer() + " (Matched with " + match.getConfidence() + "% confidence)";
        }

        if (!lastTopic.isEmpty() && nlpProcessor.containsPattern(userInput, "\\b(more|explain|details|example)\\b")) {
            return getContextResponse(lastTopic);
        }

        return randomItem(unknownResponses);
    }

    private String learn(String userInput) throws IOException {
        NLPProcessor.LearnRequest learnRequest = nlpProcessor.parseLearnRequest(userInput);
        if (learnRequest == null) {
            return "Learning format: learn question = answer";
        }
        knowledgeBase.addKnowledge(learnRequest.getQuestion(), learnRequest.getAnswer());
        return "Learned successfully. Ask me: " + learnRequest.getQuestion();
    }

    private String inferTopic(String input) {
        String normalized = nlpProcessor.normalize(input);
        if (normalized.contains("java") || normalized.contains("oop") || normalized.contains("inheritance")
                || normalized.contains("polymorphism") || normalized.contains("encapsulation")
                || normalized.contains("abstraction")) {
            return "java";
        }
        if (normalized.contains("ai") || normalized.contains("artificial") || normalized.contains("machine learning")
                || normalized.contains("data science")) {
            return "ai";
        }
        if (normalized.contains("interview") || normalized.contains("career") || normalized.contains("coding")) {
            return "career";
        }
        return "general";
    }

    private String getContextResponse(String topic) {
        switch (topic) {
            case "java":
                return "Example: in Java, a class is a blueprint and an object is a real instance created from it.";
            case "ai":
                return "Example: an AI chatbot uses text processing and decision rules or trained models to respond to users.";
            case "career":
                return "Practical next step: build two projects, revise data structures, and explain your code out loud.";
            default:
                return "Ask a follow-up with a specific keyword, and I will try to connect it to what we discussed.";
        }
    }

    private String getHelpMessage() {
        return "Commands: help, clear, time, date, exit\n"
                + "Learning: learn question = answer\n"
                + "Try asking: What is Java? What is OOP? What is Machine Learning? How can I prepare for interviews?";
    }

    private String randomItem(List<String> items) {
        return items.get(random.nextInt(items.size()));
    }

    private void ensureHistoryFile() {
        try {
            Files.createDirectories(HISTORY_FILE.getParent());
            if (!Files.exists(HISTORY_FILE)) {
                Files.createFile(HISTORY_FILE);
            }
        } catch (IOException exception) {
            System.err.println("Unable to create history file: " + exception.getMessage());
        }
    }

    public static class ChatResponse {
        private final String message;
        private final NLPProcessor.Intent intent;
        private final NLPProcessor.Sentiment sentiment;
        private final boolean command;

        public ChatResponse(String message, NLPProcessor.Intent intent, NLPProcessor.Sentiment sentiment, boolean command) {
            this.message = message;
            this.intent = intent;
            this.sentiment = sentiment;
            this.command = command;
        }

        public String getMessage() {
            return message;
        }

        public NLPProcessor.Intent getIntent() {
            return intent;
        }

        public NLPProcessor.Sentiment getSentiment() {
            return sentiment;
        }

        public boolean isCommand() {
            return command;
        }
    }
}
