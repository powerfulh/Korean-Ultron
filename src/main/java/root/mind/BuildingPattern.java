package root.mind;

import jakarta.validation.constraints.Size;
import root.plm.StaticUtil;
import root.plm.entity.Compound;
import root.plm.entity.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingPattern {
    final Map<Integer, Map<Integer, Integer>> map = new HashMap<>();

    boolean findZero(int n, List<Word> wordList, List<Compound> compoundList) {
        Compound compound = compoundList.stream().filter(item -> item.getWord() == n).findAny().orElse(null);
        if(compound == null) return false;
        if(StaticUtil.selectWord(compound.getLeftword(), wordList).getType().equals("0")) return true;
        if(findZero(compound.getLeftword(), wordList, compoundList)) return true;
        if(StaticUtil.selectWord(compound.getRightword(), wordList).getType().equals("0")) return true;
        return findZero(compound.getRightword(), wordList, compoundList);
    }
    public BuildingPattern(List<BuildingPatternItem> list, List<Word> wordList, List<Compound> compoundList) {
        int currentSentence = -1;
        List<BuildingPatternItem> cut = new ArrayList<>();
        List<Cut> cutList = new ArrayList<>();
        for (var item: list) {
            if(item.lc() || currentSentence != item.sentence()) {
                if(cut.size() > 1) cutList.add(new Cut(cut));
                cut = new ArrayList<>();
            }
            currentSentence = item.sentence();
            if(item.rt().equals("0")) {
                cut.add(item);
                continue;
            }
            if(item.cw() && findZero(item.rightword(), wordList, compoundList)) cut.add(item);
        }
        if(cut.size() > 1) cutList.add(new Cut(cut));
        cutList.forEach(item -> {
            map.computeIfAbsent(item.getBuilding(), k -> new HashMap<>());
            item.getBuilders().forEach(builder -> map.get(item.getBuilding()).merge(builder, 1, Integer::sum));
        });
    }

    public Map<Integer, Map<Integer, Integer>> get() {
        return map;
    }
}

record Cut(List<BuildingPatternItem> list) {
    Cut(@Size(min = 2) List<BuildingPatternItem> list) {
        this.list = list;
    }

    int getBuilding() {
        return list.get(list.size() - 1).context();
    }

    List<Integer> getBuilders() {
        return list.subList(0, list.size() - 1).stream().map(BuildingPatternItem::context).toList();
    }
}