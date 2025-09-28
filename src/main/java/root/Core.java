package root;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.entmap.ContextMap;
import root.plm.*;
import root.plm.entity.Context;
import root.plm.entity.Word;
import root.service.ReplaceRepeatedChars;

import java.util.*;

@RestController
@RequestMapping("/plm")
public class Core {
    final Bank bank;
    final ReplaceRepeatedChars replaceRepeatedChars;
    final SmartStartBooster smartStartBooster;
    final SqlMapper mapper;

    public Core(Bank bank, ReplaceRepeatedChars replaceRepeatedChars, SmartStartBooster smartStartBooster, SqlMapper mapper) {
        this.bank = bank;
        this.replaceRepeatedChars = replaceRepeatedChars;
        this.smartStartBooster = smartStartBooster;
        this.mapper = mapper;
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
                StaticUtil.separateToken(understandList, understandTarget.pushToke(understandList, opener), bank.wordList, failHistory, bank.contextList, sentenceList, bank.compoundList, successHistory, smartStartBooster);
            } catch (PlmException plmException) {
                e = plmException;
            }
        }
        if(sentenceList.isEmpty() && e != null) throw e; // 싹 다 실패한 경우 나중에는 편집 거리로 리트해봐야겠지
        sentenceList.sort(Comparator.comparing(item -> item.getContextPoint() * -1));
        return sentenceList.get(0);
    }
    void generate(List<Context> sentence, List<ContextMap> targetList, List<List<Context>> generated, int last) {
        for (var c: targetList) {
            if(c.getLeftword() == last) {
                sentence.add(c);
                generate(new ArrayList<>(sentence), targetList.stream().filter(item -> item != c).toList(), generated, c.getRightword());
                return;
            }
        }
        generated.add(sentence);
    }
    Toke findToke(List<Context> src) {
        boolean space = src.size() != 1 && src.get(1).getSpace() > src.get(1).getCnt();
        return new Toke(StaticUtil.selectWord(src.get(0).getLeftword(), bank.wordList), 0, 0, space);
    }
    Sentence toSentence(List<Toke> sentence, List<Context> src) {
        if(src.isEmpty()) return new Sentence(sentence, bank.contextList);
        sentence.add(findToke(src));
        return toSentence(sentence, src.subList(1, src.size()));
    }
    @GetMapping
    public List<Map<String, Object>> v1(String pureSrc) {
        Map<Integer, List<List<Context>>> listMap = new HashMap<>();
        // 복수 토큰 문장은 나중에..
        var targetList = mapper.selectGenerationTarget(understand(pureSrc).stream().map(Toke::getN).toList()).stream().map(ContextMap::new).toList();
        targetList.forEach(item -> {
            var list = listMap.get(item.getLeftword());
            if(list == null) {
                List<List<Context>> generated = new ArrayList<>();
                listMap.put(item.getLeftword(), generated);
                generate(new ArrayList<>(), targetList, generated, item.getLeftword());
            }

        });
        List<Sentence> sentenceList = new ArrayList<>();
        listMap.keySet().forEach(item -> listMap.get(item).forEach(li -> {
            if(li.isEmpty()) return;
            sentenceList.add(toSentence(new ArrayList<>(), li));
        }));
        return sentenceList.stream().map(Sentence::getDto).toList();
    }
}
