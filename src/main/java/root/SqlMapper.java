package root;

import org.apache.ibatis.annotations.Mapper;
import root.mind.BuildingPatternItem;
import root.mind.ConsumeContext;
import root.mind.Triplet;
import root.plm.Toke;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface SqlMapper {
    List<Map<String, Object>> selectWord();
    List<Map<String, Object>> selectSymbolWord();
    List<Map<String, Object>> selectContext();
    List<Map<String, Object>> selectCompound();
    List<UltronContext> selectGenerationTarget(List<Toke> list, int limit);
    List<ConsumeContext> selectConsumer();
    List<Map<String, Integer>> selectCutterPattern();
    void insertUltronCloser();
    void deleteUltronCloser();
    List<BuildingPatternItem> selectBuildingPattern();
    void insertExperiencedOpener();
    void deleteExperiencedOpener();
    Set<Triplet> selectTriplet();
}
