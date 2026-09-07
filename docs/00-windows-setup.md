# Windows 11セットアップ

## 1. JDKを入れる

[Oracle JDK 25](https://www.oracle.com/java/technologies/downloads/#java25)のWindows x64版を取得してインストールします。JREではなく、`javac.exe`を含むJDKが必要です。

Windowsの環境変数を、実際のインストール先に合わせて設定します。

| 変数 | 例 |
| --- | --- |
| `JAVA_HOME` | `C:\Program Files\Java\jdk-25` |
| `Path`へ追加 | `%JAVA_HOME%\bin` |

VS CodeとPowerShellを開き直し、確認します。

```powershell
$env:JAVA_HOME
& "$env:JAVA_HOME\bin\java.exe" -version
& "$env:JAVA_HOME\bin\javac.exe" -version
```

## 2. VS Codeを入れる

1. [Visual Studio Code](https://code.visualstudio.com/)をインストールします。
2. Extensionsで `Extension Pack for Java`（`vscjava.vscode-java-pack`）をインストールします。
3. **File → Open Folder**で、`README.md`と`src`が直下にあるリポジトリのルートを開きます。
4. 右下のJavaプロジェクト読み込み表示が終わるまで待ちます。

このプロジェクトは `.vscode/settings.json` でソースフォルダー、出力先、jCardSim JARを明示しています。`pom.xml`はありません。

## 3. ビルドとテスト

VS CodeのPowerShellターミナルで実行します。

```powershell
.\scripts\test.ps1
```

初回も外部ダウンロードは発生しません。処理は次の3段階です。

1. `lib/jcardsim-3.0.6.0.jar`のSHA-256を検証
2. `javac --release 8 -g`でAppletとテストをコンパイル
3. `java -ea`で独自テストランナーを実行

`build/classes`は毎回作り直されます。

## 4. ステップ実行

1. `PingApplet.java`の `process()` にブレークポイントを置きます。
2. 左側の**実行とデバッグ**を開きます。
3. **Java Card: PINGをステップ実行**を選択します。
4. F5で開始し、F10でステップオーバー、F11でステップインします。

SELECTとPINGで `process()` が呼ばれるため、同じブレークポイントに複数回停止するのは正常です。PING時のバッファ先頭は `00 10 00 00 04` です。

## トラブルシュート

| 症状 | 対応 |
| --- | --- |
| `javac.exe was not found` | JDKを導入し、`JAVA_HOME`をJDKルートへ設定してVS Codeを再起動する |
| `jCardSim SHA-256 mismatch` | JARが変更・破損している。社内承認済みの原本と照合して置き直す |
| `javacard.framework`が赤線 | リポジトリのルートを開いているか確認し、`Java: Clean Java Language Server Workspace`を実行する |
| ブレークポイントが灰色 | `.vscode/launch.json`の構成からF5を開始し、ビルド完了を待つ |
| PowerShellスクリプトが拒否される | 組織の実行ポリシーに従う。許可されている場合だけ `powershell.exe -ExecutionPolicy RemoteSigned -File .\scripts\test.ps1` を使う |
| `javax.smartcardio.CommandAPDU`エラー | このプロジェクトのテストはそのクラスを使わない。古いワークスペースをCleanして再読込する |

個人PCで依存を更新する場合も、会社PC上でMavenや任意のダウンロード許可を迂回しないでください。
