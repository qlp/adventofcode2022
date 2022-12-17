package nl.q8p.aoc2022.day16;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day16 implements Day {
    static final class Route2 {
        private final Route me;

        private final Route elephant;

        Route2(Route me, Route elephant) {
            this.me = me;
            this.elephant = elephant;
        }

        public Route2 withLinks(Link newMeLink, Link newElephantLink) {
            return new Route2(me.withLink(newMeLink), elephant.withLink(newElephantLink));
        }

        public boolean didNotOpen(Valve valve) {
            return me.didNotOpen(valve) && elephant.didNotOpen(valve);
        }

        public long pressure() {
            return me.pressure() + elephant.pressure();
        }
    }

    static final class Route {
        private final Valve start;
        private final List<Link> links;

        private final List<Valve> valves;

        private final int maxSteps;

        Route(Valve start, List<Link> links, int maxSteps) {
            this.start = start;
            this.links = links;
            this.maxSteps = maxSteps;

            if (links.isEmpty()) {
                valves = List.of(start);
            } else {
                valves = Stream.concat(Stream.of(links.get(0).from()), links.stream().map(Link::to)).toList();
            }
        }

        public long pressure() {
            var pressure = 0L;
            var steps = 0;

            for (var link : links) {
                steps += link.steps() + 1;
                if (steps < maxSteps) {
                    pressure += link.to().rate * (maxSteps - steps);
                }
            }

            return pressure;
        }

        public List<Valve> valves() {
            return valves;
        }

        @Override
        public String toString() {
            return valves().stream().map(d -> d.name).collect(Collectors.joining(" -> ")) + " " + steps() + " steps";
        }

        public int steps() {
            return Math.min(maxSteps, links.stream().mapToInt(Link::steps).sum() + links.size());
        }

        public Valve current() {
            return links.isEmpty() ? start : links.get(links.size() - 1).to();
        }

        public Route withLink(Link link) {
            var newLinks = new ArrayList<>(links);
            if (link != null) {
                newLinks.add(link);
            }

            return new Route(start, newLinks, maxSteps);
        }

        public boolean didNotOpen(Valve valve) {
            return !valves.contains(valve);
        }
    }

    static final class Link {
        final List<Valve> path;

        Link(List<Valve> path) {
            this.path = path;
        }

        Valve from() {
            return path.get(0);
        }

        Valve to() {
            return path.get(path.size() - 1);
        }

        int steps() {
            return path.size() - 1;
        }

        @Override
        public String toString() {
            return path.stream().map(v -> v.name).collect(Collectors.joining(" -> "));
        }
    }

    static class Volcano {
        final Map<String, Valve> valvesByName;
        final Map<String, List<Link>> links;
        private final int maxSteps;

        int time = 0;

        public Volcano(List<Valve> valves, int maxSteps) {
            this.valvesByName = valves.stream().collect(Collectors.toMap(v -> v.name, v -> v));

            this.links = valvesByName.values().stream().filter(valve -> valve.name.equals("AA") || valve.functional()).collect(Collectors.toMap(v -> v.name, Volcano::links));
            this.maxSteps = maxSteps;
        }

        Route route() {
            var candidates = valvesByName.values().stream().filter(Valve::functional).toList();

            return route(new Route(valvesByName.get("AA"), Collections.emptyList(), maxSteps), candidates.stream().filter(c -> !c.name.equals("AA")).toList());
        }

        private Route route(Route from, List<Valve> candidates) {
            var newRoutes = new ArrayList<Route>();

           candidates.forEach(candidateValve -> {
                var link = links
                    .get(from.current().name)
                    .stream()
                    .filter(l -> l.to().name().equals(candidateValve.name()))
                    .filter(l -> l.path.stream().noneMatch(v -> v.rate > l.to().rate && from.didNotOpen(v) ))
                    .min(Comparator.comparing(Link::steps))
                    .orElse(null);

                if (link != null) {
                    var newLink = from.withLink(link);

                    if (newLink.steps() < maxSteps) {
                        newRoutes.add(route(newLink, candidates.stream().filter(c -> !c.name().equals(candidateValve.name())).toList()));
                    }
                }
            });

            return newRoutes.isEmpty() ? from : newRoutes.stream().max(Comparator.comparingLong(Route::pressure)).orElseThrow();
        }

        Route2 route2() {
            var candidates = valvesByName.values().stream().filter(Valve::functional).toList();

            return route2(
                    new Route2(
                            new Route(valvesByName.get("AA"), Collections.emptyList(), maxSteps),
                            new Route(valvesByName.get("AA"), Collections.emptyList(), maxSteps)
                    ),
                    candidates.stream().filter(c -> !c.name.equals("AA")).toList());
        }

        private Route2 route2(Route2 from, List<Valve> candidates) {
            if (from.me.steps() == maxSteps && from.elephant.steps() == maxSteps) {
                return from;
            }

            var newRoutes = new ArrayList<Route2>();

            if (from.me.steps() <= from.elephant.steps()) {
                candidates.forEach(candidateValve -> {
                    var link = links
                            .get(from.me.current().name)
                            .stream()
                            .filter(l -> l.to().name().equals(candidateValve.name()))
                            .filter(l -> l.path.stream().noneMatch(v -> v.rate > l.to().rate && from.didNotOpen(v) ))
                            .min(Comparator.comparing(Link::steps))
                            .orElse(null);

                    if (link != null) {
                        var newLink = from.withLinks(link, null);

                        if (newLink.me.steps() < maxSteps) {
                            newRoutes.add(route2(newLink, candidates.stream().filter(c -> !c.name().equals(candidateValve.name())).toList()));
                        }
                    }
                });
            } else {
                candidates.forEach(candidateValve -> {
                    var link = links
                            .get(from.elephant.current().name)
                            .stream()
                            .filter(l -> l.to().name().equals(candidateValve.name()))
                            .filter(l -> l.path.stream().noneMatch(v -> v.rate > l.to().rate && from.didNotOpen(v) ))
                            .min(Comparator.comparing(Link::steps))
                            .orElse(null);

                    if (link != null) {
                        var newLink = from.withLinks(null, link);

                        if (newLink.elephant.steps() < maxSteps) {
                            newRoutes.add(route2(newLink, candidates.stream().filter(c -> !c.name().equals(candidateValve.name())).toList()));
                        }
                    }
                });
            }

            return newRoutes.isEmpty() ? from : newRoutes.stream().max(Comparator.comparingLong(Route2::pressure)).orElseThrow();
        }

        private static List<Link> links(Valve valve) {
            var inProgress = List.of(List.of(valve));
            var result = new HashMap<String, Link>();

            while(!inProgress.isEmpty()) {
                var nextProgress = new ArrayList<List<Valve>>();

                for (var current : inProgress) {
                    var last = current.get(current.size() - 1);

                    for (var nextValve : last.tunnels) {
                        if (current.stream().noneMatch(candidate -> candidate.equals(nextValve))) {
                            var nextPath = Stream.concat(current.stream(), Stream.of(nextValve)).toList();
                            var nextLink = new Link(nextPath);

                            if (nextValve.functional()) {
                                var existingRoute = result.get(nextValve.name);

                                if (existingRoute == null || existingRoute.steps() > nextLink.steps()) {
                                    result.put(nextValve.name, nextLink);
                                }
                            }

                            nextProgress.add(nextPath);
                        }
                    }
                }

                inProgress = nextProgress;
            }

            return result.values().stream().toList();
        }

        @Override
        public String toString() {
            return valvesByName.values().stream().sorted(Comparator.comparing(v -> v.name)).map(Valve::toString).collect(Collectors.joining("\n"));
        }

        static Volcano parse(String string, int maxSteps) {
            var result = new HashMap<>(Arrays.stream(string.split("\n")).map(Valve::parse).collect(Collectors.toMap(v -> v.name, v -> v)));

            var relations = Arrays.stream(string.split("\n")).collect(Collectors.toMap(
                    line -> line.substring("Valve ".length(), line.indexOf(" has ")),
                    line -> line.substring(line.indexOf(' ', line.indexOf("to valve") + "to valve".length()) + 1).split(", ")
                )
            );

            relations
                .values()
                .stream()
                .flatMap(Arrays::stream)
                .distinct()
                .filter(n -> !result.containsKey(n))
                .forEach(v -> result.put(v, new Valve(v, new HashSet<>(), 0)));

            relations.forEach((valve, tunnels) -> Arrays.stream(tunnels).forEach(tunnel -> result.get(valve).connect(result.get(tunnel))));

            return new Volcano(result.values().stream().toList(), maxSteps);
        }
    }

    static final class Valve {
        private final String name;
        private final Set<Valve> tunnels;
        private final long rate;

        Valve(String name, Set<Valve> tunnels, long rate) {
            this.name = name;
            this.tunnels = tunnels;
            this.rate = rate;
        }

        void connect(Valve tunnel) {
                tunnels.add(tunnel);
                tunnel.tunnels.add(this);
            }

            static Valve parse(String string) {
                return new Valve(
                        string.substring("Valve ".length(), string.indexOf(" has ")),
                        new HashSet<>(),
                        Long.parseLong(string.substring(string.indexOf("rate=") + "rate=".length(), string.indexOf(';'))));
            }

            @Override
            public String toString() {
                return name + " (" + rate + ": " + String.join(", ", tunnels.stream().map(t -> t.name).sorted().toList()) + ")";
            }

            public boolean functional() {
                return rate != 0;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Valve valve = (Valve) o;
                return name.equals(valve.name);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name);
            }

        public String name() {
            return name;
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> Volcano.parse(input, 30).route().pressure();
    }

    @Override
    public Assignment second() {
        return (run, input) -> Volcano.parse(input, 26).route2().pressure();
    }
}
