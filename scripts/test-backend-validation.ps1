# Backend reconciler validation against local Halo (admin:admin @ :8090)
$ErrorActionPreference = "Stop"
$Auth = "admin:admin"
$Base = "http://127.0.0.1:8090/apis/portfolio.plugin.halo.run/v1alpha1"
$TmpDir = [System.IO.Path]::GetTempPath()
$created = @()

function Get-JsonField {
    param([string]$Json, [string]$Field)
    if ($Field -in @("invalidType", "invalidPortfolio", "duplicateValue", "slugConflict", "projectCount")) {
        if ($Json -match "`"status`"\s*:\s*\{[^}]*`"$Field`"\s*:\s*(true|false|null|\d+)") {
            $val = $Matches[1]
            if ($val -eq "true") { return $true }
            if ($val -eq "false") { return $false }
            if ($val -match '^\d+$') { return [int]$val }
        }
    }
    if ($Json -match "`"$Field`"\s*:\s*(true|false|null|\d+|`"[^`"]*`")") {
        $val = $Matches[1]
        if ($val -eq "true") { return $true }
        if ($val -eq "false") { return $false }
        if ($val -eq "null") { return $null }
        if ($val -match '^\d+$') { return [int]$val }
        return $val.Trim('"')
    }
    return $null
}

function Invoke-ApiJson {
    param([string]$Method = "GET", [string]$Url, [string]$JsonBody = $null)
    $args = @("-s", "-u", $Auth, "-w", "`n%{http_code}", "-X", $Method, $Url)
    if ($JsonBody) {
        $file = Join-Path $TmpDir ("portfolio-api-" + [guid]::NewGuid().ToString("N") + ".json")
        Set-Content -Path $file -Value $JsonBody -NoNewline -Encoding utf8
        try {
            $args += @("-H", "Content-Type: application/json", "-d", "@$file")
            $raw = & curl.exe @args
        } finally {
            Remove-Item -Path $file -Force -ErrorAction SilentlyContinue
        }
    } else {
        $raw = & curl.exe @args
    }
    $lines = $raw -split "`n"
    $code = [int]$lines[-1].Trim()
    $json = ($lines[0..($lines.Length - 2)] -join "`n")
    return @{ Code = $code; Body = $json }
}

function Wait-Reconcile { Start-Sleep -Seconds 2 }

function Remove-Resource {
    param([string]$Kind, [string]$Name)
    if (-not $Name) { return }
    $path = switch ($Kind) {
        "portfolio" { "portfolios" }
        "project" { "portfolioprojects" }
        "option" { "portfoliooptions" }
    }
    Invoke-ApiJson -Method DELETE -Url "$Base/$path/$Name" | Out-Null
    Wait-Reconcile
}

try {
    Write-Host "=== 1. Slug conflict detection ==="
    $slugTest = Invoke-ApiJson -Method POST -Url "$Base/portfolios" -JsonBody '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"Portfolio","metadata":{"generateName":"portfolio-"},"spec":{"displayName":"Dup Slug","slug":"default","publicView":true}}'
    if ($slugTest.Code -ge 400) { throw "Create portfolio failed: $($slugTest.Body)" }
    $dupName = Get-JsonField $slugTest.Body "name"
    $created += @{ Kind = "portfolio"; Name = $dupName }
    Wait-Reconcile
    $dupPf = Invoke-ApiJson -Url "$Base/portfolios/$dupName"
    $slugConflict = Get-JsonField $dupPf.Body "slugConflict"
    Write-Host "slugConflict=$slugConflict (expect True)"
    if ($slugConflict -ne $true) { throw "Expected slugConflict=true" }

    Write-Host "=== 2. Portfolio delete blocked when children exist ==="
    $pf = Invoke-ApiJson -Method POST -Url "$Base/portfolios" -JsonBody '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"Portfolio","metadata":{"generateName":"portfolio-"},"spec":{"displayName":"Delete Guard","slug":"delete-guard-test","publicView":true}}'
    $guardName = Get-JsonField $pf.Body "name"
    $created += @{ Kind = "portfolio"; Name = $guardName }
    $childBody = "{`"apiVersion`":`"portfolio.plugin.halo.run/v1alpha1`",`"kind`":`"PortfolioProject`",`"metadata`":{`"generateName`":`"portfolio-project-`"},`"spec`":{`"portfolioName`":`"$guardName`",`"title`":`"Child`",`"published`":false}}"
    $child = Invoke-ApiJson -Method POST -Url "$Base/portfolioprojects" -JsonBody $childBody
    $childName = Get-JsonField $child.Body "name"
    $created += @{ Kind = "project"; Name = $childName }
    Wait-Reconcile
    Invoke-ApiJson -Method DELETE -Url "$Base/portfolios/$guardName" | Out-Null
    Wait-Reconcile
    $terminating = Invoke-ApiJson -Url "$Base/portfolios/$guardName"
    $deletionTs = Get-JsonField $terminating.Body "deletionTimestamp"
    Write-Host "deletionTimestamp=$deletionTs (expect non-null while blocked)"
    if (-not $deletionTs) { throw "Expected portfolio stuck in terminating state" }

    Write-Host "=== 3. projectCount excludes deleting projects ==="
    $default = Invoke-ApiJson -Url "$Base/portfolios/default-portfolio"
    $beforeCount = Get-JsonField $default.Body "projectCount"
    $proj = Invoke-ApiJson -Method POST -Url "$Base/portfolioprojects" -JsonBody '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"generateName":"portfolio-project-"},"spec":{"portfolioName":"default-portfolio","title":"Count Test","published":false}}'
    $projName = Get-JsonField $proj.Body "name"
    $created += @{ Kind = "project"; Name = $projName }
    Wait-Reconcile
    $afterCreate = Invoke-ApiJson -Url "$Base/portfolios/default-portfolio"
    $countAfterCreate = Get-JsonField $afterCreate.Body "projectCount"
    Write-Host "projectCount after create: $countAfterCreate (was $beforeCount)"
    Remove-Resource -Kind "project" -Name $projName
    $created = @($created | Where-Object { -not ($_.Kind -eq "project" -and $_.Name -eq $projName) })
    $afterDel = Invoke-ApiJson -Url "$Base/portfolios/default-portfolio"
    $countAfterDel = Get-JsonField $afterDel.Body "projectCount"
    Write-Host "projectCount after delete: $countAfterDel (expect $beforeCount)"
    if ($countAfterDel -ne $beforeCount) { throw "projectCount not restored (got $countAfterDel, want $beforeCount)" }

    Write-Host "=== 4. invalidPortfolio on orphan project ==="
    $orphan = Invoke-ApiJson -Method POST -Url "$Base/portfolioprojects" -JsonBody '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"generateName":"portfolio-project-"},"spec":{"portfolioName":"non-existent-portfolio","title":"Orphan","published":false}}'
    $orphanName = Get-JsonField $orphan.Body "name"
    $created += @{ Kind = "project"; Name = $orphanName }
    Wait-Reconcile
    $orphanGet = Invoke-ApiJson -Url "$Base/portfolioprojects/$orphanName"
    $invalidPortfolio = Get-JsonField $orphanGet.Body "invalidPortfolio"
    Write-Host "invalidPortfolio=$invalidPortfolio (expect True)"
    if ($invalidPortfolio -ne $true) { throw "Expected invalidPortfolio=true" }

    Write-Host "=== 5. Option validation (schema + reconciler) ==="
    $badType = Invoke-ApiJson -Method POST -Url "$Base/portfoliooptions" -JsonBody '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioOption","metadata":{"generateName":"portfolio-option-"},"spec":{"portfolioName":"default-portfolio","type":"BAD_TYPE","value":"x","label":"Bad","sortOrder":0}}'
    Write-Host "invalid type HTTP $($badType.Code) (expect 400)"
    if ($badType.Code -ne 400) { throw "Expected HTTP 400 for invalid option type" }

    $dupOpt = Invoke-ApiJson -Method POST -Url "$Base/portfoliooptions" -JsonBody '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioOption","metadata":{"generateName":"portfolio-option-"},"spec":{"portfolioName":"default-portfolio","type":"TECH_STACK","value":"python","label":"Dup Python","sortOrder":99}}'
    if ($dupOpt.Code -ge 400) { throw "Create duplicate option failed: $($dupOpt.Body)" }
    $dupOptName = Get-JsonField $dupOpt.Body "name"
    $created += @{ Kind = "option"; Name = $dupOptName }
    Wait-Reconcile
    $dupOptGet = Invoke-ApiJson -Url "$Base/portfoliooptions/$dupOptName"
    $duplicateValue = Get-JsonField $dupOptGet.Body "duplicateValue"
    Write-Host "duplicateValue=$duplicateValue (expect True)"
    if ($duplicateValue -ne $true) { throw "Expected duplicateValue=true" }

    Write-Host "=== ALL BACKEND VALIDATION TESTS PASSED ==="
}
finally {
    Write-Host "=== Cleanup ==="
    foreach ($item in ($created | Sort-Object { $_.Kind } -Descending)) {
        Remove-Resource -Kind $item.Kind -Name $item.Name
    }
}
