package root;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.plm.*;
import root.plm.entity.Word;
import root.service.ReplaceRepeatedChars;

import java.util.*;

@RestController
@RequestMapping("/plm")
public class Core {
    final Bank bank;
    final ReplaceRepeatedChars replaceRepeatedChars;
    final SmartStartBooster smartStartBooster;

    public Core(Bank bank, ReplaceRepeatedChars replaceRepeatedChars, SmartStartBooster smartStartBooster) {
        this.bank = bank;
        this.replaceRepeatedChars = replaceRepeatedChars;
        this.smartStartBooster = smartStartBooster;
    }

    @GetMapping
    public List<Map<String, Object>> v1(String pureSrc) {
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
        final int max = 5;
        return (sentenceList.size() > max ? sentenceList.subList(0, max) : sentenceList).stream().map(Sentence::getDto).toList();
    }
}
