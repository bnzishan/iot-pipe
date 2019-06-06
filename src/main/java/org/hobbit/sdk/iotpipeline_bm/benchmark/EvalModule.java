package org.hobbit.sdk.iotpipeline_bm.benchmark;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.iotpipeline_bm.Constants;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EvalModule extends AbstractEvaluationModule {

	/* Metrics */
	private Property AVG_EXECUTION_TIME = null;
	private Property AVG_EXECUTION_TIME_T1 = null;
	private Property NUM_OF_TASK_FAILURES = null;

	/* Final evaluation model */
	private Model finalModel = ModelFactory.createDefaultModel();

	private Map<String, Long> totalTimePerTaskType = new HashMap<>();
	private Map<String, Integer> numberOfTasksPerTaskType = new HashMap<>();  // 25

	private Map<String, ArrayList<Long>> executionTimes = new HashMap<>();

	Long expectedExecutionTime = null;
	Long receivedExecutionTime = null;
	Long avgExecutionTime = null;
	Long avgExecutionTimeT1 = null;
	int numOfTaskFailures = 0;

	private static final Logger logger = LoggerFactory.getLogger(EvalModule.class);


	@Override
	public void init() throws Exception {
		super.init();
		logger.trace("init()");

		totalTimePerTaskType.put("spike-detection-topology", (long) 0);  // T1
		numberOfTasksPerTaskType.put("spike-detection-topology", 0);  // T1

		Map<String, String> env = System.getenv();

		if (!env.containsKey(Constants.AVG_EXECUTION_TIME)) {
			throw new IllegalArgumentException("Couldn't get \"" + Constants.AVG_EXECUTION_TIME
					+ "\" from the environment. Aborting.");
		}
		if (!env.containsKey(Constants.AVG_EXECUTION_TIME_T1)) {
			throw new IllegalArgumentException("Couldn't get \"" + Constants.EXECUTION_TIME
					+ "\" from the environment. Aborting.");
		}
		if (!env.containsKey(Constants.NUM_OF_TASK_FAILURES)) {
			throw new IllegalArgumentException("Couldn't get \"" + Constants.NUM_OF_TASK_FAILURES
					+ "\" from the environment. Aborting.");
		}

		NUM_OF_TASK_FAILURES = this.finalModel.createProperty(env.get(Constants.NUM_OF_TASK_FAILURES));
		AVG_EXECUTION_TIME = this.finalModel.createProperty(env.get(Constants.AVG_EXECUTION_TIME));
		AVG_EXECUTION_TIME_T1 = this.finalModel.createProperty(env.get(Constants.AVG_EXECUTION_TIME_T1));
	}



	//----------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
		// evaluate the given response and store the result, e.g., increment internal counters
		logger.info("evaluate response.");


		expectedExecutionTime = RabbitMQUtils.readLong(expectedData);
		receivedExecutionTime = RabbitMQUtils.readLong(receivedData);

		String type = "spike-detection-topology"; // ToDO  write "type" inside expectedData and extract here to properly check which task type is coming.
		if (!executionTimes.containsKey(type))
			executionTimes.put(type, new ArrayList<Long>());
		executionTimes.get(type).add(responseReceivedTimestamp - taskSentTimestamp);
		logger.info("received execution time task with type: spike-detection-topology -> {} ", executionTimes.get("spike-detection-topology"));

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------

	/*
	 *  sample implementation for one task
	 */
	@Override
	protected Model summarizeEvaluation() throws Exception {
		logger.debug("summarizeEvaluation()");
		// All tasks/responsens have been evaluated. Summarize the results,
		// write them into a Jena model and send it to the benchmark controller.

		//TODO: remove this sleeping (hack for extra time;
		try {
			TimeUnit.SECONDS.sleep(30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		/*
		Model model = createDefaultModel();
		Resource experimentResource = model.getResource(experimentUri);
		model.add(experimentResource, RDF.type, HOBBIT.Experiment);
*/


		if (experimentUri == null)
			experimentUri = System.getenv().get("HOBBIT_EXPERIMENT_URI");

		Resource experiment = finalModel.createResource(experimentUri);
		finalModel.add(experiment, RDF.type, HOBBIT.Experiment);

		long totalMS = 0;
		long totalTasks = 0;
		for (Map.Entry<String, ArrayList<Long>> entry : executionTimes.entrySet()) {
			long totalMSPerTaskType = 0;
			for (long l : entry.getValue()) {
				totalMSPerTaskType += l;
			}
			logger.info ("TOTAL TIME in MS [PER TASK TYPE] -> {} ", totalMSPerTaskType);

			numberOfTasksPerTaskType.put(entry.getKey(), entry.getValue().size());
			totalTimePerTaskType.put(entry.getKey(), totalMSPerTaskType);

			totalMS += totalMSPerTaskType;
			totalTasks += entry.getValue().size();

			logger.info ("AVERAVGE TIME in MS [PER TASK TYPE] -> {} ", totalMSPerTaskType);
			logger.info(entry.getKey() + "-" + ((double)totalMSPerTaskType)/ entry.getValue().size());
		}

			logger.info (" AVERAVGE EXECUTION TIME in MS  -> {} ",  totalMS / totalTasks);

		//----------------------------------------------------------------------------------------------------------------------------------------------

		logger.info("Summary of Evaluation. Creating result model... ");

		Literal teAverageTimeLiteral = finalModel.createTypedLiteral((double) totalMS / totalTasks);
		finalModel.add(experiment, AVG_EXECUTION_TIME, teAverageTimeLiteral);
		logger.info("total evaluation Average Time Literal: {}", teAverageTimeLiteral);


	//	Literal te01AverageTimeLiteral = finalModel.createTypedLiteral(
	//			(double) totalTimePerTaskType.get("T1") / numberOfTasksPerTaskType.get("T1"), XSDDatatype.XSDdouble);
	//	if (numberOfTasksPerTaskType.get("T1") > 0)
	//		finalModel.add(experiment, AVG_EXECUTION_TIME_T1, te01AverageTimeLiteral);
	//	logger.debug("te01AverageTimeLiteral: {}", te01AverageTimeLiteral);

		Literal numOfTaskFailureLiteral = finalModel.createTypedLiteral(0);
		finalModel.add(experiment, NUM_OF_TASK_FAILURES, numOfTaskFailureLiteral);
		logger.debug("numOfTaskFailureLiteral: {}", numOfTaskFailureLiteral);


		//----------------------------------------------------------------------------------------------------------------------------------------------


		//	logger.debug("Sending result model: {}", RabbitMQUtils.writeModel2String(finalModel));
		//logger.info(finalModel.toString());


		return finalModel;

	}
	//----------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void close() {
		// Free the resources you requested here
		logger.debug("close()");
		// Always close the super class after yours!
		try {
			super.close();
		} catch (Exception e) {

		}
	}

	//----------------------------------------------------------------------------------------------------------------------------------------------

}
