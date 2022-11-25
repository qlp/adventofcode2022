mkdir -p out/script 2> /dev/null
find nl -type f -name "*.java" > out/script/sources.txt
find nl -type f ! -name "*.java" > out/script/resources.txt

/Users/jpruijs/Library/Java/JavaVirtualMachines/openjdk-19.0.1/Contents/Home/bin/javac -d ./out/script @out/script/sources.txt
rsync -a . --files-from=out/script/resources.txt out/script

/Users/jpruijs/Library/Java/JavaVirtualMachines/openjdk-19.0.1/Contents/Home/bin/java -cp out/script nl.q8p.aoc2022.Main