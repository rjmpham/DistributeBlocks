# DistributeBlocks
Demonstrate the knowledge of distributed computing via an application of blockchain

## Unix
### Compilation
```
find -name "*.java" > sources.txt
javac -cp "gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar:gs-algo-1.3.jar:gs-core-1.3.jar:gs-ui-1.3.jar" @sources.txt
```
### Running Blank Node
```
java -cp .:gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar:gs-algo-1.3.jar:gs-core-1.3.jar:gs-ui-1.3.jar distributeblocks.Node
```
### Running Local Seed 1 on port 1234
```
java -cp .:gson-2.8.5.jar:picocl4.0.0-alpha-1.jar:gs-algo-1.3.jar:gs-core-1.3.jar:gs-ui-1.3.jar distributeblocks.Node start -s -p 1234
```
### Monitor running on port 9000
```
java -cp .:gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar:gs-algo-1.3.jar:gs-core-1.3.jar:gs-ui-1.3.jar distributeblocks.Node start -p 9000 -sAddr localhost -sPort 1234 --monitor --stdout
```
### Basic Process running on port 9001 expecting a local seed on port 1234
```
java -cp .:gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar:gs-algo-1.3.jar:gs-core-1.3.jar:gs-ui-1.3.jar distributeblocks.Node start -p 9001 -sAddr localhost -sPort 1234
```

### Detailed Process running on port 9002 expecting a local seed on port 1234
```
java -cp .:gson-2.8.5.jar:picocli-4.0.0-alpha-1.jar:gs-algo-1.3.jar:gs-core-1.3.jar:gs-ui-1.3.jar distributeblocks.Node start -p 9002 -sAddr localhost -sPort 1234 --stdout
```

## Windows
### Compilation
```
dir /s /B *.java > sources.txt
javac -cp "gson-2.8.5.jar;picocli-4.0.0-alpha-1.jar;gs-algo-1.3.jar;gs-core-1.3.jar;gs-ui-1.3.jar" @sources.txt
```
### Running
```
java -cp .:gson-2.8.5.jar;picocli-4.0.0-alpha-1.jar;gs-algo-1.3.jar;gs-core-1.3.jar;gs-ui-1.3.jar distributeblocks.Node
```
