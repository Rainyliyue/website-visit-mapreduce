param(
    [string]$Image = "apache/hadoop:3.5.0"
)

$ErrorActionPreference = "Stop"

$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $Root

Write-Host "== Build project jar =="
mvn -q -DskipTests package

Write-Host "== Pull Hadoop image: $Image =="
docker pull $Image

Write-Host "== Hadoop version =="
docker run --rm `
  -v "${Root}:/workspace" `
  -w /workspace `
  $Image `
  hadoop version

$jobs = @(
    @{
        Name = "rank"
        MainClass = "com.course.mapreduce.job.WebsiteRankJob"
        Output = "target/docker-mr-rank"
    },
    @{
        Name = "peak"
        MainClass = "com.course.mapreduce.job.HourPeakJob"
        Output = "target/docker-mr-peak"
    },
    @{
        Name = "source"
        MainClass = "com.course.mapreduce.job.SourceDistributionJob"
        Output = "target/docker-mr-source"
    }
)

foreach ($job in $jobs) {
    Write-Host "== Run MapReduce job: $($job.Name) =="
    Remove-Item -Recurse -Force $job.Output -ErrorAction SilentlyContinue
    docker run --rm `
      -v "${Root}:/workspace" `
      -w /workspace `
      $Image `
      hadoop jar target/website-visit-mapreduce-1.0.0.jar $job.MainClass `
      file:///workspace/data/sample_access_log.csv `
      "file:///workspace/$($job.Output)"

    Write-Host "== Output: $($job.Output)/part-r-00000 =="
    Get-Content (Join-Path $job.Output "part-r-00000") | Select-Object -First 20
}

Write-Host "== Done =="
