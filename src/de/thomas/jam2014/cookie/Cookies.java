package de.thomas.jam2014.cookie;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Solves problem B of the Google code jam qualification 2014
 * @author Thomas Opitz
 *
 */
public class Cookies {

	public static String test(int iteration, double C, double F, double X) {
		double result = Double.MAX_VALUE;
		double best = Double.MAX_VALUE;

		for (int k = 0; k < Integer.MAX_VALUE; k++) {
			result = simulate(k, C, F, X);
			
			if (result < best) {
				best = result;
			}
			else {
				best = (double) Math.round(best * 10000000) / 10000000;
				return "Case #" + iteration + ": " + best;
			}
		}
		
		return "";
	}
	
	public static double simulate(int buildAmount, double C, double F, double X) {
		double ratio = 2.0;
		double time = 0;

		for (int k = 0; k < buildAmount; k++) {
			double buildTime = C / ratio;
			time += buildTime;
			ratio += F;
		}
		time += X / ratio;
		
		return time;
	}

	public static void main(String[] args) {
		Scanner s;
		try {
			s = new Scanner(new File("B-large.in"));
			String string = s.nextLine();
			int amount = Integer.parseInt(string);
			
			for (int run = 1; run <= amount; run++) {
				String regex = "(\\d+.\\d+) (\\d+.\\d+) (\\d+.\\d+)";
				
				String line = s.nextLine();
				
				double C = Double.parseDouble(line.replaceAll(regex, "$1"));
				double F = Double.parseDouble(line.replaceAll(regex, "$2"));
				double X = Double.parseDouble(line.replaceAll(regex, "$3"));
				
				System.out.println(test(run, C, F, X));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
