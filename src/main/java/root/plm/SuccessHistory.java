package root.plm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuccessHistory {
    Map<BranchKey, List<List<Toke>>> map = new HashMap<>();

    public List<List<Toke>> get(String right, int n) {
        return map.get(new BranchKey(right, n));
    }
    public void put(String right, int n, List<List<Toke>> v) {
        map.put(new BranchKey(right, n), v);
    }
}

record BranchKey(String src, int n) {}