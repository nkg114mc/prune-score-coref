## Prune-Score Entity Coreference Resolution System

### Related Publication
[Prune-and-Score: Learning for Greedy Coreference Resolution](http://people.oregonstate.edu/~machao/homepage/emnlp14/PruneScore14.pdf) <br />
Chao Ma, Janardhan Rao Doppa, Xiaoli Fern, Tom Dietterich, and Prasad Tadepalli (EMNLP 2014)

### Requirement Packages

You need to install the [Xgboost JVM-Package](http://xgboost.readthedocs.io/en/latest/jvm/index.html) firstly to do the training and testing.


### Other Required Files

* Mention dump files: [mentionDump.zip](http://people.oregonstate.edu/~machao/oregonstate_coref/mentionDump.zip)<br />
You need to unzip and place the unzipped `mentionDump` folder at the project root path.
* Pre-trained model file: [xgb-models.zip](http://people.oregonstate.edu/~machao/oregonstate_coref/xgb-models.zip)<br />
You need to unzip and place all the files in the unzipped folder at the project root path.

### Training


### Testing


### Current Performance

| Metric  | Precision | Recall | F1 |
| ------- |:---------:| :-----:| :--:|
|Ment. Detec.| 71.11 | 88.10 | 78.70 |
| MUC        | 65.73 | 81.20 | 72.65 |
| BCub       | 52.17 | 72.44 | 60.66 |
| CEAF_e     | 51.14 | 63.97 | 56.84 |
| CoNLL      | -     | -     | **63.38** |

- **Contact**<br />
Chao Ma (machao@engr.orst.edu, nkg114mc@hotmail.com)<br />
Oregon State University.
