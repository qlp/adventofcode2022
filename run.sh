mkdir -p out/script 2> /dev/null
find nl -type f -name "*.java" > out/script/sources.txt
find nl -type f ! -name "*.java" > out/script/resources.txt

javac -d ./out/script @out/script/sources.txt
rsync -a . --files-from=out/script/resources.txt out/script

java -cp out/script nl.q8p.aoc2022.Main