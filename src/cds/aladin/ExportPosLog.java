package cds.aladin;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shannon.watters@gmail.com
 * @SOFIAladin-extension class
 */
final class ExportPosLog extends JFrame {

	private JPanel 		contentPane;
	private	JTextArea 	txtOutput;
	private	JButton 	btnOk;
	private	String		label = "Exporting...";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExportPosLog frame = new ExportPosLog();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	ExportPosLog() {
		setBackground(new Color(0, 153, 255));
		setMinimumSize(new Dimension(710, 250));
		setType(Type.POPUP);
		setTitle("Export pos");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 710, 375);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(102, 204, 255));
		contentPane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), label, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 108, 0};
		gbl_contentPane.rowHeights = new int[]{228, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		txtOutput = new JTextArea();
		scrollPane.setViewportView(txtOutput);
		txtOutput.setLineWrap(true);
		txtOutput.setWrapStyleWord(true);
		txtOutput.setTabSize(4);
		txtOutput.setColumns(80);
		txtOutput.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		txtOutput.setFont(new Font("Monaco", Font.PLAIN, 14));
		txtOutput.setBackground(new Color(204, 255, 255));
		txtOutput.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		txtOutput.setEditable(false);
		
		setBtnOk(new JButton("OK"));
		getBtnOk().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				actionCloseFrame();
			}

		});
		
		getBtnOk().setEnabled(false);
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.gridx = 1;
		gbc_btnOk.gridy = 1;
		contentPane.add(getBtnOk(), gbc_btnOk);
	}
	
	void actionCloseFrame() {
		this.dispose();		
	}

	/**
	 * @return the contentPane
	 */
	public JPanel getContentPane() {
		return contentPane;
	}

	/**
	 * @param contentPane the contentPane to set
	 */
	void setContentPane(JPanel contentPane) {
		this.contentPane = contentPane;
	}

	/**
	 * @return the txtOutput
	 */
	JTextArea getTxtOutput() {
		return txtOutput;
	}

	/**
	 * @param txtOutput the txtOutput to set
	 */
	void setTxtOutput(JTextArea txtOutput) {
		this.txtOutput = txtOutput;
	}

	/**
	 * @return the label
	 */
	String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	void setLabel(String label) {
		contentPane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), label, TitledBorder.LEADING, TitledBorder.TOP, null, null));		
	}

	JButton getBtnOk() {
		return btnOk;
	}

	void setBtnOk(JButton btnOk) {
		this.btnOk = btnOk;
	}

}
