# Java Card 3.0.5向けCAP生成

## 1. Oracle Toolsを配置する

[Oracle Java Card Downloads](https://www.oracle.com/java/technologies/javacard-downloads.html)からJava Card Development Kit Tools 26.0を取得して展開します。対象が3.0.5でも、現行Converterの`-target 3.0.5`を使えます。

環境変数を設定します。

| 変数 | 例 |
| --- | --- |
| `JAVA_HOME` | `C:\Program Files\Java\jdk-25` |
| `JC_HOME_TOOLS` | `C:\tools\javacard-tools-26.0` |

次のファイルが存在することを確認します。

```powershell
Test-Path "$env:JC_HOME_TOOLS\bin\converter.bat"
Test-Path "$env:JC_HOME_TOOLS\lib\api_classic-3.0.5.jar"
```

Oracle JDKとJava Card Toolsはライセンスと社内承認に従って各PCへ導入し、リポジトリには含めません。

## 2. CAPを作る

```powershell
.\scripts\build-cap.ps1
```

またはVS CodeでCtrl+Shift+Bを押し、**Java Card: Build CAP (3.0.5)**を実行します。

スクリプトは次を行います。

1. jCardSimテストを実行
2. AppletだけをOracleの`api_classic-3.0.5.jar`に対して再コンパイル
3. Converterを`-target 3.0.5`で実行
4. CAPファイルの存在とサイズを確認

1つのCAPには次の2つのApplet定義が入ります。

| Applet | AID |
| --- | --- |
| `PingApplet` | `F0545542450101` |
| `FileSystemApplet` | `F0545542450201` |

出力先は次です。

```text
build/cap/io/github/tubesound/myfirstjavacard/card/javacard/card.cap
build/cap/io/github/tubesound/myfirstjavacard/card/javacard/card.exp
build/cap/io/github/tubesound/myfirstjavacard/card/javacard/card.jca
```

CAP生成は実カードへのインストール完了を意味しません。カード製品のJava Card／GlobalPlatform対応、ロード権限、セキュアチャネルと発行者鍵は別途確認します。

## 公式資料

- [Oracle Java Card 3.0.5 documentation](https://docs.oracle.com/en/java/javacard/3.0.5/index.html)
- [Using the Converter target option](https://docs.oracle.com/en/java/javacard/3.2/jctug/using-converter-target-java-card-version.html)
- [Running the Converter](https://docs.oracle.com/en/java/javacard/3.2/jctug/running-converter.html)
