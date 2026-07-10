# 이 저장소에서 Codex CLI를 workspace-write로 시작하는 래퍼
$forbiddenArguments = @(
	"-s",
	"--sandbox",
	"-a",
	"--ask-for-approval",
	"-c",
	"--config",
	"-C",
	"--cd",
	"--add-dir",
	"-p",
	"--profile",
	"--dangerously-bypass-approvals-and-sandbox"
)

foreach ($argument in $args) {
	if ($forbiddenArguments -contains $argument -or
		$argument.StartsWith("--sandbox=") -or
		$argument.StartsWith("--ask-for-approval=") -or
		$argument.StartsWith("--config=") -or
		$argument.StartsWith("--cd=") -or
		$argument.StartsWith("--add-dir=") -or
		$argument.StartsWith("--profile=")) {
		Write-Error "권한 관련 인자는 이 래퍼에서 덮어쓸 수 없습니다: $argument"
		exit 2
	}
}

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
& codex -a on-request -s workspace-write -C $repositoryRoot @args
exit $LASTEXITCODE
