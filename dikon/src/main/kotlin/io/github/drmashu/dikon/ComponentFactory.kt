package io.github.drmashu.dikon

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

    /**
     * インスタンス
     */
    private var instance :T? = null

    /**
     * インスタンスの取得
     * @return インスタンス
     */
    public override fun get(dikon: Container) : T? {
        // インスタンスが存在しない場合だけ、インスタンスを作成する
        if (instance == null) {
            instance = factory.get(dikon)
        }
        return instance
    }
}

/**
 * インスタンスを作るだけのファクトリ.
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 * @param kClass 生成対象のクラス
 */
public open class Create<T: Any>(val kClass: KClass<T>) : Factory<T> {
    override fun get(dikon: Container): T? {
        return kClass.create()
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
public open class Injection<T: Any>(val kClass: KClass<T>) : Factory<T> {

    /**
     * インスタンスの取得
     * @return インスタンス
     */
    public override fun get(dikon: Container): T? {
        val constructor = kClass.primaryConstructor
        if (constructor != null) {
            val params = constructor.parameters
            var paramArray = Array<Any?>(params.size(), { null })
            for (idx in params.indices) {
                val param = params[idx]
                var name = param.name
                for (anno in param.annotations) {
                    if (anno is inject) {
                        name = anno.name
                        break
                    }
                }
                if (name != null) {
                    if (name == "dikon") {
                        paramArray[idx] = dikon
                    } else {
                        paramArray[idx] = dikon.get(name)
                    }
                }
            }
            return constructor.call(*paramArray) as T
        }
        return null
    }
}

/**
 * オブジェクトを保持し、返すだけのクラス.
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public class Holder<T: Any>(val value: T) : Factory<T> {
    override fun get(dikon: Container): T? {
        return value
    }
}

public fun getKClassForName(name:String): KClass<out Any> = Class.forName(name)!!.kotlin
