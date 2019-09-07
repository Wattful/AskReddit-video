import javax.sound.sampled.*;
import io.humble.video.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import io.humble.video.awt.*;
import java.awt.Color;
import java.awt.RenderingHints;

public abstract class Readable{
	private String message;
	private String user;
	private String score;
	private String comments;
	//private String awards;

	private static boolean roundUp = true;
	private static int testAudio = 0;

	private int numSentences;
	private List<String> sentences;
	private List<Long> sentenceDurations;
	private List<AudioInputStream> sentenceAudio;
	private List<BufferedImage> frames;
	private Voice voice;

	//Note that replace is case insensitive as toLowerCase is used in swear filter.
	private static final String[] REPLACE =      {"fuck",  "shit", "faggot",           "nigga", "nigger"};
	private static final String[] REPLACE_WITH = {"frick", "crap", "bundle of sticks", "ninja", "ninja"};

	public Readable(String m, String u, String s, String c){
		message = m;
		user = u;
		score = s;
		comments = c;
	}

	protected abstract org.w3c.dom.Document getTemplate();

	protected abstract int heightOfBoxWithoutText();

	protected abstract Java2DRenderer getRenderer();

	protected abstract int screenHeight();

	private double calculateMargin(){
		int approxCharsPerLine = 125;
		int numLines = (int)Math.ceil(message.length()/((double)approxCharsPerLine));
		int totalHeightOfBox = heightOfBoxWithoutText() + numLines*20;
		return (screenHeight() - totalHeightOfBox)/2.0;
	}

	private void editTemplate(String m, String user, String score, String comments, int padding) {
		org.w3c.dom.Document template = getTemplate();
		template.getElementById("userreplace").setTextContent(user);
		template.getElementById("responsereplace").setTextContent(m);
		template.getElementById("scorereplace").setTextContent(score);
		template.getElementById("padreplace").setTextContent(message.substring(message.length() - padding));
		if(template.getElementById("marginreplace") != null){
			template.getElementById("marginreplace").setAttribute("style", "margin-top:" + calculateMargin() + "px;");
		}
		if(comments != null){
			template.getElementById("commentreplace").setTextContent(comments);
		}

		/*DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try{
    		DocumentBuilder builder = factory.newDocumentBuilder();
    		org.w3c.dom.Document awardsBar = builder.parse(new ByteArrayInputStream(awards.getBytes(StandardCharsets.UTF_8)));
    		template.getElementById("awardreplace").appendChild(template.adoptNode());
    	} catch(ParserConfigurationException | SAXException | IOException e){
    		System.out.println(awards);
    		System.out.println("Major Bruh moment");
    		throw new AssertionError(e);
    	}*/
	}

	private List<String> splitSentences(){
		LinkedList<String> l = new LinkedList<String>(Arrays.asList(message.split("(?<=\\s[\\da-z]+[\\.\\?\\!:]\\s)")));
		for(int i = 0; i < l.size(); i++){
			if(l.get(i).trim().equals("")){
				l.remove(i);
				i--;
			}
		}
		return l;
	}

	private List<AudioInputStream> recordAudio() throws IOException {
		assert sentences != null : "Sentences was not initialized.";
		System.out.print("Recording " + sentences.size() + " lines");
		List<AudioInputStream> answer = new LinkedList<AudioInputStream>();
		for(String s : sentences){
			try{
				answer.add(record(swearFilter(s)));
			} catch(RuntimeException e){
				for(String t : sentences){
					System.out.println("\"" + t + "\"");
				}
				throw e;
			}
			System.out.print(".");
			//System.out.println(s);
		}
		assert answer.size() == sentences.size() : "Sentence audio and sentence lengths do not match.";
		System.out.println("Done");
		return answer;
	}

	private static String swearFilter(String s){
		s = s.toLowerCase();
		for(int i = 0; i < REPLACE.length; i++){
			s = s.replaceAll(REPLACE[i], REPLACE_WITH[i]);
		}
		return s;
	}

	private AudioInputStream record(String s) throws IOException {
		//System.out.println("\"" + s + "\"");
		Process recording = Runtime.getRuntime().exec("aws polly synthesize-speech --sample-rate 16000 --output-format pcm --text \"" + s.replace("\"", "") + "\" --voice-id " + voice + " audio/temp/temp" + testAudio + ".pcm");
		BufferedReader i = new BufferedReader(new InputStreamReader(recording.getInputStream()));
		String t;
		//System.out.println("bruh");
		while ((t = i.readLine()) != null) {
			//System.out.println(t);
		}
		Process converting = Runtime.getRuntime().exec("C:\\Libraries\\ffmpeg\\ffmpeg-20190718-9869e21-win64-static\\bin\\ffmpeg -f s16le -ar 16000 -ac 1 -i audio/temp/temp" + testAudio + ".pcm -y -ar 48000 audio/temp/temp" + testAudio + ".wav");
		BufferedReader j = new BufferedReader(new InputStreamReader(converting.getErrorStream()));
		while ((t = j.readLine()) != null) {
			//System.out.println(t);
		}
		StringBuilder str = new StringBuilder("");
		str.append("file 'audio/temp/temp" + testAudio + ".wav'\n");
		str.append("file 'premade/pad350.wav'\n");
		BufferedWriter w = new BufferedWriter(new FileWriter("./list.txt"));
    	w.write(str.toString());
    	w.close();
		Process padding = Runtime.getRuntime().exec("C:\\Libraries\\ffmpeg\\ffmpeg-20190718-9869e21-win64-static\\bin\\ffmpeg -f concat -i list.txt -codec copy -y audio/temp/temp" + testAudio + "padded.wav");
		BufferedReader k = new BufferedReader(new InputStreamReader(padding.getErrorStream()));
		while ((t = k.readLine()) != null) {
			//System.out.println(t);
		}
		try{
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File("audio/temp/temp" + testAudio + "padded.wav"));
			testAudio++;
			return ais;
		} catch(FileNotFoundException e){
			try{
				testAudio++;
				return AudioSystem.getAudioInputStream(new File("premade/pad1000.wav"));
			} catch(UnsupportedAudioFileException f){
				throw new AssertionError(f);
			}
		} catch(UnsupportedAudioFileException e){
			throw new AssertionError(e);
		}
	}

	private List<Long> calculateDuration(){
		assert sentenceAudio != null : "SentenceAudio was not initialized.";
		List<Long> answer = new LinkedList<Long>();
		for(AudioInputStream a : sentenceAudio){
			answer.add((long)Math.ceil(1000 * a.getFrameLength() / a.getFormat().getFrameRate()));
		}
		assert sentenceAudio.size() == answer.size() : "Sentence audio and sentence duration lengths do not match.";
		return answer;
	}

	private List<BufferedImage> recordFrames(){
		assert sentenceDurations != null : "SentenceDurations was not initialized.";
		System.out.print("Rendering " + sentences.size() + " pictures");
		List<BufferedImage> answer = new LinkedList<BufferedImage>();
		String currentMessage = "";
		int i = 0;
		for(String s : sentences){
			currentMessage += s;
			int repeat = message.length() - currentMessage.length();
			editTemplate(currentMessage, user, score, comments, repeat > 0 ? repeat : 0);
			Java2DRenderer renderer = getRenderer();
    		ScalingOptions so = new ScalingOptions(1920, 1080, BufferedImage.TYPE_INT_ARGB, DownscaleQuality.HIGH_QUALITY, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			//renderer.setBufferedImageType(5);
			BufferedImage image = ImageUtil.getScaledInstance(so, renderer.getImage());
			//ImageUtil.clearImage(image, new Color(31, 27, 36));
    		BufferedImage convertedImg = new BufferedImage(1920, 1080, 5);
    		convertedImg.getGraphics().drawImage(image, 0, 0, null);
    		System.out.print(".");
			long numberOfFrames = (sentenceDurations.get(i)*(Post.FRAMERATE))/1000;
			for(int j = 0; j < numberOfFrames + (roundUp ? 1 : 0); j++){
				answer.add(convertedImg);
			}
			roundUp = !roundUp;
			i++;
		}
		System.out.println("Done");
		return answer;
	}

	public void saveAudio(String path) throws IOException {
		javax.sound.sampled.AudioFormat format = sentenceAudio.get(0).getFormat();
		//System.out.println(format);
		long totalFrames = 0;
		for(AudioInputStream a : sentenceAudio){
			totalFrames += a.getFrameLength();
		}
		AudioInputStream fullAudio = new AudioInputStream(concatenateAudio(sentenceAudio), format, totalFrames*2);
		AudioSystem.write(fullAudio, AudioFileFormat.Type.WAVE, new File(path));
	}

	private static InputStream concatenateAudio(List<AudioInputStream> str){
		return new SequenceInputStream(Collections.enumeration(str));
	}

	public void saveVideo(String path) throws InterruptedException, IOException {
		voice = Voice.randomVoice();
		sentences = splitSentences();
		numSentences = sentences.size();
		sentenceAudio = recordAudio();
		sentenceDurations = calculateDuration();
		frames = recordFrames();

		System.out.print("Preparing muxer");
		final Muxer muxer = Muxer.make(path, null, "mp4");
		final MuxerFormat format = muxer.getFormat();
		final Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());

		Encoder videoEncoder = Encoder.make(codec);
		videoEncoder.setWidth(1920);
		videoEncoder.setHeight(1080);
		// We are going to use 420P as the format because that's what most video formats these days use
		final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
		videoEncoder.setPixelFormat(pixelformat);
		videoEncoder.setTimeBase(Rational.make(1, Post.FRAMERATE));

		if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)){
		  videoEncoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
		}

		videoEncoder.open(null, null);
		muxer.addNewStream(videoEncoder);
		muxer.open(null, null);

		MediaPictureConverter converter = null;
		final MediaPicture picture = MediaPicture.make(
		    videoEncoder.getWidth(),
		    videoEncoder.getHeight(),
		    pixelformat);
		picture.setTimeBase(Rational.make(1, Post.FRAMERATE));
		System.out.println(" Done");

		System.out.print("Encoding " + frames.size() + " frames");
		MediaPacket packet = MediaPacket.make();
	    for (int i = 0; i < frames.size(); i++) {
	    	if(i % 100 == 99){
	    		System.out.print(".");
	    	}
			final BufferedImage img = frames.get(i);

			if (converter == null)
			converter = MediaPictureConverterFactory.createConverter(img, picture);
			converter.toPicture(picture, img, i);

			do {
			videoEncoder.encode(packet, picture);
			if (packet.isComplete())
			  muxer.write(packet, false);
			} while (packet.isComplete());
	    }
	    System.out.println("Done");

	    System.out.print("Cleaning up");
		do {
		  videoEncoder.encode(packet, null);
		  if (packet.isComplete())
		    muxer.write(packet,  false);
		} while (packet.isComplete());
		muxer.close();
	    System.out.println(" Done");
	}

	private static enum Voice{
		Matthew, Russell, Brian, Joanna, Raveena;

		private static Random r = new Random();
		private static List<Voice> v;

		static {
			refillVoices();
		}

		private static void refillVoices(){
			v = new ArrayList<Voice>(Arrays.asList(Voice.values()));
		}

		private static Voice randomVoice(){
			//System.out.println(v.size());
			if(v.size() == 0){
				refillVoices();
			}
			int result = r.nextInt(v.size());
			return v.remove(result);
		}
	}
}