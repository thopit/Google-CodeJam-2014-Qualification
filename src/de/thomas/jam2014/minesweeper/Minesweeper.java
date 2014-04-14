package de.thomas.jam2014.minesweeper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Solves problem C of the Google code jam qualification 2014
 * @author Thomas Opitz
 *
 */
public class Minesweeper implements Runnable {
	int R;
	int C;
	int M;
	int number;
	Thread worker;

	Character[][] mainBoard;
	BlockingQueue<Character[][]> queue;
	boolean done = false;
	boolean running = false;

	public Minesweeper(int R, int C, int M, int number) {
		this.R = R;
		this.C = C;
		this.M = M;
		this.number = number;
		queue = new LinkedBlockingQueue<Character[][]>();
		running = true;
		worker = new Thread(this);
		worker.start();
	}
	
	public void solveProblem() {
		generateAllBoards();
		running = false;
		worker.interrupt();

		try {
			worker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (! done) {
			System.out.println("Case #" + number + ":");
			System.out.println("Impossible");
		}
	}
	
	public void generateAllBoards() {
		Character[][] field = generateBoard();

		if (M > 0) {
			for (int y = 0; y < field[0].length; y++) {
				for (int x = 0; x < field.length; x++) {
					Character[][] newField = deepCopyMatrix(field);
					newField[x][y] = '*';
					generateAllBoards(newField, y, x, 1);

					if (done)
						return;
				}
			}
		}
		else {
			Character[][] b = generateBoard();
			b[0][0] = 'c';
			System.out.println("Case #" + number + ":");
			printBoard(b);
			worker.interrupt();
			done = true;
			return;
		}
	}

	public void generateAllBoards(Character[][] field, int lastR, int lastC, int bombCount) {
		if (done)
			return;

		lastC = ++lastC % C;

		if (lastC == 0)
			lastR = ++lastR % R;

		while (lastR < R && bombCount < M && !(lastR == 0 && lastC == 0)) {
			Character[][] newField = deepCopyMatrix(field);
			newField[lastC][lastR] = '*';

			generateAllBoards(newField, lastR, lastC, bombCount + 1);

			lastC = ++lastC % C;

			if (lastC == 0)
				lastR = ++lastR % R;
		}

		if (bombCount >= M) {
			queue.offer(field);
		}
	}
	
	public  Character[][] generateBoard() {
		Character[][] field = new Character[C][R];

		for (int y = 0; y < R; y++) {
			for (int x = 0; x < C; x++) {
				field[x][y] = '.';
			}
		}

		return field;
	}
	
	private  Character[][] deepCopyMatrix(Character[][] input) {
		if (input == null)
			return null;
		Character[][] result = new Character[input.length][];
		for (int r = 0; r < input.length; r++) {
			result[r] = input[r].clone();
		}
		return result;
	}

	public  void printBoard(Character[][] board) {
		for (int y = 0; y < board[0].length; y++) {
			for (int x = 0; x < board.length; x++) {
				System.out.print(board[x][y]);
			}
			System.out.println();
		}
	}

	public  Character countBombs(Character[][] board, int x, int y) {
		int count = 0;

		if (x > 0 && board[x - 1][y] == '*') 
			count++;
		if (x < board.length - 1 && board[x + 1][y] == '*')
			count++;
		if (y > 0 && board[x][y - 1] == '*') 
			count++;
		if (y < board[0].length - 1 && board[x][y + 1] == '*')
			count++;

		if (x > 0 && y > 0 && board[x - 1][y - 1] == '*') 
			count++;
		if (x > 0 && y < board[0].length - 1 && board[x - 1][y + 1] == '*') 
			count++;
		if (x < board.length - 1 && y > 0 && board[x + 1][y - 1] == '*')
			count++;
		if (x < board.length - 1 && y < board[0].length - 1 && board[x + 1][y + 1] == '*')
			count++;


		return Character.forDigit(count, 10);
	}

	public  void solve(int x, int y) {
		boolean cont = mainBoard[x][y] == '0';

		mainBoard[x][y] = '.';

		if (! cont)
			return;

		if (x > 0 && mainBoard[x - 1][y] != '*' && mainBoard[x - 1][y] != '.')
			solve(x - 1, y);
		if (x < mainBoard.length - 1 && mainBoard[x + 1][y] != '*' && mainBoard[x + 1][y] != '.')
			solve(x + 1, y);
		if (y > 0 && mainBoard[x][y - 1] != '*' && mainBoard[x][y - 1] != '.')
			solve(x, y - 1);
		if (y < mainBoard[0].length - 1 && mainBoard[x][y + 1] != '*' && mainBoard[x][y + 1] != '.')
			solve(x, y + 1);

		if (x > 0 && y > 0 && mainBoard[x - 1][y - 1] != '*' && mainBoard[x - 1][y - 1] != '.')
			solve(x - 1, y - 1);
		if (x > 0 && y < mainBoard[0].length - 1 && mainBoard[x - 1][y + 1] != '*' && mainBoard[x - 1][y + 1] != '.')
			solve(x - 1, y + 1);
		if (x < mainBoard.length - 1 && y > 0 && mainBoard[x + 1][y - 1] != '*' && mainBoard[x + 1][y - 1] != '.')
			solve(x + 1, y - 1);
		if (x < mainBoard.length - 1 && y < mainBoard[0].length - 1 && mainBoard[x + 1][y + 1] != '*' && mainBoard[x + 1][y + 1] != '.')
			solve(x + 1, y + 1);	
	}

	@Override
	public void run() {
		while (running || queue.size() > 0) {
			try {
				Character[][] b = queue.take();

				setNumbers(b);
				mainBoard = b;
				Character[][] saveBoard = deepCopyMatrix(mainBoard);

				for (int y = 0; y < mainBoard[0].length; y++) {
					for (int x = 0; x < mainBoard.length; x++) {

						if (mainBoard[x][y] != '0' &&  (R * C != M + 1 || mainBoard[x][y] == '*')) {
							continue;
						}


						solve(x, y);

						printBoard(mainBoard);
						System.out.println();

						if (finalCheck()) {
							System.out.println("Case #" + number +":");
							mainBoard[x][y] = 'c';
							printBoard(mainBoard);
							done = true;
							return;
						}

						mainBoard = saveBoard;
					}
				}

			} catch (InterruptedException e) {}
		}
	}
	
	public  void setNumbers(Character[][] board) {
		for (int y = 0; y < board[0].length; y++) {
			for (int x = 0; x < board.length; x++) {
				if (board[x][y] != '*')
					board[x][y] = countBombs(board, x, y);
			}
		}
	}
	
	public  boolean finalCheck() {
		for (int y = 0; y < mainBoard[0].length; y++) {
			for (int x = 0; x < mainBoard.length; x++) {
				if (mainBoard[x][y] != '.' && mainBoard[x][y] != '*')
					return false;
			}
		}

		return true;
	}

	public static void main(String[] args) {
		Scanner s;
		try {
			s = new Scanner(new File("test.txt"));
			String string = s.nextLine();
			int amount = Integer.parseInt(string);

			for (int run = 1; run <= amount; run++) {
				String line = s.nextLine();
				String regex = "(\\d+) (\\d+) (\\d+)";

				int R = Integer.parseInt(line.replaceAll(regex, "$1"));
				int C = Integer.parseInt(line.replaceAll(regex, "$2"));
				int M = Integer.parseInt(line.replaceAll(regex, "$3"));

				Minesweeper m = new Minesweeper(R, C, M, run);
				m.solveProblem();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
