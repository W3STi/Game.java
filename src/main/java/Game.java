import java.util.ArrayList;
import java.util.Objects;

public class Game {
    int sizeOfCells = 64;
    int countTurns = 0;
    boolean flagOfMovingKing = false;
    boolean flagOfMovingKing2 = false;
    boolean flagOfMovingLeftRook = false;
    boolean flagOfMovingRightRook = false;
    boolean flagOfMovingLeftRook2 = false;
    boolean flagOfMovingRightRook2 = false;
    static boolean flagOfMate = false;
    boolean castlingOO = false;
    boolean castlingOOO = false;
    boolean takeOnThePass = false;
    boolean flagOfChoose = false;

    int[] cOurKing = {7, 4};
    int[] cOpponentKing = {0, 4};
    int[] prevTurnOurPawn = { 0, 0, 0, 0 };
    int[] prevTurnOpponentPawn = { 0, 0, 0, 0 };
    ArrayList<AttackMate> arrayAttackMate = new ArrayList<AttackMate>();
    String[][] board = new String[8][8];
    int[][] attackOur = new int[8][8];
    int[][] attackOpponent = new int[8][8];
    int[][] tempAttack = new int[8][8];

    public Game () {
        String figures = "rnbqkbnrpppppppp11111111111111111111111111111111PPPPPPPPRNBQKBNR";
        for (var i = 0; i < sizeOfCells / 8; i++) {
            for (var j = 0; j < sizeOfCells / 8; j++) {
                board[i][j] = String.valueOf(figures.charAt((i * (sizeOfCells / 8)) + j));
            }
        }
        setAttack();
    }

    public static class AttackMate {
        int x;
        int y;

        public AttackMate() {

        }

        public AttackMate(int _y, int _x) {
            y = _y;
            x = _x;
        }
    }

    public String moveFigure(int from_y, int from_x, int to_y, int to_x, String choose) {
        //Если ход в ту же клетку или ход невозможен
        String result = "";
        if (!Objects.equals(board[from_y][from_x], "1") && !flagOfMate) {
            if (((from_y == to_y) && (from_x == to_x))  ||
                    (!checkTurn(from_y, from_x, to_y, to_x))) {
                return "false";
            }
            else {
                if (!Objects.equals(board[from_y][from_x], "p") &&
                        !Objects.equals(board[from_y][from_x], "P")) {
                    prevTurnOurPawn[0] = 0;
                    prevTurnOurPawn[1] = 0;
                    prevTurnOurPawn[2] = 0;
                    prevTurnOurPawn[3] = 0;

                    prevTurnOpponentPawn[0] = 0;
                    prevTurnOpponentPawn[1] = 0;
                    prevTurnOpponentPawn[2] = 0;
                    prevTurnOpponentPawn[3] = 0;
                }

                //Проверка флагов для рокировки
                checkFlags(from_y, from_x);

                //Если это не рокировка и не взятие на проходе
                if (castlingOO) {
                    result = "OO";
                    castlingOO = false;
                }
                else if (castlingOOO) {
                    result = "OOO";
                    castlingOOO = false;
                }
                else if (takeOnThePass) {
                    result = "takeOnThePass";
                    String figure = board[from_y][from_x];
                    board[from_y][from_x] = "1";
                    board[to_y + ((from_y == 3) ? 1 : -1)][to_x] = "1";
                    board[to_y][to_x] = figure;
                    takeOnThePass = false;
                }
                else if (flagOfChoose) {
                    result = "choose";
                    board[from_y][from_x] = "1";
                    board[to_y][to_x] = choose;
                    flagOfChoose = false;
                }
                else {
                    result = "true";
                    String figure = board[from_y][from_x];
                    board[from_y][from_x] = "1";
                    board[to_y][to_x] = figure;
                }

                setKings(to_y, to_x);

                countTurns++;
                setAttack();

                /*for (var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
                    String str = "";
                    for (var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
                        str += attackOur[coord_y][coord_x];
                    }
                    System.out.println(str);
                }*/

                if (checkMate()) {
                    flagOfMate = true;
                }
            }
        }
        return result;
    }

    public boolean testForCheck(int from_y, int from_x, int to_y, int to_x) {
        String tempFigure;
        String tempTo;
        if (!takeOnThePass) {
            tempFigure = board[from_y][from_x];
            tempTo = board[to_y][to_x];

            String figure = board[from_y][from_x];
            board[from_y][from_x] = "1";
            board[to_y][to_x] = figure;

            setKings(to_y, to_x);

            if (!notStayCheck(to_y, to_x)) {
                board[from_y][from_x] = tempFigure;
                board[to_y][to_x] = tempTo;
                setKings(from_y, from_x);
                setAttack();
                return true;
            }
            else {
                board[from_y][from_x] = tempFigure;
                board[to_y][to_x] = tempTo;
                setKings(from_y, from_x);
                setAttack();
                return false;
            }
        }
        else if (takeOnThePass) {
            tempFigure = board[from_y][from_x];
            tempTo = board[to_y + ((from_y == 3) ? 1 : -1)][to_x];

            String figure = board[from_y][from_x];
            board[from_y][from_x] = "1";
            board[to_y + ((from_y == 3) ? 1 : -1)][to_x] = "1";
            board[to_y][to_x] = figure;

            setKings(to_y, to_x);

            if (!notStayCheck(to_y, to_x)) {
                board[from_y][from_x] = tempFigure;
                board[to_y + ((from_y == 3) ? 1 : -1)][to_x] = tempTo;
                board[to_y][to_x] = "1";
                setAttack();
                return true;
            }
            else {
                board[from_y][from_x] = tempFigure;
                board[to_y + ((from_y == 3) ? 1 : -1)][to_x] = tempTo;
                board[to_y][to_x] = "1";
                setAttack();
                return false;
            }
        }
        return false;
    }

    public void setKings(int to_y, int to_x) {
        if (Objects.equals(board[to_y][to_x], "K")) {
            cOurKing[0] = to_y;
            cOurKing[1] = to_x;
        }
        else if (Objects.equals(board[to_y][to_x], "k")) {
            cOpponentKing[0] = to_y;
            cOpponentKing[1] = to_x;
        }
    }

    public void checkFlags(int from_y, int from_x) {
        //Проверка на ход ладьи/короля для рокировки
        if (Objects.equals(board[from_y][from_x], "K")) flagOfMovingKing = true;

        if (Objects.equals(board[from_y][from_x], "R") &&
                from_y == 7 && from_x == 0) flagOfMovingLeftRook = true;
        else if (Objects.equals(board[from_y][from_x], "R") &&
                from_y == 7 && from_x == 7) flagOfMovingRightRook = true;

        //Для оппонента
        if (Objects.equals(board[from_y][from_x], "k")) flagOfMovingKing2 = true;

        if (Objects.equals(board[from_y][from_x], "r") &&
                from_y == 0 && from_x == 0) flagOfMovingLeftRook2 = true;
        else if (Objects.equals(board[from_y][from_x], "r") &&
                from_y == 0 && from_x == 7) flagOfMovingRightRook2 = true;
    }

    public boolean checkTurn(int from_y, int from_x, int to_y, int to_x) {
        if ((board[from_y][from_x].toUpperCase().equals(board[from_y][from_x])) && (countTurns % 2 == 0)) {
            switch (board[from_y][from_x]) {
                case "K" :
                    return moveKing  (from_y, from_x, to_y, to_x);
                case "Q" :
                    return moveQueen (from_y, from_x, to_y, to_x);
                case "R" :
                    return moveRook  (from_y, from_x, to_y, to_x);
                case "B" :
                    return moveBishop(from_y, from_x, to_y, to_x);
                case "N" :
                    return moveKnight(from_y, from_x, to_y, to_x);
                case "P" :
                    return movePawn  (from_y, from_x, to_y, to_x);
            }
        }
        else if ((!board[from_y][from_x].toUpperCase().equals(board[from_y][from_x])) &&
                (countTurns % 2 != 0)) {
            switch (board[from_y][from_x]) {
                case "k":
                    return moveKing(from_y, from_x, to_y, to_x);
                case "q":
                    return moveQueen(from_y, from_x, to_y, to_x);
                case "r":
                    return moveRook(from_y, from_x, to_y, to_x);
                case "b":
                    return moveBishop(from_y, from_x, to_y, to_x);
                case "n":
                    return moveKnight(from_y, from_x, to_y, to_x);
                case "p":
                    return movePawn(from_y, from_x, to_y, to_x);
            }
        }
        return false;
    }

    public boolean notTakeOwnFigure(int from_y, int from_x, int to_y, int to_x) {
        if (Objects.equals(board[to_y][to_x], "1")) return true;
        return ((board[from_y][from_x].toUpperCase().equals(board[from_y][from_x])) &&
                (!board[to_y][to_x].toUpperCase().equals(board[to_y][to_x]))) ||
                ((!board[from_y][from_x].toUpperCase().equals(board[from_y][from_x])) &&
                        (board[to_y][to_x].toUpperCase().equals(board[to_y][to_x])));
    }

    public boolean notDepartureOfTheKing(int[] tempAtt) {
        if (tempAtt[0] == 0 && tempAtt[1] == 0) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] + 1);
        }
        else if (tempAtt[0] == 0 && tempAtt[1] == 7) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] - 1);
        }
        else if (tempAtt[0] == 7 && tempAtt[1] == 0) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] + 1);
        }
        else if (tempAtt[0] == 7 && tempAtt[1] == 7) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] - 1);
        }
        else if (tempAtt[0] == 0 && tempAtt[1] != 7 && tempAtt[1] != 0) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] - 1);
        }
        else if (tempAtt[0] == 7 && tempAtt[1] != 7 && tempAtt[1] != 0) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] - 1);
        }
        else if (tempAtt[1] == 7 && tempAtt[0] != 7 && tempAtt[0] != 0) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1]);
        }
        else if (tempAtt[1] == 0 && tempAtt[0] != 7 && tempAtt[0] != 0) {
            return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1]);
        }
        else return !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] - 1, tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0], tempAtt[1] + 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] - 1) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1]) &&
                    !moveKing(tempAtt[0], tempAtt[1], tempAtt[0] + 1, tempAtt[1] + 1);
    }

    public boolean checkForDefence(String sideAttackKing) {
        for (var fromCoord_y = 0; fromCoord_y < sizeOfCells / 8; fromCoord_y++) {
            for (var fromCoord_x = 0; fromCoord_x < sizeOfCells / 8; fromCoord_x++) {
                if (sideAttackKing.toUpperCase().equals(sideAttackKing)) {
                    switch (board[fromCoord_y][fromCoord_x]) {
                        case "Q" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveQueen(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "R" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveRook(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "B" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveBishop(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "N" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveKnight(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "P" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (movePawn(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                    }
                }
                else {
                    switch (board[fromCoord_y][fromCoord_x]) {
                        case "q" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveQueen(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "r" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveRook(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "b" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveBishop(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "n" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (moveKnight(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                        case "p" :
                            for (AttackMate attackMate : arrayAttackMate) {
                                if (movePawn(fromCoord_y, fromCoord_x, attackMate.y, attackMate.x)) {
                                    return true;
                                }
                            }
                            break;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkMate() {
        int[] tempAtt = new int[2];
        boolean flagOfDoubleCheck = false;
        String sideAttackKing;

        if (attackOur[cOpponentKing[0]][cOpponentKing[1]] > 0) {
            tempAtt[0] = cOpponentKing[0];
            tempAtt[1] = cOpponentKing[1];
            sideAttackKing = board[cOpponentKing[0]][cOpponentKing[1]];
            if (attackOur[cOpponentKing[0]][cOpponentKing[1]] > 1) flagOfDoubleCheck = true;
        }
        else if (attackOpponent[cOurKing[0]][cOurKing[1]] > 0) {
            tempAtt[0] = cOurKing[0];
            tempAtt[1] = cOurKing[1];
            sideAttackKing = board[cOurKing[0]][cOurKing[1]];
            if (attackOpponent[cOurKing[0]][cOurKing[1]] > 1) flagOfDoubleCheck = true;
        }
        else return false;

        if (notDepartureOfTheKing(tempAtt) && (flagOfDoubleCheck)) return true;
        else if (!checkForDefence(sideAttackKing) && notDepartureOfTheKing(tempAtt)) {
            return true;
        }

        arrayAttackMate.clear();
        return false;
    }

    public boolean notStayCheck(int to_y, int to_x) {
        setAttack();
        arrayAttackMate.clear();
        return ((attackOur[cOpponentKing[0]][cOpponentKing[1]] > 0 &&
                (!board[to_y][to_x].toUpperCase().equals(board[to_y][to_x]))) ||
                (attackOpponent[cOurKing[0]][cOurKing[1]] > 0 &&
                        (board[to_y][to_x].toUpperCase().equals(board[to_y][to_x]))));
    }

    public boolean moveKing(int from_y, int from_x, int to_y, int to_x) {
        if (Objects.equals(board[from_y][from_x], "K")) {
            if ((to_y == 7) && (to_x == 2)) {
                if(O_O_O()) {
                    castlingOOO = true;
                    return true;
                }
            }
            else if ((to_y == 7) && (to_x == 6)) {
                if(O_O()) {
                    castlingOO = true;
                    return true;
                }
            }
        }
        //Для рокировки оппонента
        else if (Objects.equals(board[from_y][from_x], "k")) {
            if ((to_y == 0) && (to_x == 2)) {
                if(O_O_O2()) {
                    castlingOOO = true;
                    return true;
                }
            }
            else if ((to_y == 0) && (to_x == 6)) {
                if(O_O2()) {
                    castlingOO = true;
                    return true;
                }
            }
        }
        if ((Math.abs(from_y - to_y) <= 1) && (Math.abs(from_x - to_x) <= 1)) {
            if (notTakeOwnFigure(from_y, from_x, to_y, to_x)) {
                return testForCheck(from_y, from_x, to_y, to_x);
            }

        }
        return false;
    }

    public boolean movePawn(int from_y, int from_x, int to_y, int to_x) {
        if (Objects.equals(board[from_y][from_x], "P")) {
            if (((Objects.equals(board[to_y][to_x], "1")) 										  	   &&
                    ((from_y == 6) && (from_y - to_y == 2) && (from_x == to_x) &&
                            (Objects.equals(board[5][to_x], "1")))										   			   ||
                    ((from_x == to_x) && (from_y - to_y == 1) 					   &&
                            (Objects.equals(board[to_y][to_x], "1")))					   						   ||
                    ((from_y - to_y == 1) && (Math.abs(to_x - from_x) == 1) 		   &&
                            (!Objects.equals(board[to_y][to_x], "1")) && (notTakeOwnFigure(from_y, from_x, to_y, to_x))))) {
                if (testForCheck(from_y, from_x, to_y, to_x)) {
                    //Взятие на проходе
                    if (to_y == 0) {
                        flagOfChoose = true;
                    }
                    prevTurnOurPawn[0] = from_y;
                    prevTurnOurPawn[1] = from_x;
                    prevTurnOurPawn[2] = to_y;
                    prevTurnOurPawn[3] = to_x;
                    return true;
                }
            }
            else if (((prevTurnOpponentPawn[0] == 1) && (prevTurnOpponentPawn[2] == 3) && (to_y == 2) && (from_y == 3) &&
                    (((from_x - 1 == prevTurnOpponentPawn[3]) && (to_x == from_x - 1))   ||
                            ((from_x + 1 == prevTurnOpponentPawn[3]) && (to_x == from_x + 1))))) {
                takeOnThePass = true;
                if (testForCheck(from_y, from_x, to_y, to_x)) {
                    prevTurnOurPawn[0] = from_y;
                    prevTurnOurPawn[1] = from_x;
                    prevTurnOurPawn[2] = to_y;
                    prevTurnOurPawn[3] = to_x;
                    return true;
                }
            }
        }
        //Для проверки хода соперника
        else if (Objects.equals(board[from_y][from_x], "p")) {
            if (((Objects.equals(board[to_y][to_x], "1")) 										       &&
                    ((from_y == 1) && (from_y - to_y == -2) && (from_x == to_x)&&
                            (Objects.equals(board[2][to_x], "1")))										   			   ||
                    ((from_x == to_x) && (from_y - to_y == -1) 					   &&
                            (Objects.equals(board[to_y][to_x], "1"))) 					   						   ||
                    ((from_y - to_y == -1) && (Math.abs(to_x - from_x) == 1) 	   &&
                            (!Objects.equals(board[to_y][to_x], "1")) && (notTakeOwnFigure(from_y, from_x, to_y, to_x))))) {
                if (testForCheck(from_y, from_x, to_y, to_x)) {
                    if (to_y == 7) {
                        flagOfChoose = true;
                    }
                    prevTurnOpponentPawn[0] = from_y;
                    prevTurnOpponentPawn[1] = from_x;
                    prevTurnOpponentPawn[2] = to_y;
                    prevTurnOpponentPawn[3] = to_x;
                    return true;
                }
            }
            else if (((prevTurnOurPawn[0] == 6) && (prevTurnOurPawn[2] == 4) && (to_y == 5) && (from_y == 4) &&
                    (((from_x - 1 == prevTurnOurPawn[3]) && (to_x == from_x - 1))        ||
                            ((from_x + 1 == prevTurnOurPawn[3]) && (to_x == from_x + 1))))) {
                takeOnThePass = true;
                if (testForCheck(from_y, from_x, to_y, to_x)) {
                    prevTurnOpponentPawn[0] = from_y;
                    prevTurnOpponentPawn[1] = from_x;
                    prevTurnOpponentPawn[2] = to_y;
                    prevTurnOpponentPawn[3] = to_x;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean moveRook(int from_y, int from_x, int to_y, int to_x) {
        if (((from_y == to_y) && (from_x != to_x) ||
                (from_y != to_y) && (from_x == to_x)) &&
                checkForRookInFront(from_y, from_x, to_y, to_x)) {
            return notTakeOwnFigure(from_y, from_x, to_y, to_x) &&
                    testForCheck(from_y, from_x, to_y, to_x);
        }
        return false;
    }

    public boolean moveBishop(int from_y, int from_x, int to_y, int to_x) {
        if ((((from_y + from_x) == (to_y + to_x)) ||
                ((8 - from_y + from_x) == (8 - to_y + to_x))) &&
                checkForBishopInFront(from_y, from_x, to_y, to_x)) {
            return notTakeOwnFigure(from_y, from_x, to_y, to_x) &&
                    testForCheck(from_y, from_x, to_y, to_x);
        }
        return false;
    }

    public boolean moveKnight(int from_y, int from_x, int to_y, int to_x) {
        var move_x = Math.abs(from_x - to_x);
        var move_y = Math.abs(from_y - to_y);
        if ((move_y == 1 && move_x == 2) || (move_y == 2 && move_x == 1)) {
            return notTakeOwnFigure(from_y, from_x, to_y, to_x) &&
                    testForCheck(from_y, from_x, to_y, to_x);
        }
        return false;
    }

    public boolean moveQueen(int from_y, int from_x, int to_y, int to_x) {
        if (((Math.abs(from_y - to_y) <= 1)   && (Math.abs(from_x - to_x) <= 1) ||
                (((from_y == to_y)            &&     (from_x != to_x)) 	 	    ||
                        (from_y != to_y)      &&     (from_x == to_x)) 	        ||
                ((from_y + from_x)            ==     (to_y + to_x)          	||
                        (8 - from_y + from_x) ==     (8 - to_y + to_x)))        &&
                (checkForBishopInFront(from_y, from_x, to_y, to_x) &&
                        checkForRookInFront(from_y, from_x, to_y, to_x))) {
            return notTakeOwnFigure(from_y, from_x, to_y, to_x) &&
                    testForCheck(from_y, from_x, to_y, to_x);
        }
        return false;
    }

    public boolean checkForRookInFront(int from_y, int from_x, int to_y, int to_x) {
        if ((from_x != to_x) && (from_y == to_y)) {
            if (to_x > from_x) {
                for (var i = from_x + 1; i != to_x; i++) {
                    if (!Objects.equals(board[from_y][i], "1")) return false;
                }
            }
            else {
                for (var i = from_x - 1; i != to_x; i--) {
                    if (!Objects.equals(board[from_y][i], "1")) return false;
                }
            }
        }
        else if ((from_x == to_x) && (from_y != to_y)) {
            if (to_y > from_y) {
                for (var i = from_y + 1; i != to_y; i++) {
                    if (!Objects.equals(board[i][from_x], "1")) return false;
                }
            }
            else {
                for (var i = from_y - 1; i != to_y; i--) {
                    if (!Objects.equals(board[i][from_x], "1")) return false;
                }
            }
        }
        return true;
    }

    public boolean checkForBishopInFront(int from_y, int from_x, int to_y, int to_x) {
        if (from_y > to_y && from_x > to_x) {
            for (int i = from_y - 1, j = from_x - 1; i > to_y; i--, j--) {
                if (!Objects.equals(board[i][j], "1")) return false;
            }
        }
        else if (from_y < to_y && from_x < to_x){
            for (int i = from_y + 1, j = from_x + 1; i < to_y; i++, j++) {
                if (!Objects.equals(board[i][j], "1")) return false;
            }
        }
        else if (from_y > to_y && from_x < to_x){
            for (int i = from_y - 1, j = from_x + 1; i > to_y; i--, j++) {
                if (!Objects.equals(board[i][j], "1")) return false;
            }
        }
        else if (from_y < to_y && from_x > to_x){
            for (int i = from_y + 1, j = from_x - 1; i < to_y; i++, j--) {
                if (!Objects.equals(board[i][j], "1")) return false;
            }
        }
        return true;
    }

    public boolean O_O() {
        if (attackOpponent[7][4] == 0 && attackOpponent[7][5] == 0 && attackOpponent[7][6] == 0 &&
                (Objects.equals(board[7][5], "1")) && (Objects.equals(board[7][6], "1")) && !flagOfMovingRightRook &&
                !flagOfMovingKing) {
            board[7][4] = "1";
            board[7][7] = "1";
            board[7][5] = "R";
            board[7][6] = "K";

            flagOfMovingKing = true;
            flagOfMovingRightRook = true;
            return true;
        }
        return false;
    }

    public boolean O_O_O() {
        if (attackOpponent[7][4] == 0 && attackOpponent[7][3] == 0 && attackOpponent[7][2] == 0 &&
                (Objects.equals(board[7][3], "1")) && (Objects.equals(board[7][2], "1")) && (Objects.equals(board[7][1], "1")) && !flagOfMovingLeftRook &&
                !flagOfMovingKing) {
            board[7][4] = "1";
            board[7][0] = "1";
            board[7][3] = "R";
            board[7][2] = "K";

            flagOfMovingKing = true;
            flagOfMovingLeftRook = true;
            return true;
        }
        return false;
    }

    public boolean O_O2() {
        if (attackOur[0][4] == 0 && attackOur[0][5] == 0 && attackOur[0][6] == 0 &&
                Objects.equals(board[0][5], "1") && Objects.equals(board[0][6], "1") && !flagOfMovingRightRook2 &&
                !flagOfMovingKing2) {
            board[0][7] = "1";
            board[0][4] = "1";
            board[0][5] = "r";
            board[0][6] = "k";

            flagOfMovingKing2 = true;
            flagOfMovingLeftRook2 = true;
            return true;
        }
        return false;
    }

    public boolean O_O_O2() {
        if (attackOur[0][4] == 0 && attackOur[0][3] == 0 && attackOur[0][2] == 0 &&
                Objects.equals(board[0][3], "1") && Objects.equals(board[0][2], "1") &&
                Objects.equals(board[0][1], "1") && !flagOfMovingLeftRook2 &&
                !flagOfMovingKing2) {
            board[0][4] = "1";
            board[0][0] = "1";
            board[0][3] = "r";
            board[0][2] = "k";

            flagOfMovingKing2 = true;
            flagOfMovingRightRook2 = true;
            return true;
        }
        return false;
    }

    public void attackForCheck(int y, int x, int to_y, int to_x) {
        if (((board[y][x].toUpperCase().equals(board[y][x])) &&
                Objects.equals(board[to_y][to_x], "k")) ||
                ((!board[y][x].toUpperCase().equals(board[y][x])) &&
                        Objects.equals(board[to_y][to_x], "K"))) {
            arrayAttackMate.add(new AttackMate(y, x));
        }
    }

    public void kingAttack(int y, int x) {
        if (y != 0) {
            tempAttack[y - 1][x]++;
        }
        if (y != 7) {
            tempAttack[y + 1][x]++;
        }
        if (x != 0) {
            tempAttack[y][x - 1]++;
        }
        if (x != 7) {
            tempAttack[y][x + 1]++;
        }
        if (y != 0 && x != 0) {
            tempAttack[y - 1][x - 1]++;
        }
        if (y != 0 && x != 7) {
            tempAttack[y - 1][x + 1]++;
        }
        if (y != 7 && x != 0) {
            tempAttack[y + 1][x - 1]++;
        }
        if (y != 7 && x != 7) {
            tempAttack[y + 1][x + 1]++;
        }
    }

    public void rookAttack(int c_y, int c_x) {
        for (var y = c_y + 1; y <= 7; y++) {
            if (!Objects.equals(board[y][c_x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[y][c_x], "k")) || ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                                && Objects.equals(board[y][c_x], "K"))) {
                    var temp = y - 1;
                    while(temp != c_y) {
                        arrayAttackMate.add(new AttackMate(temp, c_x));
                        --temp;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (y < 7) tempAttack[y + 1][c_x]++;
                }
                tempAttack[y][c_x]++;
                break;
            }
            else {
                tempAttack[y][c_x]++;
            }
        }
        for (var y = c_y - 1; y >= 0; y--) {
            if (!Objects.equals(board[y][c_x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[y][c_x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[y][c_x], "K"))) {
                    var temp = y + 1;
                    while(temp != c_y) {
                        arrayAttackMate.add(new AttackMate(temp, c_x));
                        ++temp;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (y > 0) tempAttack[y - 1][c_x]++;
                }
                tempAttack[y][c_x]++;
                break;
            }
            else {
                tempAttack[y][c_x]++;
            }
        }
        for (var x = c_x + 1; x <= 7; x++) {
            if (!Objects.equals(board[c_y][x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[c_y][x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[c_y][x], "K"))) {
                    var temp = x - 1;
                    while(temp != c_x) {
                        arrayAttackMate.add(new AttackMate(c_y, temp));
                        --temp;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (x < 7) tempAttack[c_y][x + 1]++;
                }
                tempAttack[c_y][x]++;
                break;
            }
            else {
                tempAttack[c_y][x]++;
            }
        }
        for (var x = c_x - 1; x >= 0; x--) {
            if (!Objects.equals(board[c_y][x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[c_y][x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[c_y][x], "K"))) {
                    var temp = x + 1;
                    while(temp != c_x) {
                        arrayAttackMate.add(new AttackMate(c_y, temp));
                        ++temp;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (x > 0) tempAttack[c_y][x - 1]++;
                }
                tempAttack[c_y][x]++;
                break;
            }
            else {
                tempAttack[c_y][x]++;
            }
        }
    }

    public void bishopAttack(int c_y, int c_x) {
        var y = c_y - 1;
        var x = c_x - 1;
        while ((y > -1) && (x > -1)) {
            if (!Objects.equals(board[y][x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[y][x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[y][x], "K"))) {
                    var temp1 = y + 1;
                    var temp2 = x + 1;
                    while(temp2 != c_x) {
                        arrayAttackMate.add(new AttackMate(temp1, temp2));
                        ++temp1;
                        ++temp2;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (y > 0 && x > 0) tempAttack[y - 1][x - 1]++;
                }
                tempAttack[y][x]++;
                break;
            }
            else {
                tempAttack[y][x]++;
                --y; --x;
            }
        }
        y = c_y - 1;
        x = c_x + 1;
        while ((y > -1) && (x < 8)) {
            if (!Objects.equals(board[y][x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[y][x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[y][x], "K"))) {
                    var temp1 = y + 1;
                    var temp2 = x - 1;
                    while(temp2 != c_x) {
                        arrayAttackMate.add(new AttackMate(temp1, temp2));
                        ++temp1;
                        --temp2;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (y > 0 && x < 7) tempAttack[y - 1][x + 1]++;
                }
                tempAttack[y][x]++;
                break;
            }
            else {
                tempAttack[y][x]++;
                --y; ++x;
            }
        }
        y = c_y + 1;
        x = c_x + 1;
        while ((y < 8) && (x < 8)) {
            if (!Objects.equals(board[y][x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[y][x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[y][x], "K"))) {
                    var temp1 = y - 1;
                    var temp2 = x - 1;
                    while(temp2 != c_x) {
                        arrayAttackMate.add(new AttackMate(temp1, temp2));
                        --temp1;
                        --temp2;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (y < 7 && x < 7) tempAttack[y + 1][x + 1]++;
                }
                tempAttack[y][x]++;
                break;
            }
            else {
                tempAttack[y][x]++;
                ++y; ++x;
            }
        }
        y = c_y + 1;
        x = c_x - 1;
        while ((y < 8) && (x > -1)) {
            if (!Objects.equals(board[y][x], "1")) {
                if (((board[c_y][c_x].toUpperCase().equals(board[c_y][c_x])) &&
                        Objects.equals(board[y][x], "k")) ||
                        ((!board[c_y][c_x].toUpperCase().equals(board[c_y][c_x]))
                        && Objects.equals(board[y][x], "K"))) {
                    var temp1 = y - 1;
                    var temp2 = x + 1;
                    while(temp2 != c_x) {
                        arrayAttackMate.add(new AttackMate(temp1, temp2));
                        --temp1;
                        ++temp2;
                    }
                    arrayAttackMate.add(new AttackMate(c_y, c_x));
                    if (y < 7 && x > 0) tempAttack[y + 1][x - 1]++;
                }
                tempAttack[y][x]++;
                break;
            }
            else {
                tempAttack[y][x]++;
                ++y; --x;
            }
        }
    }

    public void knightAttack(int y, int x) {
        if (y > 1) {
            if (x > 0) {
                tempAttack[y - 2][x - 1]++;
                attackForCheck(y, x, y - 2, x - 1);
            }
            if (x < 7) {
                tempAttack[y - 2][x + 1]++;
                attackForCheck(y, x, y - 2, x + 1);
            }
        }
        if (y < 6) {
            if (x > 0) {
                tempAttack[y + 2][x - 1]++;
                attackForCheck(y, x, y + 2, x - 1);
            }
            if (x < 7) {
                tempAttack[y + 2][x + 1]++;
                attackForCheck(y, x, y + 2, x + 1);
            }
        }
        if (x > 1) {
            if (y > 0) {
                tempAttack[y - 1][x - 2]++;
                attackForCheck(y, x, y - 1, x - 2);
            }
            if (y < 7) {
                tempAttack[y + 1][x - 2]++;
                attackForCheck(y, x, y + 1, x - 2);
            }
        }
        if (x < 6) {
            if (y > 0) {
                tempAttack[y - 1][x + 2]++;
                attackForCheck(y, x, y - 1, x + 2);
            }
            if (y < 7) {
                tempAttack[y + 1][x + 2]++;
                attackForCheck(y, x, y + 1, x + 2);
            }
        }
    }

    public void ourPawnAttack(int y, int x) {
        if (y != 0) {
            if (x == 0) {
                tempAttack[y - 1][x + 1]++;
                attackForCheck(y, x, y - 1, x + 1);
            }
            else if (x == 7) {
                tempAttack[y - 1][x - 1]++;
                attackForCheck(y, x, y - 1, x - 1);
            }
            else {
                tempAttack[y - 1][x + 1]++;
                attackForCheck(y, x, y - 1, x + 1);
                tempAttack[y - 1][x - 1]++;
                attackForCheck(y, x, y - 1, x - 1);
            }
        }
    }

    public void opponentPawnAttack(int y, int x) {
        if (y != 7) {
            if (x == 0) {
                tempAttack[y + 1][x + 1]++;
                attackForCheck(y, x, y + 1, x + 1);
            }
            else if (x == 7) {
                tempAttack[y + 1][x - 1]++;
                attackForCheck(y, x, y + 1, x - 1);
            }
            else {
                tempAttack[y + 1][x + 1]++;
                attackForCheck(y, x, y + 1, x + 1);
                tempAttack[y + 1][x - 1]++;
                attackForCheck(y, x, y + 1, x - 1);
            }
        }
    }

    public void queenAttack (int y, int x) {
        rookAttack(y, x);
        bishopAttack(y, x);
    }

    public void setAttack() {
        for (var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
            for (var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
                switch (board[coord_y][coord_x]) {
                    case "k" :
                        kingAttack(coord_y, coord_x);
                        break;
                    case "q" :
                        queenAttack(coord_y, coord_x);
                        break;
                    case "r" :
                        rookAttack(coord_y, coord_x);
                        break;
                    case "b" :
                        bishopAttack(coord_y, coord_x);
                        break;
                    case "n" :
                        knightAttack(coord_y, coord_x);
                        break;
                    case "p" :
                        opponentPawnAttack(coord_y, coord_x);
                        break;
                }
            }
        }

        for (var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
            for (var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
                attackOpponent[coord_y][coord_x] = tempAttack[coord_y][coord_x];
                tempAttack[coord_y][coord_x] = 0;
            }
        }

        for (var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
            for (var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
                    switch (board[coord_y][coord_x]) {
                        case "K" :
                            kingAttack(coord_y, coord_x);
                            break;
                        case "Q" :
                            queenAttack(coord_y, coord_x);
                            break;
                        case "R" :
                            rookAttack(coord_y, coord_x);
                            break;
                        case "B" :
                            bishopAttack(coord_y, coord_x);
                            break;
                        case "N" :
                            knightAttack(coord_y, coord_x);
                            break;
                        case "P" :
                            ourPawnAttack(coord_y, coord_x);
                            break;
                    }
                }
            }

        for (var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
            for (var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
                attackOur[coord_y][coord_x] = tempAttack[coord_y][coord_x];
                tempAttack[coord_y][coord_x] = 0;
            }
        }
    }
}
