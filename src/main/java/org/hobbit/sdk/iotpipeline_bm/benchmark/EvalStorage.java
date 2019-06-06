package org.hobbit.sdk.iotpipeline_bm.benchmark;

import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.components.test.InMemoryEvaluationStore;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple in-memory implementation of an evaluation storage that can be used for
 * testing purposes.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 * https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/test/InMemoryEvaluationStore.java
 */

public class EvalStorage extends AbstractEvaluationStorage {
    private static final Logger logger = LoggerFactory.getLogger(EvalStorage.class);
    /**
     * Map containing a mapping from task Ids to result pairs.
     */
    private Map<String, ResultPair> results = Collections.synchronizedMap(new HashMap<String, ResultPair>());

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
    }

    @Override
    public void receiveResponseData(String taskId, long timestamp, byte[] data) {
        logger.trace("receiveResponseData()->{}",new String(data));




        putResult(false, taskId, timestamp, data);
    }

    @Override
    public void receiveExpectedResponseData(String taskId, long timestamp, byte[] data) {

        // read execution time of task e.g. total time storm it took to run a topology over storm cluster
        logger.trace("receive Response Data->({}){}",taskId,new RabbitMQUtils()
                .readLong(data));
        putResult(true, taskId, timestamp, data);

    }

    /**
     * Adds the given result to the map of results.
     *
     * @param isExpectedResult
     *            true if the result has been received from a task generator,
     *            i.e., is the expected result for a task
     * @param taskId
     *            id of the task
     * @param timestamp
     *            time stamp for the task result
     * @param data
     *            the result
     */
    public synchronized void putResult(boolean isExpectedResult, String taskId, long timestamp, byte[] data) {
        InMemoryEvaluationStore.ResultPairImpl pair;
        if (results.containsKey(taskId)) {
            pair = (InMemoryEvaluationStore.ResultPairImpl) results.get(taskId);
        } else {
            pair = new InMemoryEvaluationStore.ResultPairImpl();
            results.put(taskId, pair);
        }
        if (isExpectedResult) {
            pair.setExpected(new InMemoryEvaluationStore.ResultImpl(timestamp, data));
        } else {
            pair.setActual(new InMemoryEvaluationStore.ResultImpl(timestamp, data));
        }
    }

    @Override
    protected Iterator<ResultPair> createIterator() {
        logger.debug("createIterator() for {} items", results.size());
        Iterator<ResultPair> ret = results.values().iterator();
        logger.debug("Iterator created");
        return ret;
    }

}

