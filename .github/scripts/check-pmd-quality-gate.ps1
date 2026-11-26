param(
    [string]$ReportPath = (Join-Path $PSScriptRoot "../../backend/build/reports/pmd/main.xml")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ReportPath)) {
    Write-Host "QUALITY GATE FAILED: PMD report not found: $ReportPath"
    exit 1
}

[xml]$report = Get-Content $ReportPath -Raw

$violations = $report.SelectNodes(
    "//*[local-name()='file']/*[local-name()='violation']"
)

if ($violations.Count -gt 0) {
    Write-Host "QUALITY GATE FAILED: PMD violations found: $($violations.Count)"

    foreach ($violation in $violations) {
        $file = $violation.ParentNode.GetAttribute("name")
        $rule = $violation.GetAttribute("rule")
        $line = $violation.GetAttribute("beginline")

        Write-Host "File: $file | Rule: $rule | Line: $line"
    }

    exit 1
}

Write-Host "QUALITY GATE PASSED: No PMD violations found."
exit 0

