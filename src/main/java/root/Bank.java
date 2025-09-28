package root;

import org.springframework.stereotype.Component;
import root.entmap.CompoundMap;
import root.entmap.ContextMap;
import root.entmap.WordMap;
import root.plm.entity.Compound;
import root.plm.entity.Context;
import root.plm.entity.Word;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Bank {
    final SqlMapper mapper;
    List<Word> wordList;
    char[] symbols;
    List<Context> contextList;
    List<Compound> compoundList;

    public Bank(SqlMapper mapper) {
        this.mapper = mapper;
        update();
    }

    void update() {
        wordList = mapper.selectWord().stream().map(item -> (Word) new WordMap(item)).toList();
        symbols = mapper.selectSymbolWord().stream().map(item -> item.get("word").toString()).collect(Collectors.joining()).toCharArray();
        contextList = mapper.selectContext().stream().map(item -> (Context) new ContextMap(item)).toList();
        compoundList = mapper.selectCompound().stream().map(item -> (Compound) new CompoundMap(item)).toList();
    }
}
