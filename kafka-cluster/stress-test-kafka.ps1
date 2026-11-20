param(
    [int]$Count = 2000,
    [string]$Topic = "workorders.stress.test",
    [string]$WorkOrdersBaseUrl = "http://localhost:8081",
    [string]$KafkaAdminBaseUrl = "http://localhost:8085"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  DigitalFix - Prueba de Estres para Productor Kafka" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Topico objetivo : $Topic"
Write-Host "Mensajes a enviar: $Count"
Write-Host ""

# 1. Asegurar que el topico anticipado exista usando el Microservicio Administrador de Kafka
Write-Host "[1/3] Verificando/creando topico anticipado '$Topic' via Kafka Admin API..." -ForegroundColor Yellow
$createBody = @{
    name = $Topic
    partitions = 3
    replicationFactor = 2
    configs = @{
        "retention.ms" = "86400000"
        "retention.bytes" = "536870912"
        "cleanup.policy" = "delete"
    }
} | ConvertTo-Json -Depth 4

try {
    $createResp = Invoke-RestMethod -Uri "$KafkaAdminBaseUrl/api/admin/kafka/topics" -Method Post -ContentType "application/json" -Body $createBody -ErrorAction Stop
    Write-Host "  -> Topico creado: $($createResp.message)" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 409) {
        Write-Host "  -> El topico '$Topic' ya existe en el cluster (OK)." -ForegroundColor Green
    } else {
        Write-Host "  -> Aviso al verificar topico: $($_.Exception.Message)" -ForegroundColor DarkYellow
    }
}

# 2. Ejecutar prueba de estres contra el productor optimizado de Spring Boot
Write-Host ""
Write-Host "[2/3] Ejecutando rafaga de $Count mensajes al productor optimizado (batch.size=32KB, linger.ms=10ms, snappy)..." -ForegroundColor Yellow

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $url = "$WorkOrdersBaseUrl/api/workorders/kafka/stress-test?count=$Count&topic=$Topic"
    $result = Invoke-RestMethod -Uri $url -Method Post -ErrorAction Stop
    $stopwatch.Stop()

    Write-Host ""
    Write-Host "[3/3] Resultados de la Prueba de Estres:" -ForegroundColor Green
    Write-Host "----------------------------------------------------------"
    Write-Host ("  Topico                  : {0}" -f $result.topic)
    Write-Host ("  Mensajes solicitados    : {0}" -f $result.requestedMessages)
    Write-Host ("  Mensajes confirmados    : {0}" -f $result.successMessages)
    Write-Host ("  Mensajes fallidos       : {0}" -f $result.failedMessages)
    Write-Host ("  Tiempo interno Kafka    : {0} ms" -f $result.totalTimeMs)
    Write-Host ("  Tiempo total HTTP       : {0} ms" -f $stopwatch.ElapsedMilliseconds)
    Write-Host ("  Latencia promedio/msg   : {0} ms" -f $result.avgTimePerMessageMs)
    Write-Host ("  Throughput (Rendimiento): {0} msgs/seg" -f $result.throughputMsgPerSec)
    Write-Host "----------------------------------------------------------"
    Write-Host "Puedes verificar los mensajes y particiones en Kafka-UI: http://localhost:8086" -ForegroundColor Cyan
} catch {
    $stopwatch.Stop()
    Write-Host "ERROR ejecutando la prueba de estres: $($_.Exception.Message)" -ForegroundColor Red
}
