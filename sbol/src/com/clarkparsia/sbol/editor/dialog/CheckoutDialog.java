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

package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.dialog.CheckoutDialog.CheckoutResult;
import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;
import com.clarkparsia.swing.FilterTree;
import com.clarkparsia.swing.FilterTree.FilterTreeModel;
import com.clarkparsia.swing.FilterTree.FilterTreeNode;
import com.clarkparsia.swing.FormBuilder;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.RVTFactory;
import com.clarkparsia.versioning.Ref;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.Tag;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 * 
 * @author Evren Sirin
 */
public class CheckoutDialog extends InputDialog<CheckoutResult> {
	public static class CheckoutResult {
		private final RVTDocumentIO documentIO;
		private final boolean insert;
		private CheckoutResult(RVTDocumentIO documentIO, boolean insert) {
	        super();
	        this.documentIO = documentIO;
	        this.insert = insert;
        }
		
		public RVTDocumentIO getDocumentIO() {
			return documentIO;
		}
		
		public boolean isInsert() {
			return insert;
		}		
	}
    private enum SpecialNodeType { ENDPOINT, BRANCHES, REVISIONS, TAGS };
    
    private static class SpecialNode {
    	private final SpecialNodeType type;
    	private final int count;
    	
		private SpecialNode(SpecialNodeType type, int count) {
	        this.type = type;
	        this.count = count;
        }
		
		public String toString() {
			return CharSequences.toTitleCase(type.toString()) + " (" + count + ")";
		}
    }

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private static final Function<FilterTreeNode, String> STRING_FUNC = new Function<FilterTreeNode, String>() {
		
		public String apply(FilterTreeNode node) {
			Object obj = node.getUserObject();
			if (obj instanceof SpecialNode) {
				return obj.toString();
			}
			
			Ref ref = (Ref) obj;
			ActionInfo info = ref.getActionInfo();
			StringBuilder label = new StringBuilder();
			label.append("<html>");
			if (ref instanceof Revision) {
				label.append(ref.getName().substring(0, 8));
			}
			else {
				label.append(ref.getName());
			}
			label.append(" <font color='gray'>");
			label.append(info.getMessage());
			if (ref instanceof Revision) {
				label.append(" <i>");
				label.append(DATE_FORMAT.format(info.getDate().getTime()));
				label.append("</i>");
			}
			label.append("</font></html>");			
			
			return label.toString();
		}
	};
	
	private static final Function<FilterTreeNode, Icon> ICON_FUNC = new Function<FilterTreeNode, Icon>() {
		private final Icon REPO = new ImageIcon(Images.getActionImage("repository.gif"));
		private final Icon BRANCH = new ImageIcon(Images.getActionImage("switchBranch.gif"));
		private final Icon BRANCHES = new ImageIcon(Images.getActionImage("branches.gif"));
		private final Icon TAG = new ImageIcon(Images.getActionImage("tag.png"));
		private final Icon TAGS = new ImageIcon(Images.getActionImage("tags.png"));
		
		public Icon apply(FilterTreeNode node) {
			Object obj = node.getUserObject();
			if (obj instanceof SpecialNode) {
				switch (((SpecialNode) obj).type) {
					case ENDPOINT: return REPO;
					case BRANCHES: return BRANCHES;
					case REVISIONS: return BRANCHES;
					case TAGS: return TAGS;
					default: throw new AssertionError();
				}
			}
			else if (obj instanceof Repository) {
				return REPO;
			}
			else if (obj instanceof Branch) {
				return BRANCH;
			}
			else if (obj instanceof Tag) {
				return TAG;
			}
			else {
				return null;
			}
		}
	};
    
	private static final Ordering<Ref> NEWEST_FIRST = Ordering.from(new Comparator<Ref>() {
		@Override
        public int compare(Ref rev1, Ref rev2) {
            return rev2.getActionInfo().getDate().compareTo(rev1.getActionInfo().getDate());
        }
	});
	
	private static final String LABEL = "Versioned Designs";
	
	private FilterTree tree;
	private JLabel label;
	private JCheckBox insertDesign;
	
	public CheckoutDialog(final Component parent) {
		super(parent, "Checkout", RegistryType.VERSION);
	}
	
	@Override
	protected void initFormPanel(FormBuilder builder) {
		insertDesign = new JCheckBox("Insert selection into current design"); 
		builder.add("", insertDesign);
	}
	
	@Override
	protected JPanel initMainPanel() {
		tree = new FilterTree(createTreeModel(repos()), STRING_FUNC, ICON_FUNC);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent event) {
				FilterTreeNode node = (FilterTreeNode) tree.getLastSelectedPathComponent();
				setSelectAllowed(node != null && !(node.getUserObject() instanceof SpecialNode));
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && tree.getSelectionPath() != null) {
					canceled = false;
					setVisible(false);
				}
			}
		});
		
		label = new JLabel(LABEL);
		label.setLabelFor(tree);
		
		JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(450, 200));
		scroller.setAlignmentX(LEFT_ALIGNMENT);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));		
		panel.add(label);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(scroller);
		
		return panel;
	}

    protected CheckoutResult getSelection() {
    	if (tree == null || tree.getSelectionPath() == null) {
    		return null;
    	}
    	
    	TreePath path = tree.getSelectionPath();
    	FilterTreeNode node = ((FilterTreeNode) path.getLastPathComponent());
    	Object obj = node.getUserObject();
    	
    	Repository repo = null;
    	String branch = Branch.MASTER;
    	Revision rev = null;
    	
    	if (obj instanceof Revision) {
    		rev = (Revision) obj;
    	}
    	else if (obj instanceof Tag) {
    		rev = ((Tag) obj).getRevision();
    	}
    	else if (obj instanceof Branch) {
    		repo = ((Branch) obj).getRepository();
    		branch = ((Branch) obj).getName();
    	}
    	else if (obj instanceof Repository) {
    		repo = (Repository) obj;
    	}
    	else {
    		throw new UnsupportedOperationException("Invalid selection");
    	}
    	
    	RVTDocumentIO documentIO = (rev != null) ? RVTDocumentIO.createForRevision(rev) : RVTDocumentIO.createForBranch(repo, branch);
    	return new CheckoutResult(documentIO, insertDesign.isSelected());
	}
    
    private List<Repository> repos() {
    	List<Repository> repos = ImmutableList.<Repository>of();    	
    	try {
    		if (endpoint != null) {
    			repos = RVTFactory.get(endpoint).repos().list();
    		}
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
    	return repos;
    }
	
    @Override
	public void registryChanged() {
    	List<Repository> repos = repos();

    	label.setText(LABEL + " (" + repos.size() + ")");    
    	tree.setModel(createTreeModel(repos));
    	
		repaint();
	}
    
    private FilterTreeModel createTreeModel(List<Repository> repos) {
    	FilterTreeNode node = new FilterTreeNode(new SpecialNode(SpecialNodeType.ENDPOINT, repos.size()));
    	for (Repository repo : repos) {
	        node.add(createRepoNode(repo));
        }
    	return new FilterTreeModel(node);
    }
    
    private FilterTreeNode createRepoNode(Repository repo) {
    	FilterTreeNode node = new FilterTreeNode(repo);
    	
    	if (SBOLEditorPreferences.INSTANCE.isBranchingEnabled()) {
	    	List<Branch> branches = repo.branches().list();
	    	FilterTreeNode branchNodes = new FilterTreeNode(new SpecialNode(SpecialNodeType.BRANCHES, branches.size()));
	    	for (Branch branch : branches) {
	    		try {
	                branchNodes.add(createBranchNode(branch));
                }
                catch (Exception e) {
	                e.printStackTrace();
                }
	        }
	    	node.add(branchNodes);
    	}
    	else {
    		FilterTreeNode masterNodes = createBranchNode(repo.branches().get(Branch.MASTER));
    		masterNodes.setUserObject(new SpecialNode(SpecialNodeType.REVISIONS, masterNodes.getChildCount()));
	    	node.add(masterNodes);
    	}

    	List<Tag> tags = repo.tags().list();
    	FilterTreeNode tagNodes = new FilterTreeNode(new SpecialNode(SpecialNodeType.TAGS, tags.size()));
    	for (Tag tag : tags) {
	        tagNodes.add(new FilterTreeNode(tag));
        }
    	node.add(tagNodes);
    	
    	return node;
    }
    
    private FilterTreeNode createBranchNode(Branch branch) {
    	FilterTreeNode node = new FilterTreeNode(branch);

		Set<Revision> revisions = Sets.newTreeSet(NEWEST_FIRST);
		Queue<Revision> pending = Lists.newLinkedList();
		pending.add(branch.getHead());
		while (!pending.isEmpty()) {
			Revision rev = pending.remove();
			revisions.add(rev);
			for (Revision parent : rev.getParents()) {
                if (parent.getBranch().equals(branch)) {
                	pending.add(parent);
                }
            }
		}

		for (Revision rev : revisions) {
			node.add(new FilterTreeNode(rev));
		}
		
    	return node;
    }
}
