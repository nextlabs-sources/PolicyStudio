package com.bluejungle.destiny.policymanager.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for managing OS resources associated with SWT controls such as
 * colors, fonts, images, etc.
 * 
 * !!! IMPORTANT !!! Application code must explicitly invoke the
 * <code>dispose()</code> method to release the operating system resources
 * managed by cached objects when those objects and OS resources are no longer
 * needed (e.g. on application shutdown)
 */
public class SWTResourceManager {

	/**
	 * Dispose of cached objects and their underlying OS resources. This should
	 * only be called when the cached objects are no longer needed (e.g. on
	 * application shutdown)
	 */
	public static void dispose() {
		disposeColors();
		disposeFonts();
		disposeImages();
		disposeCursors();
	}

	// ////////////////////////////
	// Color support
	// ////////////////////////////

	/**
	 * Maps RGB values to colors
	 */
	private static HashMap<RGB, Color> m_ColorMap = new HashMap<RGB, Color>();

	/**
	 * Returns the system color matching the specific ID
	 * 
	 * @param systemColorID
	 *            int The ID value for the color
	 * @return Color The system color matching the specific ID
	 */
	public static Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}

	/**
	 * Returns a color given its red, green and blue component values
	 * 
	 * @param r
	 *            int The red component of the color
	 * @param g
	 *            int The green component of the color
	 * @param b
	 *            int The blue component of the color
	 * @return Color The color matching the given red, green and blue componet
	 *         values
	 */
	public static Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}

	/**
	 * Returns a color given its red, green and blue value string, like 12,34,56
	 * 
	 * @param RGB
	 *            the string format of the color
	 * @return Color The color matching the given red, green and blue value
	 *         string
	 */
	public static Color getColor(String key, String RGB) {
		if (RGB.length() == 0) {
			if (key.equals("EDITOR_PART_BACKGROUD"))
				RGB = "233,233,233";
			else if (key.equals("TABFOLDER_FOREGROUND"))
				RGB = "255,255,255";
			else if (key.equals("TABFOLDER_SELECTION_BACKGROUND"))
				RGB = "204,204,204";
			else if (key.equals("TABFOLDER_BACKGROUND"))
				RGB = "123,134,154";
			else if (key.equals("EDITOR_BACKGROUD"))
				RGB = "255,255,255";
			else if (key.equals("TABFOLDER_SELECTION_FOREGROUND"))
				RGB = "0,0,0";
			else
				RGB = "255,255,255";
		}
		String[] color = RGB.split(",");

		int r = Integer.valueOf(color[0]);
		int g = Integer.valueOf(color[1]);
		int b = Integer.valueOf(color[2]);

		return getColor(r, g, b);
	}

	/**
	 * Returns a color given its RGB value
	 * 
	 * @param rgb
	 *            RGB The RGB value of the color
	 * @return Color The color matching the RGB value
	 */
	public static Color getColor(RGB rgb) {
		Color color = m_ColorMap.get(rgb);
		if (color == null) {
			Display display = Display.getCurrent();
			color = new Color(display, rgb);
			m_ColorMap.put(rgb, color);
		}
		return color;
	}

	/**
	 * Dispose of all the cached colors
	 */
	public static void disposeColors() {
		for (Color color : m_ColorMap.values())
			color.dispose();
		m_ColorMap.clear();
	}

	// ////////////////////////////
	// Image support
	// ////////////////////////////

	/**
	 * Maps image names to images
	 */
	private static HashMap<String, Image> m_ClassImageMap = new HashMap<String, Image>();

	/**
	 * Maps images to image decorators
	 */
	private static HashMap<Image, HashMap<Image, Image>> m_ImageToDecoratorMap = new HashMap<Image, HashMap<Image, Image>>();

	/**
	 * Returns an image encoded by the specified input stream
	 * 
	 * @param is
	 *            InputStream The input stream encoding the image data
	 * @return Image The image encoded by the specified input stream
	 */
	protected static Image getImage(InputStream is) {
		Display display = Display.getCurrent();
		ImageData data = new ImageData(is);
		if (data.transparentPixel > 0)
			return new Image(display, data, data.getTransparencyMask());
		return new Image(display, data);
	}

	/**
	 * Returns an image stored in the file at the specified path
	 * 
	 * @param path
	 *            String The path to the image file
	 * @return Image The image stored in the file at the specified path
	 */
	public static Image getImage(String path) {
		return getImage("default", path); //$NON-NLS-1$
	}

	/**
	 * Returns an image stored in the file at the specified path
	 * 
	 * @param section
	 *            The section to which belongs specified image
	 * @param path
	 *            String The path to the image file
	 * @return Image The image stored in the file at the specified path
	 */
	public static Image getImage(String section, String path) {
		String key = section + '|' + SWTResourceManager.class.getName() + '|'
				+ path;
		Image image = m_ClassImageMap.get(key);
		if (image == null) {
			try {
				FileInputStream fis = new FileInputStream(path);
				image = getImage(fis);
				m_ClassImageMap.put(key, image);
				fis.close();
			} catch (Exception e) {
				image = getMissingImage();
				m_ClassImageMap.put(key, image);
			}
		}
		return image;
	}

	public static Image getImage(String key, ImageData data) {
		Image image = m_ClassImageMap.get(key);
		if (image == null) {
			image = new Image(Display.getCurrent(), data);
			m_ClassImageMap.put(key, image);
		}
		return image;
	}

	public static Image getDisabledImage(String key, Image orgImage) {
		Image image = m_ClassImageMap.get(key);
		if (image == null) {
			image = new Image(Display.getCurrent(), orgImage, SWT.IMAGE_DISABLE);
			m_ClassImageMap.put(key, image);
		}
		return image;
	}

	/**
	 * Returns an image stored in the file at the specified path relative to the
	 * specified class
	 * 
	 * @param clazz
	 *            Class The class relative to which to find the image
	 * @param path
	 *            String The path to the image file
	 * @return Image The image stored in the file at the specified path
	 */
	public static Image getImage(Class<?> clazz, String path) {
		String key = clazz.getName() + '|' + path;
		Image image = m_ClassImageMap.get(key);
		if (image == null) {
			try {
				if (path.length() > 0 && path.charAt(0) == '/') {
					String newPath = path.substring(1, path.length());
					image = getImage(new BufferedInputStream(clazz
							.getClassLoader().getResourceAsStream(newPath)));
				} else {
					image = getImage(clazz.getResourceAsStream(path));
				}
				m_ClassImageMap.put(key, image);
			} catch (Exception e) {
				image = getMissingImage();
				m_ClassImageMap.put(key, image);
			}
		}
		return image;
	}

	private static final int MISSING_IMAGE_SIZE = 10;

	private static Image getMissingImage() {
		Image image = new Image(Display.getCurrent(), MISSING_IMAGE_SIZE,
				MISSING_IMAGE_SIZE);

		GC gc = new GC(image);
		gc.setBackground(getColor(SWT.COLOR_RED));
		gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		gc.dispose();

		return image;
	}

	/**
	 * Style constant for placing decorator image in top left corner of base
	 * image.
	 */
	public static final int TOP_LEFT = 1;
	/**
	 * Style constant for placing decorator image in top right corner of base
	 * image.
	 */
	public static final int TOP_RIGHT = 2;
	/**
	 * Style constant for placing decorator image in bottom left corner of base
	 * image.
	 */
	public static final int BOTTOM_LEFT = 3;
	/**
	 * Style constant for placing decorator image in bottom right corner of base
	 * image.
	 */
	public static final int BOTTOM_RIGHT = 4;

	/**
	 * Returns an image composed of a base image decorated by another image
	 * 
	 * @param baseImage
	 *            Image The base image that should be decorated
	 * @param decorator
	 *            Image The image to decorate the base image
	 * @return Image The resulting decorated image
	 */
	public static Image decorateImage(Image baseImage, Image decorator) {
		return decorateImage(baseImage, decorator, BOTTOM_RIGHT);
	}

	/**
	 * Returns an image composed of a base image decorated by another image
	 * 
	 * @param baseImage
	 *            Image The base image that should be decorated
	 * @param decorator
	 *            Image The image to decorate the base image
	 * @param corner
	 *            The corner to place decorator image
	 * @return Image The resulting decorated image
	 */
	public static Image decorateImage(Image baseImage, Image decorator,
			int corner) {
		HashMap<Image, Image> decoratedMap = m_ImageToDecoratorMap
				.get(baseImage);
		if (decoratedMap == null) {
			decoratedMap = new HashMap<Image, Image>();
			m_ImageToDecoratorMap.put(baseImage, decoratedMap);
		}
		Image result = decoratedMap.get(decorator);
		if (result == null) {
			Rectangle bid = baseImage.getBounds();
			Rectangle did = decorator.getBounds();
			result = new Image(Display.getCurrent(), bid.width, bid.height);
			GC gc = new GC(result);
			gc.drawImage(baseImage, 0, 0);

			if (corner == TOP_LEFT) {
				gc.drawImage(decorator, 0, 0);
			} else if (corner == TOP_RIGHT) {
				gc.drawImage(decorator, bid.width - did.width - 1, 0);
			} else if (corner == BOTTOM_LEFT) {
				gc.drawImage(decorator, 0, bid.height - did.height - 1);
			} else if (corner == BOTTOM_RIGHT) {
				gc.drawImage(decorator, bid.width - did.width - 1, bid.height
						- did.height - 1);
			}

			gc.dispose();
			decoratedMap.put(decorator, result);
		}
		return result;
	}

	/**
	 * Dispose all of the cached images
	 */
	public static void disposeImages() {
		for (Image image : m_ClassImageMap.values())
			image.dispose();
		m_ClassImageMap.clear();

		for (HashMap<Image, Image> decoratedMap : m_ImageToDecoratorMap
				.values()) {
			for (Image image : decoratedMap.values()) {
				image.dispose();
			}
		}
	}

	/**
	 * Dispose cached images in specified section
	 * 
	 * @param section
	 *            the section do dispose
	 */
	public static void disposeImages(String section) {
		for (String key : m_ClassImageMap.keySet()) {
			if (!key.startsWith(section + '|'))
				continue;
			Image image = m_ClassImageMap.get(key);
			image.dispose();
			m_ClassImageMap.remove(key);
		}
	}

	// ////////////////////////////
	// Font support
	// ////////////////////////////

	/**
	 * Maps font names to fonts
	 */
	private static HashMap<String, Font> m_FontMap = new HashMap<String, Font>();

	/**
	 * Maps fonts to their bold versions
	 */
	private static HashMap<Font, Font> m_FontToBoldFontMap = new HashMap<Font, Font>();

	/**
	 * Returns a font based on its name, height and style
	 * 
	 * @param name
	 *            String The name of the font
	 * @param height
	 *            int The height of the font
	 * @param style
	 *            int The style of the font
	 * @return Font The font matching the name, height and style
	 */
	public static Font getFont(String name, int height, int style) {
		return getFont(name, height, style, false, false);
	}

	/**
	 * Returns a font based on its font data, Windows-specific strikeout and
	 * underline flags are also supported.
	 * 
	 * @param data
	 *            FontData The font data of the font
	 * @param strikeout
	 *            boolean The strikeout flag (warning: Windows only)
	 * @param underline
	 *            boolean The underline flag (warning: Windows only)
	 * @return Font The font matching the fontdata, strikeout and underline
	 */
	public static Font getFont(FontData data, boolean strikeout,
			boolean underline) {
		return getFont(data.getName(), data.getHeight(), data.getStyle(),
				strikeout, underline);
	}

	/**
	 * Returns a font based on its name, height and style. Windows-specific
	 * strikeout and underline flags are also supported.
	 * 
	 * @param name
	 *            String The name of the font
	 * @param size
	 *            int The size of the font
	 * @param style
	 *            int The style of the font
	 * @param strikeout
	 *            boolean The strikeout flag (warning: Windows only)
	 * @param underline
	 *            boolean The underline flag (warning: Windows only)
	 * @return Font The font matching the name, height, style, strikeout and
	 *         underline
	 */
	public static Font getFont(String name, int size, int style,
			boolean strikeout, boolean underline) {
		String fontName = name + '|' + size + '|' + style + '|' + strikeout
				+ '|' + underline;
		Font font = m_FontMap.get(fontName);
		if (font == null) {
			FontData fontData = new FontData(name, size, style);
			if (strikeout || underline) {
				try {
					Class<?> logFontClass = Class
							.forName("org.eclipse.swt.internal.win32.LOGFONT"); //$NON-NLS-1$
					Object logFont = FontData.class
							.getField("data").get(fontData); //$NON-NLS-1$
					if (logFont != null && logFontClass != null) {
						if (strikeout) {
							logFontClass
									.getField("lfStrikeOut").set(logFont, new Byte((byte) 1)); //$NON-NLS-1$
						}
						if (underline) {
							logFontClass
									.getField("lfUnderline").set(logFont, new Byte((byte) 1)); //$NON-NLS-1$
						}
					}
				} catch (Throwable e) {
					System.err
							.println("Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			font = new Font(Display.getCurrent(), fontData);
			m_FontMap.put(fontName, font);
		}
		return font;
	}

	/**
	 * Return a bold version of the give font
	 * 
	 * @param baseFont
	 *            Font The font for whoch a bold version is desired
	 * @return Font The bold version of the give font
	 */
	public static Font getBoldFont(Font baseFont) {
		Font font = m_FontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData fontDatas[] = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(), data
					.getHeight(), SWT.BOLD);
			m_FontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}

	/**
	 * Dispose all of the cached fonts
	 */
	public static void disposeFonts() {
		for (Font font : m_FontMap.values())
			font.dispose();
		m_FontMap.clear();
	}

	// ////////////////////////////
	// CoolBar support
	// ////////////////////////////

	/**
	 * Fix the layout of the specified CoolBar
	 * 
	 * @param bar
	 *            CoolBar The CoolBar that shgoud be fixed
	 */
	public static void fixCoolBarSize(CoolBar bar) {
		CoolItem[] items = bar.getItems();
		// ensure that each item has control (at least empty one)
		for (CoolItem item : items) {
			if (item.getControl() == null)
				item.setControl(new Canvas(bar, SWT.NONE) {

					@Override
					public Point computeSize(int wHint, int hHint,
							boolean changed) {
						return new Point(20, 20);
					}
				});
		}
		// compute size for each item
		for (CoolItem item : items) {
			Control control = item.getControl();
			control.pack();
			Point size = control.getSize();
			item.setSize(item.computeSize(size.x, size.y));
		}
	}

	// ////////////////////////////
	// Cursor support
	// ////////////////////////////

	/**
	 * Maps IDs to cursors
	 */
	private static HashMap<Integer, Cursor> m_IdToCursorMap = new HashMap<Integer, Cursor>();

	/**
	 * Returns the system cursor matching the specific ID
	 * 
	 * @param id
	 *            int The ID value for the cursor
	 * @return Cursor The system cursor matching the specific ID
	 */
	public static Cursor getCursor(int id) {
		Integer key = new Integer(id);
		Cursor cursor = m_IdToCursorMap.get(key);
		if (cursor == null) {
			cursor = new Cursor(Display.getDefault(), id);
			m_IdToCursorMap.put(key, cursor);
		}
		return cursor;
	}

	/**
	 * Dispose all of the cached cursors
	 */
	public static void disposeCursors() {
		for (Cursor cursor : m_IdToCursorMap.values())
			cursor.dispose();
		m_IdToCursorMap.clear();
	}
}