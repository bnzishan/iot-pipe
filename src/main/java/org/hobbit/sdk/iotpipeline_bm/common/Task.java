package org.hobbit.sdk.iotpipeline_bm.common;

import org.hobbit.core.rabbit.RabbitMQUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by bushranazir on 06.02.2019.
 */
public class Task implements Serializable {

	private String taskId;
	private String query;
	private int queryType;
	private int querySubstitutionParam;
	private byte[] expectedAnswers;

	public Task(int queryType, int querySubstitutionParam, String taskId, String query, byte[] expectedAnswers ){
		this.queryType = queryType;
		this.querySubstitutionParam = querySubstitutionParam;
		this.taskId = taskId;
		this.query = query;
		this.expectedAnswers = expectedAnswers;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getQueryType() {
		return queryType;
	}

	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}

	public int getQuerySubstitutionParam() {
		return querySubstitutionParam;
	}

	public void setQuerySubstitutionParam(int querySubstitutionParam) {
		this.querySubstitutionParam = querySubstitutionParam;
	}

	public byte[] getExpectedAnswers() {
		return expectedAnswers;
	}

	// the results are preceded by the query type as this information required
	// by the evaluation module.
	public void setExpectedAnswers(byte[] expectedAnswers) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putInt(queryType);
		buffer.putInt(querySubstitutionParam);
		this.expectedAnswers = RabbitMQUtils.writeByteArrays(buffer.array(), new byte[][]{expectedAnswers}, null);
	}
}
