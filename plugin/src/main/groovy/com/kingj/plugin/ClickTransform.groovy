package com.kingj.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

/**
 * Created by Jin on 2018/8/1.
 * Description
 */
public class ClickTransform extends Transform {
	
	private Project mProject
	
	public ClickTransform(Project p) {
		mProject = p
	}
	/**
	 * Returns the unique name of the transform.
	 *
	 * <p/>
	 * This is associated with the type of work that the transform does. It does not have to be
	 * unique per variant.
	 */
	@Override
	String getName() {
		return "ClickTransformImpl"
	}
	/**
	 * Returns the type(s) of data that is consumed by the Transform. This may be more than
	 * one type.
	 * <strong>This must be of type {@link QualifiedContent.DefaultContentType}</strong>
	 * Transform的输入类型
	 */
	@Override
	Set<QualifiedContent.ContentType> getInputTypes() {
		return TransformManager.CONTENT_CLASS
	}
	/**
	 * Returns the scope(s) of the Transform. This indicates which scopes the transform consumes.
	 * Transform的作用范围
	 */
	@Override
	Set<QualifiedContent.Scope> getScopes() {
		return TransformManager.SCOPE_FULL_PROJECT
	}
	
	@Override
	boolean isIncremental() {
		return false
	}
	
	@Override
	void transform(Context context, Collection<TransformInput> inputs,
				   Collection<TransformInput> referencedInputs,
				   TransformOutputProvider outputProvider, boolean isIncremental)
			throws IOException, TransformException, InterruptedException {

		println 'hello, transform!'

		inputs.each { TransformInput input->
			input.directoryInputs.each { DirectoryInput directoryInput->
				//往类中注入代码
				injectClick(directoryInput.file.getAbsolutePath(), "com", mProject)

				def dest = outputProvider.getContentLocation(directoryInput.name,
						directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

				//将 input 的目录复制到 output 指定目录
				FileUtils.copyDirectory(directoryInput.file, dest)
			}

			input.jarInputs.each { JarInput jarInput ->
				//往类中注入代码
				injectClick(jarInput.file.getAbsolutePath(), "com.netease", mProject)

				//重命名输出文件（同目录 copyFile 会冲突）
				def jarName = jarInput.name
				def md5Name = jarInput.file.hashCode()
				if(jarName.endsWith(".jar")){
					jarName = jarName.substring(0, jarName.length() - 4)
				}
				def dest = outputProvider.getContentLocation(jarName + md5Name,
						jarInput.contentTypes, jarInput.scopes, Format.JAR)
				FileUtils.copyFile(jarInput.file, dest)
			}
		}
	}
	private void injectClick(String path, String packageName,Project project) {
		mPool.appendClassPath(path)
		mPool.appendClassPath(project.android.bootClasspath[0].toString())
		mPool.importPackage(IMPORT_CLASS_PATH)
		File dir = new File(path)
		if (dir.isDirectory()) {
			dir.eachFileRecurse {
				File file ->
					String filePath = file.absolutePath
					if (filePath.endsWith(".class") && !filePath.contains('R$')
							&& !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")) {
						int index = filePath.indexOf(packageName);
						boolean isMyPackage = index != -1;
						if (!isMyPackage) {
							return
						}
						String className = ClassUtil.getClassName(index, filePath)

						CtClass ctClass = mPool.getCtClass(className)

						if (ctClass.isFrozen())
							ctClass.defrost()
						//遍历类中的所有方法，找到onClick函数
						for (CtMethod method : ctClass.getDeclaredMethods()) {
							//找到 onClick(View) 方法
							if (checkOnClickMethod(method)) {
								injectMethod(method)
								ctClass.writeFile(path)
							}
						}
					}
			}
		}
	}

	private static void injectMethod(CtMethod method) {
		method.insertAfter("YXSConfigManager.getInstance().onInvokeClick(\$1);")
	}

	private static boolean checkOnClickMethod(CtMethod method) {
		return method.getName().endsWith("onClick") && method.getParameterTypes().length == 1 &&
				method.getParameterTypes()[0].getName().equals("android.view.View")
	}
}
