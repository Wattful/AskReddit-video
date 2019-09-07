import io.humble.video.*;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.*;
import java.awt.Color;

public class Question extends Readable{
	private static org.w3c.dom.Document template;

	static {
		System.out.print("Loading question template ");
		String text;
		try{
			text = new String(Files.readAllBytes(Paths.get("./templates/questiontemplate.xhtml")), StandardCharsets.UTF_8);
		} catch(IOException e){
			System.out.println("Bruh moment");
			throw new RuntimeException(e);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try{
    		DocumentBuilder builder = factory.newDocumentBuilder();
    		template = builder.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    	} catch(ParserConfigurationException | SAXException | IOException e){
    		System.out.println("Bruh moment");
    		throw new RuntimeException(e);
    	}
 		System.out.println("Done");
	}

	public Question(String s, String t, String i, String c){
		super(s, t, i, c);
	}

	protected org.w3c.dom.Document getTemplate(){
		return template;
	}

	protected int heightOfBoxWithoutText(){
		return 100;
	}

	protected Java2DRenderer getRenderer(){
		return new Java2DRenderer(getTemplate(), 768, 432){
			@Override
			protected BufferedImage createBufferedImage(final int width, final int height) {
    			final BufferedImage image = org.xhtmlrenderer.util.ImageUtil.createCompatibleBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    			org.xhtmlrenderer.util.ImageUtil.clearImage(image, new Color(18, 18, 18));
    			return image;
			}
		};
	}

	protected int screenHeight(){
		return 432;
	}
}