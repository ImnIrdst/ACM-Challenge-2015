# About
A Java Client for Competing in ACM Challenge 2015 

# Enviroment
Developed in Intellij Idea Community Edition 14.0

# Links

* [ACM ICPC 15 (icpc.sharif.edu)](http://icpc.sharif.edu/acmicpc15/)
* [Game rules](http://acmwiki.ir/%D9%85%D8%B3%D8%A7%D8%A8%D9%82%D9%87%E2%80%8C%DB%8C_%DA%86%D8%A7%D9%84%D8%B4%DB%8C/%DB%B1%DB%B3%DB%B9%DB%B4/%D9%81%D9%87%D8%B1%D8%B3%D8%AA)
* [Game binaries](https://www.dropbox.com/s/gixn0aj77q5fgps/Version1.rar?dl=0)
* [MIT Battle Code Competition](https://www.battlecode.org/)
* [MIT Battle Code Course](http://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-370-the-battlecode-programming-competition-january-iap-2013/)

# Improvements (After Competition)
* Using Dijkstra instead of BFS
* Writing Test Units
* Writing better commit messages :D

# Ideas

## Shuffle Players for random priorities
* this avoids some repetitive movements.

## Implement Avoid Bullets function
* ... .

## Enemy Hunter or Player on the Gold.
* Send Hunter Using BFS to Kill Him.
* If Enemy Player on the Gold Dont Send Miners. Just Send Hunters.


## Code Multiple Clients
* For Diffrent Strategies Keep Diffrent Clients Compete Them With Each Other and Compare Them.
* Use git branches and then keep they in jar files.

## Diffrent Policies for Navigation
* Code Diffrent Navigation Policies (toTarget, discovery, random walk, forceWalk, ...)
* Set Score for each cell (use constants)
* Don't go in a single direction too long (if Enemy is not behind you scape Bullets easly)
* Use Bfs for Some Constant Undiscovered Edge Locations.
* Use Wings Idea: Go Ahead Until Its Possible

## Policies for Miner (BFS From Golds and Enemies)
* We Can Run a BFS from Golds and Keep it in a table to send our Miners To There.
* We Can Run a BFS from Enemy Miners (or Players) and send our Hunters There.
* Use Fuzzy Assigning for Nearest miner to a gold.

## Policies for Hunters
* Keep Hunters Watching (Turning Around) on Gold Till One Of the Miners Get There.
* Send two hunters with each other and use them for side attacks.
* Send Hunters to kill other Miners (and Hunters).
* Send Hunters to protect Minters.
* Fire a few amount of arrows.

## Policies for Spies
* send spies for undetected cells.
* In very first rounds send spies unhidden after he sees an enemy (or after a constant period). hide him after he discovers all the map.

## Policies for each target (gold or hunt)
* have diffrent grids per diffrent types of target.
* send one miner per gold and two hunters per hunt.

## Use Tangents Theory
* Mark Blind Points
