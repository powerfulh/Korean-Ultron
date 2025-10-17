package root.mind;

public record BuildingPattern(
        int sentence,
        int context,
        boolean lc,
        boolean cw,
        String rt,
        int rightword
) {}
