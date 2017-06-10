package slamma.pst.events.Backlog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.jaunt.*;

/*
 *	This doesn't seem to be working as I had hoped. Not the code, but PSNP.
 *	First, seems that if a game has incomplete DLC, the "platinum" time doesn't show. Sound Shapes for example
 *	Same goes for PSN 100%, if there is incomplete DLC, the 100% time doesn't show. Never Alone for example
 *	May need to account for this using a check on the table for /lib/img/icons/platinum-icon.png 
 *	or /lib/img/icons/platinum-icon-off.png to see if platinum was achieved and then go off the
 *	GAP time?
 *
 * 	If the game is submitted, but doesn't have a 100% or platinum timestamp...
 * 	Determine if any DLC trophies have been earned
 * 	If no, use "gap time"
 * 	If yes... ah jeeze!
 */

public class DoJauntWork {
	
	public boolean isBasePSNGameComplete(UserAgent agent) {
		
		return true;
	}
	
	public boolean isPlatinumEarned(UserAgent agent) {
		
		return true;
	}
	
	public static void areAnyDLCTrophiesComplete(UserAgent agent) throws NotFound {
//		List<Element> dlcs = new ArrayList<Element>();
		Elements dlcs = null;
		int complete = 0;
		int incomplete = 0;
//		dlcs.add(agent.doc.findFirst("<div id='DLC-\\d'>").nextSiblingElement());
//		Elements dlcs = agent.doc.findEvery("<div id='DLC-\\d'>");
		dlcs = agent.doc.findEvery("<div class='box no-top-border'>");
		for (Element dlc : dlcs) {
			Element a1 = dlc.findFirst("<tr>");
			System.out.println(a1.hasKeyword("completed") + " : ");
			System.out.println(dlc.findFirst("<span class='title'>").getText());
		}
		System.exit(0);
	}
	
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
				// Connect
				UserAgent userAgent = new UserAgent();
				userAgent.visit(url);
//				areAnyDLCTrophiesComplete(userAgent);
				// First element to get user name and game
				Element game = userAgent.doc.findFirst("<div class='grow'>");
				String userName = game.findFirst("<a>").getText();
				System.out.print(userName + ": ");
				String gameName = game.findFirst("<h3>").getText();
				System.out.println(gameName);

				// Get elements that contain completion times
				Elements elements = userAgent.doc.findEvery("< class='box zebra'>");
				Elements types = elements.findEvery("<span class='small-title'>");
				Elements times = elements.findEvery("<span class='small-info'>");
				String platinumTimestamp;
				int i = 0;
				
				// Check to see which completion it is
				for (Element type : types) {
					String check = type.getText();
					switch(check){
						case "100%":
	//						System.out.println("This is PSN 100%");
							completions.add(check);
							index.add(i);
							i++;
							break;
						case "Platinum":
	//						System.out.println("This is a platinum");
							completions.add(check);
							index.add(i);
							i++;
							
							break;
						default:
	//						System.out.println("This is not a completion: " + type.getText() );
							i++;
							break;
					}
				}
				
				// Print the 100% or platinum
				for (int j = 0; j<index.size(); j++) {
					String completion = types.getElement(index.get(j)).getText();
					String timeDone = times.getElement(index.get(j)).getText();
					// Parse these times out into units
					String delims = "[, ]+";
					String[] tokens = timeDone.split(delims);
					List<Integer> timeOfCompletion = new ArrayList<Integer>();
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
					
					System.out.println(completion + ": " + timeDone);
					
					writer.write(userName + "," + gameName + "," + completion);
					for (int k=0; k<timeOfCompletion.size(); k++) {
						writer.write("," + timeOfCompletion.get(k));
					}
					
					writer.write("\n");
					//Send to DB
				}
				if (index.size() == 0) {
					writer.write("ERROR,CHECK MANUALLY");
					System.out.println("Please manually check this submission.");
				}
				System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			} catch(JauntException e) {
				System.err.println(e);
			} catch (IOException e) {
				e.printStackTrace();
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