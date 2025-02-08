# Hex-PROP
Repositori de la ultima practica de PROP.

This repository has two AI systems that play the HEX game. The actual codes for AI systems can be found in src/edu/upc/epsevg/prop/hex/players.

1 - MinimaxPlayer: This AI uses Minimax, and it stops at a given level of exploration. It uses Dijkstra as the main heuristic in order to calculate the number of remained pieces until a win or loss.

2 - PlayerID: This AI uses Minimax with Iterative Deepening, and the exploration stops at a timeout. Dijkstra is the main heuristic, but other heuristics are also used.

To read about other heuristics, details of implementation, performance evaluation, etc. please refer to the documentation: Documentacio_HEX.pdf
