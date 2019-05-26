# disambiguate: Neural Word Sense Disambiguation Toolkit

This repository contains a set of easy-to-use tools for training, evaluating and using neural WSD models. 
This is the implementation used in the article [Sense Vocabulary Compression through the Semantic Knowledge of WordNet for Neural Word Sense Disambiguation](https://arxiv.org/abs/1905.05677), written by Loïc Vial, Benjamin Lecouteux and Didier Schwab.
** The difference is to add file input decoder only.

**/!\ The current version of this repository does not support yet BERT embeddings, ELMo embeddings, and Transformer encoders, but it will be available soon ! (the code is being cleaned in private first ;))**

## Dependencies
- Python (version 3.6 or higher) - <https://python.org>
- Java (version 8 or higher) - <https://java.com>
- Maven - <https://maven.apache.org>
- PyTorch (version 0.4.0 or higher) - <https://pytorch.org>
- UFSAC - <https://github.com/getalp/UFSAC>

To install **Python**, **Java** and **Maven**, you can use the package manager of your distribution (apt-get, pacman...).

To install **PyTorch**, please follow [this page](https://pytorch.org/get-started).

To install **UFSAC**, simply:
- download the content of the [UFSAC repository](https://github.com/getalp/UFSAC)
- go into the `java` folder 
- run `mvn install`

## Compilation

Once the dependencies are installed, please run `./java/compile.sh` to compile the Java code. 

## Use pre-trained models

At the moment we are only providing one of our best model trained on the SemCor and the WordNet Gloss Tagged, with the vocabulary reduction applied, as described in [our article](https://arxiv.org/abs/1811.00960).

Here is the link to the data: <https://drive.google.com/file/d/1_-CxENMkmUSGkcmb6xcFBhJR114A4GsY>

Once the data are downloaded and extracted, you can use the following commands (replace `$DATADIR` with the path of the appropriate folder):
- `./decode.sh --data_path $DATADIR --weights $DATADIR/model_weights_wsd`

  This script allows to disambiguate raw text from the standard input to the standard output

- `./decode_file.sh --data_path semcor_wngt_reduced  --weights semcor_wngt_reduced/model_weights_wsd --input_file text/test_input.txt --output_file text/test_output.txt`

  This script allows to disambiguate raw text from the file input to the file output

- `./evaluate.sh --data_path $DATADIR --weights $DATADIR/model_weights_wsd --corpus [UFSAC corpus]...` 

  This script evaluates a WSD model by computing its coverage, precision, recall and F1 scores on sense annotated corpora in the UFSAC format, with and without first sense backoff.  

Description of the arguments:
- `--data_path [DIR]` is the path to the directory containing the files needed for describing the model architecture (files `config.json`, `input_vocabularyX` and `output_vocabularyX`) 
- `--weights [FILE]...` is a list of model weights: if multiple weights are given, an ensemble of these weights is used in `decode.sh`, and both the evaluation of the ensemble of weights and the evaluation of each individual weight is performed in `evaluate.sh`
- `--corpus [FILE]...` (`evaluate.sh` only) is the list of UFSAC corpora used for evaluating the WSD model

Optional arguments: 
- `--lowercase [true|false]` (default `true`) if you want to enable/disable lowercasing of input
- `--sense_reduction [true|false]` (default `true`) if you want to enable/disable the sense vocabulary reduction method.

UFSAC corpora are available in the [UFSAC repository](https://github.com/getalp/UFSAC). If you want to reproduce our results, please download UFSAC 2.1 and you will find the SemCor (file `semcor.xml`, the WordNet Gloss Tagged (file `wngt.xml`) and all the SemEval/SensEval evaluation corpora that we used.

## Train a WSD model

To train a model, first call the `./prepare_data.sh` script with the following arguments:
- `--data_path [DIR]` is the path to the directory that will contain the description of the model (files `config.json`, `input_vocabularyX` and `output_vocabularyX`) and the processed training data (files `train` and `dev`)
- `--train [FILE]...` is the list of corpora in UFSAC format used for the training set
- `--dev [FILE]...` (optional) is the list of corpora in UFSAC format used for the development set
- `--dev_from_train [N]` (default `0`) randomly extracts `N` sentences from the training corpus and use it as development corpus
- `--input_features [FEATURE]...` (default `surface_form`) is the list of input features used, as UFSAC attributes. Possible values are, but not limited to, `surface_form`, `lemma`, `pos`, `wn30_key`...
- `--input_embeddings [FILE]...` (default `null`) is the list of pre-trained embeddings to use for each input feature. Must be the same number of arguments as `input_features`, use special value `null` if you want to train embeddings as part of the model
- `--output_features [FEATURE]...` (default `wn30_key`) is the list of output features to predict by the model, as UFSAC attributes. Possible values are the same as input features
- `--lowercase [true|false]` (default `true`) if you want to enable/disable lowercasing of input
- `--sense_reduction [true|false]` (default `true`) if you want to enable/disable the sense vocabulary reduction method.
- `--add_monosemics [true|false]` (default `false`) if you want to consider all monosemic words annotated with their unique sense tag (even if they are not initially annotated) 
- `--remove_monosemics [true|false]` (default `false`) if you want to remove the tag of all monosemic words
- `--remove_duplicates [true|false]` (default `true`) if you want to remove duplicate sentences from the training set (output features are merged)

Once the data prepared, tweak the generated `config.json` file to your needs (LSTM layers, embeddings size, dropout rate...)

Finally, use the `./train.sh` script with the following arguments:
- `--data_path [DIR]` is the path to the directory generated by `prepare_data.sh` (must contains the files describing the model and the processed training data)
- `--model_path [DIR]` is the path where the trained model weights and the training info will be saved
- `--batch_size [N]` (default `100`) is the batch size
- `--ensemble_count [N]` (default `8`) is the number of different model to train
- `--epoch_count [N]` (default `100`) is the number of epoch
- `--eval_frequency [N]` (default `4000`) is the number of batch to process before evaluating the model on the development set. The count resets every epoch, and an eveluation is also performed at the end of every epoch 
- `--update_frequency [N]` (default `1`) is the number of batch to accumulate before backpropagating (if you want to accumulate the gradient of several batches)
- `--lr [N]` (default `0.0001`) is the initial learning rate of the optimizer (Adam)
- `--reset [true|false]` (default `false`) if you do not want to resume a previous training. Be careful as it will effectively resets the training state and the model weights saved in the `--model_path`

