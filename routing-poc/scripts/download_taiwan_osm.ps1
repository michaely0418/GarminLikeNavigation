# Offline preparation only: downloads public OSM data, does not start navigation.
# Uses Geofabrik Taiwan extract; OSM contributors must be attributed (ODbL).
$ErrorActionPreference = "Stop"
$DataDir = Join-Path (Split-Path $PSScriptRoot -Parent) "data/valhalla"
New-Item -ItemType Directory -Force -Path $DataDir | Out-Null
$Destination = Join-Path $DataDir "taiwan-latest.osm.pbf"
if (Test-Path $Destination) {
    Write-Host "OSM file already exists. Delete explicitly if you intend to fetch a different snapshot."
} else {
    Invoke-WebRequest -Uri "https://download.geofabrik.de/asia/taiwan-latest.osm.pbf" -OutFile $Destination
}
$Result = Get-FileHash -Algorithm SHA256 -Path $Destination
[PSCustomObject]@{
    Source = "https://download.geofabrik.de/asia/taiwan-latest.osm.pbf"
    File = $Destination
    Bytes = (Get-Item $Destination).Length
    Sha256 = $Result.Hash
    DownloadOrVerifyTimeUtc = (Get-Date).ToUniversalTime().ToString("o")
} | Format-List
Write-Host "Record the SHA256 and retrieval time in experiment notes; do not commit the PBF."
