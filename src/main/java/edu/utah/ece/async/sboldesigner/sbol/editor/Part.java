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

package edu.utah.ece.async.sboldesigner.sbol.editor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import com.google.common.collect.ImmutableList;

import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;

/**
 * 
 * @author Evren Sirin
 */
public class Part {
	/**
	 * Describes the type of the largeImage for the part. The SBOL visual images
	 * have 1x2 width/height ratio so they can be places above or below the
	 * baseline without alignment issues. But this means images have a lot of
	 * empty space and they are not ideal to use as a toolbar or a button icon.
	 * We use the largeImage type to describe the visual orientation of the
	 * largeImage associated with a part so we can automatically crop the
	 * largeImage to exclude the extra empty space.
	 * 
	 * @author Evren Sirin
	 */
	public enum ImageType {
		CENTERED_ON_BASELINE(4), SHORT_OVER_BASELINE(8), TALL_OVER_BASELINE(16);

		private final int cropRatio;

		ImageType(int ratio) {
			this.cropRatio = ratio;
		}
	}

	public static final int IMG_HEIGHT = 128;
	public static final int IMG_WIDTH = 64;

	private final String name;
	private final String displayId;
	private final List<URI> roles;
	private final Image largeImage;
	private final Image smallImage;

	public Part(String name, String displayId) {
		this(name, displayId, null, null, new URI[0]);
	}

	public Part(URI role, String name, String displayId) {
		this(name, displayId, null, null, role);
	}

	public Part(String name, String displayId, String imageFileName, ImageType imageType, URI... roles) {
		this.name = name;
		this.displayId = displayId;
		this.roles = ImmutableList.copyOf(roles);
		if (imageFileName == null) {
			largeImage = smallImage = null;
		} else {
			BufferedImage image;
			//This logic is for the prefence of whether or not the user prefers the arrow or default CDS image. 
			if(imageFileName == "cds.png") {
				if(SBOLEditorPreferences.INSTANCE.getCDSBehavior() == 1) {
					image = Images.toBufferedImage(Images.getPartImage("arrowcds.png"));
				}else {
					image = Images.toBufferedImage(Images.getPartImage(imageFileName));
				}
			}else {
				image = Images.toBufferedImage(Images.getPartImage(imageFileName));
			}

			this.smallImage = Images.scaleImageToWidth(image.getSubimage(0, image.getHeight() / imageType.cropRatio,
					image.getWidth(), image.getHeight() / 2), 24);
			this.largeImage = Images.scaleImageToWidth(image, IMG_WIDTH);
		}
	}

	public String getName() {
		return name;
	}

	public String getDisplayId() {
		return displayId;
	}

	public URI getRole() {
		return roles.isEmpty() ? null : roleCheck();
	}
	
	private URI roleCheck() {
		URI curr;
		for(int i = 0; i < roles.size(); i++) {
			curr = roles.get(i);
			if(curr.toString().startsWith("http://identifiers.org/so/")) {
				return curr;
			}
		}
		return null;
	}

	public List<URI> getRoles() {
		return roles;
	}

	/**
	 * Returns the image for the part that can be used in the SBOL design.
	 */
	public Image getImage(OrientationType orientation, boolean composite, boolean hasVariants, boolean hasSequence) {
		Image image = this.largeImage;

		if (orientation == OrientationType.REVERSECOMPLEMENT) {
			image = Images.rotate180(image);
		}

		if (composite) {
			BufferedImage scaledCompositeOverlay = Images
					.toBufferedImage(Images.scaleImageToWidth(Images.getPartImage("composite-overlay.png"), IMG_WIDTH));
			image = Images.overlay(image, scaledCompositeOverlay, IMG_WIDTH, IMG_HEIGHT);
		}

		if (hasVariants) {
			BufferedImage scaledVariantOverlay = Images
					.toBufferedImage(Images.scaleImageToWidth(Images.getPartImage("variant-overlay.png"), IMG_WIDTH));
			image = Images.overlay(image, scaledVariantOverlay, IMG_WIDTH, IMG_HEIGHT);
		} else {
			if (!hasSequence) {
				BufferedImage scaledWarningOverlay = Images.toBufferedImage(
						Images.scaleImageToWidth(Images.getPartImage("error-advice-sign-overlay.png"), IMG_WIDTH));
				image = Images.overlay(image, scaledWarningOverlay, IMG_WIDTH, IMG_HEIGHT);
			}
		}

		return image;
	}

	/**
	 * Returns the largeImage for the part with extra empty space cropped which
	 * makes it suitable to be used in a toolbar, button, etc.
	 */
	public Image getImage() {
		return smallImage;
	}

	/**
	 * Creates a CD in design using roles.
	 */
	public ComponentDefinition createComponentDefinition(SBOLDocument design) {
		try {
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, getDisplayId(), "1", "CD", design);
			ComponentDefinition comp = design.createComponentDefinition(uniqueId, "1", ComponentDefinition.DNA);

			for (URI role : roles) {
				comp.addRole(role);
			}

			return comp;
		} catch (SBOLValidationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String toString() {
		return displayId + " (" + name + ")";
	}
}