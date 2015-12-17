try {
            mapReader = new Scanner(mapFile);
            this.finishCycle = mapReader.nextInt();
            this.board.setNumberOfRows(mapReader.nextInt());
            this.board.setNumberOfColumns(mapReader.nextInt());
            Cell[][] ex = new Cell[this.board.getNumberOfRows()][this.board.getNumberOfColumns()];

            int numberOfGolds;
            int numOfPlayers;
            int teamId;
            for(int golds = 0; golds < this.board.getNumberOfRows(); ++golds) {
                for(numberOfGolds = 0; numberOfGolds < this.board.getNumberOfColumns(); ++numberOfGolds) {
                    String numberOfHunters = mapReader.next();
                    CellType numberOfWorkers = CellType.SOLID;
                    CellType[] numberOfSpies = CellType.values();
                    numOfPlayers = numberOfSpies.length;

                    for(teamId = 0; teamId < numOfPlayers; ++teamId) {
                        CellType positions = numberOfSpies[teamId];
                        if(positions.getMapFileSymbol() == numberOfHunters.charAt(0)) {
                            numberOfWorkers = positions;
                            break;
                        }
                    }

                    Cell var30 = new Cell(golds, numberOfGolds, numberOfWorkers);
                    ex[golds][numberOfGolds] = var30;
                }
            }

            this.board.setCells(ex);
            ArrayList var27 = new ArrayList();
            numberOfGolds = mapReader.nextInt();

            int var28;
            int var29;
            int var31;
            for(var28 = 0; var28 < numberOfGolds; ++var28) {
                var29 = mapReader.nextInt();
                var31 = mapReader.nextInt();
                Gold var32 = new Gold(var28, ex[var29][var31], mapReader.nextInt());
                var27.add(var32);
            }

            this.board.setGolds(var27);
            Config.BULLET_CAPICITY = mapReader.nextInt();
            Config.SPY_HIDENESS_CAPACITY = mapReader.nextInt();
            var28 = mapReader.nextInt();
            var29 = mapReader.nextInt();
            var31 = mapReader.nextInt();
            Config.NUM_OF_SPIES = var31;
            Config.NUM_OF_HUNTERS = var28;
            Config.NUM_OF_GOLD_MINERS = var29;
            numOfPlayers = Config.NUM_OF_GOLD_MINERS + Config.NUM_OF_HUNTERS + Config.NUM_OF_SPIES;
            teamId = 0;

            for(this.idToPlayer = new HashMap(); teamId < 2; ++teamId) {
                Cell[] var33 = new Cell[numOfPlayers];
                Direction[] direction = new Direction[numOfPlayers];

                int i;
                for(int teamPlayers = 0; teamPlayers < numOfPlayers; ++teamPlayers) {
                    i = mapReader.nextInt();
                    int y = mapReader.nextInt();
                    var33[teamPlayers] = this.board.getCell(i, y);
                    String dir = mapReader.next().toLowerCase();
                    byte var19 = -1;
                    switch(dir.hashCode()) {
                    case 3739:
                        if(dir.equals("up")) {
                            var19 = 2;
                        }
                        break;
                    case 3089570:
                        if(dir.equals("down")) {
                            var19 = 3;
                        }
                        break;
                    case 3317767:
                        if(dir.equals("left")) {
                            var19 = 1;
                        }
                        break;
                    case 108511772:
                        if(dir.equals("right")) {
                            var19 = 0;
                        }
                    }

                    switch(var19) {
                    case 0:
                        direction[teamPlayers] = Direction.RIGHT;
                        break;
                    case 1:
                        direction[teamPlayers] = Direction.LEFT;
                        break;
                    case 2:
                        direction[teamPlayers] = Direction.UP;
                        break;
                    case 3:
                        direction[teamPlayers] = Direction.DOWN;
                        break;
                    default:
                        l("Wrong direction in map!");
                    }
                }

                Player[] var34 = this.teams[teamId].initializeTeam(var33, direction);

                for(i = 0; i < numOfPlayers; ++i) {
                    this.idToPlayer.put(Integer.valueOf(var34[i].getId()), var34[i]);
                }
            }
        } catch (FileNotFoundException var24) {
            e(var24);
        } catch (Exception var25) {
            e(var25);
        } finally {
            if(mapReader != null) {
                mapReader.close();
            }

        }