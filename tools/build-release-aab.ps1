param(
    [switch]$SkipSdkInstall,
    [switch]$UnsignedCheckOnly
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$BuildToolsDir = Join-Path $Root ".build-tools"
$AndroidSdkRoot = Join-Path $Root ".android-sdk"
$GradleVersion = "8.7"
$GradleZip = Join-Path $BuildToolsDir "gradle-$GradleVersion-bin.zip"
$GradleDir = Join-Path $BuildToolsDir "gradle-$GradleVersion"
$CmdlineZipName = "commandlinetools-win-14742923_latest.zip"
$CmdlineZip = Join-Path $BuildToolsDir $CmdlineZipName
$CmdlineRoot = Join-Path $AndroidSdkRoot "cmdline-tools"
$CmdlineLatest = Join-Path $CmdlineRoot "latest"

function Assert-InWorkspace {
    param([string]$Path)

    $workspace = [System.IO.Path]::GetFullPath($Root)
    $candidate = [System.IO.Path]::GetFullPath($Path)
    if (-not $candidate.StartsWith($workspace, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing filesystem operation outside workspace: $candidate"
    }
    return $candidate
}

New-Item -ItemType Directory -Force -Path $BuildToolsDir | Out-Null
New-Item -ItemType Directory -Force -Path $AndroidSdkRoot | Out-Null

if (-not (Test-Path $GradleDir)) {
    if (-not (Test-Path $GradleZip)) {
        Invoke-WebRequest `
            -Uri "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip" `
            -OutFile $GradleZip
    }
    Expand-Archive -Path $GradleZip -DestinationPath $BuildToolsDir -Force
}

if (-not (Test-Path $CmdlineLatest)) {
    if (-not (Test-Path $CmdlineZip)) {
        Invoke-WebRequest `
            -Uri "https://dl.google.com/android/repository/$CmdlineZipName" `
            -OutFile $CmdlineZip
    }
    $TempCmdline = Join-Path $BuildToolsDir "cmdline-tools-expanded"
    if (Test-Path $TempCmdline) {
        Remove-Item -LiteralPath (Assert-InWorkspace $TempCmdline) -Recurse -Force
    }
    Expand-Archive -Path $CmdlineZip -DestinationPath $TempCmdline -Force
    New-Item -ItemType Directory -Force -Path $CmdlineRoot | Out-Null
    Move-Item -LiteralPath (Join-Path $TempCmdline "cmdline-tools") -Destination $CmdlineLatest
    Remove-Item -LiteralPath (Assert-InWorkspace $TempCmdline) -Recurse -Force
}

$SdkManager = Join-Path $CmdlineLatest "bin\sdkmanager.bat"
$Gradle = Join-Path $GradleDir "bin\gradle.bat"

if (-not $SkipSdkInstall) {
    $licenseAnswers = 1..20 | ForEach-Object { "y" }
    $licenseAnswers | & $SdkManager --sdk_root=$AndroidSdkRoot --licenses | Out-Host
    & $SdkManager --sdk_root=$AndroidSdkRoot "platform-tools" "platforms;android-35" "build-tools;35.0.0"
}

if (-not $UnsignedCheckOnly) {
    $requiredSigningVars = @(
        "OWL_BIKE_RELEASE_STORE_FILE",
        "OWL_BIKE_RELEASE_STORE_PASSWORD",
        "OWL_BIKE_RELEASE_KEY_ALIAS",
        "OWL_BIKE_RELEASE_KEY_PASSWORD"
    )
    $missingSigningVars = $requiredSigningVars | Where-Object { [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_)) }
    if ($missingSigningVars.Count -gt 0) {
        throw "Missing release signing environment variables: $($missingSigningVars -join ', '). Use -UnsignedCheckOnly only for local bundle validation."
    }
    if (-not (Test-Path -LiteralPath $env:OWL_BIKE_RELEASE_STORE_FILE)) {
        throw "Release keystore file not found at OWL_BIKE_RELEASE_STORE_FILE."
    }
}

$env:ANDROID_HOME = $AndroidSdkRoot
$env:ANDROID_SDK_ROOT = $AndroidSdkRoot
& $Gradle --project-dir $Root testDebugUnitTest bundleRelease
if ($LASTEXITCODE -ne 0) {
    throw "Gradle release bundle failed with exit code $LASTEXITCODE"
}

$Aab = Join-Path $Root "app\build\outputs\bundle\release\app-release.aab"
Write-Host "AAB: $Aab"
if ($UnsignedCheckOnly) {
    Write-Host "UnsignedCheckOnly: bundle built without enforcing release signing variables. Do not upload this artifact to Google Play."
}
