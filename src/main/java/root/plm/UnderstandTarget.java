package root.plm;

import root.plm.entity.Word;

import java.util.List;

public class UnderstandTarget implements Cloneable {
    final String src;
    int currentCut = 0;

    public UnderstandTarget(String src) {
        this.src = src;
    }

    public String getRight() {
        return src.substring(currentCut);
    }
    public Toke getAvailableToke(Word word) {
        final String right = getRight();
        int ignored = 0;
        int wordSpace = 0;
        for (int i = 0; i < word.getWord().length(); i++) {
            char currentWord = word.getWord().charAt(i);
            char currentSrc;
            try {
                currentSrc = right.charAt(i - wordSpace + ignored);
            } catch (StringIndexOutOfBoundsException e) {
                return null;
            }
            if(currentSrc == currentWord) continue;
            if(currentWord == ' ') {
                wordSpace++;
                continue;
            }
            if(currentSrc == ' ') {
                ignored++;
                i--;
            } else return null;
        }
        int nextSpace = 0;
        final int consumedLength = word.getWord().length() + ignored;
        while (right.length() > consumedLength + nextSpace && right.charAt(consumedLength + nextSpace) == ' ') nextSpace++;
        return new Toke(word, currentCut, currentCut + consumedLength + nextSpace, 0 < nextSpace);
    }
    public UnderstandTarget pushToke(List<Toke> understandList, Toke toke) {
        understandList.add(toke);
        currentCut = toke.end;
        return this;
    }
    public boolean success() {
        return src.length() == currentCut;
    }
    public void rollback(Toke toke) {
        currentCut = toke.start;
    }
    @Override
    public UnderstandTarget clone() {
        try {
            return (UnderstandTarget) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
