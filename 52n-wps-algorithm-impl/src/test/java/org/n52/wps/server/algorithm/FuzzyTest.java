package org.n52.wps.server.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuzzyTest {

	private static Logger LOGGER = LoggerFactory.getLogger(FuzzyTest.class);

	private String pythonName = "python.exe";

	private String command;

	private String pythonHome;

	private char fileSeparator = File.separatorChar;

	private String fuzzyHome;
	
	private String name1;
	
	private String name2;
	
	private final String lineSeparator = System.getProperty("line.separator");
	public static final String OS_Name = System.getProperty("os.name");

	public FuzzyTest() {

		pythonHome = "c:\\Python27";
		fuzzyHome = "d:\\tmp";
		name1 = "test";
		name2 = "rest";
		
		if (!OS_Name.startsWith("Windows")) {
			pythonName = "python";
		}

		executeFuzzyWuzzy();
	}

	private String getCommand() {

		if (command == null) {
			command = getPythonHome() + fileSeparator + pythonName + " "
					+ fuzzyHome + fileSeparator + "fuzz.py " + name1 + " " + name2;
		}

		return command;
	}

	private String getPythonHome() {
		return pythonHome;
	}

	private void executeFuzzyWuzzy() {

		try {

			LOGGER.info("Executing FuzzyWuzzy with " + name1 + " and " + name2);

			Runtime rt = Runtime.getRuntime();

			Process proc = rt.exec(getCommand());

			PipedOutputStream pipedOut = new PipedOutputStream();

			PipedInputStream pipedIn = new PipedInputStream(pipedOut);
			
			PipedOutputStream pipedOut1 = new PipedOutputStream();
			
			PipedInputStream pipedIn1 = new PipedInputStream(pipedOut1);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERROR", pipedOut);

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUTPUT", pipedOut1);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// fetch errors if there are any
			BufferedReader errorReader = new BufferedReader(
					new InputStreamReader(pipedIn));
			
			// fetch errors if there are any
			BufferedReader outputReader = new BufferedReader(
					new InputStreamReader(pipedIn1));

			String line = errorReader.readLine();

			String errors = "";

			while (line != null) {

				errors = errors.concat(line + lineSeparator);

				line = errorReader.readLine();
			}
			
			line = outputReader.readLine();

			String output = "";

			while (line != null) {

				output = output.concat(line + lineSeparator);

				line = outputReader.readLine();
			}

			System.out.println(output);
			
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} finally {
				proc.destroy();
			}

			if (!errors.equals("")) {
				
				System.err.println(errors);
				
//				String baseDir = WebProcessingService.BASE_DIR + File.separator
//						+ "GRASS_LOGS";
//				File baseDirFile = new File(baseDir);
//				if (!baseDirFile.exists()) {
//					baseDirFile.mkdir();
//				}
//				String host = WPSConfig.getInstance().getWPSConfig()
//						.getServer().getHostname();
//				if (host == null) {
//					host = InetAddress.getLocalHost().getCanonicalHostName();
//				}
//				String hostPort = WPSConfig.getInstance().getWPSConfig()
//						.getServer().getHostport();
//				File tmpLog = new File(tmpDir + fileSeparator + uuid
//						+ logFilename);
//				File serverLog = new File(baseDir + fileSeparator + uuid
//						+ logFilename);
//
//				if (tmpLog.exists()) {
//					FileInputStream fis = new FileInputStream(tmpLog);
//					FileOutputStream fos = new FileOutputStream(serverLog);
//					try {
//						byte[] buf = new byte[1024];
//						int i = 0;
//						while ((i = fis.read(buf)) != -1) {
//							fos.write(buf, 0, i);
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					} finally {
//						if (fis != null)
//							fis.close();
//						if (fos != null)
//							fos.close();
//					}
//
//				} else {
//					BufferedWriter bufWrite = new BufferedWriter(
//							new FileWriter(serverLog));
//					bufWrite.write(errors);
//					bufWrite.flush();
//					bufWrite.close();
//				}
//				LOGGER.error("An error occured while executing the GRASS GIS process.");
//				throw new RuntimeException(
//						"An error occured while executing the GRASS GIS process. See the log under "
//								+ "http://" + host + ":" + hostPort + "/"
//								+ WebProcessingService.WEBAPP_PATH
//								+ "/GRASS_LOGS/" + uuid + logFilename
//								+ " for more details.");
			}

		} catch (IOException e) {
			LOGGER.error(
					"An error occured while executing the FuzzyWuzzy process.",
					e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new FuzzyTest();

	}

}
