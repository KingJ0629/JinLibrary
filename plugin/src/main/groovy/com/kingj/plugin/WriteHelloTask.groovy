package com.kingj.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
/**
 * Created by Jin on 2018/7/31.
 * Description
 */
public class WriteHelloTask extends DefaultTask {
	
	private String fileName
	private File targetDirectory
	
	@OutputFile
	public File getTargetFile() {
		return new File(targetDirectory, fileName)
	}
	
	@Input
	public String getFileName() {
		return fileName
	}
	
	@InputDirectory
	public File getTargetDirectory() {
		return targetDirectory
	}
	
	@TaskAction
	public void writeObject() {
		File targetFile = new File(targetDirectory, fileName)
		try {
			FileOutputStream fos = new FileOutputStream(targetFile);
			byte[] bytes = "hello".getBytes();
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName
	}
}
