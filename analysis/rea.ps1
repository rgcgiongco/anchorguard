param([Parameter(ValueFromRemainingArguments = $true)][string[]]$REAArgs)

$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\"
$env:REA_JADX_MCP_JAR = "C:\tools\jadx\jadx-headless-mcp-0.7.1-all.jar"
$env:REA_JADX_OPERATION_TIMEOUT_MS = "1800000"
$env:REA_JADX_HEAP_MIB = "6144"

$rea = "C:\Users\Admin\AppData\Local\npm-cache\_npx\af752b90495482d7\node_modules\rea-agents\scripts\rea.mjs"
& "C:\Program Files\nodejs\node.exe" $rea @REAArgs