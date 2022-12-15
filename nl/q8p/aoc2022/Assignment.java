package nl.q8p.aoc2022;

public interface Assignment {
    enum Run {
        EXAMPLE, REAL
    }
    Object run(final Run run, final String input);
}
