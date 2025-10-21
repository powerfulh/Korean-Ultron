package root.plm;

import root.plm.entity.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dict {
    final List<Word> src;
    final Map<Integer, Word> map = new HashMap<>();
    final Map<Character, List<Word>> book = new HashMap<>();

    public Dict(List<Word> list) {
        src = list;
        list.forEach(item -> {
            map.put(item.getN(), item);
            char c = item.getWord().charAt(0);
            book.computeIfAbsent(c, k -> new ArrayList<>());
            book.get(c).add(item);
        });
    }

    Word get(int n) {
        return map.get(n);
    }
}
