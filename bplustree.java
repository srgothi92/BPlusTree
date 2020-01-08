import java.io.*;
import java.util.*;
/**
 * A java Application to test b+ tree.
 * @author Shaileshbhai Gothi
 *
 */
public class bplustree {
	private static final String OUTPUT_FILENAME = "output_file.txt";
/**
 * Reads the input file and instantiates a B+ Tree based on provided input, and writes output to file.
 * @param args The input file name
 */
	public static void main(String[] args) {
		Scanner inputScanner = null;
		try {
			String inputFileName = args.length > 0 ? args[0] : "input.txt";
			File oInputFile = new File(inputFileName);
			inputScanner = new Scanner(oInputFile);
			File oOutputFile = new File(OUTPUT_FILENAME);
			BufferedWriter outputBufferWriter = new BufferedWriter(new FileWriter(oOutputFile));

			BPlusTreeImpl oBPlusTree = null;
			while (inputScanner.hasNextLine()) {
				String newLine = inputScanner.nextLine();
				String[] input = newLine.split("\\(|,|\\)");
				if (input[0].contains("Initialize")) {
					oBPlusTree = new BPlusTreeImpl(Integer.parseInt(input[1].trim()));
				} else if (input[0].contains("Insert")) {
					oBPlusTree.insert(Integer.parseInt(input[1]), Double.parseDouble(input[2].trim()));
				} else if (input[0].contains("Search")) {
					ArrayList<Double> listValues = null;
					if (input.length == 3) {
						listValues = oBPlusTree.search(Integer.parseInt(input[1].trim()), Integer.parseInt(input[2].trim()));
					} else if (input.length == 2) {
						listValues = oBPlusTree.search(Integer.parseInt(input[1].trim()));
					}
					if (listValues != null) {
						writeToFile(outputBufferWriter, listValues);
					}
				} else if (input[0].contains("Delete")) {
					oBPlusTree.delete(Integer.parseInt(input[1].trim()));
				}
			}
//			oBPlusTree.printBPlusTree();
			outputBufferWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputScanner != null) {
				inputScanner.close();
			}
		}

	}
/**
 * Writes search values to output file.
 * @param outputBufferWriter Buffer writer for output file.
 * @param listValues List of values found in search.
 * @throws IOException
 */
	private static void writeToFile(BufferedWriter outputBufferWriter, ArrayList<Double> listValues)
			throws IOException {
		String values = Arrays.toString(listValues.toArray());
		values = values.replaceAll("\\[|\\s|\\]", "");
		if (values.length() == 0) {
			values = "Null";
		}
		outputBufferWriter.write(values);
		outputBufferWriter.newLine();
	}

}
