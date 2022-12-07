package nl.q8p.aoc2022.day07;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Day07 implements Day {

    static String withIndent(int indent, String string) {
        return "  ".repeat(indent) + string;
    }

    sealed interface Item permits Folder, File {
        String name();

        long size();
    }

    record Path(LinkedList<String> segments) {
        Path() {
            this(new LinkedList<>());
        }

        void gotoRoot() {
            segments.clear();
        }

        void gotoParent() {
            segments.removeLast();
        }

        public void gotoChild(String child) {
            segments.add(child);
        }

        @Override
        public String toString() {
            return String.join("/", segments);
        }
    }

    record Folder(String name, List<Item> contents)
    implements Item {
        Folder() {
            this(null);
        }

        Folder(String name) {
            this(name, new ArrayList<>());
        }

        public void createItem(Path path, Item item) {
            var target = item(path);

            if (target instanceof Folder folder) {
                folder.contents.add(item);
            } else {
                throw new IllegalStateException("Can't add file to file: " + target + " (in " + path + ")");
            }
        }

        public Item item(Path path) {
            Item result = this;

            for(String segment : path.segments) {
                if (result instanceof Folder folder) {
                    result = folder.contents.stream().filter(i -> i.name().equals(segment)).findFirst().orElseThrow(() -> new IllegalStateException("File not found: " + segment + " in " + path));
                } else {
                    throw new IllegalStateException("Can't list file " + segment + " when evaluating path: " + path);
                }
            }

            return result;
        }

        public long size() {
            return contents.stream().mapToLong(Item::size).sum();
        }

        public List<Item> flatten() {
            return Stream.concat(Stream.of(this), contents.stream().flatMap(i -> {
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

    record File(String name, long size)
    implements Item {

    }

    record Device(Folder filesystem, Path path) {
        Device() {
            this(new Folder(), new Path());
        }

        void execute(String command) {
            if (command.equals("$ cd /")) {
                path.gotoRoot();
            } else if (command.equals("$ cd ..")) {
                path.gotoParent();
            } else if (command.startsWith("$ cd ")) {
                path.gotoChild(command.substring("$ cd ".length()));
            } else if (command.equals("$ ls")) {
                // ignore, reading in next lines
            } else if (command.startsWith("dir ")) {
                var name = command.substring("dir ".length());
                filesystem.createItem(path, new Folder(name));
            } else {
                // should be a file
                var size = Long.parseLong(command.substring(0, command.indexOf(' ')));
                var name = command.substring(command.indexOf(' ') + 1);

                filesystem.createItem(path, new File(name, size));
            }
        }
    }


    @Override
    public Assignment first() {
        return input -> {
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
        return input -> {
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
