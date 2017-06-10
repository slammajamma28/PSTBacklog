package slamma.pst.events.Backlog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DoJsoupWork {

	public static String YEAR = "2017";
	public static String MONTH = "Apr";
	
	/**
	 * This class will check the time stamp of the platinum to determine
	 * if it was earned during the event
	 * 
	 * @param info - This is the main HTML of the user's PSNP game page
	 * @return boolean - main class uses this info to determine if the platinum
	 * 						should count for this event
	 */
	public static boolean wasPlatinumEarnedDuringEvent(Element info) {
		Element platinumElement = info.select("tr[class='completed']").first();
//		Element chk = platinumElement.select("title=Platinum").first();
		String platinumDatestamp = platinumElement.select("span.typo-top-date").text();
		
		System.out.print("Platinum earned on: " + platinumDatestamp); 
		String delims = "[ ]";
		String[] tokens = platinumDatestamp.split(delims);
		if (tokens[2].equals(YEAR) && tokens[1].equals(MONTH)) {
			System.out.println(", during event.");
			return true;
		} else {
			System.out.println(", NOT during event.");
			return false;
		}
	}
	
	public static boolean wasDLCTrophyEarnedDuringEvent(Element dlc) {
		Elements trophies = dlc.select("span.typo-top-date");
		for (Element trophy : trophies) {
			String trophyTimestamp = trophy.text();
			String delims = "[ ]";
			String[] tokens = trophyTimestamp.split(delims);
			if (tokens[2].equals(YEAR) && tokens[1].equals(MONTH)) {
				System.out.println(", during event.");
				return true;
			} else {
				System.out.println(", NOT during event.");
			}
		}
		return false;
	}
	
	public static boolean was100PercentEarnedDuringEvent(Element info) {
		Elements tmpEl = info.select("div.box.no-top-border");
		
		for (Element tmp1 : tmpEl) {
//			Element tmp = tmp1.select("tbody").get(1);
			Elements tmpers = tmp1.select("tbody");
			for (Element tmp : tmpers) {
			if (tmp.select("span.title").first() != null) {
				System.out.println("Skipping info element");
			} else {
				Elements trophies = tmp.select("tr.completed");
				for (Element trophy : trophies) {
					String trophyTimestamp = trophy.select("span.typo-top-date").text();
					String delims = "[ ]";
					String[] tokens = trophyTimestamp.split(delims);
					if (tokens[2].equals(YEAR) && tokens[1].equals(MONTH)) {
						System.out.println(", during event.");
						return true;
					} 
				}
//				System.out.println(", NOT during event.");
//				return false;
			}}
		}
		System.out.println(", NOT during event.");
		return false;
	}

	/**
	 * This method will look at the PSNP info to determine if any DLC has been
	 * played which could affect the "Gap" time
	 * 
	 * @param info - This is the main HTML of the user's PSNP game page
	 * @return boolean - isThisGapTimeBaseGame method will use this info to determine
	 * 						if the Gap time reflects earned DLC too.
	 */
	public static boolean hasAnyDLCBeenPlayed(Element info) {
		Elements dlcs = info.select("div[id*=DLC]");
		for (Element dlc : dlcs) {
			Element checkThis = dlc.select("div.trophy-count").first();
			if (checkThis != null) {
				Element checkThis2 = checkThis.select("div.progress-bar").first();
				if (checkThis2 != null) {
					// TODO: Check if any of the DLC time stamps were during the event
					if(wasDLCTrophyEarnedDuringEvent(dlc)) {
						System.out.println("There has been DLC played during the event");
						return true;
					}
				}
			}
		}
		System.out.println("No DLC trophies have been earned during the event");
		return false;
	}
	
	/**
	 * This method will be used for "Gap" times to determine if it applies to
	 * a Platinum earned or a 100% completed
	 * 
	 * @param info - This is the main HTML of the user's PSNP game page
	 * @return string - either "Platinum", "PSN", or "ERROR" (if somehow neither)
	 */
	public static String platinumOrPSN(Element info) {
		Element psn = info.select("img[src=/lib/img/icons/complete-icon.png]").first();
		Element platinum = info.select("img[src=/lib/img/icons/platinum-icon.png]").first();
		if (psn != null && platinum == null) return "100%"; 
		if (psn == null && platinum != null) return "Platinum";
		return "ERROR";
	}
	
	/**
	 * This method looks at the PSNP page to determine if the base game is complete
	 * and then calls upon the hasAnyDLCBeenPlayedForPSN method to determine if any
	 * extra DLC trophies have been completed that may throw off the gap time
	 * 
	 * @param info - This is the main HTML of the user's PSNP game page
	 * @return boolean - main class will use to determine if the "Gap" time
	 * 						will be labeled "Platinum" or "100%"
	 */
	public static boolean isThisGapTimeBaseGame(Element info) {
		Element baseGameCompletion = info.select("div.box.no-top-border").first().select("tbody").first();
		Element check = baseGameCompletion.select("div.progress-bar").first();
		if ( check != null) {
			if (check.select("span").text().equals("100%")) {
				System.out.println("Base is completed.");
				if (hasAnyDLCBeenPlayed(info)) return false;
				else return true;
			}
		}
		System.out.println("Base PSN is not completed.");
		return false;
	}

	/**
	 * 
	 * @param args - Don't anticipate needing to use any input parameters
	 */
	public static void main(String[] args) {
		
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		
		BufferedWriter writer = null;
		
		// Create list of urls to process
		List<String> urls = new ArrayList<String>();
		String line = null;
		
		try {
			FileReader fr = new FileReader("processthese.txt");
			BufferedReader br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				urls.add(line);
			}
			br.close();
			writer = new BufferedWriter(new FileWriter("./output.csv"));
		} catch (IOException e1) {
			System.out.println("File issue, c'mon man!");
			e1.printStackTrace();
		}
		// Iterate through each URL
		for (String url : urls) {
			System.out.println("URL in use: " + url);
			List<String> completions = new ArrayList<String>();
			List<Integer> index = new ArrayList<Integer>();
			//System.out.println(whatIsThis(checkThis));
			try {    
				Document doc = Jsoup.connect(url).get();
				Element mainInfo = doc.select("div.col-xs").first();
				Element timeInfo = doc.select("table.box.zebra").first();
				Element metaInfo = doc.select("div.grow").first();
				Element tmp1 = metaInfo.select("h3").first();
				String game = tmp1.select("span").first().nextSibling().toString();
				String name = metaInfo.select("a").first().text();
				
				System.out.println(name + ": " + game);
				
				Elements completionTimes = timeInfo.select("span.small-info");
				Elements completionTypes = timeInfo.select("span.small-title");
				Elements completionStamps = timeInfo.select("td[^style]");
				Elements completionDates = completionStamps.select("span.typo-top-date");
				Elements completionTimestamps = completionStamps.select("span.typo-bottom-date");
				int i = 0;
				for (Element e : completionTypes) {
					String check = e.text();
					switch(check) {
						case "100%":
							System.out.println("Checking 100%");
							if (was100PercentEarnedDuringEvent(mainInfo)) {
								completions.add(check);
								index.add(i);
							}
							i++;
							break;
						case "Platinum":
							System.out.println("Checking Platinum");
							if (wasPlatinumEarnedDuringEvent(mainInfo)) {
								completions.add(check);
								index.add(i);
							}
							i++;
							break;
						case "Gap":
							System.out.println("Checking Gap");
							if (isThisGapTimeBaseGame(mainInfo)) {
								String tempType = platinumOrPSN(mainInfo);
								if (tempType == "Platinum") {
									if (wasPlatinumEarnedDuringEvent(mainInfo)) {
										completions.add(tempType);
										Element platinumElement = mainInfo.select("tr[class='completed']").first();
										Element platinumDatestamp = platinumElement.select("span.typo-top-date").first();
										Element platinumTimestamp = platinumElement.select("span.typo-bottom-date").first();
										completionDates.add(i, platinumDatestamp);
										completionTimestamps.add(i, platinumTimestamp);
										index.add(i);
									}
								} else if (tempType == "100%") {
									if (was100PercentEarnedDuringEvent(mainInfo)) {
										completions.add(tempType);
										index.add(i);
									}
								} else {
									System.out.println("Gap is nothing.");
								}
							}
							i++;
						default:
							i++;
							break;
					}
				}
				
				// Does this game have both DLC and platinum?
				boolean platinum = false;
				boolean onehundred = false;
				for (int j = 0; j < index.size(); j++) {
					if (completionTypes.get(index.get(j)).text().equals("Platinum")) {
						System.out.println("This is a platinum");
						platinum = true;
					} else if (completionTypes.get(index.get(j)).text().equals("100%")) {
						System.out.println("This is 100% completed");
						onehundred = true;
					}
					else {
						System.out.println("This is a gap thing, still working on this logic.");
					}
				}
				
				boolean ignorePlatinum = false;
				if (platinum && onehundred) {
					// We need to ignore the lower number, which will be the platinum
					// Because a game cannot be at 100% without the platinum ;)
					System.out.println("Game is completely completed. Platinum will be marked as all zeroes in output file.");
					ignorePlatinum = true;
				}
				
				// Print the 100% or platinum
				for (int j = 0; j<index.size(); j++) {
//					String completion = completionTypes.get(index.get(j)).text();
					String completion = completions.get(j);
					String timeDone = completionTimes.get(index.get(j)).text();
					String delims = "[, ]+";
					String[] tokens = timeDone.split(delims);
					List<Integer> timeOfCompletion = new ArrayList<Integer>();
					
					if (ignorePlatinum && completion.equals("Platinum")) {
						timeOfCompletion.add(0);
						timeOfCompletion.add(0);
						timeOfCompletion.add(0);
						timeOfCompletion.add(0);
						timeOfCompletion.add(0);
						timeOfCompletion.add(0);
						timeOfCompletion.add(0);
					} else {
						// Parse these times out into units
						i=0;
						try {
							if (tokens[i+1].equals("year") || tokens[i+1].equals("years")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
							if (tokens[i+1].equals("month") ||  tokens[i+1].equals("months")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
							if (tokens[i+1].equals("week") ||  tokens[i+1].equals("weeks")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
							if (tokens[i+1].equals("day") ||  tokens[i+1].equals("days")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
							if (tokens[i+1].equals("hour") ||  tokens[i+1].equals("hours")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
							if (tokens[i+1].equals("minute") ||  tokens[i+1].equals("minutes")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
							if (tokens[i+1].equals("second") ||  tokens[i+1].equals("seconds")) {
								timeOfCompletion.add(Integer.parseInt(tokens[i]));
								i=i+2;
							} else {
								timeOfCompletion.add(0);
							}
						} catch (Exception e) {
							switch(timeOfCompletion.size()){
								case 0:
	//								System.out.println("There are no years");
									break;
								case 1:
	//								System.out.println("There are no months");
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									break;
								case 2:
	//								System.out.println("There are no weeks");
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									break;
								case 3:
	//								System.out.println("There are no days");
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									break;
								case 4:
	//								System.out.println("There are no hours");
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									break;
								case 5:
	//								System.out.println("There are no minutes");
									timeOfCompletion.add(0);
									timeOfCompletion.add(0);
									break;
								case 6:
	//								System.out.println("There are no seconds");
									timeOfCompletion.add(0);
									break;
							}
						}
					}
					
					System.out.println(completion + ": " + timeDone);
					
					String delims2 = "[ ]";
					String ft, ct;
					String justDaDate = "";
					String[] tokens2 = completionDates.get(0).text().split(delims2);
					for (int z = 0; z < tokens2.length; z++) {
						if (z == 0) {
							if (tokens2[z].length() == 4) {
								justDaDate = tokens2[z].substring(0, 2);
							} else if (tokens2[z].length() == 3) {
								justDaDate = tokens2[z].substring(0, 1);
							} else {
								justDaDate = tokens2[z];
							}
						}
					}
					ft = justDaDate + " " + tokens2[1] + " " + tokens2[2] + " " + completionTimestamps.get(0).text();
					
					String[] tokens3 = completionDates.get(index.get(j)).text().split(delims2);
					for (int z = 0; z < tokens3.length; z++) {
						if (z == 0) {
							
							if (tokens3[z].length() == 4) {
								justDaDate = tokens3[z].substring(0, 2);
							} else if (tokens3[z].length() == 3) {
								justDaDate = tokens3[z].substring(0, 1);
							} else {
								justDaDate = tokens3[z];
							}
						}
					}
					ct = justDaDate + " " + tokens3[1] + " " + tokens3[2] + " " + completionTimestamps.get(index.get(j)).text();
					
					System.out.println("First trophy: " + ft);
					System.out.println("Completion timestamp: " + ct);

					// 16th Feb 2017 3:21:39 PM
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy h:mm:ss a");

					LocalDateTime platinumDate = LocalDateTime.parse(ct, formatter);
//					LocalTime todayTime = LocalTime.parse(platinumTrophyT, formatter2);
					
					LocalDateTime firstTrophy = LocalDateTime.parse(ft, formatter);
//					LocalTime firstTrophyTime = LocalTime.parse(firstTrophyT, formatter2);

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
					
					writer.write(name + "," + game + "," + url + "," + completion + "," + ft + "," + ct);
//					for (int k=0; k<timeOfCompletion.size(); k++) {
//						writer.write("," + timeOfCompletion.get(k));
						writer.write(","+ years + "," + months + "," + days + "," + hours + "," + minutes + "," + seconds);
//					}
					
					writer.write("\n");
					//Send to DB
				}
				if (index.size() == 0) {
					writer.write(name + "," + game + "," + url + "," + "ERROR,CHECK MANUALLY\n");
					System.out.println("Please manually check this submission.");
				}
				System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
//				writer.write("\n");
			} catch(Exception e) {
				System.err.println(e);
			}
		} if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
		stopwatch.stop();
		System.out.println(stopwatch.getTime(TimeUnit.MILLISECONDS) + " ms elapsed");
	}
}
