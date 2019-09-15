package reddit;

import io.humble.video.*;
import io.humble.video.awt.*;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.*;
import java.awt.Color;

public class Response extends Readable{
	private static org.w3c.dom.Document template;

	public Response(String s, String t, String i){
		super(s, t, i, null);
	}

	static void setTemplate(String path){
		String text;
		try{
			text = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
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
	}

	org.w3c.dom.Document getTemplate(){
		return template;
	}

	int heightOfBoxWithoutText(){
		return 50;
	}

	Java2DRenderer getRenderer(String path){
		return new Java2DRenderer(getTemplate(), "file:///" + System.getProperty("user.dir").replace("\\", "/") + "/" + path.substring(0, path.lastIndexOf("/") + 1), 1024, 576){
			@Override
			protected BufferedImage createBufferedImage(final int width, final int height) {
    			final BufferedImage image = org.xhtmlrenderer.util.ImageUtil.createCompatibleBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    			org.xhtmlrenderer.util.ImageUtil.clearImage(image, new Color(18, 18, 18));
    			return image;
			}
		};
	}

	int screenHeight(){
		return 576;
	}
}