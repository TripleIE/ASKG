package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;

import javax.swing.border.LineBorder;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import util.NGramAnalyzer;
import util.removestopwords;
import NER.ontologyMapping;
import HRCHY.hierarchy;


public class userWindow {

	private JFrame frmAutomatedOntologyExtraction;
	private JTable table;
	private JTextField textField;
	private JTable table_1;
	private JPanel panel_2 ;
	private JPanel panel_3 ;
	private JTree tree ;
	private DefaultMutableTreeNode top ;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					userWindow window = new userWindow();
					window.frmAutomatedOntologyExtraction.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public userWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAutomatedOntologyExtraction = new JFrame();
		frmAutomatedOntologyExtraction.setTitle("Automated Ontology Extraction (AOE-LBO) Tools");
		frmAutomatedOntologyExtraction.setBounds(100, 100, 700, 493);
		frmAutomatedOntologyExtraction.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAutomatedOntologyExtraction.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Semantic Group Types");
		lblNewLabel.setBounds(20, 60, 129, 14);
		frmAutomatedOntologyExtraction.getContentPane().add(lblNewLabel);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 11, 664, 38);
		frmAutomatedOntologyExtraction.getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_1 = new JLabel("Sentence:  ");
		panel.add(lblNewLabel_1, BorderLayout.WEST);
		
		textField = new JTextField();
		panel.add(textField, BorderLayout.CENTER);
		textField.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(10, 54, 664, 71);
		frmAutomatedOntologyExtraction.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JButton btnNewButton = new JButton("Get Concepts");
		btnNewButton.setBackground(Color.LIGHT_GRAY);
		btnNewButton.setBounds(510, 39, 144, 23);
		panel_1.add(btnNewButton);
		
		final JList list = new JList();
		list.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.BLUE, null));
		list.setBounds(10, 24, 129, 38);
		panel_1.add(list);
		list.setToolTipText("You can select one or more Semantic Group Types");
		list.setModel(new AbstractListModel() {
			String[] values = new String[] {"Disorders", "Chimecal & Drugs"};
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		
		panel_2 = new JPanel();
		panel_2.setBounds(10, 130, 331, 263);
		frmAutomatedOntologyExtraction.getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		table_1 = new JTable();
		table_1.setBackground(Color.CYAN);
		table_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		table_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//table_1.setFillsViewportHeight(true);
		table_1.setModel(new DefaultTableModel(
			new Object[][] {
				{"Concept", "Type", "URI"},
			},
			new String[] {
				"Concept", "Type", "URI"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, Object.class, Object.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		table_1.getColumnModel().getColumn(0).setPreferredWidth(163);
		table_1.getColumnModel().getColumn(1).setPreferredWidth(130);
		table_1.getColumnModel().getColumn(2).setPreferredWidth(178);
		table_1.setBounds(10, 11, 311, 241);
		panel_2.add(table_1);

		///////////////////////////////////////////////////////////////////////////////// Taxonomic ///////////////////////////////////////////////////////////////
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(351, 136, 323, 257);
		//frmAutomatedOntologyExtraction.getContentPane().add(panel_3);
		panel_3.setLayout(null);
		
		top =  new DefaultMutableTreeNode("Entity");
		tree = new JTree(top);
		tree.setBounds(0, 0, 323, 323);
		tree.removeAll();
		
	    // Lastly, put the JTree into a JScrollPane.
	    JScrollPane scrollpane = new JScrollPane();
	    scrollpane.getViewport().add(tree);
	    scrollpane.setBounds(0, 0, 323, 250);
	    panel_3.add( scrollpane);
		frmAutomatedOntologyExtraction.getContentPane().add(panel_3);
		
		

		JPanel panel_4 = new JPanel();
		panel_4.setBounds(20, 404, 654, 40);
		frmAutomatedOntologyExtraction.getContentPane().add(panel_4);
		
		JButton btnGenerateOntology = new JButton("Generate Ontology");
		btnGenerateOntology.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		panel_4.add(btnGenerateOntology);
		
		JButton btnToxanomic = new JButton("Toxanomic");
		btnToxanomic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int rowIndex = table_1.getSelectedRow() ; 
				if(rowIndex>=1)
				{
					Object concept = table_1.getModel().getValueAt(rowIndex, 0);
					
					try {
						loadTaxonomic(concept.toString(),3) ;
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
			}
		});
		panel_4.add(btnToxanomic);
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
				
				if(textField.getText().isEmpty() || list.isSelectionEmpty()){
					
					
				}
				else
				{
					
					String text  = removestopwords.removestopwordfromsen(textField.getText()) ;
					Map<String, Integer> mentions = NGramAnalyzer.entities(1,3, text ) ;
					Map<String, String> concepts = null ; 
					try {
						concepts = ontologyMapping.getAnnotationWSemanticType(mentions,list)  ;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String selctedItems = "";  
					for (Object item:list.getSelectedValuesList())
					{
						selctedItems += item.toString() ;
					}
					loadTable(concepts) ;
				}
			}
		});

		
		
	}
	
public void loadTable(Map<String, String> concepts) {
	

		DefaultTableModel model = (DefaultTableModel) table_1.getModel();
		for ( int I =1; I < model.getRowCount(); ++I)
		{
			model.removeRow(I);
		}
		
		for(String concept:concepts.keySet())
		{
			String[] tokens =  concepts.get(concept).split(",");
			
			model.addRow(new Object[]{concept, tokens[0],tokens[1]});
		}
	}

public DefaultMutableTreeNode loadTaxonomic(String concept,int maxLevel) throws IOException {
	
	
	List<String>   listTaxon = hierarchy.Taxonomic_Extractor(concept,0,maxLevel) ; 
	 ArrayList al = new ArrayList(maxLevel); 
	
	for (int i = 0; i <=  maxLevel ; ++i)
	{
		List<String> levelList = new ArrayList<String>() ;
		al.add(levelList);
	}
	
	for(String item:listTaxon)
	{
		String[] token  = item.split("!");
		int index= Integer.parseInt(token[2]); 
		List<String> level = (List<String>) al.get(index);
		level.add(item);
		al.set(index, level);
		
	}
	
	for (int index = maxLevel; index >= 0 ; --index)
	{
		
		List<String> level = (List<String>) al.get(index);
		
		DefaultMutableTreeNode parent = addlevel(level) ;
		
		top.getFirstLeaf().add(parent);
		
	}
	
	return top ;

}

public DefaultMutableTreeNode addlevel(List<String> levelList) throws IOException
{
	
	    DefaultMutableTreeNode parent = new DefaultMutableTreeNode();
		boolean firstItem = true ; // the label of the parent 
		for(String item:levelList)
		{
			String[] token  = item.split("!");
			DefaultMutableTreeNode firstchild = new DefaultMutableTreeNode(token[1]);
			parent.add(firstchild);		
		}
		return parent ; 


}

	
}
