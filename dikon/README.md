# dikon

**dikon** is "DI CONtainer written in Kotlin".
**dikon** is Japanese "Daikon" radish.

Kotlinの勉強を兼ねて、KotlinでDI コンテナを中心としたMVCフレームワークを作っています。

## 設定
Dikonには設定ファイルというもの無く、オブジェクト名をキーにファクトリを割り当てるMapを作り、Dikonのコンストラクタに渡すことで設定します。

ファクトリはインターフェイスと含め、5種類用意しています。

### Factory
インターフェイスです。
用意されたファクトリではオブジェクトの生成に問題がある場合などには、こちらを利用してください。

### Create
指定したクラスのインスタンスを作るだけのファクトリです。

### Injection
指定したクラスのインスタンスを生成する際に、コンストラクタインジェクションを行います。

### Singleton
ファクトリで生成したオブジェクトをシングルトンで提供するファクトリです。

### Holder
生成済みのオブジェクトを保持するだけで、生成はしません。

## 今後の予定

+ [Buri](./buri/README.md)の強化
+ KotlinによるMicro ORM実装？
+ もしくはJPA準拠ORMのKotlin向けラッパーの作成？
