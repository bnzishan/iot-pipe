package org.hobbit.sdk.iotpipeline_bm.utils.X;

/**
 * Created by bushranazir on 22.10.2018.
 */
public class Test2 {

	public Test2() {
	}
/*
// We could store your shell script as a resource (e.g. inside your jar file), then exec a shell and pipe the content of your script as standard input to the running shell.
// shell script stored in resources, called from java

	ProcessBuilder processBuilder = new ProcessBuilder( "/usr/bin/bash" );
Process process = processBuilder.start();
OutputStream outputStream = process.getOutputStream();
InputStream resourceStream = getClass().getResourceAsStream(
        "/path/to/dockerutils/script.sh" );
IOUtils.copy( resourceStream, outputStream );

*/
}
