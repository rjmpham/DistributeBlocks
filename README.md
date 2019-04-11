# DistributeBlocks
Demonstrate the knowledge of distributed computing via an application of blockchain

## Unix
### Compilation
```
find -name "*.java" > sources.txt
javac -cp "gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar" @sources.txt
```
### Running
```
java -cp .:gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar distributeblocks.Node
```

## Windows
### Compilation
```
dir /s /B *.java > sources.txt
javac -cp "gson-2.8.5.jar;picocli-4.0.0-alpha-1.jar" @sources.txt
```
### Running
```
java -cp .:gson-2.8.5.jar;picocli-4.0.0-alpha-1.jar distributeblocks.Node
```
