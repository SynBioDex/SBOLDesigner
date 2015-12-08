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

package com.clarkparsia.versioning.ui;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ColorPool {
	private static final Iterable<Color> DEFAULT_COLORS = ImmutableList.of(
		new Color(133, 166, 214),
		new Color(221, 205, 93), new Color(199, 134, 57),
		new Color(131, 150, 98), new Color(197, 123, 127),
		new Color(139, 136, 140), new Color(48, 135, 144),
		new Color(190, 93, 66), new Color(143, 163, 54), new Color(180, 148, 74),
		new Color(101, 101, 217), new Color(72, 153, 119),
		new Color(23, 101, 160), new Color(132, 164, 118),
		new Color(255, 230, 59), new Color(136, 176, 70), new Color(255, 138, 1),
		new Color(123, 187, 95), new Color(233, 88, 98), new Color(93, 158, 254),
		new Color(175, 215, 0), new Color(140, 134, 142),
		new Color(232, 168, 21), new Color(0, 172, 191), new Color(251, 58, 4),
		new Color(63, 64, 255), new Color(27, 194, 130), new Color(0, 104, 183) 
	);
	
	private Deque<Color> colors = new ArrayDeque<Color>();
	
	public ColorPool() {
		refill();
	}

	private void refill() {
		Iterables.addAll(colors, DEFAULT_COLORS);
	}

	public Color remove() {
		if (colors.isEmpty()) {
			refill();
		}
		return colors.remove();
	}

	public void put(Color color) {
		colors.push(color);
	}
	
	public void clear() {
		colors.clear();
	}
}
