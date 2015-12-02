# Links

* [ACM ICPC 15 (icpc.sharif.edu)](http://icpc.sharif.edu/acmicpc15/)
* [Game rules](http://acmwiki.ir/%D9%85%D8%B3%D8%A7%D8%A8%D9%82%D9%87%E2%80%8C%DB%8C_%DA%86%D8%A7%D9%84%D8%B4%DB%8C/%DB%B1%DB%B3%DB%B9%DB%B4/%D9%81%D9%87%D8%B1%D8%B3%D8%AA)
* [Game binaries](https://www.dropbox.com/s/gixn0aj77q5fgps/Version1.rar?dl=0)
* [MIT Battle Code Competition](https://www.battlecode.org/)
* [MIT Battle Code Course](http://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-370-the-battlecode-programming-competition-january-iap-2013/)

# Ideas
## Code Multiple Clients
* For Diffrent Strategies Keep Diffrent Clients Compete Them With Each Other and Compare Them.
* Use git branches and then keep they in jar files.

## Use Sorushes Wings Idea
* Go Ahead Until

## Use Tangents Theory
* Mark Blind Points

## BFS From Golds and Enemies
* We Can Run a BFS from Golds and Keep it in a table to send our Miners To There.
* We Can Run a BFS from Enemy Miners (or Players) and send our Hunters There.

## Diffrent Policies for Navigation
* Set Score for each cell (use constants)
* Don't go in a single direction too long (if Enemy is not behind you scape Bullets easly)


## Diffrent Policies for Sending Hunters
* Send two hunters with each other and use them for side attacks.
* Send Hunters to kill other Miners (and Hunters).
* Send Hunters to protect Minters.
* Fire low amount of arrows.

## Policies for each target (gold or hunt)
* have diffrent grids per diffrent types of target.
* send one miner per gold and two hunters per hunt.

## Policies for Spies
* send spies for undetected cells.
* in very first rounds send spies unhidden after he sees an enemy (or after a constant period). hide him after he discovers all the map.

# Todos
06. fix the probelm with enemyInfo.
07. Code Miner Logic (BFS)
09. Code Hunter Logic (go Hunt)

# Done
00. Create Git Repository
01. Rename TiZii Files. (Move TiZii suffix to prefix)
02. Comment Codes (add todos)
03. Add Blocks to TiZii Board
04. Code Spy Logic
05. add Scoring function
