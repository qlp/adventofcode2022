package nl.q8p.aoc2022.day07;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class Day07 implements Day {

    sealed interface Item permits Folder, File {
        long size();
    }

    record Path(LinkedList<String> segments) {
        Path() {
            this(new LinkedList<>());
        }

        public String firstSegment() {
            return segments.getFirst();
        }

        public Path withoutFirstSegment() {
            return new Path(new LinkedList<>(segments.subList(1, segments.size())));
        }

        public void changeDirectory(String name) {
            switch (name) {
                case "/" -> segments.clear();
                case ".." -> segments.removeLast();
                default -> segments.add(name);
            }
        }

        public boolean isEmpty() {
            return segments.isEmpty();
        }
    }

    record Folder(Map<String, Item> contents)
    implements Item {
        Folder() {
            this(new HashMap<>());
        }

        public void createItem(Path path, String name, Item item) {
            folder(path).contents.put(name, item);
        }

        public Folder folder(Path path) {
            if (path.isEmpty()) {
                return this;
            } else if (contents.get(path.firstSegment()) instanceof Folder f) {
                return f.folder(path.withoutFirstSegment());
            } else {
                throw new NoSuchElementException(path.toString());
            }
        }

        public long size() {
            return contents.values().stream().mapToLong(Item::size).sum();
        }

        public List<Item> flatten() {
            return Stream.concat(Stream.of(this), contents.values().stream().flatMap(i -> {
                if (i instanceof Folder f) {
                    return f.flatten().stream();
                } else if (i instanceof File f) {
                    return Stream.<Item>of(f);
                } else {
                    throw new IllegalStateException("Unexpected item: " + i);
                }
            })).toList();
        }
    }

    record File(long size)
    implements Item { }

    record Device(Folder filesystem, Path path) {
        Device() {
            this(new Folder(), new Path());
        }

        void execute(String command) {
            if (command.startsWith("$ cd ")) {
                path.changeDirectory(command.substring("$ cd ".length()));
            } else if (command.equals("$ ls")) {
                // ignore, reading in next lines
            } else if (command.startsWith("dir ")) {
                var name = command.substring("dir ".length());
                filesystem.createItem(path, name, new Folder());
            } else {
                // should be a file
                var size = Long.parseLong(command.substring(0, command.indexOf(' ')));
                var name = command.substring(command.indexOf(' ') + 1);

                filesystem.createItem(path, name, new File(size));
            }
        }
    }


    @Override
    public Assignment first() {
        return (run, input) -> {
            var device = new Device();

            Arrays.stream(input.split("\\n")).forEach(device::execute);

            return device.filesystem.flatten().stream()
                    .filter(Folder.class::isInstance)
                    .mapToLong(Item::size)
                    .filter(s -> s <= 100000L)
                    .sum();
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var device = new Device();

            Arrays.stream(input.split("\\n")).forEach(device::execute);

            long total = 70000000L;
            long required = 30000000L;
            long used = device.filesystem.size();

            long free = total - used;

            long toDelete = required - free;

            return device.filesystem.flatten().stream()
                    .filter(Folder.class::isInstance)
                    .mapToLong(Item::size)
                    .filter(s -> s >= toDelete)
                    .min()
                    .orElseThrow();
        };
    }
}
