package io.github.drmashu.dikon

import org.apache.logging.log4j.LogManager
import kotlin.reflect.*

/**
 * オブジェクトの生成方法を定義するためのインターフェイス
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public interface Factory<T: Any> {
    /**
     * インスタンスの取得
     * @return インスタンス
     */
    fun get(dikon: Container) : T?
}

/**
 * シングルトンを実装するファクトリ
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public open class Singleton<T: Any>(val factory: Factory<T>) : Factory<T> {
    companion object{
        val logger = LogManager.getLogger(Singleton::class.java)
    }
    /**
     * インスタンス
     */
    private var instance :T? = null

    /**
     * インスタンスの取得
     * @return インスタンス
     */
    public override fun get(dikon: Container) : T? {
        logger.entry(dikon)
        // インスタンスが存在しない場合だけ、インスタンスを作成する
        if (instance == null) {
            instance = factory.get(dikon)
        }
        logger.trace("get ${if (instance == null) "null" else instance!!.javaClass.kotlin.qualifiedName}:${instance.toString()}")
        logger.exit(instance)
        return instance
    }
}

/**
 * インスタンスを作るだけのファクトリ.
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 * @param kClass 生成対象のクラス
 */
public open class Create<T: Any>(val kClass: KClass<T>) : Factory<T> {
    companion object{
        val logger = LogManager.getLogger(Create::class.java)
    }
    override fun get(dikon: Container): T? {
        logger.entry(dikon)
        logger.trace("get ${kClass.qualifiedName}")
        val instance =  kClass.create()
        logger.exit(instance)
        return instance
    }
}

/**
 * コンストラクターインジェクションを行うファクトリ.
 * コンストラクターインジェクションはプライマリコンストラクタを対象に行われる。
 * インジェクションさせない項目はnull可とすること。
 * デフォルト値を設定してもnullを設定するため注意。
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 * @param kClass 生成対象のクラス
 */
public open class Injection<T: Any>(val kClass: KClass<T>, vararg names: String) : Factory<T> {
    companion object{
        val logger = LogManager.getLogger(Injection::class.java)
    }
    val names: List<String>
    init {
        this.names = names.asList()
    }
    /**
     * インスタンスの取得
     * @return インスタンス
     */
    public override fun get(dikon: Container): T? {
        logger.entry(dikon)
        logger.trace("Injection get ${kClass.qualifiedName}")
        var result: T? = null
        val constructor = kClass.primaryConstructor
        if (constructor != null) {
            logger.trace("KClass constructor")
            val params = constructor.parameters
            var paramArray = Array<Any?>(params.size(), { null })
            for (idx in params.indices) {
                val param = params[idx]
                var name = param.name
                logger.trace("param name $name")
                for (anno in param.annotations) {
                    if (anno is inject) {
                        name = anno.name
                        break
                    }
                }
                if (name != null) {
                    val obj = if (name == "dikon") {
                        dikon
                    } else {
                        dikon.get(name)
                    }
                    logger.trace("Inject $name ${(obj?:"").javaClass.name}")
                    paramArray[idx] = obj
                } else {
                    logger.trace("Inject failed")
                }
            }
            if (logger.isTraceEnabled) {
                var params = "constructor.call params "
                paramArray.forEach { item ->
                    params += "$item, "
                }
                logger.trace(params)
            }
            result = constructor.call(*paramArray)
        } else {
            logger.trace("Java Class constructor")
            val jConstractor = kClass.java.constructors[0]
            var idx = 0
            var paramArray = Array<Any?>(jConstractor.parameters.size(), { null })
            for(param in jConstractor.parameters) {
                var name = param.name
                logger.trace("param name $name")
                for (anno in param.annotations) {
                    if (anno is inject) {
                        name = anno.name
                        break
                    }
                }
                if (names.size() > idx) {
                    name = names[idx]
                }
                if (name != null) {
                    logger.trace("Inject $name")
                    if (name == "dikon") {
                        paramArray[idx] = dikon
                    } else {
                        paramArray[idx] = dikon.get(name)
                    }
                }
                idx++
            }
            result = jConstractor.newInstance(*paramArray) as T
        }

        logger.exit(result)
        return result
    }
}

/**
 * オブジェクトを保持し、返すだけのクラス.
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public class Holder<T: Any>(val value: T) : Factory<T> {
    companion object{
        val logger = LogManager.getLogger(Holder::class.java)
    }
    override fun get(dikon: Container): T? {
        logger.entry()
        logger.trace("get ${value.javaClass.name}:${value.toString()}")
        logger.exit(value)
        return value
    }
}

public fun getKClassForName(name:String): KClass<out Any> = Class.forName(name)!!.kotlin
