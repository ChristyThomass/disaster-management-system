Add-Type -AssemblyName System.Device
$watcher = New-Object System.Device.Location.GeoCoordinateWatcher(1)
$watcher.Start()
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Milliseconds 200
    if ($watcher.Status -eq [System.Device.Location.GeoPositionStatus]::Ready) {
        break
    }
}
$coord = $watcher.Position.Location
if (-not $coord.IsUnknown) {
    Write-Output "$($coord.Latitude),$($coord.Longitude),$($coord.HorizontalAccuracy)"
} else {
    Write-Output "UNKNOWN"
}
