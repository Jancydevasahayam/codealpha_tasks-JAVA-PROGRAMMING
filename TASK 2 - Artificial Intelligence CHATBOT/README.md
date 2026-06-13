# AI Chatbot Assistant

A complete Java Swing chatbot project for internship submission. It demonstrates OOP, NLP techniques, rule-based AI, file handling, event handling, Swing GUI design, and the Collections Framework.

## Project Structure

```text
AI Chatbot Assistant/
├── README.md
├── data/
│   ├── chat_history.txt
│   └── knowledge_base.txt
└── src/
    ├── ChatBot.java
    ├── ChatBotGUI.java
    ├── KnowledgeBase.java
    ├── Main.java
    └── NLPProcessor.java
```

## Features

- Modern Java Swing interface with `BorderLayout`, `JScrollPane`, `JTextArea`, `JTextField`, and buttons.
- NLP processing: lowercase conversion, punctuation removal, tokenization, keyword extraction, pattern matching, intent recognition, and sentiment analysis.
- Rule-based AI responses using `HashMap`, collections, string processing, and similarity matching.
- Built-in FAQ knowledge for general, Java, programming, AI, and college/career questions.
- Commands: `help`, `clear`, `time`, `date`, and `exit`.
- Learning command: `learn question = answer`.
- Persistent custom knowledge in `data/knowledge_base.txt`.
- Conversation logging in `data/chat_history.txt`.
- Bonus features: dark mode, FAQ management panel, sentiment status, and export chat history.

## Compile and Run from Command Line

Open a terminal in the project folder and run:

```bash
mkdir out
javac -d out src/*.java
java -cp out Main
```

On Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac -d out src/*.java
java -cp out Main
```

## Run in an IDE

### NetBeans

1. Create a new Java project.
2. Copy the five files from `src/` into the project's source package or default package.
3. Copy the `data/` folder into the project root.
4. Set `Main.java` as the main class.
5. Run the project.

### Eclipse

1. Create a new Java Project.
2. Copy the `src/` files into the project's `src` folder.
3. Copy the `data/` folder into the project root.
4. Right-click `Main.java` and choose `Run As > Java Application`.

### IntelliJ IDEA

1. Open this folder as a project.
2. Mark `src/` as the Sources Root if needed.
3. Run `Main.main()`.

## Example Questions

- Hello
- How are you?
- What is Java?
- What is OOP?
- What is inheritance?
- What is polymorphism?
- What is Python?
- What is Machine Learning?
- Tell me about computer science
- How can I prepare for interviews?

## Learning Examples

```text
learn what is swing = Swing is Java's GUI toolkit for building desktop applications.
learn what is jdbc = JDBC is a Java API used to connect applications with databases.
```

After learning, the chatbot saves the new response automatically in `data/knowledge_base.txt`.

## Notes

- The project uses only standard Java libraries, so no external dependency is required.
- Voice response is listed as a bonus idea in the requirement, but Java Speech API support depends on external libraries and platform setup. This project stays dependency-free for easy IDE compatibility.
