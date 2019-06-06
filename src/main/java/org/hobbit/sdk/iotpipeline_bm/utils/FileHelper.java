package org.hobbit.sdk.iotpipeline_bm.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FileHelper {

	/**
	 * A recursive method for collecting a list of all files with a given file
	 * extension. Will scan all sub-folders recursively.
	 *
	 * @param startFolder - where to start from
	 * @param collectedFilesList - collected list of <File>
	 * @param fileExtFilter
	 * @param recurseFolders
	 * @throws IOException
	 */

	Logger logger = LoggerFactory.getLogger(FileHelper.class);


	//--------------------------------------------------------------------------------------------
	public void collectFilesList(String startFolder, List<File> collectedFilesList, String fileExtFilter, boolean recurseFolders) throws IOException {
		File file = new File(startFolder);
		File[] filesList = file.listFiles();

		for (File f : filesList) {
			if (f.isDirectory() && recurseFolders) {
				collectFilesList(f.getAbsolutePath(), collectedFilesList, fileExtFilter, recurseFolders);
			} else //no filter
				if (fileExtFilter.isEmpty() || fileExtFilter.equals("*")) {
					collectedFilesList.add(f);
				} else if (fileExtFilter.equalsIgnoreCase(getFileExtension(f))) {
					collectedFilesList.add(f);
				}
		}
	}


	//--------------------------------------------------------------------------------------------
	public String getFileExtension(File f) {
		String fileName = f.getName();
		String fileExtension = fileName;

		int lastPos = fileName.lastIndexOf('.');

		if (lastPos > 0 && lastPos < (fileName.length() - 1)) {
			fileExtension = fileName.substring(lastPos + 1).toLowerCase();
		}

		return fileExtension;
	}


	//--------------------------------------------------------------------------------------------

	public void removeDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				for (File aFile : files) {
					removeDirectory(aFile);
				}
			}
			dir.delete();
		} else {
			dir.delete();
		}
	}


//--------------------------------------------------------------------------------------------

	/*
	read a file from resources folder
	 */
	public String getFileWithUtil(String fileName, Class classLoader) {

		String result = "";

		//ClassLoader classLoader = getClass().getClassLoader();  // DO IT FROM CALLING CLASS AND SEND classLoader as parameter to this method
		try {
			result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}


	//--------------------------------------------------------------------------------------------
	/*
	read a file from resources folder
	param file full path under resources dir e.g. REFITPowerData111215/dataset!House1.csv
	returns the file contents in String format
	 */
	public String getFile(String fileName, Class classLoader) {

		StringBuilder result = new StringBuilder("");

		//Get file from resources folder
		//  ClassLoader classLoader = getClass().getClassLoader();   //  do it in calling class
		File file = new File(classLoader.getResource(fileName).getFile());

		try (Scanner scanner = new Scanner(file)) {

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}

			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}


	//--------------------------------------------------------------------------------------------
	public List<String> listFilesForFolder(final File folder) {


		// final File folder = new File("/home/you/Desktop"); // in calling class

		List<String> filenames = new LinkedList<String>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.getName().contains(".csv"))
					filenames.add(fileEntry.getName());
			}
		}

		return filenames;
	}


	//--------------------------------------------------------------------------------------------
	public void iterateOverFilesInsideFolder() {

		String path = "data/sf1/dataset";
		File[] files = new File(path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File path) {
				if (path.isFile()) {
					//Do something with a single file
					//or just return true to put it in the list
					return true;
				}
				return false;
			}
		});
	}

	//--------------------------------------------------------------------------------------------
	public void csvHandler() throws IOException {

		BufferedReader reader = Files.newBufferedReader(Paths.get("datafile1.csv"));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("SensorId", "Appliance1").withIgnoreHeaderCase().withTrim());
		for (CSVRecord csvRecord : csvParser) {
			// Accessing Values by Column Index
			String name = csvRecord.get(0);
			//Accessing the values by column header name
			String fees = csvRecord.get("Appliance1");
		}
	}

	/*
	comment: will not work once the project is packaged into jar;
	*java7
	* @param fileLocation  : URI fileLocation = this.getClass().getClassLoader().getResource(FILE_PATH).toURI();
	*  @return the file's contents
	* */
	public String readFileIntoStringByBytes(String fileLocation, Charset encoding)
			throws IOException {
		if (encoding == null) {
			encoding = Charsets.UTF_8;
		}
		Path path = Paths.get(fileLocation);
		List<String> file = Files.readAllLines(path,
				Charsets.UTF_8);
		String fileContents = file.get(0);
		return fileContents;
	}

	/**
	 * will not work once the project is packaged into jar;
	 * java7
	 * @param : URI fileLocation = this.getClass().getClassLoader().getResource(FILE_PATH).toURI();
	 *  @return the file's contents
	 */
	public String readFileIntoStringByLines(String fileLocation, Charset encoding)
			throws IOException {
		if (encoding == null) {
			encoding = Charsets.UTF_8;
		}
		Path path = Paths.get(fileLocation);
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, encoding);
		// alternatively: String stringFromFile = new String( java.nio.file.Files.readAllBytes(path) ); return stringFromFile;
	}

	/**
	 * Reads given resource file as a string.
	 * Apache Commons
	 * @param : URI fileLocation = this.getClass().getClassLoader().getResource(FILE_PATH).toURI();
	 * @return the file's contents or null if the file could not be opened
	 */
	public String getResourceFileAsStringWithFileUtils(String fileLocation) throws IOException {

		File file = new File(fileLocation);
		String fileContents = FileUtils.readFileToString(file);
		return  fileContents;
	}

	/**
	 * Reads given resource file as a string.
	 * Apache Commons
	 * @param : file Location relatove to resources folder e.g. scripts/docker/forwardToPipeline.sh (the nested path is relative to the root of the classloader)
	 * @return the file's contents or null if the file could not be opened
	 */
	public String getResourceFileAsStringWithIOUtils(String fileLocation) throws IOException {

		String result = "";

		ClassLoader classLoader = getClass().getClassLoader();
		try {
			result = IOUtils.toString(classLoader.getResourceAsStream(fileLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}



	/**
	 * Reads given resource file as a string.
	 * Java8
	 * @param fileLocation the rel path to the resource file
	 * @return the file's contents or null if the file could not be opened
	 */
	public String getResourceFileAsString(String fileLocation) throws IOException {

		InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileLocation);
		if (is != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String str=  reader.lines().collect(Collectors.joining(System.lineSeparator()));
			is.close();
			return str;
		}
	logger.error("could not read file");
		return null;
	}


	/**
	 * Reads given resource file as a string. works either for filsesystem and jar.
	 * Java8
	 */
	public String getResourceFileAsStringUniversal(String fileName, String content) throws IOException {
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
		if (is != null) {
			BufferedReader resReader = new BufferedReader(new InputStreamReader(is));
			content = resReader.lines().collect(Collectors.joining());
		}
		return content;
	}
}
//--------------------------------------------------------------------------------------------
/*

	// load a file from resource folder
	InputStream inputStream = ClassLoaderUtil.getResourceAsStream("temp.csv", YourCallingClass.class);
	InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
	BufferedReader reader = new BufferedReader(streamReader);
for(String line;(line=reader.readLine())!=null;){
		// Process line
		}}
*/


//--------------------------------------------------------------------------------------------
/*
read file from resource folder

URL file = this.getClass().getResource("keywords.txt");
Scanner s = new Scanner(new File(file.getFile()));
 */





/*
read a file from resource within a jar

InputStream in = getClass().getResourceAsStream("/file.txt");
BufferedReader reader = new BufferedReader(new InputStreamReader(in));

or

ClassLoader classLoader = getClass().getClassLoader();
InputStream in = classLoader.getResourceAsStream(fileName);
BufferedReader br = new BufferedReader(new InputStreamReader(in));

or

private static void loadLib(String path, String name) {
  name = System.mapLibraryName(name); // extends name with .dll, .so or .dylib
  try {
        InputStream in = ACWrapper.class.getResourceAsStream("/"+path + name);
        File fileOut = new File("your lib path");
        OutputStream out = FileUtils.openOutputStream(fileOut);
        IOUtils.copy(in, out);
        in.close();
        out.close();
        System.load(fileOut.toString());//loading goes here
   } catch (Exception e) {
               //handle
   }
}

 */



// create dir

		/*
		datasetFolderName = "dataset";
		pathToDatasetFolder = "/sensemark2/data/" + datasetFolderName;
		Path path = Paths.get(pathToDatasetFolder);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(Paths.get(pathToDatasetFolder));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/



		/*  loading file at top level of jar or src/main/resources

		 try {
         final String path;
         if(args.length == 1) path = args[0].trim();
         else path = "etc/application.properties";

         final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
         if(is == null) throw new RuntimeException("Failed to load " + path + " as a resource");
         else System.out.printf("Loaded resource from path: %s\n", path);
      } catch(Exception e) {
         e.printStackTrace();
      }
   }
		 */


		// System.out.println(System.getProperty("user.dir"));  gives current dir