package root.entmap;

import root.plm.entity.Word;

import java.util.Map;

public class WordMap extends Entmap implements Word {
    public WordMap(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getWord() {
        return map.get("word").toString();
    }

    @Override
    public String getType() {
        return map.get("type").toString();
    }

    @Override
    public String getMemo() {
        return map.get("memo").toString();
    }

    @Override
    public Integer getN() {
        return (Integer) map.get("n");
    }
}
