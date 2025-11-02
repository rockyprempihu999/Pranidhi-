package com.pranidhi.nlu

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors

data class IntentResult(val intentId: String, val confidence: Float, val slots: Map<String,String> = emptyMap())

class NluInterpreter(private val context: Context) {
    private val exec = Executors.newSingleThreadExecutor()
    private val interpreter: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context,"models/intent_mobilebert_int8.tflite")
        val opts = Interpreter.Options().apply { setNumThreads(2) }
        Interpreter(model, opts)
    }
    private val vocab: Map<String,Int> by lazy { loadVocab() }

    private fun loadVocab(): Map<String,Int> {
        return try {
            val map = mutableMapOf<String,Int>()
            context.assets.open("vocab.txt").bufferedReader().useLines { lines ->
                var i=0; lines.forEach { line -> map[line.trim()] = i++ }
            }; map
        } catch(e: Exception){ mapOf("[PAD]" to 0,"[UNK]" to 1) }
    }

    fun warmUp() = exec.submit {
        val bb = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder())
        val out = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder())
        synchronized(interpreter){ interpreter.run(bb,out) }
    }

    fun predict(text:String): IntentResult {
        val tokens = tokenize(text,16)
        val input = ByteBuffer.allocateDirect(tokens.size*4).order(ByteOrder.nativeOrder())
        tokens.forEach{ input.putInt(it) }; input.rewind()
        val out = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder())
        synchronized(interpreter){ interpreter.run(input,out) }
        out.rewind(); val conf = if(out.remaining()>=4) out.float else 0f
        return IntentResult("intent_unknown",conf)
    }

    private fun tokenize(text:String,max:Int):IntArray{
        val cleaned=text.lowercase().replace(Regex("[^a-z0-9\\s]")," ").trim()
        val words=cleaned.split(Regex("\\s+")).filter{it.isNotEmpty()}
        val arr=IntArray(max){0}; var i=0
        for(w in words){ if(i>=max)break; arr[i++]=vocab[w]?:vocab["[UNK]"]?:1 }
        return arr
    }
}
