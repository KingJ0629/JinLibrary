package com.kingj.plugin


import javassist.CtClass
import javassist.CtMethod

class TestInjects {

    static void inject(String path) {
        File dir = new File(path)
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                println("filePath = " + filePath)
                if (file.getName() == "MainActivity.class") {

                    //获取MainActivity.class
                    CtClass ctClass = pool.getCtClass("com.kingj.gradlepluginapp.MainActivity")
                    println("ctClass = " + ctClass)
                    //解冻
                    if (ctClass.isFrozen())
                        ctClass.defrost()

                    //获取到OnCreate方法
                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")

                    println("方法名 = " + ctMethod)

                    String insetBeforeStr = """ android.widget.Toast.makeText(this,"我是被插入的Toast代码~!!单纯注入transform",android.widget.Toast.LENGTH_SHORT).show();
					"""
                    //在方法开头插入代码
                    ctMethod.insertBefore(insetBeforeStr)
                    ctClass.writeFile(path)
                    ctClass.detach()//释放
                }
            }
        }
    }
}