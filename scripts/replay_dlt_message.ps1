param(
    [Parameter(Mandatory)][ValidateRange(0, [int]::MaxValue)][int]$Partition,
    [Parameter(Mandatory)][ValidateRange(0, [long]::MaxValue)][long]$Offset,
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9._@-]+$')][string]$ApprovedBy,
    [Parameter(Mandatory)][ValidatePattern('^[\p{L}\p{N} .,_-]+$')][string]$Reason
)

$ErrorActionPreference = 'Stop'
$arguments = "--spring.profiles.active=local --spring.main.web-application-type=none --ranking.consumer.enabled=false --dlt.replay.enabled=true --dlt.replay.partition=$Partition --dlt.replay.offset=$Offset --dlt.replay.approved-by=$ApprovedBy --dlt.replay.reason='$Reason'"
& .\gradlew.bat bootRun "--args=$arguments"
exit $LASTEXITCODE
