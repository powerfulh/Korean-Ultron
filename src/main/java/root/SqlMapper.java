package root;

import org.apache.ibatis.annotations.Mapper;
import root.entmap.WordMap;
import root.plm.entity.Compound;
import root.plm.entity.Context;
import root.plm.entity.Word;

import java.util.List;
import java.util.Map;

@Mapper
public interface SqlMapper {
    List<Map<String, Object>> selectWord();
    List<Map<String, Object>> selectSymbolWord();
    List<Map<String, Object>> selectContext();
    List<Map<String, Object>> selectCompound();
}
