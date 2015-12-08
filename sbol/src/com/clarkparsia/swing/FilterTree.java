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

package com.clarkparsia.swing;

import java.awt.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class FilterTree extends JTree {
	private final Function<? super FilterTreeNode, String> stringFunc;

	public FilterTree(FilterTreeModel model, Function<? super FilterTreeNode, String> stringFunc) {
		this(model, stringFunc, null);
	}
	
	public FilterTree(FilterTreeModel model, Function<? super FilterTreeNode, String> stringFunc, Function<? super FilterTreeNode, Icon> iconFunc) {
		super(model);
		
		this.stringFunc = stringFunc;
		
		setCellRenderer(new FilterTreeCellRenderer(stringFunc, iconFunc));
		
	}
	
	public FilterTree(Function<? super FilterTreeNode, String> stringFunc) {
		this.stringFunc = stringFunc;
		
		setCellRenderer(new FilterTreeCellRenderer(stringFunc));
	}
	
	public void setFilterField(final JTextField field) {
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent paramDocumentEvent) {
				setFilter(field.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent paramDocumentEvent) {
				setFilter(field.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent paramDocumentEvent) {
				setFilter(field.getText());
			}
		});
	}
	
	public void setFilter(final String filterText) {
		Predicate<FilterTreeNode> filter = Strings.isNullOrEmpty(filterText) ? null : new FilterTreePredicate(filterText, stringFunc);
		((FilterTreeModel) getModel()).setFilter(filter);
		expandAll();
	}
	
	public void expandAll() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}
	
	public static class FilterTreeModel extends DefaultTreeModel {
		public FilterTreeModel(FilterTreeNode node) {
			this(node, null);
		}

		public FilterTreeModel(FilterTreeNode root, Predicate<FilterTreeNode> filter) {
			super(root);
			if (root != null) {
				root.setFilter(filter);
			}
		}

		public void setFilter(Predicate<FilterTreeNode> filter) {
			if (root != null) {
				((FilterTreeNode) root).setFilter(filter);
				Object[] path = { root };
				fireTreeStructureChanged(this, path, null, null);
			}
		}

		public int getChildCount(Object parent) {
			return (parent instanceof FilterTreeNode) ? (((FilterTreeNode) parent).getChildCount()) : 0;
		}

		public Object getChild(Object parent, int index) {
			return (parent instanceof FilterTreeNode) ? (((FilterTreeNode) parent).getChildAt(index)) : null;
		}
	}

	public static class FilterTreeNode extends DefaultMutableTreeNode {
		private Predicate<FilterTreeNode> filter;
		private boolean passed = true;
		private List<FilterTreeNode> filteredChildren = Lists.newArrayList();

		public FilterTreeNode(Object userObject) {
			super(userObject);
		}

		@Override
		public FilterTreeNode getParent() {
		    return (FilterTreeNode) super.getParent();
		}
		
		public Predicate<FilterTreeNode> getFilter() {
			return filter;
		}

		public void setFilter(Predicate<FilterTreeNode> filter) {
			this.filter = filter;
			passed = false;
			filteredChildren.clear();
			passFilterDown(filter);
			passed = (filter == null || filteredChildren.size() != 0 || filter.apply(this));
		}

		private void passFilterDown(Predicate<FilterTreeNode> filter) {
			int realChildCount = super.getChildCount();
			for (int i = 0; i < realChildCount; i++) {
				FilterTreeNode realChild = (FilterTreeNode) super.getChildAt(i);
				realChild.setFilter(filter);
				if (realChild.isPassed()) {
					filteredChildren.add(realChild);
				}
			}
		}

		public int getChildCount() {
			return (filter == null) ? super.getChildCount() : filteredChildren.size();
		}

		public FilterTreeNode getChildAt(int index) {
			return (filter == null) ? (FilterTreeNode) super.getChildAt(index) : filteredChildren.get(index);
		}

		public boolean isPassed() {
			return passed;
		}
	}

	public static class FilterTreePredicate implements Predicate<FilterTreeNode> {
		private final Matcher matcher;
		private final Function<? super FilterTreeNode, String> stringFunc;

		private FilterTreePredicate(String filterText, Function<? super FilterTreeNode, String> stringFunc) {
			this.matcher = Pattern.compile(Pattern.quote(filterText), Pattern.CASE_INSENSITIVE).matcher("");
			this.stringFunc = stringFunc;
		}

		public boolean apply(FilterTreeNode node) {
			return matcher.reset(stringFunc.apply(node)).find();
		}
	};

	public static class FilterTreeCellRenderer extends DefaultTreeCellRenderer {
		private final Function<? super FilterTreeNode, String> stringFunc;
		private final Function<? super FilterTreeNode, Icon> iconFunc;

		public FilterTreeCellRenderer(Function<? super FilterTreeNode, String> stringFunc) {
			this(stringFunc, null);
		}
		
		public FilterTreeCellRenderer(Function<? super FilterTreeNode, String> stringFunc, Function<? super FilterTreeNode, Icon> iconFunc) {
			this.stringFunc = stringFunc;
			this.iconFunc = iconFunc;

			setLeafIcon(null);
			setClosedIcon(null);
			setOpenIcon(null);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		                boolean leaf, int row, boolean hasfocus) {

			FilterTreeNode node = (FilterTreeNode) value;
			String label = stringFunc.apply(node);

			Predicate<FilterTreeNode> filter = node.getFilter();
			if (filter instanceof FilterTreePredicate && node.getFilter().apply(node)) {
				StringBuffer sb = new StringBuffer("<html>");
				Matcher m = ((FilterTreePredicate) node.getFilter()).matcher.reset(label);
			    while (m.find()) {
			        m.appendReplacement(sb, "<b>" + m.group() + "</b>");
			    }
			    m.appendTail(sb);
			    sb.append("</html>");
				label = sb.toString();
			}
			
			super.getTreeCellRendererComponent(tree, label, selected, expanded, leaf, row, hasfocus);
			
			if (iconFunc != null) {
				setIcon(iconFunc.apply(node));
			}

			return this;
		}
	}
}
