package com.kingj.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class GroovyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
//        WriteHelloTask task = project.getTasks().create("writeHello", WriteHelloTask.class)
//        task.setFileName("HelloWorld.txt")
//        task.setTargetDirectory(new File("D:/gradlePlugin"))
//        task.setGroup("yeshello")
//
//        project.task('buhello') << {
//            println 'hello, world!'
//        }

        createFile(project)
    }

    void searchRepeat(Project project) {
        System.out.println("------------------开始----------------------")
        System.out.println("这是我们的自定义插件!!!!!!!!")
        System.out.println("------------------0----------------------")

        //AppExtension就是build.gradle中android{...}这一块
        def android = project.extensions.getByType(AppExtension)

        //生产一个类
        if (project.plugins.hasPlugin(AppPlugin)) {
            //获取到Extension，Extension就是 build.gradle中的{}闭包
            android.applicationVariants.all { variant ->
                //获取到scope,作用域
                def variantData = variant.variantData
                def scope = variantData.scope

                System.out.println("------------------1----------------------")

                //创建一个task
                def createTaskName = scope.getTaskName("CeShi", "MyTestPlugin")
                def createTask = project.task(createTaskName)
                //设置task要执行的任务
                createTask.doLast {

                    System.out.println("------------------2----------------------")
                    //获取到BuildConfig类的路径
                    File outputDir = variant.getVariantData().getScope().getBuildConfigSourceOutputDir()

                    println("outputDir-------- " + outputDir)

                    if (outputDir.isDirectory()) {
                        System.out.println("------------------3----------------------")
                        //遍历文件夹
                        outputDir.eachFileRecurse { File file ->
                            String filePath = file.absolutePath
                            println("filePath-------- " + filePath)
                        }
                    }
                }
                //设置task依赖于生成BuildConfig的task，然后在生成BuildConfig后生成我们的类
                String generateBuildConfigTaskName = variant.getVariantData().getScope().getGenerateBuildConfigTask().name
                def generateBuildConfigTask = project.tasks.getByName(generateBuildConfigTaskName)
                if (generateBuildConfigTask) {
                    createTask.dependsOn generateBuildConfigTask
                    generateBuildConfigTask.finalizedBy createTask
                }
            }
        }

        System.out.println("------------------结束了吗----------------------")
    }

    void createFile(Project project) {
        System.out.println("------------------开始----------------------");
        System.out.println("这是我们的自定义插件!!!!!!!!");
        //AppExtension就是build.gradle中android{...}这一块
        def android = project.extensions.getByType(AppExtension)

        //注册一个Transform
        def classTransform = new MyClassTransform(project)
        android.registerTransform(classTransform)

        //创建一个Extension，名字叫做testCreatJavaConfig 里面可配置的属性参照MyPlguinTestClass
        project.extensions.create("testCreateJavaConfig", MyPluginTestClass)

        //生产一个类
        if (project.plugins.hasPlugin(AppPlugin)) {
            //获取到Extension，Extension就是 build.gradle中的{}闭包
            android.applicationVariants.all { variant ->
                //获取到scope,作用域
                def variantData = variant.variantData
                def scope = variantData.scope

                //拿到build.gradle中创建的Extension的值
                def config = project.extensions.getByName("testCreateJavaConfig");

                //创建一个task
                def createTaskName = scope.getTaskName("CeShi", "MyTestPlugin")
                def createTask = project.task(createTaskName)
                //设置task要执行的任务
                createTask.doLast {
                    //生成java类
                    createJavaTest(variant, config)
                }
                //设置task依赖于生成BuildConfig的task，然后在生成BuildConfig后生成我们的类
                String generateBuildConfigTaskName = variant.getVariantData().getScope().getGenerateBuildConfigTask().name
                def generateBuildConfigTask = project.tasks.getByName(generateBuildConfigTaskName)
                if (generateBuildConfigTask) {
                    createTask.dependsOn generateBuildConfigTask
                    generateBuildConfigTask.finalizedBy createTask
                }
            }

        }
        System.out.println("------------------结束了吗----------------------");
    }

    static def void createJavaTest(variant, config) {
        //要生成的内容
        def content = """package com.kingj.gradlepluginapp;

                        /**
                         * Created by Jin on 2017/8/30.
                         */

                        public class MyPluginTestClass {
                            public static final String str = "${config.str}";
                        }
                        """;
        //获取到BuildConfig类的路径
        File outputDir = variant.getVariantData().getScope().getBuildConfigSourceOutputDir()

        def javaFile = new File(outputDir, "MyPluginTestClass.java")

        javaFile.write(content, 'UTF-8')
    }
}