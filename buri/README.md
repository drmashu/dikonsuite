# buri

**buri** is "Bind action to URI".
**buri** is Web Framework for Dikon written in Kotlin.
**buri** is Japanese amberjack.

ぶり大根って言いたいだけでこの名前にしました。

DIコンテナである[Dikon](../README.md)に組み合わせる、Web用の(M)VCフレームワークです。

## テンプレートエンジン
テンプレートエンジンとして、[BuriTeri](../buriteri/README.md)を用意しています。

BuriTeriについては別途。

## 設定
BuriもDikon同様設定ファイルはなく、configプロパティが設定を記したMapを返すようにします。

ファクトリについては、Dikonの説明文を参照してくだい。

キーにオブジェクト名の代わりにパスを指定することで、そのパスへアクセスされた際にMapで割り当てたアクションを呼びます。

アクションはコンストラクタインジェクションを行う必要があるため、
必ずInjectionファクトリを使用し、またSigletonファクトリを使用してはなりません。

また、パスは正規表現を使用でき、名前付きグループを指定することで、
そのグループに該当する値をその名前でコンストラクタにインジェクションします。

    public class Sample : Buri() {
        override val config: Map<String, Factory<*>>
            get() = mapOf(
                Pair("/", Injection(startpage::class)),　// 通常の割り当て
                Pair("/(?<id>[a-z0-9_@$]+)", Injection(Content::class)), // Contentにidをインジェクションする
                Pair("/content", Injection(content::class)
        )
    }
    
リクエストのメソッドごとにアクションを切り替えたい場合は

    public class Sample : Buri() {
        override val config: Map<String, Factory<*>>
            get() = mapOf(
                Pair("/xxx:GET", Injection(GetXXX::class)), // GETメソッド
                Pair("/xxx:POST", Injection(PostXXX::class)), // POSTメソッド
                Pair("/xxx:PUT", Injection(PutXXX::class)), // PUTメソッド
                Pair("/xxx:DELETE", Injection(DeleteXXX::class)), // DELETEメソッド
                Pair("/content:POST,PUT", Injection(content::class)) // POSTまたはPUTメソッド
        )
    }

のように、パスの後ろに「:」を付加し、カンマ区切りで対応させたいメソッド名を指定してください。

## アクション

アクションはActionクラス(または多くの場合はHTML向けに拡張したHtmlAction)を継承する必要があります。

ActionにはRESTfulな実装に向けてget/post/put/deleteのメソッドに対応するメソッドを用意しています。



## 今後の予定

+ FormDTOをView(HTML)とViewModel間でやりとりする仕組み
+ Ajax
+ RESTful RPC
+ WebSoket
+ 上記を組み合わせたWebページに対して行った操作に対するイベントハンドリングをサーバ側で行う仕組み
+ バインディングを型安全にしたいけど、DIとの相性は悪いだろうなぁ
