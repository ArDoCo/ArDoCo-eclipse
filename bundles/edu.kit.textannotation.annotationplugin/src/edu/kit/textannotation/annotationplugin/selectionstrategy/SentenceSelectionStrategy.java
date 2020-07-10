package edu.kit.textannotation.annotationplugin.selectionstrategy;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;

/**
 * This selection expands the user selection to include full sentences.
 *
 * @see SelectionStrategy
 * @see edu.kit.textannotation.annotationplugin.views.AnnotationControlsView
 */
public class SentenceSelectionStrategy implements SelectionStrategy {
    private List<Character> breakingCharacters = Arrays.asList('.', ';', '?', '!');
    private static List<String> abbreviations = populateAbbreviations();

    @Override
    public String getName() {
        return "Sentence-based";
    }

    @Override
    public String getId() {
        return "selectionstrategy/sentence";
    }

    @Override
    public String getDescription() {
        return "The selection made in the editor view will be expanded to include full sentences before being annotated.";
    }

    @Override
    public Region evaluateSelection(Region selection, IDocument document) {
        int start = selection.getOffset();
        int end = selection.getOffset() + selection.getLength() - 1;
        String text = document.get();

        while (!sentenceBoundaryCheck(start - 2, text)) {
            start--;
        }

        while (!sentenceBoundaryCheck(end, text)) {
            end++;
        }
        // case when multiple (up to three) breaking characters are used
        int maxMultiplePunctuationMark = 3;
        for (int i = 1; i <= maxMultiplePunctuationMark; i++) {
            if (end < text.length() && breakingCharacters.contains(text.charAt(end))) {
                end++;
            }
        }

        return new Region(start, end - start);
    }

    /**
     * Checks the boundaries of a sentence. Returns true, if the character at the given position is a "breaking
     * character", e.g., a full stop/period. Does return false, if the character is not a breaking character and if a
     * period is combined with a known abbreviation.
     *
     * @param position
     *            Position within the text
     * @param text
     *            the text
     * @return Returns true, if the character at the given position is a "breaking character", e.g., a full stop/period.
     *         Does return false, if the character is not a breaking character and if a period is combined with a known
     *         abbreviation.
     */
    private boolean sentenceBoundaryCheck(int position, String text) {
        if (position < 0 || position >= text.length()) {
            return true;
        }

        char currCharacter = text.charAt(position);
        if (!breakingCharacters.contains(currCharacter)) {
            return false;
        }

        // If the current character is a '.' then we will check whether there is a known abbreviation.
        // if ('.' == currCharacter) {
        int maxWindow = getMaxCheckWindowSize();
        for (int window = 2; window <= maxWindow; window++) {
            if (position - window > 0) {
                String testTextWindow = text.subSequence(position - window, position + 1)
                                            .toString()
                                            .toLowerCase()
                                            .strip();
                if (abbreviations.stream()
                                 .anyMatch(testTextWindow::equalsIgnoreCase)) {
                    return false;
                }
            }
        }
        // }

        return true;
    }

    private static int getMaxCheckWindowSize() {
        return abbreviations.stream()
                            .mapToInt(abbrv -> abbrv.length() + 1)
                            .max()
                            .orElse(0);
    }

    private static List<String> populateAbbreviations() {
        return List.of("Mr.", "Dr.", "Mrs.", "Prof.", "Nr.", "No.", "Ltd.", "min.", "max.", "approx.", ",est.", "vs.",
                "temp.", "tel.");
    }
}
