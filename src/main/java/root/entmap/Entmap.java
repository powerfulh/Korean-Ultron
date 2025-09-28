package root.entmap;

import java.util.Map;

public abstract class Entmap {
    final Map<String, Object> map;

    public Entmap(Map<String, Object> map) {
        this.map = map;
    }
}
