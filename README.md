Token Ring Simulator
============

This java program simulates a token ring network. It was written for class at NMT.

To execute first generate the input by doing the following:
```
cd input
./Project_2_GenerateInput.py <Number of Nodes>
```

The program can then be executed with:
```
mvn compile exec:java -Dexec.args=<Number of Nodes>
```

where ```<Number of Nodes>``` is replaced with an integer. 
