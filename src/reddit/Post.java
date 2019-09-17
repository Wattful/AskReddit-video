package reddit;

import io.humble.video.*;
import java.util.*;
import java.io.IOException;
import javax.sound.sampled.*;
import java.io.*;
import org.jsoup.nodes.*;
import org.jsoup.Jsoup;
import org.apache.commons.cli.*;

/*
Future features:
1. Include awards
2. Autoscaling
*/

public class Post extends ArrayList<Readable> {
	public static final int FRAMERATE = 25;

	public Post(org.jsoup.nodes.Document doc, int maxComments){
		super(getPost(doc, maxComments));
	}

	private static List<Readable> getPost(org.jsoup.nodes.Document doc, int maxComments){
		LinkedList<Readable> answer = new LinkedList<Readable>();
		Element element = doc.body().getElementsByClass("comment").get(0);
		for(int i = 0; element != null && i < maxComments; element = element.nextElementSibling(), i++){
			while(element.hasClass("deleted")){
				element = element.nextElementSibling();
			}
			if(element.className().equals("clearleft")){
				i--;
				continue;
			} else if(element.className().equals("morechildren")){
				break;
			}
			try{
				answer.add(new Response(element.getElementsByClass("usertext").get(0).text(), element.getElementsByClass("author").get(0).text(), element.getElementsByClass("score unvoted").get(0).text()));
			} catch(IndexOutOfBoundsException e){
				System.out.println(element.className());
				i--;
			}
		}
		Collections.shuffle(answer);
		Element q = doc.body().getElementById("siteTable");
		answer.add(0, new Question(q.getElementsByClass("title").get(0).text().replaceAll("\\(self.AskReddit\\)", "").replaceAll("Serious Replies Only", ""), q.getElementsByClass("tagline").get(0).text(), q.getElementsByClass("score unvoted").get(0).text(), q.getElementsByClass("comments").get(0).text()));
		return answer;
	}

	public void saveVideo(Map<String, String> options) throws IOException, InterruptedException {
		System.out.print("Clearing file system");
		File[] foldersToClear = {new File("../debug/merged"), new File("../debug/audio"), new File("../debug/audio/temp"), new File("../debug/video")};
		for(File f : foldersToClear){
			for(File g : f.listFiles()){
				if(!g.isDirectory()){
					g.delete();
				}
			}
		}
		System.out.println(" Done");

		System.out.print("Loading response template");
		Response.setTemplate(options.get("response-template"));
		System.out.println(" Done");

		System.out.print("Loading question template");
		Question.setTemplate(options.get("question-template"));
		System.out.println(" Done");

		int total = 0;
		for(int i = 0; i < this.size(); i++){
			Readable r = this.get(i);
			System.out.println("============================================");
			String type;
			if(i == 0){
				type = "Question";
			} else {
				type = "Response";
			}
			System.out.println(type + " " + total);
			try{
				r.saveVideo("../debug/video/r" + total + ".mp4", options.get(type.toLowerCase() + "-template"));
				r.saveAudio("../debug/audio/r" + total + ".wav");
			} catch(RuntimeException e){
				e.printStackTrace();
				total--;
			}
			total++;
			this.set(i, null);
		}

		System.out.print("Merging");
		for(int i = 0; i < total; i++){
			Process merging = Runtime.getRuntime().exec("ffmpeg -y -i ../debug/video/r" + i + ".mp4 -i ../debug/audio/r" + i + ".wav -acodec aac -vcodec h264 ../debug/merged/r" + i + ".mp4");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(merging.getErrorStream()));
			String s;
			while ((s = stdInput.readLine()) != null) {
    			//System.out.println(s);
			}
			System.out.print(".");
		}
		System.out.println("Done");

		boolean questionMusic = Boolean.parseBoolean(options.get("question-music"));
		String transition = options.get("transition");
		StringBuilder s = new StringBuilder("");
		//Change i to 1 and uncomment two lines below to have question without music.
		for(int i = 0 + (questionMusic ? 0 : 1); i < total; i++){
			s.append("file '../debug/merged/r" + i + ".mp4'\n");
			if(transition != null){
				s.append("file '" + transition + "'\n");
			}
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter("./list.txt"));
    	writer.write(s.toString());
    	writer.close();

    	System.out.print("Combining reponses");
    	Process concat = Runtime.getRuntime().exec("ffmpeg -y -f concat -safe 0 -i list.txt -c copy ../debug/merged/noq.mp4");
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(concat.getErrorStream()));
		String t;
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		System.out.println(" Done");

		String music = options.get("music");
		if(music != null){
			System.out.print("Overlaying music");
			Process overlaying = Runtime.getRuntime().exec("ffmpeg -y -i ../debug/merged/noq.mp4 -i " + music + " -filter_complex \"[0:a][1:a]amerge=inputs=2[a]\" -map 0:v -map \"[a]\" -c:v copy -c:a aac -ac 1 -shortest ../debug/merged/almostdone.mp4");
	    	stdInput = new BufferedReader(new InputStreamReader(overlaying.getErrorStream()));
			while ((t = stdInput.readLine()) != null) {
				//System.out.println(t);
			}
			System.out.println(" Done");
		}

		s = new StringBuilder("");
		String intro = options.get("intro");
		String outro = options.get("outro");
		if(intro != null){
			s.append("file '" + intro + "'\n");
		}
		if(!questionMusic){
			s.append("file '../debug/merged/r0.mp4'\n");
			if(transition != null){
				s.append("file '" + transition + "'\n");
			}
		}
		if(music == null){
			s.append("file '../debug/merged/noq.mp4\n");
		} else {
			s.append("file '../debug/merged/almostdone.mp4\n");
		}
		//s.append("file '" + transition + "'\n");
		if(outro != null){
			s.append("file '" + outro + "'\n");
		}

		BufferedWriter w = new BufferedWriter(new FileWriter("./list.txt"));
    	w.write(s.toString());
    	w.close();

	    System.out.print("Finishing");
	    Process concatAgain = Runtime.getRuntime().exec("ffmpeg -y -f concat -safe 0 -i list.txt -c copy " + options.get("output"));
    	stdInput = new BufferedReader(new InputStreamReader(concatAgain.getErrorStream()));
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		System.out.println(" Done");
	}

	public static void main(String[] args) throws IOException, InterruptedException, UnsupportedAudioFileException, ParseException {
		Options options = new Options();
		options.addOption(Option.builder("p").longOpt("page").numberOfArgs(1).required().build());
		options.addOption(Option.builder("o").longOpt("output").numberOfArgs(1).required().build());
		options.addOption(Option.builder("m").longOpt("music").numberOfArgs(1).build());
		options.addOption(Option.builder("r").longOpt("resopnses").numberOfArgs(1).build());
		options.addOption(Option.builder("qm").longOpt("question-music").numberOfArgs(1).build());
		options.addOption(Option.builder("rt").longOpt("response-template").numberOfArgs(1).build());
		options.addOption(Option.builder("qt").longOpt("question-template").numberOfArgs(1).build());
		options.addOption(Option.builder("t").longOpt("transition").numberOfArgs(1).build());
		options.addOption(Option.builder("ot").longOpt("outro").numberOfArgs(1).build());
		options.addOption(Option.builder("it").longOpt("intro").numberOfArgs(1).build());
		CommandLineParser parser = new DefaultParser();
		CommandLine input = parser.parse(options, args);

		String responseTemplate = input.getOptionValue("rt", "../essential/templates/responsetemplate.xhtml");
		String questionTemplate = input.getOptionValue("qt", "../essential/templates/questiontemplate.xhtml");
		String music = input.getOptionValue("m");
		String transition = input.getOptionValue("t");
		String intro = input.getOptionValue("it");
		String outro = input.getOptionValue("ot");
		String page = input.getOptionValue("p");

		String[] testExists = {responseTemplate, questionTemplate, music, transition, intro, outro, page};
		for(String s : testExists){
			if(s != null && !(new File(s).exists())){
				throw new FileNotFoundException(s + " does not exist.");
			}
		}

		int responses;
		try{
			responses = Integer.parseInt(input.getOptionValue("r", "100000"));
		} catch(NumberFormatException e){
			throw new IllegalArgumentException("Responses was not a valid number.", e);
		}

		Document doc = Jsoup.parse(new File(page), "UTF-8");
		Post p = new Post(doc, responses);
		Map<String, String> passingOptions = new TreeMap<String, String>();
		passingOptions.put("output", input.getOptionValue("o"));
		passingOptions.put("response-template", responseTemplate);
		passingOptions.put("question-template", questionTemplate);
		passingOptions.put("question-music", input.getOptionValue("qm", "true"));
		passingOptions.put("music", music);
		passingOptions.put("transition", transition);
		passingOptions.put("outro", outro);
		passingOptions.put("intro", intro);
		p.saveVideo(passingOptions);
	}
}