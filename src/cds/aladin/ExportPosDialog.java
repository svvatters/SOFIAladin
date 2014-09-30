package cds.aladin;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import cds.aladin.Aladin;
import cds.aladin.Plan;
import cds.aladin.CatalogPanel;
import cds.aladin.ExportPosLog;
import cds.astro.Astrocoo;
import cds.astro.Astroformat;
import cds.savot.model.FieldSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotResource;
import cds.savot.model.TDSet;
import cds.savot.model.TRSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

/**
 * Dialog window that saves catalog planes as mccs .pos files.
 * @author shannon.watters@gmail.com
 * @SOFIA_Aladin-extension class
 */
final class ExportPosDialog extends JDialog {
	
	private final Aladin aladin;
	private 	int catalogCount;			
	private 	JFrame progressBarFrame;
	private 	StringBuilder errLog = new StringBuilder();
	private 	ExportPosLog exportPosLog;
	
	// GUI Components
	private final Color BKGRND_COLOR = new Color(102, 204, 255);
	private final JPanel contentPane;
	private final JPanel btnsPanel;
	private final JButton exportBtn;
	private final JButton cancelBtn;
	private final JScrollPane catalogsScroll;
	private final JPanel catalogsPanel;
	private 	Plan[] catalogPlanes;
	private 	JCheckBox[] catalogChkbxs;
	private 	JTextField[] catalogComments;

	/**
	 *
	 * @throws IOException 
	 */
	ExportPosDialog(Aladin aladin) throws IOException {
		
		super(aladin.f, ModalityType.APPLICATION_MODAL);
		this.aladin = aladin;
		setTitle("Export to .pos format");
		setBackground(BKGRND_COLOR);
		setMinimumSize(new Dimension(710, 240));
		setMaximumSize(new Dimension(710, 600));
//		setResizable(false);
		
		contentPane = new JPanel();
//		contentPane.setMaximumSize(new Dimension(705, 600));
		contentPane.setBackground(BKGRND_COLOR);
		contentPane.setLayout(new GridLayout(0, 1));

		// Call method to create the dynamic content of the Panel
		try {
			catalogsPanel = packContentPane();			
			catalogsScroll = new JScrollPane(catalogsPanel);
//			int h = Math.min(400, aladin.calque.getNbUsedPlans() * 50 + 30);
//			catalogsScroll.setMaximumSize(new Dimension(705, 600));
			contentPane.add(catalogsScroll);
		} catch (IOException io) {
			throw io;
		}

		// TODO replace
		Aladin.makeAdd(this, contentPane, "Center");

		btnsPanel = new JPanel();
		btnsPanel.setBackground(BKGRND_COLOR);
		btnsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionPosDialogClose();
			}
		});
		btnsPanel.add(cancelBtn);

		exportBtn = new JButton("Export...");
		exportBtn.setEnabled(false);
		exportBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExportPos();
			}
		});
		btnsPanel.add(exportBtn);

		// TODO replace
		Aladin.makeAdd(this, btnsPanel, "South");

		pack();
	}

	/**
	 * Gathers the current view's catalog type planes into a JPanel.
	 * 
	 * @return JPanel
	 */
	private JPanel packContentPane() throws IOException {

		int planeCntr = 0;						// general plane counter
		int catalogCntr = 0; 					// catalog plane counter	
		int nb = aladin.calque.getNbUsedPlans();	// number current planes
		
		GridLayout g = new GridLayout(0, 1);
		JPanel posCatalogsPanel = new JPanel();
		posCatalogsPanel.setLayout(g);
		catalogComments = new JTextField[nb];
		catalogChkbxs = new JCheckBox[nb];
		catalogPlanes = new Plan[nb];			
		Plan[] allPlan = aladin.calque.getPlans();
		
		for (planeCntr = 0; planeCntr < allPlan.length; planeCntr++) {

			CatalogPanel panel;
			Plan catalogPlane;			
			
			catalogPlane = allPlan[planeCntr];				
			panel = new CatalogPanel(catalogPlane.label);							

			if (catalogPlane.type != Plan.CATALOG) {continue;}
			
			catalogPlanes[catalogCntr] = catalogPlane;	
			catalogComments[catalogCntr] = panel.getPosPlaneComment();
			catalogChkbxs[catalogCntr] = panel.getChckbxCatalog();				
			catalogChkbxs[catalogCntr].addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							checkBoxCallback();
				}
			});			
			posCatalogsPanel.add(panel);		
			catalogCntr++;	// increment the catalog plane counter
		}
		if (catalogCntr == 0) {
			throw new IOException();
		}
		// Set the attribute for the number of plans available from the 
		// current Save Panel
		catalogCount = catalogCntr;
		return posCatalogsPanel;
	}
	
	/**
	 * Responds if any of the check-boxes were clicked and updates exportBtn.
	 */
	private void checkBoxCallback() {
	
		// Iterate through the checkBoxes to determine if any are checked
		for (int k = 0; k < catalogCount; k++) {
			if (catalogChkbxs[k].isSelected()) {					
				// As soon as a checked box is found enable the button and
				// return
				exportBtn.setEnabled(true);
				return;
			}
		}
		// If it made it this far there aren't any checked checkBoxes
		exportBtn.setEnabled(false);
	}

	/**
	 * Responds when a user presses the button to export pos formatted data.
	 * 
	 */
	private void actionExportPos() {
							
		final JProgressBar progressBar;			
		final PosFileChooser fc = new PosFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	
		// In response to a button click:
		int retVal = fc.showSaveDialog(this);
		File outfile = fc.getSelectedFile();
	
		switch (retVal) {
	
			case JFileChooser.APPROVE_OPTION:
	
				// Give the user something to look at while exporting					
				progressBar = new JProgressBar(0, 100);
				progressBar.setIndeterminate(true);
				JPanel progressPanel = new JPanel();
				progressPanel.add(progressBar);
				progressBarFrame = new JFrame("Exporting .pos data");
				progressBarFrame.setDefaultCloseOperation(
													JFrame.EXIT_ON_CLOSE);
				// Create and set up the content pane.
				JComponent newContentPane = progressPanel;
				// content panes must be opaque
				newContentPane.setOpaque(true); 
				progressBarFrame.setContentPane(newContentPane);
				// Display the window.
				progressBarFrame.pack();
				progressBarFrame.setAlwaysOnTop(true);
				progressBarFrame.setVisible(true);
				progressBarFrame.setLocationRelativeTo(this);
				this.setCursor(Cursor.getPredefinedCursor(
												Cursor.WAIT_CURSOR));
	
				// For each catalog plane
				for (int i = 0; i < catalogCount; i++) {
					if (!catalogChkbxs[i].isSelected()) {continue;}

					// Gather the plane data to write to pos file
					String comment = catalogComments[i].getText() + '\n';
					String posData = votableToPos(
											catalogPlanes[i].getLabel());
					
					// Write the comment and pos formatted data to file
					try {
						DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(
								outfile, true)));
	
						// First char of a comment line must be '#'
						out.writeBytes("#");
						out.writeBytes(comment);
						out.writeBytes(posData);
						out.close();
					} catch (FileNotFoundException fnfe) {
						///////////////////////////////////////////////////
						// TODO
					} catch (IOException ioe) {
						///////////////////////////////////////////////////
						// TODO
						// errorFile=errorFile+"\n"+file;return false;}
						// aladin.log("export","catalog VOTABLE");
					}						
				}
				// Update the progress cues and display any warnings
				progressBarFrame.setVisible(false);
				this.setCursor(Cursor.getPredefinedCursor(
						Cursor.DEFAULT_CURSOR));

				if (!errLog.toString().isEmpty()) {
					exportPosLog = new ExportPosLog();
					exportPosLog.setLocationRelativeTo(this);
					exportPosLog.setLabel(
										"Export finished with warnings");
					exportPosLog.getBtnOk().setEnabled(true);
					exportPosLog.setAlwaysOnTop(true);
					exportPosLog.setVisible(true);
					exportPosLog.getTxtOutput().setText(errLog.toString());
				}	
				
				// Close the exportPosDialog window to finish the process
				actionPosDialogClose();
				break; // break case JFileChooser.APPROVE_OPTION
	
		case JFileChooser.CANCEL_OPTION:
			// Close JFileChooser.fc and return focus to its parent.
			break;
		default:
			break;
		} 
	}

	/**
	 * Parses a votable and gathers the data for mccs .pos files.
	 * 
	 * @param votable
	 * @return pos data
	 */
	private String votableToPos(String planeLabel) {
		
	    // TODO: Use Obj instead of VOTable; this is also poorly named as votableToPos()
	    
		StringBuilder posTable = new StringBuilder();
		
		// Create a votable object with the plane's data
		InputStream votStream = aladin.getVOTable(planeLabel);
		
		// Parse the votable
		SavotPullParser sb = new SavotPullParser(votStream,
				SavotPullEngine.SEQUENTIAL, "UTF-8");
	
		// Get the next resource of the VOTable file
		SavotResource currentResource = sb.getNextResource();
	
		// While a resource is available
		while (currentResource != null) {
			
			// For each table of this resource
			for (int i = 0; i < currentResource.getTableCount(); i++) {
				int ra_col = -1;
				int dec_col = -1;
				ArrayList<Integer> nameIndexes = new ArrayList<Integer>();
	
				FieldSet fs = currentResource.getFieldSet(i);
				// For each field of this table
				for (int f = 0; f < fs.getItemCount(); f++) {
					SavotField field = (SavotField) fs.getItemAt(f);
					String ucd = field.getUcd().toLowerCase();
	
					if (ucd.equals("pos.eq.ra;meta.main")) {
						ra_col = f;
					} else if (ucd.equals("pos.eq.dec;meta.main")) {
						dec_col = f;
					} else if ((ucd.equals("meta.id;meta.main"))
							|| (ucd.equals("meta.id.part;meta.main"))) {
						nameIndexes.add(f);						
					}
				}				
				// If the ra, dec, and/or name aren't found
				// alert the user and skip the current table
				if ( (ra_col < 0) || (dec_col < 0) || 
						(nameIndexes.size() < 1) ) {
					progressBarFrame.setVisible(false);
					JOptionPane.showMessageDialog(this,
							"Couldn't find all required column data from a "
							+ "table in: " 
							+ currentResource.getName() + '\n' 
							+ "Data from this table will not be exported",
							"Missing Metadata Error!",
							JOptionPane.ERROR_MESSAGE);	
					progressBarFrame.setVisible(true);		
					continue;
				}	
				TRSet tr = currentResource.getTRSet(i);
				if (tr == null) {
					///////////////////////////////////////////////////////
					// TODO this is an extreme error
					// TODO Need to delete temp_votable?
					continue;						
				}				
				// For each row of the table	
				for (int j = 0; j < tr.getItemCount(); j++) {
					
					// Data from this row
					TDSet 		TDs = tr.getTDSet(j); 
					Astrocoo 	ac;
	
					// Variables for this row's .pos file data
					String 		astroObjName;
					String		COORDSYS = "J2000";	
					
					// Put the full name together if it's in parts
					StringBuilder nameBuilder = new StringBuilder();
					for (int a = 0; a < nameIndexes.size(); a++) {
						nameBuilder.append(TDs.getContent(nameIndexes
								.get(a)) + "-");
					}
					// TODO clean this up
					// Delete the last '-' and cast into a String
					nameBuilder.deleteCharAt(nameBuilder.length() - 1);
					astroObjName = SOFIA_Aladin.formatName(
												nameBuilder.toString());
	
					// An Astrocoo instance can parse different kinds
					// of formatted coord strings and return the coords
					// in different formats as well. Using them
					// for consistency among coordinates and possibly to
					// use more of their functionality in the future.
					ac = SOFIA_Aladin.createAstrocoo(
									TDs.getContent(ra_col).toString(),
									TDs.getContent(dec_col).toString());
					
					// Check that the resulting strings are viable 
					// MCCS pos coordinates.  
					// If not add info to errLog and skip this row			
					String regex = SOFIA_Aladin.Regex.RAHMS.getValue() +
									SOFIA_Aladin.Regex.DECDMS.getValue();
					if (ac.toString(Astroformat.SEXA3h).matches(regex)) {
						String[] sexCoords; // Coords in sexagesimal
						
						// Get the hmsdms format from the AstroCoo and
						// split it into 2 strings; ra & dec
						sexCoords = ac.toString(9).split("s", 2);
						// Replace the 's' stripped from the ra coord
						sexCoords[0] = sexCoords[0] + 's';		
						
						// Append this row's data to the StringBuilder
						posTable.append(astroObjName 	+ '\t'
										+ sexCoords[0] 	+ '\t' 
										+ sexCoords[1] 	+ '\t'
										+ COORDSYS 		+ '\n');						
					} else {
						errLog.append("Warning:  The coords for " + astroObjName 
									+ " (" + ac.toString(9) + ") "
									+ "aren't properly formatted and "
									+ "weren't included in the output.\n");		
						continue;
					}	
				} 
			} 		
			// Get the next resource
			currentResource = sb.getNextResource();					
		} 		
		return posTable.toString();
	}

	/**
	 * Responds when a user presses the Cancel button to close this Dialog. 
	 * 
	 */
	private void actionPosDialogClose() {
		this.dispose();
	}

	/**
	 * An extended JFileChooser for files with a ".pos" extension
	 * @author swatters@sofia.usra.edu
	 *
	 */
	@SuppressWarnings("serial")
	private class PosFileChooser extends JFileChooser {  

		private String extension;
		  
		PosFileChooser() {
		    super();
		    extension = "pos";
		}
		
		/**
		 */
		@Override 
		public File getSelectedFile() {
		    File selectedFile = super.getSelectedFile();

		    if (selectedFile != null) {
		    	String name = selectedFile.getName();
		    	
	    		if (!name.endsWith("." + this.extension.toLowerCase())) {
	    			// TODO If it does have a non-pos extension strip it before 
	    			// appending .pos
	    			selectedFile = new File(selectedFile.getParentFile(), 
							name + '.' + this.extension);
	    		} 
		    }
    		return selectedFile;
		}
		
		/**
		 * 
		 */
		@Override
		public void approveSelection() {
			if (getDialogType() == SAVE_DIALOG) {
				File selectedFile = getSelectedFile();
									
				if ( (selectedFile != null) && (selectedFile.exists()) ) {
					int response = JOptionPane.showConfirmDialog(this
									, "The file " + selectedFile.getName()
										+ " already exists. Do you want to"
										+ " replace the existing file?"
									, "Ovewrite file"
									, JOptionPane.YES_NO_OPTION
									, JOptionPane.WARNING_MESSAGE);
					if (response != JOptionPane.YES_OPTION) {
						return;
					}
					// TODO handle exceptions
					selectedFile.delete();
				}
		    }
		    super.approveSelection();
		}	
	} // end PosFileChooser inner class of ExportPosDialog
}