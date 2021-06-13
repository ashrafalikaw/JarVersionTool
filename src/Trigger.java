import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import net.miginfocom.swing.MigLayout;

public class Trigger {

	static String pomLocation = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		final JFrame frame = new JFrame("Latest Dependency Version Reproting Tool");
		final JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		JLabel label = new JLabel("Latest Dependency Version Reporting Tool");
		JLabel label1 = new JLabel("Provide the pom.xml file path location here:");
		label.setFont(new Font("Times New Roman", 30, 30));
		label1.setFont(new Font("Times New Roman", 30, 20));

		JButton button = new JButton("Execute");
		button.setBackground(Color.white);
		// panel.setLayout(new FloLayout());
		final JTextField textField = new JTextField(60);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mvnBuild(textField.getText());
				} catch (IOException el) {
					el.printStackTrace();
				}
			}

		});
		panel.setBorder(new LineBorder(Color.BLACK));
		panel.add(label, "newline, center");
		panel.add(label1, "newline 100px");
		panel.add(textField, "newline");
		panel.add(button, "newline, center");
		panel.setBackground(Color.LIGHT_GRAY);
		frame.setResizable(false);
		frame.add(panel);
		frame.add(panel);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		// frame.dispose();
		// frame.dispatchEvent(new WindowEvent (frame, WindowEvent.WINDOW_CLOSING));
	}

	static void mvnBuild(String pomLocation) throws IOException {
		// System.setOut(new PrintStream(new FileOutputStream("consoleLogg.txt")));
		String updatedPomLocation = pomLocation.replace("\\", "/");
		String startString = "The following dependencies in Dependencies";
		String endString = "BUILD SUCCESS";
		//String endString = "No dependencies in pluginManagment of plugins have newer versions";
		//String endString1 = "Reactor Summary:";
		String flag = "N";
		int flag1;
		System.out.flush();
		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("consoleLog.txt")), true));
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(updatedPomLocation));
		request.setGoals(Arrays.asList("versions:display-dependency-updates"));
		// request.setGoals(Arrays.asList("clean"));

		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File(System.getenv().get("MAVEN_HOME")));
		try {
			//invoker.execute(request);
			System.out.println(invoker.execute(request));
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new FileReader("consoleLog.txt"));
		String line, line1, txtSearch, txtIteration;
		FileWriter fw = new FileWriter("latestJarVersionDetails.txt");
		BufferedReader br1 = null;
		BufferedWriter bw = new BufferedWriter(fw);
		while ((line = br.readLine()) != null) {
			if (line.contains(startString)) {
				flag = "Y";
				String removedLine = line.replace("[INFO]", "");
				bw.write(removedLine);
				bw.newLine();
				while ((line1 = br.readLine()) != null) {
					flag1 = 0;
					if (!line1.contains(endString)) {
						txtSearch = line1;
						br1 = new BufferedReader(new FileReader("latestJarVersionDetails.txt"));
						while ((txtIteration = br1.readLine()) != null) {
							if (txtIteration.contains(txtSearch)) {
								flag1++;
							}
						}
						if (flag1 == 0) {
							String removedLine1 = line1.replace("[INFO]", "");
							bw.write(removedLine1);
							bw.newLine();
							bw.flush();
						}
					} else {
						break;
					}
				}
			}
			if (flag.equalsIgnoreCase("Y")) {
				break;
			}
		}
		bw.close();
		br1.close();
		br.close();
		// bw.flush();
		/*
		 * FileWriter fw - new FileWriter("latestJarDetails.txt"); BUfferedWriter bw =
		 * new BufferedWriter(fw); bw.write(pomLocation); bw.newLine(); bw.flush();
		 * bw.close();
		 */
	}

}
