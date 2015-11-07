package io.github.drmashu.dikon
import java.lang.reflect.Method
import org.apache.logging.log4j.LogManager
import kotlin.reflect.*
import kotlin.reflect.jvm.*

/**
 * DI KONtainer.
 *
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 * @param objectMap 生成したいオブジェクトに名前を付けたMap
 */
public class Dikon(val objectMap: Map<String, Factory<*>>) : Container {

    companion object {
        val logger = LogManager.getLogger(Dikon::class.java)
    }
    /**
     * オブジェクトの取得
     * @param name
     */
    public override fun get(name:String) : Any? {
        logger.entry(name)
        val obj:Any? = if (name == "logger") logger else objectMap[name]

        val result = injectProperties(
            if (obj == null) {
                logger.trace("get null")
                null
            } else if (obj is Factory<*>) {
                logger.trace("get From Factory $obj")
                obj.get(this)
            } else {
                logger.trace("get obj $obj")
                obj
            }
        )
        logger.exit(result)
        return result
    }

    /**
     * プロパティへの依存性注入
     * @param obj 対象のオブジェクト
     */
    protected fun injectProperties(obj: Any?): Any? {
        logger.entry(obj)
        if (obj != null) {
            val kClass = obj.javaClass.kotlin
            try {
                members@ for(member in kClass.members) {
                    if (member is KMutableProperty<*>) {
                        for (annotation in member.annotations) {
                            // 注入元指定のアノテーションがある場合は、その名称で注入する
                            if (annotation is inject) {
                                var name = member.name
                                if (!annotation.name.isEmpty()) {
                                    name = annotation.name
                                }
                                // set可能なプロパティを対象にする
                                callSetter(obj, member.javaSetter, get(name))
                                break
                            }
                        }
                    } else if (member is KFunction) {
                        for (annotation in member.annotations) {
                            // 注入元指定のアノテーションがある場合は、その名称で注入する
                            if (annotation is inject) {
                                var name = member.name

                                if (!annotation.name.isEmpty()) {
                                    name = annotation.name
                                } else if( name.length > 3 && (name.startsWith("set") || name.startsWith("Set"))) {
                                    // setで始まる場合はsetを除く
                                    name = member.name.substring(3)
                                }
                                // setで始まるメソッドを対象にする
                                callSetter(obj, member.javaMethod, get(name))
                                break
                            }
                        }
                    }
                }
            } catch (e: KotlinReflectionInternalError) {
                // Kotlinでのリフレクションが失敗する場合(Javaのクラスなど)は自動ではインジェクションしない
            }
        }
        logger.exit(obj)
        return obj
    }

    /**
     * setterに指定可能な値が取得できていたら注入する
     * @param obj 対象のオブジェクト
     * @param setter セッターメソッド
     * @param value セットする値
     */
    protected fun callSetter(obj: Any?, setter: Method?, value: Any?) {
        logger.entry(obj, setter, value)
        if (setter != null && value != null) {
            val paramTypes = setter.parameterTypes
            if (paramTypes.size() == 1 && paramTypes[0].isAssignableFrom(value.javaClass)) {
                setter.invoke(obj, value)
            }
        }
        logger.exit()
    }
}

interface Container {
    fun get(name: String) : Any?
}
/**
 * 注入アノテーション
 * @param name
 */
annotation class inject(val name: String = "")

val kClassLogger = LogManager.getLogger(KClass::class.java)
/**
 * デフォルトコンストラクタを探して呼び出す
 */
public fun <T: Any> KClass<T>.create(): T? {
    var instance :T? = null
    kClassLogger.entry()
    for (it in this.constructors) {
        // デフォルトコンストラクタを探して呼び出す
        val params = it.parameters
        if (params.size() == 0) {
            instance = it.call()
            break
        }
    }
    kClassLogger.exit(instance)
    return instance
}
