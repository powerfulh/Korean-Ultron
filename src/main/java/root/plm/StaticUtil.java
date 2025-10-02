package root.plm;

import root.plm.entity.*;

import java.util.*;
import java.util.function.Predicate;

public class StaticUtil {
    static final int opener = 2903;

    static Predicate<Twoken> getContextFinder(int lw, int rw) {
        return item -> item.getLeftword() == lw && item.getRightword() == rw;
    }
    public static <T extends Ntity>T selectWord(int n, List<T> data) {
        return data.stream().filter(item -> item.getN() == n).findAny().orElseThrow();
    }
    public static void separateToken(List<Toke> understandList, UnderstandTarget src, final List<Word> wordList, Map<String, List<Word>> failHistory, List<Context> contextList, List<Sentence> sentenceList, List<Compound> compoundList, SuccessHistory successHistory, ContextCore contextCore) {
        if(src.success()) sentenceList.add(new Sentence(understandList, contextList));
        else {
            Toke lastUnderstand = understandList.get(understandList.size() - 1);
            var sh = successHistory.get(src.getRight(), lastUnderstand.getN());
            if(sh != null) {
                sh.forEach(item -> {
                    var merge = new ArrayList<>(understandList);
                    merge.addAll(item);
                    sentenceList.add(new Sentence(merge, contextList));
                });
                return;
            }
            var h = failHistory.get(src.getRight());
            var sameList = wordList.stream()
                    .map(item -> {
                        Toke toke = src.getAvailableToke(item);
                        if(toke == null || understandList.isEmpty()) return toke;
                        try {
                            contextCore.rightContext(toke, lastUnderstand, toke, contextList, compoundList, wordList, lastUnderstand.isRightSpace(), lastUnderstand.otherOption, 0);
                        } catch (PlmException e) {
                            return null;
                        }
                        return contextCore.lengthRate(toke);
                    })
                    .filter(item -> {
                        if(item != null) {
                            if(h == null) return true;
                            return h.stream().noneMatch(hi -> Objects.equals(hi.getN(), item.getN()));
                        }
                        return false;
                    })
                    .sorted(Comparator.comparing(Toke::getRightContext))
                    .toList();
            if(sameList.isEmpty()) {
                if(understandList.size() < 2) throw failHistory.isEmpty() ? new PlmException("Fail to continue after open", lastUnderstand.getWord()) : new PlmException("Fail to understand", failHistory);
                src.rollback(lastUnderstand);
                failHistory.computeIfAbsent(src.getRight(), k -> new ArrayList<>());
                failHistory.get(src.getRight()).add(lastUnderstand);
                understandList.remove(understandList.size() - 1);
                separateToken(understandList, src, wordList, failHistory, contextList, sentenceList, compoundList, successHistory, contextCore);
                return;
            }
            final Toke best = sameList.get(sameList.size() - 1);
            int ss = sentenceList.size();
            if(sameList.size() > 1) {
                sameList.subList(0, sameList.size() - 1).stream()
                        .filter(item -> item.getRightContext() > 0)
                        .forEach(item -> {
                            var clone = new ArrayList<>(understandList);
                            separateToken(clone, src.clone().pushToke(clone, item), wordList, failHistory, contextList, sentenceList, compoundList, successHistory, contextCore);
                        });
                if(best.getRightContext() < 1) best.otherOption = true;
            }
            final String right = src.getRight();
            final int understandSize = understandList.size();
            separateToken(understandList, src.pushToke(understandList, best), wordList, failHistory, contextList, sentenceList, compoundList, successHistory, contextCore);
            var branchList = sentenceList.subList(ss, sentenceList.size());
            if(!branchList.isEmpty()) {
                successHistory.put(right, lastUnderstand.getN(), branchList.stream().map(item -> item.subList(understandSize, item.size())).toList());
            }
        }
    }
}
