package root;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import root.mind.BuildingPattern;
import root.mind.CutterPattern;
import root.mind.Triplet;
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
    final Map<List<Integer>, Map<Integer, Double>> cutterPattern;
    final Map<Integer, Map<Integer, Integer>> buildingPattern;
    final Set<Triplet> tripletSet;

    public Core(Bank bank, ReplaceRepeatedChars replaceRepeatedChars, ContextCore contextCore, SqlMapper mapper) {
        this.bank = bank;
        this.replaceRepeatedChars = replaceRepeatedChars;
        this.contextCore = contextCore;
        this.mapper = mapper;
        mapper.selectConsumer().forEach(item -> {
            consumerMap.computeIfAbsent(item.getMer(), k -> new ArrayList<>());
            consumerMap.get(item.getMer()).add(item.getMable());
        });
        cutterPattern = new CutterPattern(mapper.selectCutterPattern()).getMap();
        mapper.deleteUltronCloser();
        mapper.insertUltronCloser();
        buildingPattern = new BuildingPattern(mapper.selectBuildingPattern(), bank.wordList, bank.compoundList).get();
        mapper.deleteExperiencedOpener();
        mapper.insertExperiencedOpener();
        tripletSet = mapper.selectTriplet();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void badReq() {}

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
                StaticUtil.separateToken(understandList, understandTarget.pushToke(understandList, opener), new Dict(bank.wordList), failHistory, bank.contextList, sentenceList, bank.compoundList, successHistory, contextCore);
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
                if(c.closerContext) generated.add(new ArrayList<>(clone));
                generate(clone, targetList.stream().filter(item -> item != c).toList(), generated, c.getRightword(), historyList);
            }
        }
        if(!match && !sentence.get(sentence.size() - 1).closerContext) generated.add(sentence);
    }

    @GetMapping
    public V1Res v1(@Valid @Size(min = 1, max = 40) String pureSrc, boolean export) {
        if(pureSrc.replaceAll("\\s+", "").length() > 25) throw new IllegalArgumentException();
        Map<Integer, List<List<UltronContext>>> listMap = new HashMap<>();
        var understood = understand(pureSrc);
        var targetList = mapper.selectGenerationTarget(understood, 50 + understood.size());
        targetList.forEach(item -> {
            item.buildingPattern = buildingPattern.get(item.context);
            if(item.noneOpener) return;
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
            sentenceList.add(new UltronSentence(li, consumerMap, cutterPattern, tripletSet));
        }));
        var res = sentenceList.stream()
                .distinct().sorted(Comparator.comparing(item -> item.point * -1)).map(item -> item.toDto(export)).toList();
        return new V1Res(understood.stream().map(Toke::getN).toList(), res.size() > 5 ? res.subList(0, 5) : res);
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
    Integer rcutter;
    boolean noneOpener;
    boolean closerContext;
    Map<Integer, Integer> buildingPattern;
    boolean exOpener;
    Integer rightAbstract;

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
    final String bonusLog;

    double cutterChance(List<Integer> lastPattern, Map<List<Integer>, Map<Integer, Double>> pattern, int cutter) {
        var existLastPattern = pattern.get(lastPattern);
        if(existLastPattern == null) return 0;
        return Objects.requireNonNullElse(existLastPattern.get(cutter), 0.0);
    }
    int cutBonus(List<UltronContext> cut) {
        if(cut.size() < 2) return 0;
        Map<Integer, Integer> building = null;
        int point = 0;
        for (int i = 0; i < cut.size(); i++) {
            final UltronContext item = cut.get(cut.size() - 1 - i);
            if(building == null && item.buildingPattern != null) building = item.buildingPattern;
            else if(building != null) point = Integer.sum(point, Objects.requireNonNullElse(building.get(item.context), 0));
        }
        return point;
    }
    UltronSentence(List<UltronContext> list, Map<Integer, List<Integer>> consumerMap, Map<List<Integer>, Map<Integer, Double>> pattern, Set<Triplet> tripletSet) {
        super(list);
        final var opener = get(0);
        export = opener.lw.concat(stream().map(item -> (item.space > item.cnt ? " " : "").concat(item.rw)).collect(Collectors.joining()));
        int basic = 0, unconsumedPenalty = 0, buildingPatternBonus = 0, tripletBonus = 0, breakAbstractPenalty = 0;
        List<Double> cutterPatternAdjust = new ArrayList<>();
        List<UltronContext> cut = new ArrayList<>();
        boolean breakAbstract = false;
        for (int i = 0; i < size(); i++) {
            final var current = get(i);
            basic += current.getPoint();
            // Triplet bonus
            final var last = i > 0 ? get(i - 1) : null;
            if(i > 0 && current.pri == 1 && last.pri == 1 && tripletSet.contains(new Triplet(last.context, current.context))) tripletBonus += current.rcnt;
            // Penalty
            final int cn = current.context;
            if (current.pri != 1 && consumerMap.containsKey(cn)) {
                final var currentSub = subList(0, i + 1);
                if(currentSub.stream().noneMatch(item -> consumerMap.get(cn).contains(item.context))) unconsumedPenalty = i + 1;
            }
            // Cutter pattern Adjust
            if(current.rcutter != null) {
                final var lastSub = subList(0, i);
                final var lastPattern = lastSub.stream().filter(item -> item.rcutter != null).map(item -> item.rcutter).toList();
                if(!lastPattern.isEmpty()) cutterPatternAdjust.add(cutterChance(lastPattern, pattern, current.rcutter));
            }
            // Building pattern bonus
            if(current.rcutter != null) {
                buildingPatternBonus += cutBonus(cut);
                cut = new ArrayList<>();
            }
            cut.add(current);
            // Abstract penalty
            if(breakAbstract) breakAbstractPenalty += current.getPoint();
            else if(last != null && last.rightAbstract != null && last.rightAbstract != cn) breakAbstract = true;
        }
        // Cutter pattern last Adjust
        final var lastPattern = stream().filter(item -> item.rcutter != null).map(item -> item.rcutter).toList();
        if(!lastPattern.isEmpty()) cutterPatternAdjust.add(cutterChance(lastPattern, pattern, CutterPattern.closer));
        // None closer penalty
        final int ncp = get(size() - 1).closerContext ? 0 : size();
        // Building pattern last bonus
        buildingPatternBonus += cutBonus(cut);
        final int openBonus = opener.exOpener ? opener.getPoint() : 0;
        final int tripletAverage = size() == 1 ? 0 : (tripletBonus / (size() - 1));
        final double adjust = cutterPatternAdjust.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        point = (int) ((basic * tripletAverage + openBonus - unconsumedPenalty - ncp - breakAbstractPenalty) * Math.max(buildingPatternBonus, 1) * adjust);
        bonusLog = "(" + basic + " * " + tripletAverage + " + " + openBonus + " - " + unconsumedPenalty + " - " + ncp + " - " + breakAbstractPenalty + ") * (" + buildingPatternBonus + " || 1) * %" + ((int)(adjust * 100));
    }

    Map<String, Object> toDto(boolean e) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("point", point);
        dto.put("export", e ? export : this);
        dto.put("bonusLog", bonusLog);
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

record V1Res(
        List<Integer> in,
        List<Map<String, Object>> out
) {}