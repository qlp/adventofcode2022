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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day16 implements Day {
    private static final Logger LOG = Logger.getLogger(Day16.class.getName());

    static final class Route2 {
        private Route me;

        private Route elephant;

        Route2(Route me, Route elephant) {
            this.me = me;
            this.elephant = elephant;
        }

        public Route2 withLinks(Link newMeLink, Link newElephantLink) {
            return new Route2(me.withLink(newMeLink), elephant.withLink(newElephantLink));
        }
    }

    static final class Route {
        private Valve start;
        private final List<Link> links;

        Route(Valve start, List<Link> links) {
            this.start = start;
            this.links = links;
        }

        public List<Valve> valves() {
            return Stream.concat(Stream.of(links.get(0).from()), links.stream().map(Link::to)).toList();
        }

        @Override
        public String toString() {
            return valves().stream().map(d -> d.name).collect(Collectors.joining(" -> ")) + " " + steps() + " steps";
        }

        public int steps() {
            return links.stream().mapToInt(Link::steps).sum();
        }

        public Valve current() {
            return links.isEmpty() ? start : links.get(links.size() - 1).to();
        }

        public Route withLink(Link link) {
            var newLinks = new ArrayList<>(links);
            if (link != null) {
                newLinks.add(link);
            }

            return new Route(start, newLinks);
        }
    }

    static final class Path {
        private final List<Valve> valves;

        Path(List<Valve> valves) {
            this.valves = valves;
        }

        @Override
        public String toString() {
            return valves.stream().map(d -> d.name).collect(Collectors.joining(" -> "));
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

            this.links = valvesByName.values().stream().filter(valve -> valve.name.equals("AA") || valve.functional()).collect(Collectors.toMap(v -> v.name, v -> links(v)));
            this.maxSteps = maxSteps;
        }

        List<Route> routes() {
            var candidates = valvesByName.values().stream().filter(Valve::functional).toList();

            return routes(new Route(valvesByName.get("AA"), Collections.emptyList()), candidates.stream().filter(c -> !c.name.equals("AA")).toList());
        }

        private List<Route> routes(Route from, List<Valve> candidates) {
            var newRoutes = new ArrayList<Route>();

            candidates.stream().filter(c -> !c.name().equals(from.current().name())).forEach(candidateValve -> {
                var link = links
                    .get(from.current().name)
                    .stream()
                    .filter(l -> l.to().name().equals(candidateValve.name()))
                    .min(Comparator.comparing(Link::steps))
                    .orElseThrow();

                var newLink = from.withLink(link);

                if (newLink.steps() < maxSteps) {
                    newRoutes.addAll(routes(newLink, candidates.stream().filter(c -> !c.name().equals(candidateValve.name())).toList()));
                }
            });

            return newRoutes.isEmpty() ? Collections.singletonList(from) : newRoutes;
        }


        List<Route2> routes2() {
            var candidates = valvesByName.values().stream().filter(Valve::functional).toList();

            return routes2(
                    new Route2(
                            new Route(valvesByName.get("AA"), Collections.emptyList()),
                            new Route(valvesByName.get("AA"), Collections.emptyList())
                    ),
                    candidates.stream().filter(c -> !c.name.equals("AA")).toList());
        }

        private List<Route2> routes2(Route2 from, List<Valve> candidates) {
            var newRoutes = new ArrayList<Route2>();

            Pair.from(candidates)
                    .stream()
                    .filter(c -> c.me == null || !c.me.name().equals(from.me.current().name()))
                    .filter(c -> c.elephant == null || !c.elephant.name().equals(from.elephant.current().name()))
                    .forEach(candidatePair -> {

                Link meLink = null;
                if (candidatePair.me != null) {
                    meLink = links
                            .get(from.me.current().name)
                            .stream()
                            .filter(l -> l.to().name().equals(candidatePair.me.name()))
                            .min(Comparator.comparing(Link::steps))
                            .orElseThrow();
                }

                Link elephantLink = null;
                if (candidatePair.elephant != null) {
                    elephantLink = links
                            .get(from.elephant.current().name)
                            .stream()
                            .filter(l -> l.to().name().equals(candidatePair.elephant.name()))
                            .min(Comparator.comparing(Link::steps))
                            .orElseThrow();
                }

                var newRoute = from.withLinks(meLink, elephantLink);

                if (newRoute.me.steps() < maxSteps || newRoute.elephant.steps() < maxSteps) {
                    var newCandidates = candidates.stream()
                            .filter(c -> candidatePair.me == null || !c.name().equals(candidatePair.me.name()))
                            .filter(c -> candidatePair.elephant == null || !c.name().equals(candidatePair.elephant.name()))
                            .toList();
                    newRoutes.addAll(routes2(newRoute, newCandidates));
                }
            });

            return newRoutes.isEmpty() ? Collections.singletonList(from) : newRoutes;
        }

        record Pair(Valve me, Valve elephant) {
            static List<Pair> from(List<Valve> candidates) {
                if (candidates.size() == 1) {
                    var single = candidates.get(0);

                    return List.of(new Pair(single, null), new Pair(null, single));
                }

                return candidates
                        .stream()
                        .flatMap(me -> candidates.stream().map(elephant -> new Pair(me, elephant)))
                        .filter(p -> !p.me.equals(p.elephant))
                        .toList();
            }

            @Override
            public String toString() {
                return me.name + ", " + elephant.name;
            }
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

        public long valvesToOpen() {
            return valvesByName.values().stream().filter(v -> !v.open && v.rate > 0).count();
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
                .forEach(v -> result.put(v, new Valve(v, new HashSet<>(), 0, 0, false)));


            relations.forEach((valve, tunnels) -> Arrays.stream(tunnels).forEach(tunnel -> result.get(valve).connect(result.get(tunnel))));

            return new Volcano(result.values().stream().toList(), maxSteps);
        }

        public long pressure() {
            return valvesByName.values().stream().mapToLong(v -> v.pressure).sum();
        }

        public void reset() {
            valvesByName.values().forEach(Valve::reset);
            time = 0;
        }

        public void open(Valve valve) {
            valvesByName.get(valve.name).openValve();
        }

        private void tick() {
            if (time < maxSteps) {
                time++;
                valvesByName.values().forEach(Valve::tick);
            }
        }
    }

    static final class Valve {
        private final String name;
        private final Set<Valve> tunnels;
        private final long rate;
        long pressure;
        boolean open;

        Valve(String name, Set<Valve> tunnels, long rate, long pressure, boolean open) {
            this.name = name;
            this.tunnels = tunnels;
            this.rate = rate;
            this.pressure = pressure;
            this.open = open;
        }

        void reset() {
            pressure = 0;
            open = false;
        }

        void connect(Valve tunnel) {
                tunnels.add(tunnel);
                tunnel.tunnels.add(this);
            }

            static Valve parse(String string) {
                return new Valve(
                        string.substring("Valve ".length(), string.indexOf(" has ")),
                        new HashSet<>(),
                        Long.parseLong(string.substring(string.indexOf("rate=") + "rate=".length(), string.indexOf(';'))),
                        0L,
                        false);
            }

            @Override
            public String toString() {
                return name + ": " + pressure + " (" + rate + ": " + String.join(", ", tunnels.stream().map(t -> t.name).sorted().toList()) + ")";
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

        public Set<Valve> tunnels() {
            return tunnels;
        }

        public long rate() {
            return rate;
        }

        public long pressure() {
            return pressure;
        }

        public boolean open() {
            return open;
        }

        public void openValve() {
            open = true;
        }

        public void tick() {
            if (open) {
                pressure += rate;
            }
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var volcano = Volcano.parse(input, 30);

            return volcano.routes().stream().mapToLong(route -> {
                volcano.reset();

                for(var link : route.links) {
                    link.path.subList(1, link.path.size()).forEach(s -> {
                        volcano.tick();
                    });

                    volcano.tick();
                    volcano.open(link.to());
                }

                IntStream.range(volcano.time, 30 + 10).forEach(i -> {
                    volcano.tick();
                });

                return volcano.pressure();
            }).max().orElseThrow();
        };
    }

    static Action open(final Valve value) {
        return new Action() {
            @Override
            public void apply(Volcano volcano) {
                volcano.open(value);
            }

            @Override
            public String toString() {
                return "open " + value;
            }
        };
    }


    static Action walk(final Valve value) {
        return new Action() {
            @Override
            public void apply(Volcano volcano) {
            }

            @Override
            public String toString() {
                return "walk " + value;
            }
        };
    }


    interface Action {
        void apply(Volcano volcano);

        static List<Action> create(Route route) {
            var result = new ArrayList<Action>();

            for (var link : route.links) {
                link.path.subList(1, link.path.size()).forEach(s -> {
                    result.add(walk(s));
                });

                result.add(open(link.to()));
            }

            return result;
        }
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var volcano = Volcano.parse(input, 26);

            List<Route2> routes = volcano.routes2();
            long max = 0;
            for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
//                LOG.info("route " + (routeIndex + 1) + " / " + routes.size() + ": max = " + max);

                var route = routes.get(routeIndex);

                volcano.reset();

                var meActions = Action.create(route.me);
                var elephantActions = Action.create(route.elephant);

                for (int i = 0; i < 28 + 5; i++) {
                    volcano.tick();

                    if (i < meActions.size()) {
                        var action = meActions.get(i);
                        action.apply(volcano);

//                        LOG.info(() -> "" + volcano.time + ": me      : " + action);
                    }
                    if (i < elephantActions.size()) {
                        var action = elephantActions.get(i);
                        action.apply(volcano);

//                        LOG.info(() -> "" + volcano.time + ": elephant: " + action);
//                        LOG.info(() -> "pressure: " + volcano.pressure());
                    }
                }

                max = Math.max(max, volcano.pressure());
            }

            return max;
        };
    }
}
