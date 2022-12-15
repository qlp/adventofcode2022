rm -rf out
mkdir -p out/target 2> /dev/null
find nl -type f -name "*.java" > out/sources.txt
find nl -type f ! -name "*.java" > out/resources.txt


javac -d ./out/target @out/sources.txt
rsync -a . --files-from=out/resources.txt out/target
cp logging.properties out/target

cd out/target || exit
jar cfm ../aoc2022.jar ../../Manifest.txt ./*
cd ../..
