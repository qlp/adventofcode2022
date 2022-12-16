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
            newLinks.add(link);

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
            tick();
            valvesByName.get(valve.name).openValve();
        }

        public void walk() {
            tick();
        }

        public void rest() {
            tick();
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
                        volcano.walk();
                    });

                    volcano.open(link.to());
                }

                IntStream.range(volcano.time, 30 + 10).forEach(i -> {
                    volcano.rest();
                });

                return volcano.pressure();
            }).max().orElseThrow();
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var volcano = Volcano.parse(input, 26);

            LOG.info(() -> "Volcano:\n" + volcano);

//            var result = volcano.routes().stream().mapToLong(route -> {
////                LOG.info(() -> "==========================================================");
//
////                LOG.info(route::toString);
//
//                volcano.reset();
//
//                for(var link : route.links) {
//                    link.path.subList(1, link.path.size()).forEach(s -> {
//                        volcano.walk();
//
////                        LOG.info(() -> "" + volcano.time + ": " + volcano.pressure() + " WALKED TO " + s.name());
//                    });
//
//                    volcano.open(link.to());
////                    LOG.info(() -> "" + volcano.time + ": " + volcano.pressure() + " OPEN " + link.to().name);
//                }
//
//                IntStream.range(volcano.time, 30 + 10).forEach(i -> {
//                    volcano.rest();
////                    LOG.info(() -> "" + volcano.time + ": " + volcano.pressure() + " REST");
//                });
//
////                LOG.info(() -> "==========================================================");
////                  LOG.info(() -> "" + volcano.time + ": " + volcano.pressure() + " DONE");
//
//                return volcano.pressure();
//            }).max().orElseThrow();

            return volcano.routes();
        };
    }
}
