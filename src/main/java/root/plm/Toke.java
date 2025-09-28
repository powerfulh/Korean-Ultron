package root.plm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import root.plm.entity.Word;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Toke implements Word {
    final Word src;
    final int start;
    final int end;
    final boolean rightSpace;
    int rightContext; // 내가 오른쪽일때 문맥 점수
    final Map<Integer, List<Integer>> contextHistory = new HashMap<>();
    @JsonIgnore
    public boolean otherOption;

    public Toke(Word llmWord, int start, int end, boolean rightSpace) {
        src = llmWord;
        this.start = start;
        this.end = end;
        this.rightSpace = rightSpace;
    }

    @Override
    public Integer getN() {
        return src.getN();
    }

    public boolean isRightSpace() {
        return rightSpace;
    }

    public int getRightContext() {
        return rightContext;
    }

    @Override
    public String getWord() {
        return src.getWord();
    }

    @Override
    public String getType() {
        return src.getType();
    }

    @Override
    public String getMemo() {
        return src.getMemo();
    }
}
