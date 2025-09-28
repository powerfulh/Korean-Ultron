package root.entmap;

import root.plm.entity.Context;

import java.util.Map;

public class ContextMap extends Entmap implements Context {
    public ContextMap(Map<String, Object> map) {
        super(map);
    }

    @Override
    public int getCnt() {
        return (int) map.get("cnt");
    }

    @Override
    public int getSpace() {
        return (int) map.get("space");
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
