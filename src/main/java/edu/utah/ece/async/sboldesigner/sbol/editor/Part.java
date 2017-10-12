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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;

import com.google.common.collect.ImmutableList;

import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;

/**
 * 
 * @author Evren Sirin
 */
public class Part {
	/**
	 * Describes the type of the image for the part. The SBOL visual images have
	 * 1x2 width/height ratio so they can be places above or below the baseline
	 * without alignment issues. But this means images have a lot of empty space
	 * and they are not ideal to use as a toolbar or a button icon. We use the
	 * image type to describe the visual orientation of the image associated
	 * with a part so we can automatically crop the image to exclude the extra
	 * empty space.
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
	private final Image positiveImage;
	private final Image negativeImage;
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
			positiveImage = negativeImage = smallImage = null;
		} else {
			BufferedImage image = Images.toBufferedImage(Images.getPartImage(imageFileName));
			this.positiveImage = Images.scaleImageToWidth(image, IMG_WIDTH);
			this.negativeImage = Images.rotate180(positiveImage);
			this.smallImage = Images.scaleImageToWidth(image.getSubimage(0, image.getHeight() / imageType.cropRatio,
					image.getWidth(), image.getHeight() / 2), 24);
		}
	}

	public String getName() {
		return name;
	}

	public String getDisplayId() {
		return displayId;
	}

	public URI getRole() {
		return roles.isEmpty() ? null : roles.get(0);
	}

	public List<URI> getRoles() {
		return roles;
	}

	/**
	 * Returns the image for the part that can be used in the SBOL design.
	 */
	public Image getImage(OrientationType orientation) {
		return orientation == OrientationType.REVERSECOMPLEMENT ? negativeImage : positiveImage;
	}

	/**
	 * Returns the image for the part with extra empty space cropped which makes
	 * it suitable to be used in a toolbar, button, etc.
	 */
	public Image getImage() {
		return smallImage;
	}


	/**
	 * Creates a CD in design using roles.
	 */
	public ComponentDefinition createComponentDefinition(SBOLDocument design) 
	{
		return createComponentDefinition(design, ComponentDefinition.DNA);
	}
	
	/**
	 * Create a ComponentDefinition that will set the roleType base on the given ComponentDefinition Type.
	 * @param design - The SBOL document that the ComponentDefinition will be created in.
	 * @param compDefType - The type that the ComponentDefinition will be set to.
	 * @return The ComponentDefinition that was created.
	 */
	public ComponentDefinition createComponentDefinition(SBOLDocument design, URI compDefType) {
		// change list of roles to set of roles
		Set<URI> setRoles = new HashSet<URI>(roles);
		
		// create ComponentDefinition using the following parameters
		try 
		{
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, getDisplayId(), "1", "CD", design);
			ComponentDefinition comp = design.createComponentDefinition(uniqueId, "1", compDefType);
			
			if(compDefType.equals(ComponentDefinition.DNA))
			{
				setRoles.add(SequenceOntology.ENGINEERED_REGION);
			}
			else if(compDefType.equals(ComponentDefinition.RNA))
			{
				setRoles.add(SequenceOntology.MRNA);
			}
			else if(compDefType.equals(ComponentDefinition.SMALL_MOLECULE))
			{
				setRoles.add(ComponentDefinition.EFFECTOR);
			}
			
			// If a CD is being created by a SEQUENCE_FEATURE part, replace
			// SEQUENCE_FEATURE with ENGINEERED_REGION. SBOLDesigner creates
			// ENGINEERED_REGIONs, not SEQUENCE_FEATUREs. However, GENERIC parts
			// are of role SEQUENCE_FEATURE, so searching registries for GENERIC
			// still matches all CDs.
			if (setRoles.contains(SequenceOntology.SEQUENCE_FEATURE)) {
				setRoles.clear();
				setRoles.add(SequenceOntology.ENGINEERED_REGION);
			}
			comp.setRoles(setRoles);
			return comp;
		} 
		catch (SBOLValidationException e) 
		{
			e.printStackTrace();
			return null;
		}
	}

	public String toString() {
		return displayId + " (" + name + ")";
	}
}