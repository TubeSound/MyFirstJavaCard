# MyFirstJavaCard

Windows 11、VS Code、jCardSimでJava Card Appletを学ぶための、**Mavenを使わない**最小プロジェクトです。

最初のAppletは、APDU `00 10 00 00 04` に対してASCIIの `PING` とステータスワード `9000` を返します。

## この構成で必要なもの

| 用途 | 採用 | ネットワーク |
| --- | --- | --- |
| Javaのコンパイル／実行 | JDK 25 | 導入時のみ |
| 編集／ステップ実行 | VS Code + Extension Pack for Java | 導入時のみ |
| PC上のカードシミュレーション | jCardSim 3.0.6.0 | 不要（JAR同梱） |
| ビルド／テスト | PowerShell 5.1 + `javac`／`java` | 不要 |
| Java Card 3.0.5向けCAP生成 | Oracle Java Card Development Kit Tools 26.0 | 導入時のみ |

Maven、Gradle、JUnitは使いません。ビルド中にインターネットへ接続せず、同梱jCardSim JARのSHA-256を毎回検証します。

> jCardSimの配布バージョン `3.0.6.0` はJava Card OSのバージョンではありません。このforkはJava Card 3.0.5 APIを実装しています。CAPはOracle Converterへ `-target 3.0.5` を指定して生成します。

## 1. Windows 11へ導入

詳しくは[Windows 11セットアップ](docs/00-windows-setup.md)を参照してください。

1. JDK 25とVS Codeをインストールします。
2. VS Codeへ `Extension Pack for Java` をインストールします。
3. このリポジトリのルートをVS Codeで開きます。
4. PowerShellで次を実行します。

```powershell
.\scripts\test.ps1
```

成功すると最後に次のように表示されます。

```text
[PASS] PING returns PING + 9000
...
All 7 tests passed.
```

## 2. VS Codeでステップ実行

1. [PingApplet.java](src/main/java/io/github/tubesound/myfirstjavacard/card/PingApplet.java)の `process()` 内、`byte[] buffer = apdu.getBuffer();` にブレークポイントを置きます。
2. 実行とデバッグビューを開きます。
3. **Java Card: PINGをステップ実行**を選び、F5を押します。
4. SELECT APDUの呼び出し後、PING APDUの呼び出しで停止します。F10／F11で進め、`buffer[0..4]` と `le` を確認します。

これはjCardSim上で通常のJavaクラスとして実行されるAppletのデバッグです。実カードやCAPファイル内部を直接デバッグするものではありません。

## 3. よく使う操作

| 操作 | PowerShell | VS Code |
| --- | --- | --- |
| コンパイル | `.\scripts\build.ps1` | `Terminal: Run Task` → **Java Card: Build** |
| 全テスト | `.\scripts\test.ps1` | `Terminal: Run Test Task` |
| デバッグ | ― | F5 → **Java Card: PINGをステップ実行** |
| CAP生成 | `.\scripts\build-cap.ps1` | Ctrl+Shift+B |

## 4. ソース構成

```text
src/main/java/    カードへ載せるAppletコード
src/test/java/    PC上だけで動くjCardSimテスト
scripts/          Maven不要のビルド／テスト／CAP生成
lib/              固定済みjCardSim JARとチェックサム
.vscode/          クラスパス、タスク、デバッグ設定
docs/             導入と学習資料
```

最初に読む順番は次のとおりです。

1. [Windows 11セットアップ](docs/00-windows-setup.md)
2. [最初のAppletとAPDU](docs/01-first-applet.md)
3. [CAP生成](docs/02-build-cap.md)
4. [社内環境への持ち込みと依存管理](docs/03-offline-security.md)

## 識別子

| 項目 | 値 |
| --- | --- |
| パッケージ | `io.github.tubesound.myfirstjavacard.card` |
| パッケージAID | `F05455424501` |
| Applet | `PingApplet` |
| Applet AID | `F0545542450101` |
| 対象 | Java Card Classic 3.0.5 |

## ライセンス

このサンプルコードは[MIT License](LICENSE)です。同梱するjCardSimとその第三者ソフトウェアについては[Third-party notices](THIRD_PARTY_NOTICES.md)を参照してください。
