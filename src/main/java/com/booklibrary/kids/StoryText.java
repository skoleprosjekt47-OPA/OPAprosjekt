package com.booklibrary.kids;

import com.booklibrary.reader.ReadAloud;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * The large story text of a page. While the story is read aloud, the sentence being read is
 * lighter and the word being read is gold and underlined.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented word and sentence highlighting for the children's stories.
 */
public class StoryText extends TextFlow {

    private static final Color NORMAL = Color.web("#efe8d8");
    private static final Color SENTENCE = Color.web("#fff6cf");
    private static final Color WORD = Color.web("#ffd54a");

    private final List<Text> nodes = new ArrayList<>();
    private final List<int[]> ranges = new ArrayList<>();
    private double fontSize = 28;

    /**
     * Shows a text.
     *
     * @param text     the page text
     * @param fontSize the letter size
     */
    public void setStory(String text, double fontSize) {
        this.fontSize = fontSize;
        getChildren().clear();
        nodes.clear();
        ranges.clear();
        List<int[]> words = ReadAloud.wordRanges(text, 0, text.length());
        for (int i = 0; i < words.size(); i++) {
            int[] word = words.get(i);
            int end = i + 1 < words.size() ? words.get(i + 1)[0] : text.length();
            Text node = new Text(text.substring(word[0], end));
            node.setFont(Font.font("Georgia", FontWeight.NORMAL, fontSize));
            node.setFill(NORMAL);
            nodes.add(node);
            ranges.add(new int[]{word[0], word[1]});
            getChildren().add(node);
        }
        setLineSpacing(fontSize * 0.25);
    }

    /**
     * Changes the letter size without changing the text.
     *
     * @param size the size
     */
    public void setFontSize(double size) {
        this.fontSize = size;
        for (Text node : nodes) {
            node.setFont(Font.font("Georgia", FontWeight.NORMAL, size));
        }
        setLineSpacing(size * 0.25);
    }

    /**
     * Marks the sentence and the word that are read now.
     *
     * @param sentenceStart start of the sentence in the text
     * @param sentenceEnd   end of the sentence
     * @param wordStart     start of the word, or -1 for no word
     */
    public void highlight(int sentenceStart, int sentenceEnd, int wordStart) {
        for (int i = 0; i < nodes.size(); i++) {
            int[] range = ranges.get(i);
            Text node = nodes.get(i);
            boolean inSentence = range[0] >= sentenceStart && range[1] <= sentenceEnd;
            boolean isWord = wordStart >= 0 && range[0] == wordStart;
            node.setFill(isWord ? WORD : inSentence ? SENTENCE : NORMAL);
            node.setUnderline(isWord);
        }
    }

    /** Removes all marks. */
    public void clear() {
        for (Text node : nodes) {
            node.setFill(NORMAL);
            node.setUnderline(false);
        }
    }

    /**
     * Returns the letter size.
     *
     * @return the size
     */
    public double getFontSize() {
        return fontSize;
    }
}
