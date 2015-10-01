package io.github.drmashu.dikon
import java.lang.reflect.Method
import kotlin.reflect.*
import kotlin.reflect.jvm.*

/**
 * DI KONtainer.
 *
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 * @param objectMap 生成したいオブジェクトに名前を付けたMap
 */
public class Dikon(val objectMap: Map<String, Factory<*>>) : Container {

    /**
     * オブジェクトの取得
     * @param name
     */
    public override fun get(name:String) : Any? {
        val obj = objectMap[name]

        return injectProperties(
            if (obj == null) {
                null
            } else if (obj is Factory<*>) {
                obj.get(this)
            } else {
                obj
            }
        )
    }

    /**
     * プロパティへの依存性注入
     * @param obj 対象のオブジェクト
     */
    protected fun injectProperties(obj: Any?): Any? {
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
                                } else if( name.length() > 3 && (name.startsWith("set") || name.startsWith("Set"))) {
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
        return obj
    }

    /**
     * setterに指定可能な値が取得できていたら注入する
     * @param obj 対象のオブジェクト
     * @param setter セッターメソッド
     * @param value セットする値
     */
    protected fun callSetter(obj: Any?, setter: Method?, value: Any?) {
        if (setter != null && value != null) {
            val paramTypes = setter.parameterTypes
            if (paramTypes.size() == 1 && paramTypes[0].isAssignableFrom(value.javaClass)) {
                setter.invoke(obj, value)
            }
        }
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

/**
 * デフォルトコンストラクタを探して呼び出す
 */
public fun <T: Any> KClass<T>.create(): T? {
    var instance :T? = null
    for (it in this.constructors) {
        // デフォルトコンストラクタを探して呼び出す
        val params = it.parameters
        if (params.size() == 0) {
            instance = it.call() as T
            break
        }
    }
    return instance
}
