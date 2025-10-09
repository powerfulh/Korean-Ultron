package root.plm;

import root.plm.entity.Context;

import java.util.*;
import java.util.stream.Collectors;

public class Sentence extends ArrayList<Toke> {
    final int contextPoint;
    final String export;

    public Sentence(List<Toke> list, List<Context> contextList) {
        super(list);
        var openerContext = contextList.stream().filter(StaticUtil.getContextFinder(StaticUtil.opener, get(0).getN())).findAny().orElse(null);
        int p = openerContext == null ? 0 : (openerContext.getCnt() * get(0).getWord().length());
        if(p == 0) p = get(0).getWord().length() - 1; // 오프너도 마찬가지로 오프너 콘텍스트가 없더라도 길이가 긴 것부터 잡게 해보자
        contextPoint = p + list.stream().mapToInt(Toke::getRightContext).sum();
        export = stream().map(item -> item.isRightSpace() ? item.getWord() + " " : item.getWord()).collect(Collectors.joining());
        for (int i = 0; i < size() - 1; i++) {
            get(i).rightword = get(i + 1).getN();
        }
    }

    public int getContextPoint() {
        return contextPoint;
    }
    @SuppressWarnings("unused")
    public Map<String, Object> getDto(boolean export) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("point", contextPoint);
        dto.put("list", export ? this.export : this);
        return dto;
    }

    @Override
    public int hashCode() {
        return export.length();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Sentence s) return s.export.equals(export);
        return false;
    }
}
