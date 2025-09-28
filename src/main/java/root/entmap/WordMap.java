package root.entmap;

import root.plm.entity.Word;

import java.util.Map;

public class WordMap extends Entmap implements Word {
    public WordMap(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getWord() {
        return (String) map.get("word");
    }

    @Override
    public String getType() {
        return (String) map.get("type");
    }

    @Override
    public String getMemo() {
        return (String) map.get("memo");
    }

    @Override
    public Integer getN() {
        return (Integer) map.get("n");
    }
}
