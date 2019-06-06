package org.hobbit.sdk.iotpipeline_bm.docker.builder;

import org.hobbit.sdk.docker.BuildBasedDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bushranazir on 29.03.2019.
 */
public class  MyDynamicDockerFileBuilder extends BuildBasedDockersBuilder {

	private static final Logger logger = LoggerFactory.getLogger(MyDynamicDockerFileBuilder.class);

	private Class[] runnerClass;
	private Path dockerWorkDir;
	private List<String> filesToAdd = new ArrayList();
	private String dockerfilePath;

	public  MyDynamicDockerFileBuilder(String dockerizerName) {
		super(dockerizerName);
	}

	public  MyDynamicDockerFileBuilder runnerClass(Class... values) {
		this.runnerClass = values;
		return this;
	}

	public  MyDynamicDockerFileBuilder dockerWorkDir(String value) {
		this.dockerWorkDir = Paths.get(value, new String[0]);
		return this;
	}

	public MyDynamicDockerFileBuilder jarFilePath(String value) {
		this.jarFilePath = Paths.get(value, new String[0]).toAbsolutePath();
		return this;
	}

	public MyDynamicDockerFileBuilder useCachedImage(Boolean value) {
		super.useCachedImage(value);
		return this;
	}

	public MyDynamicDockerFileBuilder useCachedContainer(Boolean value) {
		super.useCachedContainer(value);
		return this;
	}

	public MyDynamicDockerFileBuilder imageName(String value) {
		super.imageName(value);
		return this;
	}

	public MyDynamicDockerFileBuilder addFileOrFolder(String path) {
		this.filesToAdd.add(path);
		return this;
	}

	private MyDynamicDockerFileBuilder initFileReader() throws Exception {

		logger.debug("******************************** dockerfilepath is missing!   Inside initFileReader() ");


		if(this.runnerClass == null) {
			throw new Exception("Runner class is not specified for " + this.getClass().getSimpleName());
		} else if(this.dockerWorkDir == null) {
			throw new Exception("WorkingDirName class is not specified for " + this.getClass().getSimpleName());
		} else if(this.jarFilePath == null) {
			throw new Exception("JarFileName class is not specified for " + this.getClass().getSimpleName());
		} else {
			List classNames = (List) Arrays.stream(this.runnerClass).map((c) -> {
				return c.getCanonicalName();
			}).collect(Collectors.toList());
			String datasetsStr = "";

			String content;
			String destPath;
			Path var11;
			for(Iterator jarPathRel = this.filesToAdd.iterator(); jarPathRel.hasNext(); datasetsStr = datasetsStr + "ADD ./" + var11 + " " + destPath + "\n") {
				content = (String)jarPathRel.next();
				Path destPathRel = Paths.get(content, new String[0]);
				if(destPathRel.isAbsolute()) {
					destPathRel = this.getBuildDirectory().relativize(destPathRel);
				}

				Path parent = destPathRel.getParent();
				ArrayList dirsToCreate = new ArrayList();
				if(Files.isDirectory(destPathRel, new LinkOption[0])) {
					dirsToCreate.add(destPathRel.toString());
				}

				while(parent != null) {
					dirsToCreate.add(parent.toString());
					parent = parent.getParent();
				}

				for(int sourcePath = dirsToCreate.size() - 1; sourcePath >= 0; --sourcePath) {
					datasetsStr = datasetsStr + "RUN mkdir -p " + this.dockerWorkDir.resolve((String)dirsToCreate.get(sourcePath)) + "\n";
				}

				var11 = destPathRel;
				destPath = this.dockerWorkDir.resolve(destPathRel).toString();
				if(destPathRel.toFile().isDirectory()) {
					var11 = destPathRel.resolve("*");
					destPath = destPath + "/";
				} else if(destPathRel.getParent() != null) {
					destPath = this.dockerWorkDir.resolve(destPathRel.getParent()).toString();
				} else {
					destPath = this.dockerWorkDir.toString();
				}
			}

			Path var10 = this.jarFilePath;
			if(var10.isAbsolute()) {
				var10 = this.getBuildDirectory().relativize(this.jarFilePath);
			}

			content = "FROM java\nRUN mkdir -p " + this.dockerWorkDir + "\nWORKDIR " + this.dockerWorkDir + "\n" + datasetsStr + "ADD " + var10 + " " + this.dockerWorkDir + "\nCMD java -cp " + var10.getFileName() + " " + String.join(" ", classNames) + "\n";
			this.dockerFileReader(new StringReader(content));
			return this;
		}
	}

	public BuildBasedDockerizer build() throws Exception {
		if(this.dockerfilePath == null) {

			logger.debug("******************************** dockerfilepath is missing!");

			this.initFileReader();
		}

		this.containerName(this.runnerClass[this.runnerClass.length - 1].getSimpleName());

		logger.debug("******************************** calling super.build()!  ------  " + this.getContainerName());

		byte[] ret = Files.readAllBytes(Paths.get(this.dockerfilePath, new String[0]));
		if (ret == null){
			logger.debug("******************************** file not null");
		}
		return super.build();
	}
}
