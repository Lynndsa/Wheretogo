# .github/scripts/check-pmd-quality-gate.ps1

$reportPath = "backend/build/reports/pmd/main.txt"
$blockingRules = @("CyclomaticComplexity", "MissingSerialVersionUID", "UnusedPrivateMethod")

Write-Host "=== Backend Quality Gate Check ==="

if (-Not (Test-Path $reportPath)) {
    Write-Error "PMD report not found at '$reportPath'. Run 'cd backend; .\gradlew.bat pmdMain' first."
    exit 1
}

$content = Get-Content $reportPath -Raw
$foundIssues = $false

foreach ($rule in $blockingRules) {
    if ($content -match $rule) {
        Write-Warning "BLOCKING VIOLATION: $rule"
        $foundIssues = $true
    }
}

if ($foundIssues) {
    Write-Error "Quality Gate FAILED."
    exit 1
} else {
    Write-Host "Quality Gate PASSED."
    exit 0
}