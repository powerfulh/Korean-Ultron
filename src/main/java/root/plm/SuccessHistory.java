package root.plm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuccessHistory {
    Map<BranchKey, BranchValue> map = new HashMap<>();

    public BranchValue get(String right, int n) {
        return map.get(new BranchKey(right, n));
    }
    public void put(String right, int n, List<List<Toke>> v, int retryCnt) {
        map.put(new BranchKey(right, n), new BranchValue(v, retryCnt));
    }
}

record BranchKey(String src, int n) {}
record BranchValue(List<List<Toke>> toBe, int retryCnt) {}