import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores built-in and user-trained question-answer pairs.
 */
public class KnowledgeBase {
    private static final String DATA_DIRECTORY = "data";
    private static final String KNOWLEDGE_FILE = "knowledge_base.txt";

    private final Map<String, String> responses = new LinkedHashMap<>();
    private final NLPProcessor nlpProcessor;
    private final Path knowledgeFilePath;

    public KnowledgeBase(NLPProcessor nlpProcessor) {
        this.nlpProcessor = nlpProcessor;
        this.knowledgeFilePath = Paths.get(DATA_DIRECTORY, KNOWLEDGE_FILE);
        loadDefaultKnowledge();
        loadKnowledgeFromFile();
    }

    public String getExactResponse(String question) {
        return responses.get(nlpProcessor.normalize(question));
    }

    public MatchResult findBestMatch(String question) {
        String normalizedQuestion = nlpProcessor.normalize(question);
        String exact = responses.get(normalizedQuestion);
        if (exact != null) {
            return new MatchResult(exact, 100);
        }

        Set<String> userKeywords = nlpProcessor.extractKeywords(question);
        String bestAnswer = null;
        int bestScore = 0;

        for (Map.Entry<String, String> entry : responses.entrySet()) {
            Set<String> storedKeywords = nlpProcessor.extractKeywords(entry.getKey());
            int score = calculateSimilarity(normalizedQuestion, entry.getKey(), userKeywords, storedKeywords);
            if (score > bestScore) {
                bestScore = score;
                bestAnswer = entry.getValue();
            }
        }

        if (bestAnswer != null && bestScore >= 35) {
            return new MatchResult(bestAnswer, bestScore);
        }
        return null;
    }

    public void addKnowledge(String question, String answer) throws IOException {
        if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
            throw new IllegalArgumentException("Question and answer are required.");
        }

        String normalizedQuestion = nlpProcessor.normalize(question);
        responses.put(normalizedQuestion, answer.trim());
        saveKnowledgePair(normalizedQuestion, answer.trim());
    }

    public Map<String, String> getAllResponses() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(responses));
    }

    private int calculateSimilarity(String question, String storedQuestion, Set<String> userKeywords,
                                    Set<String> storedKeywords) {
        int score = 0;

        if (storedQuestion.contains(question) || question.contains(storedQuestion)) {
            score += 45;
        }

        int keywordMatches = 0;
        for (String keyword : userKeywords) {
            if (storedKeywords.contains(keyword) || storedQuestion.contains(keyword)) {
                keywordMatches++;
            }
        }

        if (!userKeywords.isEmpty()) {
            score += (keywordMatches * 50) / userKeywords.size();
        }

        score += Math.max(0, 20 - levenshteinDistance(question, storedQuestion));
        return Math.min(score, 100);
    }

    private int levenshteinDistance(String first, String second) {
        int[][] dp = new int[first.length() + 1][second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int cost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[first.length()][second.length()];
    }

    private void loadDefaultKnowledge() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("hello", "Hello! I am happy to chat with you. Ask me about Java, AI, programming, or college preparation.");
        defaults.put("hi", "Hi there! How can I help you today?");
        defaults.put("how are you", "I am running smoothly and ready to help you learn.");
        defaults.put("what is your name", "My name is AI Chatbot Assistant.");
        defaults.put("who created you", "I was created as a Java Swing internship project using OOP, NLP, and file handling concepts.");
        defaults.put("goodbye", "Goodbye! Keep learning and coding.");
        defaults.put("what is java", "Java is a high-level, object-oriented programming language known for platform independence through the JVM.");
        defaults.put("what is oop", "OOP, or Object-Oriented Programming, organizes software around classes, objects, encapsulation, inheritance, polymorphism, and abstraction.");
        defaults.put("what is inheritance", "Inheritance allows one class to acquire fields and methods from another class, promoting code reuse.");
        defaults.put("what is polymorphism", "Polymorphism lets the same method name behave differently based on the object or parameters involved.");
        defaults.put("what is encapsulation", "Encapsulation protects data by keeping fields private and exposing controlled access through methods.");
        defaults.put("what is abstraction", "Abstraction hides implementation details and shows only essential features to the user.");
        defaults.put("what is python", "Python is a beginner-friendly, high-level programming language used in web development, automation, AI, and data science.");
        defaults.put("what is machine learning", "Machine Learning is a branch of AI where systems learn patterns from data and improve predictions or decisions over time.");
        defaults.put("what is artificial intelligence", "Artificial Intelligence is the field of creating systems that can perform tasks requiring human-like intelligence.");
        defaults.put("what is data science", "Data Science combines statistics, programming, and domain knowledge to extract useful insights from data.");
        defaults.put("what is a database", "A database is an organized collection of data that can be stored, searched, updated, and managed efficiently.");
        defaults.put("tell me about computer science", "Computer Science studies computation, algorithms, software, hardware, networks, data, and intelligent systems.");
        defaults.put("what are software engineering careers", "Software engineering careers include developer, tester, DevOps engineer, data engineer, cloud engineer, security analyst, and project manager.");
        defaults.put("how can i prepare for interviews", "Prepare by revising core subjects, practicing coding problems, building projects, improving communication, and doing mock interviews.");
        defaults.put("how can i improve coding skills", "Improve coding skills by practicing daily, reading good code, building projects, debugging patiently, and learning data structures.");

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            responses.put(nlpProcessor.normalize(entry.getKey()), entry.getValue());
        }
    }

    private void loadKnowledgeFromFile() {
        try {
            Files.createDirectories(knowledgeFilePath.getParent());
            if (!Files.exists(knowledgeFilePath)) {
                Files.createFile(knowledgeFilePath);
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(knowledgeFilePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        responses.put(nlpProcessor.normalize(parts[0]), parts[1].trim());
                    }
                }
            }
        } catch (IOException exception) {
            System.err.println("Unable to load knowledge file: " + exception.getMessage());
        }
    }

    private void saveKnowledgePair(String question, String answer) throws IOException {
        Files.createDirectories(knowledgeFilePath.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(knowledgeFilePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(question + "|" + answer);
            writer.newLine();
        }
    }

    public static class MatchResult {
        private final String answer;
        private final int confidence;

        public MatchResult(String answer, int confidence) {
            this.answer = answer;
            this.confidence = confidence;
        }

        public String getAnswer() {
            return answer;
        }

        public int getConfidence() {
            return confidence;
        }
    }
}
