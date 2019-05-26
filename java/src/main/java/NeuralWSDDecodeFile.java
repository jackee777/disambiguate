import getalp.wsd.common.wordnet.WordnetHelper;
import getalp.wsd.method.neural.NeuralDisambiguator;
import getalp.wsd.ufsac.core.Sentence;
import getalp.wsd.ufsac.core.Word;
import getalp.wsd.ufsac.utils.CorpusPOSTaggerAndLemmatizer;
import getalp.wsd.utils.ArgumentParser;
import getalp.wsd.utils.WordnetUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class NeuralWSDDecodeFile
{
    public static void main(String[] args) throws Exception
    {
        ArgumentParser parser = new ArgumentParser();
        parser.addArgument("python_path");
        parser.addArgument("data_path");
        parser.addArgumentList("weights");
	parser.addArgument("input_file");
	parser.addArgument("output_file");
        parser.addArgument("lowercase", "true");
        parser.addArgument("sense_reduction", "true");
        if (!parser.parse(args)) return;

        String pythonPath = parser.getArgValue("python_path");
        String dataPath = parser.getArgValue("data_path");
	String input_filename = parser.getArgValue("input_file");
	String output_filename = parser.getArgValue("output_file");
        List<String> weights = parser.getArgValueList("weights");
        boolean lowercase = parser.getArgValueBoolean("lowercase");
        boolean senseReduction = parser.getArgValueBoolean("sense_reduction");

        CorpusPOSTaggerAndLemmatizer tagger = new CorpusPOSTaggerAndLemmatizer();
        NeuralDisambiguator disambiguator = new NeuralDisambiguator(pythonPath, dataPath, weights);
        disambiguator.lowercaseWords = lowercase;
        if (senseReduction) disambiguator.reducedOutputVocabulary = WordnetUtils.getReducedSynsetKeysWithHypernyms3(WordnetHelper.wn30());
        else disambiguator.reducedOutputVocabulary = null;

	try 
	{
		FileReader filereader = new FileReader(input_filename);
		FileWriter filewriter = new FileWriter(output_filename);

		BufferedReader reader = new BufferedReader(filereader);

		for (String line = reader.readLine() ; line != null ; line = reader.readLine())
        	{
			Sentence sentence = new Sentence(line);
            		tagger.tag(sentence.getWords());
            		disambiguator.disambiguate(sentence, "wsd");
            		for (Word word : sentence.getWords())
            		{
                		filewriter.write(word.getValue().replace("|", ""));
                		if (word.hasAnnotation("lemma") && word.hasAnnotation("pos") && word.hasAnnotation("wsd"))
                		{
                    			filewriter.write("|" + word.getAnnotationValue("wsd"));
                		}
                		filewriter.write(" ");
            		}
            		filewriter.write("\n");
		}
		
		filereader.close();
        	filewriter.close();
        	reader.close();
	}
	catch(FileNotFoundException ex)
       	{
		System.out.println("Unable to open file " + input_filename
					+ " or writing file"+ output_filename);
		ex.printStackTrace();
	}
	catch(IOException ex) 
	{
		System.out.println("Error reading file '" + input_filename
					+ " or writing file"+ output_filename);
		ex.printStackTrace();
	}

        disambiguator.close();
    }
}

