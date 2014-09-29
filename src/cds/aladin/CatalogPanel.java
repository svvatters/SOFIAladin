package cds.aladin;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;
import java.awt.Insets;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.Dimension;

/**
 * @author shannon.watters@gmail.com
 *
 */
final class CatalogPanel extends JPanel {

	private final JCheckBox chckbxCatalog;
	private final JTextField posPlaneComment;
	private final JLabel lblComment;
	
	/**
	 * Create the panel.
	 */
	CatalogPanel(String label) {
		setSize(new Dimension(870, 75));
		setMinimumSize(new Dimension(870, 75));
		setMaximumSize(new Dimension(900, 85));
		setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		setBackground(new Color(102, 204, 255));
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), label, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		lblComment = new JLabel("Comment (limit 79 characters)");
		GridBagConstraints gbc_lblComment = new GridBagConstraints();
		gbc_lblComment.insets = new Insets(0, 0, 5, 0);
		gbc_lblComment.gridx = 1;
		gbc_lblComment.gridy = 0;
		add(lblComment, gbc_lblComment);
		
		chckbxCatalog = new JCheckBox("");
		chckbxCatalog.setToolTipText("Select catalog for export");
		GridBagConstraints gbc_chckbxCatalog = new GridBagConstraints();
		gbc_chckbxCatalog.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxCatalog.gridx = 0;
		gbc_chckbxCatalog.gridy = 1;
		add(chckbxCatalog, gbc_chckbxCatalog);
		
//		comboBox = new JComboBox();
//		comboBox.setBackground(new Color(204, 255, 255));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
//		add(comboBox, gbc_comboBox);
		
		posPlaneComment = new JTextField(
						new FixedLengthPlainDocument(79), "", 79);
		posPlaneComment.setColumns(79);
		posPlaneComment.setToolTipText("Enter user comment to be included in export");
		posPlaneComment.setFont(new Font("Monospaced", Font.PLAIN, 14));
		posPlaneComment.setName("");
		posPlaneComment.setBackground(new Color(204, 255, 255));
		posPlaneComment.setText(label);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 1;
		add(posPlaneComment, gbc_formattedTextField);

	}
	
	/**
	 * @return the chckbxCatalog
	 */
	JCheckBox getChckbxCatalog() {
		return chckbxCatalog;
	}

	/**
	 * @return the posPlaneComment
	 */
	JTextField getPosPlaneComment() {
		return posPlaneComment;
	}

	/**
	 * @return the lblComment
	 */
	JLabel getLblComment() {
		return lblComment;
	}

	class FixedLengthPlainDocument extends PlainDocument {   

		private int maxlength;

		// This creates a Plain Document with a maximum length called maxlength.   
		FixedLengthPlainDocument(int maxlength) {   
			this.maxlength = maxlength;   
		}

		/**
		 *  This is the method used to insert a string to a Plain Document.	
		 */
		@Override 
		public void insertString(int offset, String str, AttributeSet a) throws
			BadLocationException {   
					
			// If the current length of the string
			// + the length of the string about to be entered 
			//(through typing or copy and paste)
			// is less than the maximum length passed as an argument..
			// We call the Plain Document method insertString.
			// If it isn't, the string is not entered.
					
			if (!((getLength() + str.length()) > maxlength)) {   
				super.insertString(offset, str, a);   
			}
		}
	}
}
