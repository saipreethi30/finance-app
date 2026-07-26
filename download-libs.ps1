# Downloads required JAR files into lib/ (no Maven needed)
$ErrorActionPreference = "Stop"
$libDir = Join-Path $PSScriptRoot "lib"
New-Item -ItemType Directory -Force -Path $libDir | Out-Null

$version = "21.0.2"
$base = "https://repo1.maven.org/maven2"

$jars = @(
    "$base/com/h2database/h2/2.2.224/h2-2.2.224.jar",
    "$base/org/openjfx/javafx-base/$version/javafx-base-$version.jar",
    "$base/org/openjfx/javafx-base/$version/javafx-base-$version-win.jar",
    "$base/org/openjfx/javafx-controls/$version/javafx-controls-$version.jar",
    "$base/org/openjfx/javafx-controls/$version/javafx-controls-$version-win.jar",
    "$base/org/openjfx/javafx-graphics/$version/javafx-graphics-$version.jar",
    "$base/org/openjfx/javafx-graphics/$version/javafx-graphics-$version-win.jar",
    "$base/org/openjfx/javafx-fxml/$version/javafx-fxml-$version.jar",
    "$base/org/openjfx/javafx-fxml/$version/javafx-fxml-$version-win.jar"
)

foreach ($url in $jars) {
    $name = Split-Path $url -Leaf
    $out = Join-Path $libDir $name
    if (Test-Path $out) {
        Write-Host "Already exists: $name"
        continue
    }
    Write-Host "Downloading $name ..."
    Invoke-WebRequest -Uri $url -OutFile $out
}

Write-Host ""
Write-Host "Done. JARs are in: $libDir"
