package com.owino.financebill;
public class FinancebillApplication {
	public static void main(String[] args) {
		String sourceFile = "first_voting_data.txt";
		String csvOutputFile = "voting_data.csv";
		DataCleaning.convertToCSV(sourceFile,csvOutputFile);
		DataCleaning.generateBasicResults(sourceFile);
	}
}
