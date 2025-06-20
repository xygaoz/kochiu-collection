#!/bin/bash
export LANG="zh_CN.UTF-8"  # 关键设置
export LC_ALL="zh_CN.UTF-8"
cd /volume1/resource/app
nohup /volume1/@appstore/java-17-openjdk/jvm/openjdk-17/bin/java -Xms512m -Xmx8G -XX:MaxRAMPercentage=70.0 -XX:+UseZGC -XX:NativeMemoryTracking=detail -jar -Dfile.encoding=UTF-8 -Dserver.address=0.0.0.0 -Dserver.port=9000 -Djava.io.tmpdir=/volume1/resource/app/tmp -Dspring.profiles.active=prod kochiu-collection-0.1.0.jar >/dev/null 2>&1 &