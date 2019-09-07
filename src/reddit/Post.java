import io.humble.video.*;
import java.util.*;
import java.io.IOException;
import javax.sound.sampled.*;
import java.io.*;
import org.jsoup.nodes.*;
import org.jsoup.Jsoup;

/*
Future features:
1. Include awards
2. Autoscaling
*/

public class Post extends ArrayList<Readable> {
	public static final int FRAMERATE = 25;

	private String song;
	private String transition;
	private String outro;

	public Post(org.jsoup.nodes.Document doc, int maxComments, String t, String s, String o){
		super(getPost(doc, maxComments));
		song = s;
		transition = t;
		outro = o;
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

	public void saveVideo(String path) throws IOException, InterruptedException {
		System.out.print("Clearing file system");
		File[] foldersToClear = {new File("./merged"), new File("./audio"), new File("./audio/temp"), new File("./video")};
		for(File f : foldersToClear){
			for(File g : f.listFiles()){
				if(!g.isDirectory()){
					g.delete();
				}
			}
		}
		System.out.println(" Done");

		int total = 0;
		for(int i = 0; i < this.size(); i++){
			Readable r = this.get(i);
			System.out.println("============================================");
			System.out.println("Readable " + total);
			try{
				r.saveVideo("./video/r" + total + ".mp4");
				r.saveAudio("./audio/r" + total + ".wav");
			} catch(RuntimeException e){
				e.printStackTrace();
				total--;
			} catch(OutOfMemoryError e){
				total++;
				System.out.println("Ran out of memory.");
				break;
			}
			total++;
			this.set(i, null);
		}

		System.out.print("Merging");
		for(int i = 0; i < total; i++){
			Process merging = Runtime.getRuntime().exec("C:\\Libraries\\ffmpeg\\ffmpeg-20190718-9869e21-win64-static\\bin\\ffmpeg -y -i ./video/r" + i + ".mp4 -i ./audio/r" + i + ".wav -acodec aac -vcodec h264 ./merged/r" + i + ".mp4");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(merging.getErrorStream()));
			String s;
			while ((s = stdInput.readLine()) != null) {
    			//System.out.println(s);
			}
			System.out.print(".");
		}
		System.out.println("Done");

		StringBuilder s = new StringBuilder("");
		//Change i to 1 and uncomment two lines below to have question without music.
		for(int i = 0; i < total; i++){
			s.append("file 'merged/r" + i + ".mp4'\n");
			s.append("file '" + transition + "'\n");
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter("./list.txt"));
    	writer.write(s.toString());
    	writer.close();

    	System.out.print("Combining reponses");
    	Process concat = Runtime.getRuntime().exec("C:\\Libraries\\ffmpeg\\ffmpeg-20190718-9869e21-win64-static\\bin\\ffmpeg -y -f concat -i list.txt -c copy merged/noq.mp4");
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(concat.getErrorStream()));
		String t;
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		System.out.println(" Done");

		System.out.print("Overlaying music");
		Process music = Runtime.getRuntime().exec("C:\\Libraries\\ffmpeg\\ffmpeg-20190718-9869e21-win64-static\\bin\\ffmpeg -y -i merged/noq.mp4 -i " + song + " -filter_complex \"[0:a][1:a]amerge=inputs=2[a]\" -map 0:v -map \"[a]\" -c:v copy -c:a aac -ac 1 -shortest merged/almostdone.mp4");
    	stdInput = new BufferedReader(new InputStreamReader(music.getErrorStream()));
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		System.out.println(" Done");

		s = new StringBuilder("");
		//s.append("file 'merged/r0.mp4'\n");
		//s.append("file '" + transition + "'\n");
		s.append("file 'merged/almostdone.mp4\n");
		//s.append("file '" + transition + "'\n");
		s.append("file '" + outro + "'\n");

		BufferedWriter w = new BufferedWriter(new FileWriter("./list.txt"));
    	w.write(s.toString());
    	w.close();

	    System.out.print("Adding outro");
	    Process concatAgain = Runtime.getRuntime().exec("C:\\Libraries\\ffmpeg\\ffmpeg-20190718-9869e21-win64-static\\bin\\ffmpeg -y -f concat -i list.txt -c copy " + path);
    	stdInput = new BufferedReader(new InputStreamReader(concatAgain.getErrorStream()));
		while ((t = stdInput.readLine()) != null) {
			//System.out.println(t);
		}
		System.out.println(" Done");
	}

	public static void main(String[] args) throws IOException, InterruptedException, UnsupportedAudioFileException {
		if(!(new File(args[0])).exists()){
			throw new FileNotFoundException(args[0] + " does not exist.");
		}
		if(!(new File(args[3])).exists()){
			throw new FileNotFoundException(args[3] + " does not exist.");
		}
		if(!(new File(args[4])).exists()){
			throw new FileNotFoundException(args[4] + " does not exist.");
		}
		if(!(new File(args[5])).exists()){
			throw new FileNotFoundException(args[5] + " does not exist.");
		}
		Document doc = Jsoup.parse(new File(args[0]), "UTF-8");
		Post p = new Post(doc, Integer.parseInt(args[2]), args[3], args[4], args[5]);
		p.saveVideo(args[1]);

		/*File file = new File(args[0]);
        FileOutputStream fos = null;
        InputStream is = null;
        fos = new FileOutputStream("templates/templatesource.xhtml");
        is = new FileInputStream(file);
        Tidy tidy = new Tidy(); 
        tidy.setInputEncoding("UTF-8");
    	tidy.setOutputEncoding("UTF-8");
        tidy.setXHTML(true); 
        tidy.setForceOutput(true);
        tidy.parse(is, fos);*/
        //AudioSystem.getAudioInputStream(new File("audio/awstest.wav"));
	}
}