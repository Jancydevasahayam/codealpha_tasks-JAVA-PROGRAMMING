import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs simple natural language processing tasks for the chatbot.
 */
public class NLPProcessor {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "am", "was", "were", "be", "been",
            "being", "to", "of", "for", "in", "on", "at", "by", "with", "and",
            "or", "from", "me", "my", "you", "your", "please", "can", "could",
            "would", "should", "tell", "about", "what", "who", "how", "do", "does"
    ));

    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "good", "great", "excellent", "happy", "awesome", "thanks", "thank",
            "love", "nice", "wonderful", "amazing", "best", "cool", "helpful"
    ));

    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
            "bad", "sad", "angry", "upset", "hate", "terrible", "poor", "worst",
            "confused", "problem", "issue", "error", "difficult", "hard"
    ));

    private static final Pattern LEARN_PATTERN = Pattern.compile(
            "^learn\\s+(.+?)\\s*=\\s*(.+)$", Pattern.CASE_INSENSITIVE);

    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s=]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public List<String> tokenize(String input) {
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(normalized.split("\\s+")));
    }

    public Set<String> extractKeywords(String input) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String token : tokenize(input)) {
            if (!STOP_WORDS.contains(token) && token.length() > 1) {
                keywords.add(token);
            }
        }
        return keywords;
    }

    public Intent recognizeIntent(String input) {
        String normalized = normalize(input);
        Set<String> keywords = extractKeywords(input);

        if (normalized.isEmpty()) {
            return Intent.EMPTY;
        }
        if (normalized.equals("help") || keywords.contains("help")) {
            return Intent.HELP;
        }
        if (normalized.equals("clear")) {
            return Intent.CLEAR;
        }
        if (normalized.equals("time")) {
            return Intent.TIME;
        }
        if (normalized.equals("date")) {
            return Intent.DATE;
        }
        if (normalized.equals("exit") || normalized.contains("goodbye") || normalized.contains("bye")) {
            return Intent.EXIT;
        }
        if (normalized.matches(".*\\b(hello|hi|hey)\\b.*")) {
            return Intent.GREETING;
        }
        if (normalized.matches(".*\\b(thank|thanks|thankyou)\\b.*")) {
            return Intent.THANKS;
        }
        if (normalized.startsWith("learn ")) {
            return Intent.LEARN;
        }
        return Intent.QUESTION;
    }

    public LearnRequest parseLearnRequest(String input) {
        Matcher matcher = LEARN_PATTERN.matcher(input == null ? "" : input.trim());
        if (matcher.matches()) {
            return new LearnRequest(matcher.group(1).trim(), matcher.group(2).trim());
        }
        return null;
    }

    public Sentiment analyzeSentiment(String input) {
        int score = 0;
        for (String token : tokenize(input)) {
            if (POSITIVE_WORDS.contains(token)) {
                score++;
            }
            if (NEGATIVE_WORDS.contains(token)) {
                score--;
            }
        }
        if (score > 0) {
            return Sentiment.POSITIVE;
        }
        if (score < 0) {
            return Sentiment.NEGATIVE;
        }
        return Sentiment.NEUTRAL;
    }

    public boolean containsPattern(String input, String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input).find();
    }

    public enum Intent {
        EMPTY, GREETING, THANKS, HELP, CLEAR, TIME, DATE, EXIT, LEARN, QUESTION
    }

    public enum Sentiment {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    public static class LearnRequest {
        private final String question;
        private final String answer;

        public LearnRequest(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
