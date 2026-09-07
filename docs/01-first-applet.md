# 最初のAppletとAPDU

## 処理するコマンド

```text
Command : 00 10 00 00 04
Response: 50 49 4E 47 90 00
```

| フィールド | 値 | 意味 |
| --- | --- | --- |
| CLA | `00` | ISO系の基本クラス |
| INS | `10` | この教材で定義したPING命令 |
| P1 | `00` | パラメーターなし |
| P2 | `00` | パラメーターなし |
| Le | `04` | 4バイトの応答を要求 |
| Data | `50 49 4E 47` | ASCII `PING` |
| SW1/SW2 | `90 00` | 正常終了 |

## Appletのライフサイクル

`install()`はAppletインスタンスを生成し、`register()`でJCREへ登録します。選択時と通常コマンド受信時には`process()`が呼ばれます。

このため、`process()`の先頭では次を必ず判定します。

```java
if (selectingApplet()) {
    return;
}
```

## エラー応答

テストでは正常系だけでなく、Appletが次のステータスを返すことも確認します。

| 条件 | ステータス |
| --- | --- |
| 未対応CLA | `6E00` |
| 未対応INS | `6D00` |
| P1/P2不正 | `6A86` |
| Leが4未満 | `6C04` |

テストコードはjCardSimの`Simulator`へ生のAPDUバイト列を渡します。`javax.smartcardio`を使わないため、VS Codeのモジュール解決問題を避けられます。

## jCardSimと実カードの違い

jCardSimはJavaクラスをPC上で直接実行するため、速くステップ実行できます。一方で、実カード固有のメモリー制約、処理時間、暗号実装、通信、GlobalPlatform設定を完全には再現しません。

したがって学習の流れは次の順になります。

1. jCardSimで命令処理とエラーをテスト
2. Oracle ConverterでJava Card 3.0.5向けCAPを生成・検証
3. 対象カードの仕様と鍵を確認して実機へロード
