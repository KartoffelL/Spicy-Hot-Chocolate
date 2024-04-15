package Kartoffel.Licht;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import Kartoffel.Licht.Java.BufferedImage;
import Kartoffel.Licht.Java.ImageIO;

public class FileLoader {

	public static String loadText(String name) {
		InputStream is = FileLoader.class.getClassLoader().getResourceAsStream(name);
		try {
			String res = new String(is.readAllBytes(), Charset.forName("UTF-8"));
			is.close();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to load " + name + "!");
			System.exit(0);
			return null;
		}
	}
	
	public static BufferedImage loadImage(String name) {
		InputStream is = FileLoader.class.getClassLoader().getResourceAsStream(name);
		try {
			BufferedImage res = ImageIO.read(is);
			is.close();
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to load " + name + "!");
			System.exit(0);
			return null;
		}
	}

}
