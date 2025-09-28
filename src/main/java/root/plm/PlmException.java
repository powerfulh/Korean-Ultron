package root.plm;

import root.plm.entity.Word;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlmException extends RuntimeException {
  public Map<String, Object> info;

    public PlmException(String title, String data) {
      info = new HashMap<>();
      info.put(title, data);
    }

    public PlmException(String failToUnderstand, Map<String, List<Word>> failHistory) {
      info = new HashMap<>();
      info.put(failToUnderstand, failHistory.keySet().stream().map(item -> {
        Map<String, Object> m = new HashMap<>();
        m.put("key", item);
        m.put("length", item.length());
        m.put("v", failHistory.get(item));
        return m;
      }).sorted(Comparator.comparing(item -> (Integer) item.get("length"))).toList());
    }
}
