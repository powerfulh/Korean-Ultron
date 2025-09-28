package root.entmap;

import root.plm.entity.Compound;

import java.util.Map;

public class CompoundMap extends Entmap implements Compound {
    public CompoundMap(Map<String, Object> map) {
        super(map);
    }

    @Override
    public int getWord() {
        return (int) map.get("word");
    }

    @Override
    public int getLeftword() {
        return (int) map.get("leftword");
    }

    @Override
    public int getRightword() {
        return (int) map.get("rightword");
    }
}
