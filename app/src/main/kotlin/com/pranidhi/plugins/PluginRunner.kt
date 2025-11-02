package com.pranidhi.plugin

import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.json.JSONObject

object PluginRunner {
    private val exec=Executors.newCachedThreadPool()
    data class PluginResult(val success:Boolean,val data:JSONObject?,val error:String?,val runtimeMs:Long)

    fun runPluginWithLimits(helperPath:String?,memMb:Int,cpuSec:Int,script:String,timeoutSec:Long=20):PluginResult{
        val start=System.currentTimeMillis()
        val pb=if(helperPath!=null&&File(helperPath).canExecute())
            ProcessBuilder(helperPath,memMb.toString(),cpuSec.toString(),"python3",script)
        else ProcessBuilder("python3",script)
        pb.redirectErrorStream(true)
        val proc=pb.start()
        val pid=getPid(proc)
        if(helperPath==null&&pid>0) monitorMemory(proc,pid,memMb)
        exec.submit{ if(!proc.waitFor(timeoutSec,TimeUnit.SECONDS)) proc.destroyForcibly() }
        val reader=BufferedReader(InputStreamReader(proc.inputStream))
        var last:String?=null
        reader.forEachLine{ line-> last=line; runCatching{ val obj=JSONObject(line)
            if(!obj.has("progress")) return PluginResult(true,obj,null,System.currentTimeMillis()-start) } }
        proc.destroyForcibly()
        return PluginResult(false,null,last?:"no-output",System.currentTimeMillis()-start)
    }

    private fun getPid(proc:Process):Long{
        return try{
            val f:Field=proc.javaClass.getDeclaredField("pid"); f.isAccessible=true; (f.get(proc)as Int).toLong()
        }catch(_:Exception){-1}
    }

    private fun monitorMemory(proc:Process,pid:Long,maxMb:Int){
        Thread{
            val file=File("/proc/$pid/status")
            while(proc.isAlive){
                try{
                    val line=file.takeIf{it.exists()}?.readLines()?.find{it.startsWith("VmRSS:")}
                    val kb=line?.split(Regex("\\s+"))?.getOrNull(1)?.toLongOrNull()?:0
                    if(kb>maxMb*1024L){proc.destroyForcibly();break}
                }catch(_:Exception){}
                Thread.sleep(500)
            }
        }.start()
    }
}
