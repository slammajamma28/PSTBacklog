package slamma.pst.events.Backlog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ManualCalculations {

	public static void main(String[] args) {
		
		//d MMM yyyy h:mm:ss a
		// example: 01 Apr 2017 12:12:12 am
		String ft = "5 Dec 2010 3:11:20 PM";
		String ct = "28 Apr 2017 7:29:38 PM";
		
		System.out.println("First trophy: " + ft);
		System.out.println("Completion timestamp: " + ct);

		// 16th Feb 2017 3:21:39 PM
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy h:mm:ss a");

		LocalDateTime platinumDate = LocalDateTime.parse(ct, formatter);
		
		LocalDateTime firstTrophy = LocalDateTime.parse(ft, formatter);

		LocalDateTime tempDateTime = LocalDateTime.from(firstTrophy);
		
		long years = tempDateTime.until(platinumDate, ChronoUnit.YEARS);
		tempDateTime = tempDateTime.plusYears( years );

		long months = tempDateTime.until(platinumDate, ChronoUnit.MONTHS);
		tempDateTime = tempDateTime.plusMonths( months );

		long days = tempDateTime.until(platinumDate, ChronoUnit.DAYS);
		tempDateTime = tempDateTime.plusDays( days );


		long hours = tempDateTime.until(platinumDate, ChronoUnit.HOURS);
		tempDateTime = tempDateTime.plusHours( hours );

		long minutes = tempDateTime.until(platinumDate, ChronoUnit.MINUTES);
		tempDateTime = tempDateTime.plusMinutes( minutes );

		long seconds = tempDateTime.until(platinumDate, ChronoUnit.SECONDS);

		System.out.println( years + " years " + 
		        months + " months " + 
		        days + " days " +
		        hours + " hours " +
		        minutes + " minutes " +
		        seconds + " seconds.");
	}

}
