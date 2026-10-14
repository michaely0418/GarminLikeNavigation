# Stage 1 Android APK 更新與固定測試簽章 SOP

## 問題說明
- `app/build.gradle.kts` 原先固定 `versionCode = 2`，GitHub Actions 未固定 Signing Key。
- 每個 GitHub-hosted runner 的預設 `~/.android/debug.keystore` 可能重新產生，導致同一 `applicationId=app.garminlike.nav` 的前後兩個 APK 擁有不同憑證，Android 因簽章不一致而拒絕覆蓋安裝。
- 常見錯誤：`INSTALL_FAILED_UPDATE_INCOMPATIBLE` 或安裝畫面只顯示「應用程式未安裝」；實際碼需用 adb 取得。
- 本修正讓 GitHub CI 從 **Repository Actions Secrets** 載入固定的**內部測試簽章**，並將 `versionCode` 設為 `10000 + GITHUB_RUN_NUMBER`。如果尚未設定 Secrets，CI 仍可建置供測試，但 Artifact 會標記為 `navigation-ephemeral-debug-apk`（不同 Run 間不可依賴直接更新）。

## 1. Windows 11 上產生一組專用測試憑證（只做一次）

在 PowerShell 執行（請依實際 Android Studio 安裝位置調整 Java 路徑）：

```powershell
$KeyTool = "E:\Android\Android Studio\jbr\bin\keytool.exe"
$KeyStore = Join-Path $env:USERPROFILE "GarminLikeNavigation-test-signing.p12"
& $KeyTool -genkeypair -alias navtest -keyalg RSA -keysize 3072 -validity 3650 -storetype PKCS12 -keystore $KeyStore -dname "CN=GarminLikeNavigation TEST, OU=Development, O=Personal, L=Taipei, C=TW"
```

- 依畫面輸入**自己保管**的密碼；不要把密碼或 `.p12` 原檔上傳到 GitHub Repository、Issue、PR、聊天或雲端公開空間。
- `PKCS12` 金鑰別名固定為 **`navtest`**，Key Password 應與 Store Password 相同。
- 務必在個人安全位置備份 `.p12` 與密碼；遺失後將無法再以相同簽章更新既有測試 APK。

## 2. 將私鑰存為 GitHub Actions Secrets（請勿 Commit）

到 `https://github.com/michaely0418/GarminLikeNavigation/settings/secrets/actions` → **New repository secret**：

| Secret Name | Value |
|---|---|
| `NAV_TEST_KEYSTORE_B64` | `.p12` 檔案的 Base64 字串 |
| `NAV_TEST_KEYSTORE_PASSWORD` | 建立 `.p12` 時輸入的密碼 |

複製 Base64 到剪貼簿的 PowerShell 指令：

```powershell
$KeyStore = Join-Path $env:USERPROFILE "GarminLikeNavigation-test-signing.p12"
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes($KeyStore)) | Set-Clipboard
```

請將剪貼簿貼到 GitHub Secret `NAV_TEST_KEYSTORE_B64`，儲存後再建立 `NAV_TEST_KEYSTORE_PASSWORD`。

**注意：** GitHub Secrets 無法供非可信 Fork Pull Request 使用。此測試金鑰只能用於本專案內部 Debug／測試 APK，不可重複用於正式 Google Play／Production Signing。

## 3. 重新觸發 GitHub CI 並下載正確 APK

兩項 Secrets 設定後，提交新的整合 PR Commit 觸發 CI。當 CI 能讀取金鑰時，下載 Artifact 名稱應是：

`navigation-stable-test-apk`

若顯示 `navigation-ephemeral-debug-apk`，代表 Runner 沒有讀到兩項 Secrets；**該 Artifact 無法保證後續覆蓋更新**。請先檢查 Secret 名稱與 PR/Repo 權限。

CI 的「Report signed APK identity」步驟會印出 `versionCode` 及公開 SHA-256 憑證指紋，協助驗證不同 Run 是否保持同一簽章。

## 4. 手機首次切換至固定測試簽章

**重要：** 原先安裝的舊版若由不同 Debug Key 簽署，無法透過正常覆蓋安裝改為新簽章。Android 不允許任意變更既有 App Signing Certificate。

第一次轉換必須手動解除安裝**舊版一次**（`android:allowBackup=false` 的既有版本資料通常無法透過系統備份保留）；然後安裝 `navigation-stable-test-apk` 中的 `app-debug.apk`。

後續所有 GitHub Run 如**同一把測試金鑰**、**相同 `applicationId`**、**更高 `versionCode`**，就可以直接安裝更新、保留設定（包括車種、地圖日夜選擇）。

如果交替安裝 Android Studio 本地 Run 產生的 APK，它可能使用另外一把 `~/.android/debug.keystore`，一樣不能直接覆蓋 GitHub 穩定簽章版本。建議手機測試一致使用 GitHub Stable Artifact。

## 5. 取得安裝失敗的實際錯誤碼（可選）

若 Android SDK `platform-tools`／USB ADB 已就緒，透過 PowerShell：

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "C:\path\to\app-debug.apk"
```

- `INSTALL_FAILED_UPDATE_INCOMPATIBLE`：簽章不一致
- `INSTALL_FAILED_VERSION_DOWNGRADE`：安裝版本 `versionCode` 較低
- 其他錯誤：保留完整輸出以精確分析，不要直接假定原因

## 驗收
- [ ] GitHub Actions 穩定測試簽章 Secrets 已正確設定
- [ ] CI 產出 `navigation-stable-test-apk`
- [ ] 兩次不同 GitHub Run 的憑證 SHA-256 指紋相同
- [ ] 第二次 Run 的 `versionCode` 大於第一次
- [ ] 第二次安裝**不需解除安裝**，且保留手機上的車種／日夜設定

## 固定簽章 CI 驗收紀錄（2026-10-14）

Repository Owner 已在 GitHub Actions 設定兩個必要 Secrets。為避免洩漏私鑰與密碼，驗收時**只記錄 CI 公開的 APK versionCode 與簽章憑證 SHA-256 指紋**，不輸出 Base64 或 keystore 密碼。

- 第一次啟用後 CI（待驗證）：預期 Artifact `navigation-stable-test-apk`。
- 第二次獨立 CI（待驗證）：憑證指紋應與第一次相同，且 versionCode 嚴格遞增。
- 實際在手機直接覆蓋安裝、設定保存：由 Product Owner 完成；首次從舊簽章切換仍須移除舊版一次。

**判定原則：** 只有檢查到兩個不同 Run 的實際簽章憑證一致、APK 版本遞增，才將 CI 簽章與版本控管標示為 PASS；建置成功本身並不能證明手機能覆蓋更新。

### 實機覆蓋更新驗收 — PASS（2026-10-14）

Product Owner 在 Xiaomi Android 手機上依照下列流程測試並回覆「測試正常」：

- 使用 GitHub Actions [Run #38584551230](https://github.com/michaely0418/GarminLikeNavigation/actions/runs/38584551230) 的穩定簽章 Debug APK（`versionCode=10030`）。
- 使用 [Run #38584965844](https://github.com/michaely0418/GarminLikeNavigation/actions/runs/38584965844) 的同簽章 APK（`versionCode=10031`）直接覆蓋安裝，**無須再次卸載**：**PASS（使用者回報）**。
- 覆蓋後車種及日夜模式設定仍保留：**PASS（使用者回報）**。
- 兩 Run 的 APK 憑證 SHA-256 完全相同（`dcfa4ade7aad8b6ccb2c03eeeabeb55f28992abdc1bf31c7792df09e47c2e214`）：**PASS（CI 日誌）**。

**INSTALL-UPDATE-01 結案。** 此項為內部測試 Debug 簽章方案，不等同 Google Play 正式發布簽章或更新管道；請妥善備份固定 PKCS12 測試私鑰與密碼。Stage 1 的地圖/HUD 其他功能驗收仍需另行追蹤。
