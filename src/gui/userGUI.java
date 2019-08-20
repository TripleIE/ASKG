package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import util.NGramAnalyzer;
import util.removestopwords;
import NER.ontologyMapping;

public class userGUI extends javax.swing.JFrame{

    // these are the components we need.
    private final JSplitPane splitPane;  // split the window in top and bottom
    private final JPanel topPanel;       // container panel for the top
    private final JPanel bottomPanel;    // container panel for the bottom
    private final JScrollPane scrollPane; // makes the text scrollable
    private final JTextArea textArea;     // the text
    private final JPanel inputPanel;      // under the text a container for all the input elements
    private final JTextField textField;   // a textField for the text the user inputs
    private final JButton button;         // and a "send" button
    private final JList   list ;

    public userGUI(){

        // first, lets create the containers:
        // the splitPane devides the window in two components (here: top and bottom)
        // users can then move the devider and decide how much of the top component
        // and how much of the bottom component they want to see.
        splitPane = new JSplitPane();

        topPanel = new JPanel();         // our top component
        bottomPanel = new JPanel();      // our bottom component

        // in our bottom panel we want the text area and the input components
        scrollPane = new JScrollPane();  // this scrollPane is used to make the text area scrollable
        textArea = new JTextArea();      // this text area will be put inside the scrollPane

        // the input components will be put in a separate panel
        inputPanel = new JPanel();

        textField = new JTextField();    // first the input field where the user can type his text
        button = new JButton("Concept");    // and a button at the right, to send the text
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
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
/*				try {
					loadTree(concepts) ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			 // loadTable(concepts) ;
			}
		});
        //button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        // list 
		list = new JList();
		list.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
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
		

        // now lets define the default size of our window and its layout:
        setPreferredSize(new Dimension(400, 400));     // let's open the window with a default size of 400x400 pixels
        // the contentPane is the container that holds all our components
        getContentPane().setLayout(new GridLayout());  // the default GridLayout is like a grid with 1 column and 1 row,
        // we only add one element to the window itself
        getContentPane().add(splitPane);               // due to the GridLayout, our splitPane will now fill the whole window

        // let's configure our splitPane:
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);  // we want it to split the window verticaly
        splitPane.setDividerLocation(75);                    // the initial position of the divider is 200 (our window is 400 pixels high)
        splitPane.setTopComponent(topPanel);                  // at the top we want our "topPanel"
        splitPane.setBottomComponent(bottomPanel);            // and at the bottom we want our "bottomPanel"

        // our topPanel doesn't need anymore for this example. Whatever you want it to contain, you can add it here
        
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // BoxLayout.Y_AXIS will arrange the content vertically
        topPanel.add(textField);                // first we add the scrollPane to the bottomPanel, so it is at the top
       // scrollPane.setViewportView(textField);       // the scrollPane should make the textArea scrollable, so we define the viewport
        topPanel.add(inputPanel);                // then we add the inputPanel to the bottomPanel, so it under the scrollPane / textArea
        

        // let's set the maximum size of the inputPanel, so it doesn't get too big when the user resizes the window
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));     // we set the max height to 75 and the max width to (almost) unlimited
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));   // X_Axis will arrange the content horizontally
       // inputPanel.add(textField);        // left will be the textField
        inputPanel.add(list);   // Category 
        inputPanel.add(button);           // and right the "send" button
       

        pack();   // calling pack() at the end, will ensure that every layout and size we just defined gets applied before the stuff becomes visible
    }

    public static void main(String args[]){
        EventQueue.invokeLater(new Runnable(){
            @Override
            public void run(){
                new userGUI().setVisible(true);
            }
        });
    }
}