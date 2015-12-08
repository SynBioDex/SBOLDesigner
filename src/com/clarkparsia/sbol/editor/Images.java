/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.sbol.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * Utility functions for dealing with images.
 * 
 * @author Evren Sirin
 */
public class Images {
	public static BufferedImage getPartImage(String fileName) {
		return getImage("parts/" + fileName);
	}
	
	public static BufferedImage getActionImage(String fileName) {		
		return getImage("actions/" + fileName);
	}
	
	public static BufferedImage getImage(String fileName) {		
		try {
			if (fileName != null) {
				return ImageIO.read(Images.class.getResourceAsStream("images/" + fileName));
			}
		} catch (Exception e) {
		}
		
		return null;
	}

	public static Image scaleImageToWidth(Image image, int scaleWidth) {
		return image == null ? null : image.getScaledInstance(scaleWidth, -1, Image.SCALE_SMOOTH);
	}

	public static Image scaleImage(Image image, double scaleFactor) {
		return image == null ? null : image.getScaledInstance((int) (image.getWidth(null) * scaleFactor), (int) (image.getHeight(null) * scaleFactor), Image.SCALE_SMOOTH);
	}
	
	public static BufferedImage createBorderedImage(final Image image, final Color color) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.setColor(color);
		g2d.drawRect(0, 0, w - 1, h - 1);
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		return bufferedImage;
	}
	
	public static void makeAllImagesTransparent(File dir) throws IOException {
        for (File f : dir.listFiles()) {
            BufferedImage image = ImageIO.read(f);
            Image img = makeColorTransparent(image, Color.white);
        	BufferedImage transparent = toBufferedImage(img);

        	ImageIO.write(transparent, "PNG", f);
        }
	}

	public static Image makeColorTransparent(final BufferedImage im, final Color color) {
		final ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFFFFFFFF;

			public final int filterRGB(final int x, final int y, final int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				}
				else {
					// nothing to do
					return rgb;
				}
			}
		};

		final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

		return bufferedImage;
	}
	
	public static BufferedImage flipVertical(Image image) {
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		return applyTransform(image, tx);
	}

	public static BufferedImage flipHorizontal(Image image) {
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
		return applyTransform(image, tx);
	}

	public static BufferedImage rotate180(Image image) {
		AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
		tx.translate(-image.getWidth(null), -image.getHeight(null));
		return applyTransform(image, tx);
	}
		
	public static BufferedImage applyTransform(Image image, AffineTransform tx) {	
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(toBufferedImage(image), null);
	}

	public static BufferedImage createImage(JComponent component) {
		Dimension dim = component.getSize();
		BufferedImage image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();

		// Paint a background for non-opaque components, otherwise the background will be black
		if (!component.isOpaque()) {
			g2d.setColor(component.getBackground());
			g2d.fillRect(0, 0, dim.width, dim.height);
		}
		
		component.paint(g2d);
		g2d.dispose();
		return image;
	}
	
	public static void copyToClipboard(Image image) {
		TransferableImage trans = new TransferableImage(image);
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(trans, new ClipboardOwner() {				
			@Override
			public void lostOwnership(Clipboard c, Transferable t) {
				// nothing to do
			}
		});
	}

	private static class TransferableImage implements Transferable {
		private Image i;

		public TransferableImage(Image i) {
			this.i = i;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
				return i;
			}
			else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] flavors = new DataFlavor[1];
			flavors[0] = DataFlavor.imageFlavor;
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			DataFlavor[] flavors = getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavor.equals(flavors[i])) {
					return true;
				}
			}

			return false;
		}
	}
}
