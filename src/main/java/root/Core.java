package root;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.plm.*;
import root.plm.entity.Twoken;
import root.plm.entity.Word;
import root.service.ReplaceRepeatedChars;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/plm")
public class Core {
    final Bank bank;
    final ReplaceRepeatedChars replaceRepeatedChars;
    final ContextCore contextCore;
    final SqlMapper mapper;

    final Map<Integer, List<Integer>> consumerMap = new HashMap<>();

    public Core(Bank bank, ReplaceRepeatedChars replaceRepeatedChars, ContextCore contextCore, SqlMapper mapper) {
        this.bank = bank;
        this.replaceRepeatedChars = replaceRepeatedChars;
        this.contextCore = contextCore;
        this.mapper = mapper;
        mapper.selectConsumer().forEach(item -> {
            consumerMap.computeIfAbsent(item.getMer(), k -> new ArrayList<>());
            consumerMap.get(item.getMer()).add(item.getMable());
        });
    }

    Sentence understand(String pureSrc) {
        final UnderstandTarget understandTarget = new UnderstandTarget(replaceRepeatedChars.replaceRepeatedChars(pureSrc, bank.symbols));
        var openerList = bank.wordList.stream().map(understandTarget::getAvailableToke).filter(Objects::nonNull).toList();
        final String src = replaceRepeatedChars.replaceRepeatedChars(pureSrc.replaceAll("\\s", ""), bank.symbols);
        if (openerList.isEmpty()) throw new PlmException("Fail to set the opening word", src);
        Map<String, List<Word>> failHistory = new HashMap<>();
        List<Sentence> sentenceList = new ArrayList<>();
        SuccessHistory successHistory = new SuccessHistory();
        PlmException e = null;
        for (var opener: openerList) {
            List<Toke> understandList = new ArrayList<>();
            try {
                StaticUtil.separateToken(understandList, understandTarget.pushToke(understandList, opener), bank.wordList, failHistory, bank.contextList, sentenceList, bank.compoundList, successHistory, contextCore);
            } catch (PlmException plmException) {
                e = plmException;
            }
        }
        if(sentenceList.isEmpty() && e != null) throw e; // 싹 다 실패한 경우 나중에는 편집 거리로 리트해봐야겠지
        sentenceList.sort(Comparator.comparing(item -> item.getContextPoint() * -1));
        return sentenceList.get(0);
    }
    void generate(List<UltronContext> sentence, List<UltronContext> targetList, List<List<UltronContext>> generated, int last, Map<Integer, List<UltronHistory>> historyList) {
        boolean match = false;
        for (var c: targetList) {
            if(c.getLeftword() == last && (historyList.get(sentence.size()) == null || !historyList.get(sentence.size()).contains(new UltronHistory(sentence.get(sentence.size() - 1).getLeftword(), c.getLeftword(), c.getRightword())))) {
                match = true;
                var clone = new ArrayList<>(sentence);
                clone.add(c);
                int size = clone.size();
                if (size > 1) {
                    historyList.computeIfAbsent(size - 1, k -> new ArrayList<>());
                    historyList.get(size - 1).add(new UltronHistory(clone.get(size - 2).getLeftword(), c.getLeftword(), c.getRightword()));
                }
                generate(clone, targetList.stream().filter(item -> item != c).toList(), generated, c.getRightword(), historyList);
            }
        }
        if(!match) generated.add(sentence);
    }

    @GetMapping
    public List<Map<String, Object>> v1(@Valid @Size(min = 1, max = 18) String pureSrc, boolean export) {
        Map<Integer, List<List<UltronContext>>> listMap = new HashMap<>();
        var targetList = mapper.selectGenerationTarget(understand(pureSrc));
        targetList.forEach(item -> {
            Map<Integer, List<UltronHistory>> historyList = new HashMap<>(); // 원랜 공통이였는데 억울하게 탈락되는 기대 문장이 생겨서 오프너 별로 내렸다 251011
            var list = listMap.get(item.getLeftword());
            if(list == null) {
                List<List<UltronContext>> generated = new ArrayList<>();
                listMap.put(item.getLeftword(), generated);
                generate(new ArrayList<>(), targetList, generated, item.getLeftword(), historyList);
            }
        });
        List<UltronSentence> sentenceList = new ArrayList<>();
        listMap.keySet().forEach(item -> listMap.get(item).forEach(li -> {
            if(li.isEmpty()) return;
            sentenceList.add(new UltronSentence(li, consumerMap));
        }));
        var res = sentenceList.stream()
                .distinct().sorted(Comparator.comparing(item -> item.point * -1)).map(item -> item.toDto(export)).toList();
        return res.size() > 5 ? res.subList(0, 5) : res;
    }
}

class UltronContext implements Twoken {
    int leftword;
    int rightword;
    String lw;
    String rw;
    int cnt;
    int space;
    int pri;
    int rcnt;
    int context;

    @Override
    public int getLeftword() {
        return leftword;
    }

    @Override
    public int getRightword() {
        return rightword;
    }

    int getPoint() {
        return (pri + 1) * rcnt;
    }
}
class UltronSentence extends ArrayList<UltronContext> {
    final String export;
    final int point;

    UltronSentence(List<UltronContext> list, Map<Integer, List<Integer>> consumerMap) {
        super(list);
        export = get(0).lw.concat(stream().map(item -> (item.space > item.cnt ? " " : "").concat(item.rw)).collect(Collectors.joining()));
        int penalty = 0;
        for (int i = 0; i < size(); i++) {
            final int current = get(i).context;
            if (get(i).pri == 1 || !consumerMap.containsKey(current)) continue;
            if(subList(0, i + 1).stream().noneMatch(item -> consumerMap.get(current).contains(item.context))) {
                penalty = i + 1;
                break;
            }
        }
        point = stream().mapToInt(UltronContext::getPoint).sum() - penalty;
    }

    Map<String, Object> toDto(boolean e) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("point", point);
        dto.put("export", e ? export : this);
        return dto;
    }
    @Override
    public int hashCode() {
        return size();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof UltronSentence sentence) return export.equals(sentence.export);
        return false;
    }
}
// 우선 같은 자리에 연속된 두개의 콘텍스트만 방지해보자, 필요하다면 같은 자리에 딱 하나의 콘텍스트 중복 방지도 가능하다
record UltronHistory(int w0, int w1, int w2) {}