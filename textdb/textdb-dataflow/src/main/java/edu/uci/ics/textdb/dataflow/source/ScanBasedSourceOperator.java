package edu.uci.ics.textdb.dataflow.source;

import edu.uci.ics.textdb.api.exception.TextDBException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.MatchAllDocsQuery;

import edu.uci.ics.textdb.api.common.ITuple;
import edu.uci.ics.textdb.api.common.Schema;
import edu.uci.ics.textdb.api.dataflow.ISourceOperator;
import edu.uci.ics.textdb.api.storage.IDataReader;
import edu.uci.ics.textdb.api.storage.IDataStore;
import edu.uci.ics.textdb.common.exception.DataFlowException;
import edu.uci.ics.textdb.storage.DataReaderPredicate;
import edu.uci.ics.textdb.storage.reader.DataReader;

/**
 * Created by chenli on 3/28/16.
 */
public class ScanBasedSourceOperator implements ISourceOperator {

    private IDataStore dataStore;
    private Analyzer luceneAnalyzer;
    
    private IDataReader dataReader;

    public ScanBasedSourceOperator(IDataStore dataStore, Analyzer luceneAnalyzer) throws DataFlowException {
        this.dataStore = dataStore;
        this.luceneAnalyzer = luceneAnalyzer;
    }

    @Override
    public void open() throws TextDBException {
        try {
            DataReaderPredicate predicate = new DataReaderPredicate(
                    new MatchAllDocsQuery(), dataStore, luceneAnalyzer);
            // TODO add an option to set if payload is added in the future.
            predicate.seIsPayloadAdded(false);
            this.dataReader = new DataReader(predicate);
            this.dataReader.open();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }

    @Override
    public ITuple getNextTuple() throws TextDBException {
        try {
            return dataReader.getNextTuple();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws TextDBException {
        try {
            dataReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFlowException(e.getMessage(), e);
        }
    }

    @Override
    public Schema getOutputSchema() {
        // TODO Auto-generated method stub
        return dataReader.getOutputSchema();
    }
}
