# HOTFIX-002: Gradle 8.13 wrapper bootstrap for Windows PowerShell 5.1 / PowerShell 7
# Official source: https://github.com/gradle/gradle/tree/v8.13.0/gradle/wrapper
# Official SHA-256: https://gradle.org/release-checksums/ (Gradle 8.13 / Wrapper JAR)
# The Gradle distribution site publishes a checksum for wrapper JARs, but NOT the wrapper JAR itself.
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# Enable TLS 1.2 for older Windows PowerShell 5.1 environments.
[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$directory = Join-Path $root 'gradle\wrapper'
$jarPath = Join-Path $directory 'gradle-wrapper.jar'
$expectedSha256 = '81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f'

# Both URLs point to the official Gradle GitHub repository at a fixed release tag.
$sources = @(
    'https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar',
    'https://github.com/gradle/gradle/raw/refs/tags/v8.13.0/gradle/wrapper/gradle-wrapper.jar'
)

if (-not (Test-Path -LiteralPath $directory)) {
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
}

if (Test-Path -LiteralPath $jarPath) {
    $existingSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $jarPath).Hash.ToLowerInvariant()
    if ($existingSha256 -eq $expectedSha256) {
        Write-Host 'SUCCESS: Existing Gradle 8.13 Wrapper JAR already passes official SHA-256 verification.'
        Write-Host "Saved to: $jarPath"
        return
    }
    Write-Warning 'The existing Wrapper JAR does not match the official checksum. It will be replaced ONLY after a validated download.'
}

$errors = New-Object 'System.Collections.Generic.List[string]'
foreach ($url in $sources) {
    $tempPath = Join-Path $directory ('gradle-wrapper.' + [Guid]::NewGuid().ToString('N') + '.download')
    Write-Host "Downloading Gradle 8.13 Wrapper JAR from: $url"
    try {
        Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $tempPath -MaximumRedirection 10 -ErrorAction Stop
        $length = (Get-Item -LiteralPath $tempPath).Length
        if ($length -lt 10000) {
            throw "Downloaded file too small ($length bytes); probably not a valid JAR."
        }
        $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $tempPath).Hash.ToLowerInvariant()
        if ($actual -ne $expectedSha256) {
            throw "SHA-256 checksum mismatch. Expected $expectedSha256; downloaded $actual."
        }
        Move-Item -LiteralPath $tempPath -Destination $jarPath -Force
        Write-Host 'SUCCESS: Gradle 8.13 Wrapper JAR verified against the official checksum.'
        Write-Host "SHA-256: $actual"
        Write-Host "Saved to: $jarPath"
        return
    }
    catch {
        $details = $_.Exception.Message
        Write-Warning "Download/verification failed: $details"
        $errors.Add("$url : $details")
    }
    finally {
        if (Test-Path -LiteralPath $tempPath) {
            Remove-Item -LiteralPath $tempPath -Force
        }
    }
}

throw ("Failed to obtain a verified official Gradle Wrapper JAR from both URLs.`n" + ($errors -join "`n") + "`nYou can alternatively generate it from an installed Gradle: gradle wrapper --gradle-version 8.13")
