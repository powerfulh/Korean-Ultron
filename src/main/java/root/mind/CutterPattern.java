package root.mind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CutterPattern {
    public static final int closer = -1;
    final Map<List<Integer>, Map<Integer, Integer>> map = new HashMap<>();

    public CutterPattern(List<Map<String, Integer>> list) {
        Map<Integer, List<Integer>> sentence = new HashMap<>();
        list.forEach(item -> {
            sentence.computeIfAbsent(item.get("n"), k -> new ArrayList<>());
            sentence.get(item.get("n")).add(item.get("cutter")); // 쿼리에서 오다 바이를 했으니 순서는 보장될 것이다
        });
        sentence.keySet().forEach(item -> {
            final var s = sentence.get(item);
            List<Integer> pattern = new ArrayList<>();
            for (int i = 0; i < s.size(); i++) {
                pattern.add(s.get(i));
                map.computeIfAbsent(new ArrayList<>(pattern), k -> new HashMap<>());
                final int next = i == s.size() - 1 ? closer : s.get(i + 1);
                map.get(pattern).merge(next, 1, Integer::sum);
            }
        });
    }

    public Map<List<Integer>, Map<Integer, Integer>> getMap() {
        return map;
    }
}
