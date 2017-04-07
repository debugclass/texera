package edu.uci.ics.textdb.exp.sampler;

import edu.uci.ics.textdb.api.dataflow.ISourceOperator;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uci.ics.textdb.exp.common.AbstractSingleInputOperator;
import edu.uci.ics.textdb.exp.sampler.SamplerPredicate.SampleType;

/**
 * @author Qinhua Huang
 * 
 * This class is to sample tuples from the incoming stream tuples. It has two modes,
 * first k-arrival and random. 
 * 
 * Here are two examples to sample 3 tuples from tuple source.
 * 1. Example: SampleType.FIRST_K_ARRIVAL mode.
 * Tuple source: A, B，C，D，E，F，G，H
 * output: A，B，C
 * 
 * 2. Example: SampleType.RANDOM_SAMPLE mode.
 * Tuples source: A, B，C，D，E，F，G，H
 * The result is in randomly distributed.
 * One possible result: E, B, H
 */
public class Sampler extends AbstractSingleInputOperator implements ISourceOperator{
    private SamplerPredicate predicate;
    private List<Tuple> sampleBuffer;
    private int bufferCursor;
    
    public Sampler(SamplerPredicate predicate) {
        this.predicate = predicate;
        this.sampleBuffer = null;
        this.bufferCursor = -1;
    }
    
    @Override
    protected void setUp() throws DataFlowException {
        this.outputSchema = inputOperator.getOutputSchema();
    }
    
    private void constructSampleBuffer() throws TextDBException {
        sampleBuffer = new ArrayList<Tuple>();
        
        Random random = new Random(System.currentTimeMillis());
        
        Tuple tuple;
        int count = 0;
        while ((tuple = inputOperator.getNextTuple()) != null) {
            if (count < predicate.getSampleSize()) {
                sampleBuffer.add(tuple);
            } else {
                /* In SampleType.FIRST_K_ARRIVAL mode, when the samleBuffer is full,
                 * it will finish constructing the buffer tuples and return.
                 */
                if (this.predicate.getSampleType() == SampleType.FIRST_K_ARRIVAL) {
                    break;
                }
                /*
                 *  In SampleType.RANDOM_SAMPLE mode, the reservoir sampling algorithm is
                 *  used to sample tuples.
                 *  When the buffer is full, the ith incoming tuple is chosen to replace
                 *  tuple the buffer with probability of bufferSize / i.
                 */
                if (this.predicate.getSampleType() == SampleType.RANDOM_SAMPLE) {
                    int randomPos = random.nextInt(count);
                    if (randomPos < predicate.getSampleSize()) {
                        sampleBuffer.set(randomPos, tuple);
                    }
                }
            }
            count++;
        }
    }
    
    @Override
    protected Tuple computeNextMatchingTuple() throws TextDBException {
        if (sampleBuffer == null) {
            constructSampleBuffer();
            this.bufferCursor = 0;
        }
        if (bufferCursor == sampleBuffer.size()) {
            return null;
        }
        
        // Compute one result tuple.
        Tuple resultTuple = sampleBuffer.get(bufferCursor);
        bufferCursor++;
        
        return resultTuple;
    }
    
    @Override
    protected void cleanUp() throws TextDBException {
        sampleBuffer = null;
        bufferCursor = -1;
    }
    
    public SamplerPredicate getPredicate() {
        return this.predicate;
    }
    
    @Override
    public Tuple processOneInputTuple(Tuple inputTuple) throws TextDBException {
        throw new RuntimeException("Sampler does not support process one tuple");
    }
}