package root.mind;

public record BuildingPatternItem(
        int sentence,
        int context,
        boolean lc,
        boolean cw,
        String rt,
        int rightword
) {
}
