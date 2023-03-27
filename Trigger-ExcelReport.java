import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Trigger {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String startString = "The following dependencies in Dependencies";
		String endString = "No denpendencies in pluginManagement of plugin have newer versions";
		String endString1 = "BUILD SUCCESS";
		String path = args.toString();
		String flag = "N";
		System.out.flush();
		System.setOut(
				new PrintStream(
						new BufferedOutputStream(
								new FileOutputStream("consoleLog.txt")
								), true)
				);
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File("F:/Java Projects/sampleMavenProject/sample-project-maven-master"));
		request.setGoals(Arrays.asList("versions:display-dependency-updates"));
		//request.setGoals(Array.asList("clean"));
		
		Invoker invoker = new DefaultInvoker();
		try {
			//invoker.execute(request);
			System.out.println(invoker.execute(request));
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader( new FileReader("consoleLog.txt"));
		String line, line1;
		FileWriter fw = new FileWriter("latestJarVersionDetails.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		while ((line = br.readLine()) != null) {
			if (line.contains(startString)) {
				flag = "Y";
				String removedLine = line.replace("[INFO]", "");
				bw.write(removedLine);
				bw.newLine();
				while ((line1 = br.readLine()) != null) {
					if(!line1.contains(endString)) {
						if(!line.contains(endString1)) {
							String removedLine1 = line1.replace("[INFO]", "");
							bw.write(removedLine1);
							bw.newLine();
						}
					} else {
						break;
					}
				}
			} if (flag.equalsIgnoreCase("Y") ) {
				break;
			}
		}
		br.close();
		//bw.flush();
		bw.close();
		
		//Excel sorcery starts here
		
		//Blank workBook
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		//create a blank sheet
		XSSFSheet sheet = workbook.createSheet("Jar Data");
		
		//This data needs to be writter (Object [])
		Map<String, Object[]> data = new TreeMap<String, Object[]>();
		data.put("1", new Object[] {"Jar Name", "Existing Version", "Latest Version" });
		
		BufferedReader br1 = new BufferedReader(new FileReader("latestJarVersionDetails.txt"));
		String lineMain, artifactName, oldVersion, newVersion;
		String [] str1;
		String [] str2;
		String remainder, finalArtifact;
		String[] artifactArr;
		int i = 2;
		String openLine = "The following dependencies";
		String closeLine = "artifact com.wellsfargo.rmol";
		String k;
		
		try {
			while ((lineMain = br1.readLine()) != null && !lineMain.trim().isEmpty()) {
				if (lineMain.startsWith(closeLine)) {
					break;
				}
				System.out.println("Line is: "+ lineMain);
				if (!lineMain.contains(openLine)) {
					if (lineMain.endsWith(".")) {
						System.out.println("inside IF");
						lineMain = lineMain + " " + br1.readLine().trim();
					}
					System.out.println("New line is " + lineMain);
					str1 = lineMain.split("\\.\\. ");
					System.out.println("Length is: " + str1.length);
					artifactName = str1[0];
					remainder = str1[1];
					System.out.println("Artifact name: " + artifactName);
					System.out.println("Remainder: "+ remainder);
					artifactArr = artifactName.split(" \\.");
					finalArtifact = artifactArr[0];
					System.out.println("Final Artifact " + finalArtifact);
					str2 = remainder.split("->");
					oldVersion = str2[0];
					newVersion = str2[1];
					StringBuilder sb = new StringBuilder();
					sb.append(i);
					String strI = sb.toString();
					data.put(strI, new Object[] { finalArtifact, oldVersion, newVersion });
					i++;
				}
			}
		// Iterate over data and write to sheet
			Set<String> keyset = data.keySet();
			
			int rownum = 0;
			for (String key : keyset ) {
				// create a row of excelsheet
				Row row = sheet.createRow(rownum++);
				
				//get object array of particular key
				Object[] objArr = data.get(key);
				
				int cellnum = 0;
				for (Object obj : objArr) {
					Cell cell = row.createCell(cellnum++);
					if(obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Integer) {
						cell.setCellValue((Integer) obj);
					}
				}
			}
			try {
				//Write the workbook in file system
				FileOutputStream out = new FileOutputStream( new File("latestJarVersionReport.xlsx"));
				workbook.write(out);
				out.close();
				System.out.println("xlsx written successfully on disk");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
