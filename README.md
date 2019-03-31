# DistributeBlocks
Demonstrate the knowledge of distributed computing via an application of blockchain


# Running a Network

To run a seed node:

java -jar <theJarFile> seed port <port>


To run a node:

java -jar <theJarFile> port <port> config <configFile> chainfile <chainfile>


If you are lazy:

java -jar CoinCoin.jar seed port 1234

java -jar CoinCoin.jar port 2000 seedAddr localhost 1234 config ./node_1_config.txt chainfile node_1_chainfile.txt

java -jar CoinCoin.jar port 2001 seedAddr localhost 1234 config ./node_2_config.txt chainfile node_2_chainfile.txt

java -jar CoinCoin.jar port 2002 seedAddr localhost 1234 config ./node_3_config.txt chainfile node_3_chainfile.txt

java -jar CoinCoin.jar port 2003 seedAddr localhost 1234 config ./node_4_config.txt chainfile node_4_chainfile.txt

Just remember to change the name of the .jar
